package com.Gonigon.bombi;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class MyInfoActivity extends AppCompatActivity {

    private AdView mAdView;

    ArrayList<String> dbDeleteArray = new ArrayList<>();

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseUser user = firebaseAuth.getCurrentUser();
    String myUid = firebaseAuth.getCurrentUser().getUid();
    String myEmail = firebaseAuth.getCurrentUser().getEmail();

    private Button button_logout;
    private Button button_delete_account;
    Button button_password_change;
    private TextView textView_info;
    String TAG = "seonggon";

    Handler handler = new Handler();
    ProgressDialog progressDialog;

    int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myinfo);

        MobileAds.initialize(this, getString(R.string.admob_app_id));
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("DB에서 개인정보를 제거하는 중입니다.\n잠시만 기다려주세요.");

        button_logout = findViewById(R.id.activity_myinfo_button_logout);
        button_delete_account = findViewById(R.id.activity_myinfo_button_delete_account);
        button_password_change = findViewById(R.id.activity_myinfo_button_password_change);
        textView_info = findViewById(R.id.activity_myinfo_textview_info);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        getValue();

        button_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alert_logout = new AlertDialog.Builder(MyInfoActivity.this);
                alert_logout.setMessage("로그아웃 하시겠습니까?").setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        firebaseAuth.signOut();
                        Toast.makeText(MyInfoActivity.this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();
                        finish();
                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);

                    }
                });
                alert_logout.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                alert_logout.show();

            }
        });



        button_password_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                AlertDialog.Builder alert_password_chage = new AlertDialog.Builder(MyInfoActivity.this);

                alert_password_chage.setTitle("비밀번호 변경");
                alert_password_chage.setMessage("비밀번호를 변경하시겠습니까? 6자리 이상");

                // alert에 editText 2개 넣기 위해 layout 만듬
                LinearLayout layout = new LinearLayout(getApplicationContext());
                layout.setOrientation(LinearLayout.VERTICAL);
                final EditText editText = new EditText(MyInfoActivity.this);
                final EditText editText2 = new EditText(MyInfoActivity.this);
                editText.setInputType( InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                editText2.setInputType( InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                editText.setHint("비밀번호를 입력하세요.");
                editText2.setHint("비밀번호를 입력하세요.");
                layout.addView(editText);
                layout.addView(editText2);
                alert_password_chage.setView(layout);


                alert_password_chage.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String value = editText.getText().toString().trim();
                        String value2 = editText2.getText().toString().trim();

                        if (value.length() < 6 ){
                            Toast.makeText(MyInfoActivity.this, "비밀번호는 6자리 이상이어야 합니다.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (!value.equals(value2)){
                            Toast.makeText(MyInfoActivity.this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        user.updatePassword(value)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(MyInfoActivity.this, "비밀번호가 변경되었습니다.", Toast.LENGTH_SHORT).show();

                                            db.collection("users").document(myUid).update("password", value);

                                        } else {
                                            Toast.makeText(MyInfoActivity.this, "비밀번호 변경 실패", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                    }
                });

                alert_password_chage.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                alert_password_chage.show();

            }
        });



        button_delete_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder alert_delete = new AlertDialog.Builder(MyInfoActivity.this);

                alert_delete.setTitle("회원탈퇴");
                alert_delete.setMessage("탈퇴하시겠습니까?");

                final EditText editText = new EditText(MyInfoActivity.this);
                editText.setInputType( InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                editText.setHint("비밀번호를 입력하세요.");

                alert_delete.setView(editText);

                alert_delete.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String value = editText.getText().toString().trim();
                        passwordCheck(value);

                    }
                });

                alert_delete.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                alert_delete.show();

            }
        });

    }


    public void passwordCheck(String pasword){

        if (pasword == null || TextUtils.isEmpty(pasword)){
            Toast.makeText(MyInfoActivity.this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        firebaseAuth.signInWithEmailAndPassword(myEmail, pasword)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            progressDialog.show();
                            dbDelete();


                        } else {
                            Toast.makeText(MyInfoActivity.this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }
    public void dbDelete() {
        db.collection("chats").document(myUid).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                dbDeleteArray = (ArrayList<String>) document.get("uid");

                                if (dbDeleteArray == null || dbDeleteArray.size() == 0){ //대화 했었지만 현재는 대화목록을 다 지운 사용자 일 때
                                    handler.postDelayed(new deleteAccount(), 1000);
                                }
                                for (int i=0; i<dbDeleteArray.size(); i++){

                                    db.collection("chats").document(dbDeleteArray.get(i)).update("uid", FieldValue.arrayRemove(myUid));
                                    db.collection("sends").document(dbDeleteArray.get(i)).update("uid", FieldValue.arrayRemove(myUid));

                                    db.collection("chats").document(myUid).collection(dbDeleteArray.get(i)).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()) {
                                                for (QueryDocumentSnapshot document : task.getResult()) {
                                                    document.getReference().delete();
                                                }
                                                count++;
                                                if (count == dbDeleteArray.size()*2){
                                                    handler.postDelayed(new deleteAccount(), 1000);
                                                }
                                            } else {
                                            }
                                        }
                                    });

                                    db.collection("chats").document(dbDeleteArray.get(i)).collection(myUid).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()) {
                                                for (QueryDocumentSnapshot document : task.getResult()) {
                                                    document.getReference().delete();
                                                }
                                                count++;
                                                if (count == dbDeleteArray.size()*2){
                                                    handler.postDelayed(new deleteAccount(), 1000);
                                                }
                                            } else {
                                            }
                                        }
                                    });
                                }

                            } else { //가입 후 아무 DB도 쓰지 않았을 때 (no document)
                                Log.d(TAG, "No such document");
                                handler.postDelayed(new deleteAccount(), 1000);
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                        }
                    }
                });
//        handler.postDelayed(new deleteAccount(), 7000);
    }
    public class deleteAccount implements Runnable {
        public void run() {
            db.collection("users").document(myUid).delete();

            db.collection("chats").document(myUid).delete();

            db.collection("sends").document(myUid).delete();

            user.delete();

            handler.postDelayed(new deleteFinish(), 1000);
        }
    }
    public class deleteFinish implements Runnable {
        public void run() {
            progressDialog.dismiss();
            Toast.makeText(MyInfoActivity.this, "탈퇴되었습니다.", Toast.LENGTH_SHORT).show();
            finish();
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }


    public void getValue() {

        db.collection("users").document(myUid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        textView_info.setText(document.getData().get("userName") + "님 반갑습니다.\n이메일 : " + user.getEmail() + "\n가입일 : " + document.getData().get("signupTime") + "\n" + document.getData().get("area") + " " + document.getData().get("sex") + " " + document.getData().get("age"));
//                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                    } else {
//                        Log.d(TAG, "No such document");
                    }
                } else {
//                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

    }
}
