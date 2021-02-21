package com.Gonigon.bombi;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.Gonigon.bombi.model.MessageModel;
import com.Gonigon.bombi.model.NotificationModel;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
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

public class RoomActivity extends AppCompatActivity {

    private AdView mAdView;

    String TAG = "seonggon";

    Map<String, String> nullData = new HashMap<>();

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    String yourUid;
    String token;
    String myUid = firebaseAuth.getCurrentUser().getUid();
    String myEmail = firebaseAuth.getCurrentUser().getEmail();
    String myName;

    EditText editText;
    ImageButton buttonSend, buttonExit;

    TextView textViewName, textViewArea, textViewSex, textViewAge;

    MessageModel getMessageModel;
    MessageModel setMessageModel;

    ArrayList<MessageModel> messageModelArrayList = new ArrayList<>();

    RecyclerView recyclerView;

    ProgressDialog progressDialog;

    int count = 0;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        MobileAds.initialize(this, getString(R.string.admob_app_id));
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("DB에서 제거중입니다..\n잠시만 기다려주세요...");


        nullData.put("nullData","nullData");

        // 리사이클러뷰에 LinearLayoutManager 객체 지정.
        recyclerView = findViewById(R.id.activity_room_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        //TimeStamp
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));

        yourUid = getIntent().getStringExtra("uid");
        token = getIntent().getStringExtra("token");


        editText = findViewById(R.id.activity_room_editText_message);
        buttonSend = findViewById(R.id.activity_room_button_send);
        buttonExit = findViewById(R.id.activity_room_button_exit);
        textViewName = findViewById(R.id.activity_room_textView_userName);
        textViewAge = findViewById(R.id.activity_room_textView_age);
        textViewSex = findViewById(R.id.activity_room_textView_sex);
        textViewArea = findViewById(R.id.activity_room_textView_area);

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(editText.getText().toString().trim())) {
                    Toast.makeText(RoomActivity.this, "메시지를 입력하세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
                sendGcm();
                buttonSend.setEnabled(false);

                setMessageModel = new MessageModel();
                setMessageModel.message = editText.getText().toString();
                setMessageModel.writerUid = myUid;
                setMessageModel.writerName = myName;
                setMessageModel.writerEmail = myEmail;
                setMessageModel.read = false;

                setMessageModel.timestamp = new Date().getTime();
                setMessageModel.formatTime = simpleDateFormat.format(setMessageModel.timestamp);
                setValue();
            }
        });

        buttonExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alert_logout = new AlertDialog.Builder(RoomActivity.this);
                alert_logout.setMessage("대화방을 나가시겠습니까?").setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        progressDialog.show();
                        dbDelete();

                    }
                });
                alert_logout.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                alert_logout.show();

            }
        });

        getUserInfo();
        getMessageList();

    }

//    @Override // 베경터치시 키보드 숨김
//    public boolean onTouchEvent(MotionEvent event) {
//        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
//        return true;
//    }

    void setValue () {
        db.collection("chats").document(myUid).collection(yourUid).add(setMessageModel).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                getMessageList();
            }
        });
        db.collection("chats").document(yourUid).collection(myUid).add(setMessageModel);


        db.collection("sends").document(myUid).set(nullData, SetOptions.merge());
        db.collection("sends").document(myUid).update("uid",FieldValue.arrayUnion(yourUid));
        db.collection("sends").document(yourUid).set(nullData, SetOptions.merge());
        db.collection("sends").document(yourUid).update("uid",FieldValue.arrayUnion(myUid));

        db.collection("chats").document(myUid).set(nullData, SetOptions.merge());
        db.collection("chats").document(myUid).update("uid",FieldValue.arrayUnion(yourUid));
        db.collection("chats").document(yourUid).set(nullData, SetOptions.merge());
        db.collection("chats").document(yourUid).update("uid",FieldValue.arrayUnion(myUid));


    }
    void getMessageList() {
        db.collection("chats").document(myUid).collection(yourUid).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot document : task.getResult()){
                        if (document.getData().get("writerUid").equals(yourUid)){
                            document.getReference().update("read",true);
                        }
                    }
                }
            }
        });
        db.collection("chats").document(yourUid).collection(myUid).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot document : task.getResult()){
                        if (document.getData().get("writerUid").equals(yourUid)){
                            document.getReference().update("read",true);
                        }
                    }
                }
            }
        });


        db.collection("chats")
                .document(myUid)
                .collection(yourUid)
                .orderBy("timestamp")
                .get()

                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            messageModelArrayList.clear();

                            for (QueryDocumentSnapshot document : task.getResult()) {

                                getMessageModel = new MessageModel();
                                getMessageModel.writerUid = (String) document.getData().get("writerUid");
                                getMessageModel.writerEmail = (String) document.getData().get("writerEmail");
                                getMessageModel.writerName = (String) document.getData().get("writerName");
                                getMessageModel.message = (String) document.getData().get("message");
                                getMessageModel.timestamp = document.getData().get("timestamp");
                                getMessageModel.formatTime = (String) document.getData().get("formatTime");
                                getMessageModel.read = (Boolean) document.getData().get("read");


                                messageModelArrayList.add(getMessageModel);
                            }
                        } else {

                        }
                        buttonSend.setEnabled(true);
                        RoomAdapter adapter = new RoomAdapter(messageModelArrayList);
                        recyclerView.setAdapter(adapter);
                        // 스크롤 최하단으로 이동
                        recyclerView.scrollToPosition(messageModelArrayList.size()-1);
                        editText.setText("");
                    }
                });


    }
    public void getUserInfo() {
        //내 정보 확인
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

        //상대방 정보 확인
        db.collection("users").document(yourUid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        textViewName.setText((String) document.getData().get("userName"));
                        textViewArea.setText((String) document.getData().get("area"));
                        textViewSex.setText((String) document.getData().get("sex"));
                        textViewAge.setText((String) document.getData().get("age"));

                        if ( document.getData().get("sex").equals("남성")) {
                            textViewSex.setTextColor(0xFF2196F3);
                        } else if ( document.getData().get("sex").equals("여성")) {
                            textViewSex.setTextColor(0xFFFF96D7);
                        }
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

    void sendGcm(){

        Gson gson = new Gson();

        NotificationModel notificationModel = new NotificationModel();
        notificationModel.to = token;
        notificationModel.notification.title = myName;
        notificationModel.notification.body = editText.getText().toString();
        notificationModel.data.title = myName;
        notificationModel.data.body = editText.getText().toString();


        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf8"),gson.toJson(notificationModel));

        Request request = new Request.Builder()
                .header("Content-Type","application/json")
                //키는 key= 쓰고 뒤에는 프로젝트 설정에 클라우드 메시징에 서버
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

    public void dbDelete() {
        db.collection("chats").document(myUid).update("uid", FieldValue.arrayRemove(yourUid)).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                count ++;
                if (count==6){
                    progressDialog.dismiss();
                    Toast.makeText(RoomActivity.this, "대화방이 제거되었습니다.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
        db.collection("chats").document(yourUid).update("uid", FieldValue.arrayRemove(myUid)).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                count ++;
                if (count==6){
                    progressDialog.dismiss();
                    Toast.makeText(RoomActivity.this, "대화방이 제거되었습니다.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
        db.collection("sends").document(myUid).update("uid", FieldValue.arrayRemove(yourUid)).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                count ++;
                if (count==6){
                    progressDialog.dismiss();
                    Toast.makeText(RoomActivity.this, "대화방이 제거되었습니다.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
        db.collection("sends").document(yourUid).update("uid", FieldValue.arrayRemove(myUid)).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                count ++;
                if (count==6){
                    progressDialog.dismiss();
                    Toast.makeText(RoomActivity.this, "대화방이 제거되었습니다.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
        db.collection("chats").document(myUid).collection(yourUid).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        document.getReference().delete();
                    }
                    count ++;
                    if (count==6){
                        progressDialog.dismiss();
                        Toast.makeText(RoomActivity.this, "대화방이 제거되었습니다.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                }
            }
        });
        db.collection("chats").document(yourUid).collection(myUid).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        document.getReference().delete();
                    }
                    count ++;
                    if (count==6){
                        progressDialog.dismiss();
                        Toast.makeText(RoomActivity.this, "대화방이 제거되었습니다.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                }
            }
        });

    }

}
