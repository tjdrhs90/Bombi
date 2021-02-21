package com.Gonigon.bombi;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private AdView mAdView;

    ArrayList<String> myChatList = new ArrayList<>(); // sends 아래 내 uid 아래 uid필드에 있는 목록

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ImageButton button_move_send, button_move_read, button_move_myinfo, button_move_notice;
    String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    String TAG = "seonggon";

    long oldTime;
    long nowTime;
    long resultTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MobileAds.initialize(this, getString(R.string.admob_app_id));
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        nowTime = new Date().getTime();

        button_move_send = findViewById(R.id.activity_main_move_send);
        button_move_read = findViewById(R.id.activity_main_move_read);
        button_move_myinfo = findViewById(R.id.activity_main_move_myinfo);
        button_move_notice = findViewById(R.id.activity_main_move_notice);

        button_move_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),SendActivity.class));
            }
        });
        button_move_read.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),ChatActivity.class));
            }
        });
        button_move_myinfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),MyInfoActivity.class));
            }
        });
        button_move_notice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),NoticeActivity.class));
            }
        });


        passPushTokenToServer();

        //2020.06.04 db 사용 최소화를 위한 주석처리
//        oldMessageCheck();

    }



    void passPushTokenToServer(){



//        db.collection("users").whereEqualTo("uid",myUid).get()
//                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                        if (task.isSuccessful()) {
//                            for (QueryDocumentSnapshot document : task.getResult()) {
//
//                                String token = FirebaseInstanceId.getInstance().getToken();
//                                Map<String, Object> map = new HashMap<>();
//                                map.put("token", token);
//
//                                document.getReference().update(map);
//
//                            }
//                        }
//                    }
//                });

        String token = FirebaseInstanceId.getInstance().getToken();
        Map<String, Object> map = new HashMap<>();
        map.put("token", token);
        db.collection("users").document(myUid).update(map);
    }


    public void oldMessageCheck() {
        db.collection("chats").document(myUid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    if (document.getData() == null || document.getData().size() == 0 || document.getData().isEmpty()) {
                        return;
                    }

                    if (document.get("uid") == null){
                        return;
                    }

                    myChatList = (ArrayList<String>) document.get("uid");

                    for (int i=0; i<myChatList.size(); i++){
                        final int finalI = i;
                        db.collection("chats").document(myUid).collection(myChatList.get(i)).orderBy("timestamp", Query.Direction.DESCENDING).limit(1).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {

                                        oldTime = (long) document.get("timestamp");
                                        resultTime = nowTime - oldTime;
                                        if (resultTime > 864000000) { // 10일 이상 지났는지 체크
                                            Log.d("test" , "10일이상 지남");
                                            dbDelete(myChatList.get(finalI));
                                        }

                                    }


                                } else {
                                }
                            }
                        });
                    }
                } else {
                }
            }
        });
    }

    public void dbDelete(final String yourUid) {
        db.collection("chats").document(myUid).update("uid", FieldValue.arrayRemove(yourUid));
        db.collection("chats").document(yourUid).update("uid", FieldValue.arrayRemove(myUid));
        db.collection("sends").document(myUid).update("uid", FieldValue.arrayRemove(yourUid));
        db.collection("sends").document(yourUid).update("uid", FieldValue.arrayRemove(myUid));

        db.collection("chats").document(yourUid).collection(myUid).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        document.getReference().delete();
                    }
                } else {
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
                } else {
                }
            }
        });
//        db.collection("sends").document(yourUid).collection(myUid).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                if (task.isSuccessful()) {
//                    for (QueryDocumentSnapshot document : task.getResult()) {
//                        document.getReference().delete();
//                    }
//                } else {
//                }
//            }
//        });
//        db.collection("sends").document(myUid).collection(yourUid).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                if (task.isSuccessful()) {
//                    for (QueryDocumentSnapshot document : task.getResult()) {
//                        document.getReference().delete();
//                    }
//                } else {
//                }
//            }
//        });


    }

}
