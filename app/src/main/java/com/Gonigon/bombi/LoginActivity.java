package com.Gonigon.bombi;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.room.Room;

import com.Gonigon.bombi.room.AppDatabase;
import com.Gonigon.bombi.room.DbEntity;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class LoginActivity extends AppCompatActivity {

    private AdView mAdView;

    FirebaseRemoteConfig mFirebaseRemoteConfig;

    EditText editTextEmail;
    EditText editTextPassword;
    Button buttonSignin;
    Button buttonSignupMove;
    Button buttonFindPassword;
    ProgressDialog progressDialog;
    FirebaseAuth firebaseAuth;

    Boolean findPasswordFlag = false;

    String TAG = "seonggon";

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd");

    ConstraintLayout constraintLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        constraintLayout = findViewById(R.id.loginActivity_constraintLayout);


        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(0)
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);


        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);

        mFirebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(this, new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(@NonNull Task<Boolean> task) {
                        if (task.isSuccessful()) {
                            boolean updated = task.getResult();
                            Log.d(TAG, "Config params updated: " + updated);
//                            Toast.makeText(LoginActivity.this, "Fetch and activate succeeded",
//                                    Toast.LENGTH_SHORT).show();
                        } else {
//                            Toast.makeText(LoginActivity.this, "Fetch failed",
//                                    Toast.LENGTH_SHORT).show();
                        }
                        displayMessage();
                    }
                });


        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));

        MobileAds.initialize(this, getString(R.string.admob_app_id));
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signOut();

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonSignin = findViewById(R.id.buttonLogin);
        buttonSignupMove = findViewById(R.id.buttonSignupMove);
        buttonFindPassword = findViewById(R.id.activity_login_findPassword);
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        final AppDatabase localDB = Room.databaseBuilder(this,AppDatabase.class, "db").allowMainThreadQueries().build();

//        Log.d("sgsg", String.valueOf(localDB.dbDao().getAll().size()));

        if (localDB.dbDao().getAll().size() != 0) {
            editTextEmail.setText(localDB.dbDao().getAll().get(0).getId());
            editTextPassword.setText(localDB.dbDao().getAll().get(0).getPw());
        }



        buttonSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userLogin();
            }
        });

        buttonSignupMove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivity(intent);
            }
        });

        buttonFindPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alert_delete = new AlertDialog.Builder(LoginActivity.this);

                alert_delete.setTitle("비밀번호 찾기");
                alert_delete.setMessage("입력한 주소로 비밀번호 재설정 메일이 발송됩니다.");

                final EditText editText = new EditText(LoginActivity.this);
                editText.setHint("이메일을 입력하세요.");
                editText.setSingleLine();

                alert_delete.setView(editText);

                alert_delete.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String value = editText.getText().toString().trim();
                        findPassword(value);

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

    public void displayMessage() {
        boolean splash_message_caps = mFirebaseRemoteConfig.getBoolean("splash_message_caps");
        String splash_message = mFirebaseRemoteConfig.getString("splash_message");
        boolean splash_notice_caps = mFirebaseRemoteConfig.getBoolean("splash_notice_caps");
        String splash_notice = mFirebaseRemoteConfig.getString("splash_notice");

        String splash_background = mFirebaseRemoteConfig.getString("splash_background");
        constraintLayout.setBackgroundColor(Color.parseColor(splash_background));


        if(splash_notice_caps){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(false);
            builder.setMessage(splash_notice).setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            builder.create().show();
        }
        if(splash_message_caps){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(false);
            builder.setMessage(splash_message).setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            builder.create().show();
        }

    }


//    @Override // 베경터치시 키보드 숨김
//    public boolean onTouchEvent(MotionEvent event) {
//        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
//        return true;
//    }

    private void userLogin() {
        final String email = editTextEmail.getText().toString().trim();
        final String password = editTextPassword.getText().toString().trim();

        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "email을 입력해 주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "password를 입력해 주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        progressDialog.setMessage("로그인중입니다. 잠시 기다려 주세요...");
        progressDialog.show();

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        if(task.isSuccessful()){

//                            db.collection("users")
//                                    .whereEqualTo("uid", FirebaseAuth.getInstance().getUid())
//                                    .get()
//                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                                        @Override
//                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
//
//                                            if (task.isSuccessful()) {
//
//                                                for (QueryDocumentSnapshot document : task.getResult()) {
//
//                                                    document.getReference().update("password", password);
//
//                                                }
//
//                                            } else {
//                                            }
//                                        }
//                                    });


                            //2020.06.04 db 사용 최소화를 위한 주석처리
//                            db.collection("users").document(FirebaseAuth.getInstance().getUid()).update("password", password);
//                            db.collection("users").document(FirebaseAuth.getInstance().getUid()).update("lastSigninTime", simpleDateFormat.format(new Date().getTime()));


                            final AppDatabase localDB = Room.databaseBuilder(getApplicationContext(),AppDatabase.class, "db").allowMainThreadQueries().build();
                            if (localDB.dbDao().getAll().size() != 0) {
                                DbEntity dbEntity = new DbEntity(0);
                                dbEntity.setId(email);
                                dbEntity.setPw(password);
                                localDB.dbDao().update(dbEntity);
                            } else {
                                DbEntity dbEntity = new DbEntity(0);
                                dbEntity.setId(email);
                                dbEntity.setPw(password);
                                localDB.dbDao().insert(dbEntity);
                            }


                            Toast.makeText(LoginActivity.this, "로그인 되었습니다.", Toast.LENGTH_SHORT).show();

                            finish();

                            Log.d("sgsg",email);
                            if (email.equals("tjdrhs90@gmail.com")) {
//                                startActivity(new Intent(getApplicationContext(), ManagerActivity.class));
                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            } else {
                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            }


                        } else {
                            Toast.makeText(LoginActivity.this, "이메일 또는 비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void findPassword(final String email){

        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {

                            findPasswordFlag = false;

                            for (QueryDocumentSnapshot document : task.getResult()) {

                                findPasswordFlag = true;
                                //비밀번호 재설정 메일 발송
                                firebaseAuth.sendPasswordResetEmail(email)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(getApplicationContext(), "해당 주소로 비밀번호 재설정 메일이 발송되었습니다.", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                            if (!findPasswordFlag){
                                Toast.makeText(getApplicationContext(), "가입되지 않은 계정입니다.", Toast.LENGTH_SHORT).show();
                            }

                        } else {
                        }
                    }
                });




    }
}
