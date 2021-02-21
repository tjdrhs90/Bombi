package com.Gonigon.bombi;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

//        MobileAds.initialize(this, getString(R.string.admob_app_id));

        //상단 상태표시줄 없애기
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        firebaseAuth = FirebaseAuth.getInstance();
//        firebaseAuth.signOut();

        // gif 사용을 위해 외부라이브러리 이용
        ImageView logoImg = (ImageView) findViewById(R.id.activity_splash_imageView);
        Glide.with(this).load(R.drawable.gif).into(logoImg);

        Handler handler = new Handler();
        handler.postDelayed(new splashHander(), 2000);


    }

    class splashHander implements Runnable {
        public void run() {

//            if(firebaseAuth.getCurrentUser() != null){ //이미 로그인 되었다면
//                intent = new Intent(getApplicationContext(), MainActivity.class);
//            } else {
//                intent = new Intent(getApplicationContext(), LoginActivity.class);
//            }

            intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            SplashActivity.this.finish();
        }
    }
}
