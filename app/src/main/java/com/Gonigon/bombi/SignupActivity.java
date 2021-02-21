package com.Gonigon.bombi;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.Gonigon.bombi.model.UserModel;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class SignupActivity extends AppCompatActivity {

    private AdView mAdView;

    EditText editTextEmail;
    EditText editTextPassword;
    EditText editTextPassword2;
    EditText editTextUsername;
    Button buttonSignup;
    Button buttonCancel;
    Spinner spinner_area, spinner_age;
    String area = "지역선택";
    String age = "나이선택";
    String sex;
    RadioGroup radioGroup;

    ProgressDialog progressDialog;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    UserModel userModel = new UserModel();

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        MobileAds.initialize(this, getString(R.string.admob_app_id));
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


        //TimeStamp
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));

        firebaseAuth = FirebaseAuth.getInstance();

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextPassword2 = findViewById(R.id.editTextPassword2);
        editTextUsername = findViewById(R.id.editTextUsername);
        buttonSignup = findViewById(R.id.buttonSignup);
        buttonCancel = findViewById(R.id.buttonCancel);
        spinner_area = findViewById(R.id.activity_signup_area_spinner);
        spinner_age = findViewById(R.id.activity_signup_age_spinner);
        radioGroup = findViewById(R.id.activity_signup_sex_radioGroup);
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        buttonSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // spinner 구현
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.area, android.R.layout.simple_spinner_dropdown_item);
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this,R.array.age, android.R.layout.simple_spinner_dropdown_item);
        spinner_area.setAdapter(adapter);
        spinner_age.setAdapter(adapter2);

        spinner_area.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                if(spinner.getSelectedItemPosition() > 0) {                }
//                spinner.getSelectedItem().toString()
//                parent.getItemAtPosition(position).toString()
                area = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinner_age.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                age = spinner_age.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.activity_signup_sex_radioButton1){
                    sex = "남성";
                } else {
                    sex = "여성";
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


    private void registerUser(){
        final String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String password2 = editTextPassword2.getText().toString().trim();
        String username = editTextUsername.getText().toString().trim();

        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "이메을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        if(password.length() < 6){
            Toast.makeText(this, "비밀번호를 6자리 이상 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!password.equals(password2)){
            Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(username)){
            Toast.makeText(this, "닉네임을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        if(sex == null) {
            Toast.makeText(this, "성별을 선택해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        if(area.equals("지역선택")){
            Toast.makeText(this, "지역을 선택해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        if(age.equals("나이선택")){
            Toast.makeText(this, "나이를 선택해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        userModel.email = editTextEmail.getText().toString().trim();
        userModel.userName = editTextUsername.getText().toString();
        userModel.sex = sex;
        userModel.area = area;
        userModel.age = age;
        userModel.signupTime = simpleDateFormat.format(new Date().getTime());
        userModel.lastSigninTime = simpleDateFormat.format(new Date().getTime());
        userModel.password = password;

        progressDialog.setMessage("등록중입니다.\n기다려주세요...");
        progressDialog.show();

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
//                            user.put("email",editTextEmail.getText().toString().trim());
//                            user.put("name",editTextUsername.getText().toString().trim());
                            userModel.uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                            setValue();

                        } else {
                            //에러발생시
                            Toast.makeText(SignupActivity.this, "이미 등록된 이메일 또는\n암호 6자리 미만 또는\n서버에러", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }

                    }
                });
    }
    public void setValue() {
//        db.collection("users")
//                .add(userModel)
//                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
//                    @Override
//                    public void onSuccess(DocumentReference documentReference) {
////                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
//                        progressDialog.dismiss();
//                        Toast.makeText(SignupActivity.this, "가입 되었습니다.", Toast.LENGTH_SHORT).show();
//                        finish();
//                        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
//                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                        startActivity(intent);
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
////                        Log.w(TAG, "Error adding document", e);
//                    }
//                });

        final AppDatabase localDB = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "db").allowMainThreadQueries().build();
        if (localDB.dbDao().getAll().size() != 0) {
            DbEntity dbEntity = new DbEntity(0);
            dbEntity.setId(editTextEmail.getText().toString().trim());
            dbEntity.setPw(editTextPassword.getText().toString().trim());
            localDB.dbDao().update(dbEntity);
        } else {
            DbEntity dbEntity = new DbEntity(0);
            dbEntity.setId(editTextEmail.getText().toString().trim());
            dbEntity.setPw(editTextPassword.getText().toString().trim());
            localDB.dbDao().insert(dbEntity);
        }


        db.collection("users")
                .document(userModel.uid)
                .set(userModel).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {


                progressDialog.dismiss();
                Toast.makeText(SignupActivity.this, "가입 되었습니다.", Toast.LENGTH_SHORT).show();
                finish();
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }
}
