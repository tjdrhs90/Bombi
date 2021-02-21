package com.Gonigon.bombi;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.Gonigon.bombi.model.MessageModel;
import com.Gonigon.bombi.model.NotificationModel;
import com.Gonigon.bombi.model.UserModel;
import com.Gonigon.bombi.room.AppDatabase;
import com.Gonigon.bombi.room.AppDatabase2;
import com.Gonigon.bombi.room.DbEntity;
import com.Gonigon.bombi.room.DbEntity2;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SendActivity extends AppCompatActivity {

    private AdView mAdView;

    Handler handler = new Handler();
    ProgressDialog progressDialog;

    Map<String, String> nullData = new HashMap<>(); // 필드 값없이 update하면 오류발생하여 만든 널데이터 (update전에 set해줌, SetOptions.merge() 이전 데이터를 포함하면서)
    ArrayList<UserModel> userModelList = new ArrayList<>(); //자신 빼고 전체 유저리스트
    ArrayList<String> chatList = new ArrayList<>(); //채팅중인사람 (상대방이 보내서 내 쪽지함에 있는 리스트)
    ArrayList<String> sendList; //쪽지보낸사람 (내가 쪽지 보냈거나 대화중인 사람)
    UserModel userModel; //유저 클래스 모델

    EditText editText_message; // 보낼 내용
    Button button_send; //보내기 버튼

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private FirebaseUser user = firebaseAuth.getCurrentUser();
    String myUid = firebaseAuth.getCurrentUser().getUid();
    String myEmail = firebaseAuth.getCurrentUser().getEmail();
    String myName;
    long oldTime; //마지막으로 보낸시간
    long nowTime; //지금 보낼 시간
    long resultTime; // 1분에 하나씩 보낼 수 있도록 확인하기 위함

    String token;

    int randomNumber;

    MessageModel setMessageModel; // 보낼 내용 클래스 모델

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm");

    String TAG = "seonggon";

    AppDatabase2 localDB2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);

        localDB2 = Room.databaseBuilder(getApplicationContext(), AppDatabase2.class, "db2").allowMainThreadQueries().build();

        MobileAds.initialize(this, getString(R.string.admob_app_id));
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("쪽지를 보내는 중입니다.\n잠시만 기다려주세요...");
        progressDialog.setCancelable(false);

        nullData.put("nullData","nullData");

        //TimeStamp
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));

        editText_message = findViewById(R.id.activity_send_edittext);
        button_send = findViewById(R.id.activity_send_button);

        getMyInfo();
        getUserList();
        getChatList();

        //2020.06.04 db 사용 최소화를 위한 주석처리
//        timeCheck();

        button_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(TextUtils.isEmpty(editText_message.getText().toString().trim())){
                    Toast.makeText(SendActivity.this, "메시지를 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                nowTime = new Date().getTime();
//                resultTime = nowTime-oldTime;
                resultTime = nowTime-timeCheck();

                Log.d(TAG, "oldTime " + timeCheck());
                Log.d(TAG, "nowTime " + nowTime);
                Log.d(TAG, "resultTime " + resultTime);

                if( resultTime < 60000) {
                    Toast.makeText(SendActivity.this, "메시지는 1분에 하나씩 보낼 수 있습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressDialog.show();

                for (int i=0; i<100; i++){
                    randomNumber = (int) (Math.random()*userModelList.size());

                    if (sendList == null) { //chatList == null || sendList == null
                        //쪽지 온게 없고(chatList), 보낸 적이 없을 때(sendList)  따로 체크할 것 없이 그냥 보내고 break
                        setMessageModel = new MessageModel();
                        setMessageModel.message = editText_message.getText().toString();
                        setMessageModel.writerUid = myUid;
                        setMessageModel.writerName = myName;
                        setMessageModel.writerEmail = myEmail;
                        setMessageModel.timestamp = new Date().getTime();
                        setMessageModel.formatTime = simpleDateFormat.format(setMessageModel.timestamp);
                        setMessageModel.read = false;
                        setSendMessage(randomNumber);
                        Log.d(TAG, String.valueOf(randomNumber));
                        break;
                    }
                    if (sendList != null && !sendList.contains(userModelList.get(randomNumber).uid)) {
                        //2번째 우선순위 = 대화중인상대(chatList) 또는 내가 쪽지보냈던 사람(sendList)은 제외
                        Log.d("test1","확인");
                        setMessageModel = new MessageModel();
                        setMessageModel.message = editText_message.getText().toString();
                        setMessageModel.writerUid = myUid;
                        setMessageModel.writerName = myName;
                        setMessageModel.writerEmail = myEmail;
                        setMessageModel.timestamp = new Date().getTime();
                        setMessageModel.formatTime = simpleDateFormat.format(setMessageModel.timestamp);
                        setMessageModel.read = false;
                        setSendMessage(randomNumber);
                        Log.d(TAG, String.valueOf(randomNumber));
                        break;
                    }
//                    if (chatList != null && !chatList.contains(userModelList.get(randomNumber).uid)) {
//                        //마지막 우선순위 = 모든 유저와 대화하고 있거나 쪽지를 보냈을 때는 그냥 대화중인 상대 빼고 보냄 (즉, 대화중이지 않고 보냈던 사람에게 다시 보냄)
//                        Log.d("test2","확인");
//                        setMessageModel = new MessageModel();
//                        setMessageModel.message = editText_message.getText().toString();
//                        setMessageModel.writerUid = myUid;
//                        setMessageModel.writerName = myName;
//                        setMessageModel.writerEmail = myEmail;
//                        setMessageModel.timestamp = new Date().getTime();
//                        setMessageModel.formatTime = simpleDateFormat.format(setMessageModel.timestamp);
//                        setMessageModel.read = false;
//                        setSendMessage(randomNumber);
//                        break;
//                    }
                    if (i==99){ //모든 유저랑 대화하고 있거나 쪽지를 보냈을 때 그냥 보낸척만함. (실제로 보내지진 않음)
                        Log.d(TAG,"99 확인");
                        //2020.06.04 db 사용 최소화를 위한 주석처리
                        // time이라는 필드에 보낸 시간 저장
//                        Map<String, Long> time = new HashMap<>();
//                        time.put("time",new Date().getTime());

//                        db.collection("sends").document(myUid).set(time, SetOptions.merge());

//                        final AppDatabase2 localDB2 = Room.databaseBuilder(getApplicationContext(), AppDatabase2.class, "db2").allowMainThreadQueries().build();
                        if (localDB2.dbDao2().getAll().size() != 0) {
                            DbEntity2 dbEntity2 = localDB2.dbDao2().getAll().get(0);
                            dbEntity2.setTime(new Date().getTime());
                            localDB2.dbDao2().update(dbEntity2);
                        } else {
                            DbEntity2 dbEntity2 = new DbEntity2(0);
                            dbEntity2.setTime(new Date().getTime());
                            localDB2.dbDao2().insert(dbEntity2);
                        }
                        Log.d(TAG,localDB2.dbDao2().getAll().get(0).toString());


                        handler.postDelayed(new sendHander(), 2000);
                    }


                }

            }
        });
    }

//    @Override // 베경터치시 키보드 숨김
//    public boolean onTouchEvent(MotionEvent event) {
//        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
//        return true;
//    }


    void setSendMessage(int randomNumber) {

//        final AppDatabase2 localDB2 = Room.databaseBuilder(getApplicationContext(), AppDatabase2.class, "db2").allowMainThreadQueries().build();
        if (localDB2.dbDao2().getAll().size() != 0) {
            DbEntity2 dbEntity2 = localDB2.dbDao2().getAll().get(0);
            dbEntity2.setTime(new Date().getTime());
            localDB2.dbDao2().update(dbEntity2);
        } else {
            DbEntity2 dbEntity2 = new DbEntity2(0);
            dbEntity2.setTime(new Date().getTime());
            localDB2.dbDao2().insert(dbEntity2);
        }
        Log.d(TAG,localDB2.dbDao2().getAll().get(0).toString());



        if (userModelList.size() == 0){ // user가 1명밖에 없을 때, 그냥 보낸척만함. (실제로 보내지진 않음)
            Log.d(TAG, "Check : " + userModelList);
            handler.postDelayed(new sendHander(), 2000);
            return;
        }

        token = userModelList.get(randomNumber).token;
        sendGcm();

        // collection 정보를 가져올 수 없기 때문에 uid 라는 필드에 컬렉션 정보를 저장 (set nullData를 만드는 이유는 도큐먼트가 없이 update를 할 수가 없기 때문에 uid update 하기 위해서 만드는 것)
        db.collection("chats").document(myUid).set(nullData, SetOptions.merge());
        db.collection("chats").document(userModelList.get(randomNumber).uid).set(nullData, SetOptions.merge());
        db.collection("sends").document(userModelList.get(randomNumber).uid).set(nullData, SetOptions.merge());
        db.collection("sends").document(myUid).set(nullData, SetOptions.merge());

        db.collection("chats").document(myUid).update("uid",FieldValue.arrayUnion(userModelList.get(randomNumber).uid));
        db.collection("chats").document(userModelList.get(randomNumber).uid).update("uid",FieldValue.arrayUnion(myUid));
        db.collection("sends").document(userModelList.get(randomNumber).uid).update("uid", FieldValue.arrayUnion(myUid));
        // sends - 내 uid 밑에는 기록하지 않는다 (채팅 목록에서 내가 보내기만 한 메시지는 확인 안해야하기때문에 상대방이 답장할때 sends에도 기록함)


        //메시지 전달
        db.collection("chats").document(myUid).collection(userModelList.get(randomNumber).uid).add(setMessageModel);
        db.collection("chats").document(userModelList.get(randomNumber).uid).collection(myUid).add(setMessageModel);


        //2020.06.04 db 사용 최소화를 위한 주석처리
        // time이라는 필드에 보낸 시간 저장
//        Map<String, Long> time = new HashMap<>();
//        time.put("time",new Date().getTime());
//        db.collection("sends").document(myUid).set(time, SetOptions.merge());

        handler.postDelayed(new sendHander(), 2000);
    }

    public void getUserList() { //자신 빼고 전체 유저리스트 가져옴
        db.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
//                                Log.d("test", document.getId() + " => " + document.getData());
                                if(myUid.equals((String) document.getData().get("uid"))){
                                    continue;
                                }
                                userModel = new UserModel();
                                userModel.email = (String) document.getData().get("email");
                                userModel.userName = (String) document.getData().get("userName");
                                userModel.uid = (String) document.getData().get("uid");
                                userModel.sex = (String) document.getData().get("sex");
                                userModel.age = (String) document.getData().get("age");
                                userModel.area = (String) document.getData().get("area");
                                userModel.token = (String) document.getData().get("token");

                                userModelList.add(userModel);
                            }

                        } else {
//                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

    }

    public void getChatList() {  //채팅중인사람 (상대방이 보내서 내 쪽지함에 있는 리스트)
//        db.collection("sends")
//                .document(myUid)
//                .get()
//                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                        if (task.isSuccessful()) {
//                            DocumentSnapshot document = task.getResult();
//                            if (document.exists()) {
//                                if (document.getData() == null || document.getData().size() == 0 || document.getData().isEmpty()) {
//                                    return;
//                                }
//                                if (document.get("uid") == null){
//                                    return;
//                                }
//                                chatList = (ArrayList<String>) document.getData().get("uid");
//                            } else {
//                                Log.d("test2", "No such document");
//                            }
//                        } else {
//                            Log.d("test3", "get failed with ", task.getException());
//                        }
//                    }
//                });
        //쪽지보낸사람 (내가 쪽지 보냈거나 대화중인 사람)
        db.collection("chats")
                .document(myUid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                if (document.getData() == null || document.getData().size() == 0 || document.getData().isEmpty()) {
                                    return;
                                }
                                if (document.get("uid") == null){
                                    return;
                                }
                                sendList = (ArrayList<String>) document.getData().get("uid");
                            } else {
                                Log.d("test2", "No such document");
                            }
                        } else {
                            Log.d("test3", "get failed with ", task.getException());
                        }
                    }
                });
    }


    public void getMyInfo() { //내 정보 가져오기
//        db.collection("users")
//                .whereEqualTo("uid", myUid)
//                .get()
//                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                        if (task.isSuccessful()) {
//                            for (QueryDocumentSnapshot document : task.getResult()) {
////                                Log.d("test", document.getId() + " => " + document.getData());
//                                myName = (String) document.getData().get("userName");
//                            }
//                        } else {
////                            Log.d(TAG, "Error getting documents: ", task.getException());
//                        }
//                    }
//                });
        db.collection("users").document(myUid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        myName = (String) document.getData().get("userName");
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

    }

    public long timeCheck(){ // 1분에 하나씩 보낼 수 있도록 하기 위해 타임체크로직

        //2020.06.04 db 사용 최소화를 위한 주석처리
//        db.collection("sends")
//                .document(myUid)
//                .get()
//                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//
//                        DocumentSnapshot document = task.getResult();
//
//                        if (document.getData() == null || document.getData().size() == 0 || document.getData().isEmpty()) {
//                            return;
//                        }
//                        if (document.get("time") == null){
//                            return;
//                        }
//
//                        oldTime = (long) document.get("time");
//
//                     }
//              });

//        final AppDatabase2 localDB2 = Room.databaseBuilder(getApplicationContext(), AppDatabase2.class, "db2").allowMainThreadQueries().build();
        if (localDB2.dbDao2().getAll().size() != 0) {
            DbEntity2 dbEntity2 = localDB2.dbDao2().getAll().get(0);
            return dbEntity2.getTime();
        } else {
            return 0;
        }

    }




    void sendGcm(){

        Gson gson = new Gson();

        NotificationModel notificationModel = new NotificationModel();
        notificationModel.to = token;
        notificationModel.notification.title = myName;
        notificationModel.notification.body = editText_message.getText().toString();
        notificationModel.data.title = myName;
        notificationModel.data.body = editText_message.getText().toString();


        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf8"),gson.toJson(notificationModel));

        Request request = new Request.Builder()
                .header("Content-Type","application/json")
                //키는 key= 쓰고 뒤에는 프로젝트 설정에 클라우드 메시징에 서버 키
                .addHeader("Authorization",getString(R.string.fcm_key))
                .url("https://fcm.googleapis.com/fcm/send")
                .post(requestBody)
                .build();
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

            }
        });
    }
    class sendHander implements Runnable {
        public void run() {
            progressDialog.dismiss();
            Toast.makeText(getApplicationContext(), "쪽지를 보냈습니다.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

}
