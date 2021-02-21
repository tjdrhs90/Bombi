package com.Gonigon.bombi;


import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.Gonigon.bombi.model.ListCheckModel;
import com.Gonigon.bombi.model.UserModel;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;

public class ChatActivity extends AppCompatActivity {

    private AdView mAdView;


    ArrayList<String> myChatList = new ArrayList<>(); // sends 아래 내 uid 아래 uid필드에 있는 목록
    ArrayList<UserModel> list = new ArrayList<>();  //정렬 안된 대화목록 리스트
    ArrayList<ListCheckModel> sortArrayList = new ArrayList<>(); //마지막 메시지 시간순으로 정렬한 리스트


    UserModel userModel;
    ListCheckModel listCheckModel;


    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    String myUid = firebaseAuth.getCurrentUser().getUid();
    String myName;

    TextView textView_noMember;

    RecyclerView recyclerView;

    String TAG = "seonggon";

    int count = 0; //for문 완료 타이밍 이슈를 잡기 위한 변수
    int count2 = 0; //for문 완료 타이밍 이슈를 잡기 위한 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        MobileAds.initialize(this, getString(R.string.admob_app_id));
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


        textView_noMember = findViewById(R.id.activity_chat_textview_noMember);

        getMyInfo();

        // 리사이클러뷰에 LinearLayoutManager 객체 지정.
        recyclerView = findViewById(R.id.activity_chat_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    protected void onResume() {
        super.onResume();
        recyclerView.setAdapter(null);
        myChatList.clear();
        list.clear();
        sortArrayList.clear();
        getChatList();
    }

    public void getChatList() {
        // 리사이클러뷰에 표시할 데이터 리스트 생성.
        db.collection("sends")
                .document(myUid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        final DocumentSnapshot documentSnapshot = task.getResult();

                        if (documentSnapshot.getData() == null || documentSnapshot.getData().isEmpty() || documentSnapshot.getData().size() == 0){
                            textView_noMember.setVisibility(View.VISIBLE); //대화 중인 상대가 없습니다. 표시
                            recyclerView.setAdapter(null);
                            return;
                        }

                        if (documentSnapshot.get("uid") == null){
                            textView_noMember.setVisibility(View.VISIBLE); //대화 중인 상대가 없습니다. 표시
                            recyclerView.setAdapter(null);
                            return;
                        }


                        myChatList = (ArrayList<String>) documentSnapshot.get("uid");

                        if (myChatList.size() == 0 ){
                            textView_noMember.setVisibility(View.VISIBLE); //대화 중인 상대가 없습니다. 표시
                            recyclerView.setAdapter(null);
                            return;
                        }

//                        if ( documentSnapshot.get("uid") == null ){
//                            textView_noMember.setVisibility(View.VISIBLE); //대화 중인 상대가 없습니다. 표시
//                            return;
//                        }

                        textView_noMember.setVisibility(View.INVISIBLE); //대화 중인 상대가 없습니다. 제거


//                        db.collection("users")
//                                .get()
//                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                                    @Override
//                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//
//
//                                        for (QueryDocumentSnapshot document : task.getResult()) {
//
//                                            if (myChatList.contains(document.getData().get("uid"))) {
//                                                userModel = new UserModel();
//                                                userModel.email = (String) document.getData().get("email");
//                                                userModel.userName = (String) document.getData().get("userName");
//                                                userModel.uid = (String) document.getData().get("uid");
//                                                userModel.sex = (String) document.getData().get("sex");
//                                                userModel.age = (String) document.getData().get("age");
//                                                userModel.area = (String) document.getData().get("area");
//                                                userModel.token = (String) document.getData().get("token");
//
//                                                list.add(userModel);
//                                            }
//
//                                        }
////                                        // 리사이클러뷰에 ChatAdapter 객체 지정.
////                                        ChatAdapter adapter = new ChatAdapter(list);
////                                        recyclerView.setAdapter(adapter);
//
//                                        sortList();
//
//                                    }
//                                });

                        for (int i=0; i<myChatList.size(); i++){

                            if (i==0){
                                count = 0;
                            }

                            final int finalI = i;

                            db.collection("users").document(myChatList.get(i)).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot document = task.getResult();
                                        if (document.exists()) {
                                            Log.d(TAG, "DocumentSnapshot data: " + document.getData());

                                            userModel = new UserModel();
                                            userModel.email = (String) document.getData().get("email");
                                            userModel.userName = (String) document.getData().get("userName");
                                            userModel.uid = (String) document.getData().get("uid");
                                            userModel.sex = (String) document.getData().get("sex");
                                            userModel.age = (String) document.getData().get("age");
                                            userModel.area = (String) document.getData().get("area");
                                            userModel.token = (String) document.getData().get("token");

                                            list.add(userModel);

                                            count++;

                                            if (count == myChatList.size()){ //마지막에 정렬
                                                sortList();
                                                count = 0;
                                            }

                                        } else {
                                            Log.d(TAG, "No such document");
                                        }
                                    } else {
                                        Log.d(TAG, "get failed with ", task.getException());
                                    }
                                }
                            });

                        }




//                        db.collection("users")
//                                .whereArrayContains("uid",myChatList)
//                                .get()
//                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                                    @Override
//                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//
//                                        Log.d(TAG, String.valueOf(task.getResult()));
//
//                                        for (QueryDocumentSnapshot document : task.getResult()) {
//
//                                            Log.d(TAG, String.valueOf(document));
//
//                                                userModel = new UserModel();
//                                                userModel.email = (String) document.getData().get("email");
//                                                userModel.userName = (String) document.getData().get("userName");
//                                                userModel.uid = (String) document.getData().get("uid");
//                                                userModel.sex = (String) document.getData().get("sex");
//                                                userModel.age = (String) document.getData().get("age");
//                                                userModel.area = (String) document.getData().get("area");
//                                                userModel.token = (String) document.getData().get("token");
//
//                                                list.add(userModel);
//
//                                        }
////                                        // 리사이클러뷰에 ChatAdapter 객체 지정.
////                                        ChatAdapter adapter = new ChatAdapter(sortArrayList);
////                                        recyclerView.setAdapter(adapter);
//
//                                        sortList();
////                                        oldMessageCheck();
//
//                                    }
//                                });

//                        for (int i=0; i<myChatList.size(); i++){
//                            db.collection("users")
//                                .whereEqualTo("uid",myChatList.get(i))
//                                .get()
//                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                                    @Override
//                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//
//                                        if (task.isSuccessful()) {
//                                            for (QueryDocumentSnapshot document : task.getResult()) {
//                                                userModel = new UserModel();
//                                                userModel.email = (String) document.getData().get("email");
//                                                userModel.userName = (String) document.getData().get("userName");
//                                                userModel.uid = (String) document.getData().get("uid");
//                                                userModel.sex = (String) document.getData().get("sex");
//                                                userModel.age = (String) document.getData().get("age");
//                                                userModel.area = (String) document.getData().get("area");
//                                                userModel.token = (String) document.getData().get("token");
//
//                                                list.add(userModel);
//                                            }
//                                            sortList();
//                                        } else {
//
//                                        }
//                                    }
//                                });
//                        }


                    }
                });
    }
    public void sortList() {
        for (int i=0; i<list.size(); i++){

            if (i==0){
                count2 = 0;
            }

            final int finalI = i;

            db.collection("chats").document(myUid).collection(list.get(i).uid).orderBy("timestamp", Query.Direction.DESCENDING).limit(1)
                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {

                            listCheckModel = new ListCheckModel();
                            listCheckModel.email = list.get(finalI).email;
                            listCheckModel.userName = list.get(finalI).userName;
                            listCheckModel.uid = list.get(finalI).uid;
                            listCheckModel.sex = list.get(finalI).sex;
                            listCheckModel.age = list.get(finalI).age;
                            listCheckModel.area = list.get(finalI).area;
                            listCheckModel.token = list.get(finalI).token;
                            listCheckModel.lastMessageTime = String.valueOf(document.getData().get("timestamp"));
                            listCheckModel.lastMessage = String.valueOf(document.getData().get("message"));
                            listCheckModel.read = (Boolean) document.getData().get("read");
                            listCheckModel.chatBubbleWriter = (String) document.getData().get("writerUid");

                            sortArrayList.add(listCheckModel);

                            count2++;

                            if (count2 == list.size()){ //마지막에 정렬
                                Collections.sort(sortArrayList);
                                Collections.reverse(sortArrayList);

                                // 리사이클러뷰에 ChatAdapter 객체 지정.
                                ChatAdapter adapter2 = new ChatAdapter(sortArrayList);
                                recyclerView.setAdapter(adapter2);
                                count2 = 0;
                            }

                        }




                    } else {

                    }

                }
            });

        }


    }


    public void getMyInfo() {
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



}
