package com.vs18.taskmanager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import com.vs18.taskmanager.databinding.ActivitySplashBinding;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    ActivitySplashBinding binding;
    SharedPreferences preferences;
    String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferences = getSharedPreferences("user_data", MODE_PRIVATE);
        token = preferences.getString("token", null);

        new Handler().postDelayed(() -> {
            if(token != null){
                startActivity(new Intent(this, MainActivity.class));
            } else{
                startActivity(new Intent(this, LoginActivity.class));
            }
            finish();
        },2000);
    }
}