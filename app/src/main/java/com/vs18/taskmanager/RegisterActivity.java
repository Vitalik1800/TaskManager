package com.vs18.taskmanager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.vs18.taskmanager.databinding.ActivityRegisterBinding;
import org.json.JSONException;
import org.json.JSONObject;

public class RegisterActivity extends AppCompatActivity {

    ActivityRegisterBinding binding;
    String URL = "http://10.0.2.2:3000/register";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.registerButton.setOnClickListener(v -> registerUser());
        binding.registerTextView.setOnClickListener(v -> startActivity(new Intent(this, LoginActivity.class)));
    }

    private void registerUser(){
        String userName = binding.name.getText().toString();
        String userEmail = binding.email.getText().toString();
        String userPassword = binding.password.getText().toString();

        if(userName.isEmpty() || userEmail.isEmpty() || userPassword.isEmpty()){
            Toast.makeText(this, "Будь ласка заповніть всі поля!", Toast.LENGTH_LONG).show();
            return;
        }

        JSONObject postData = new JSONObject();
        try{
            postData.put("name", userName);
            postData.put("email", userEmail);
            postData.put("password", userPassword);
        } catch (JSONException e){
            Log.e("RegisterActivity", "Error: " + e.getMessage());
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL, postData,
                response -> {
                    Log.d("RegisterActivity", "Відповідь сервера: " + response.toString());
                    Toast.makeText(this, "Реєстрація успішна!", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(this, LoginActivity.class));
                },
                error -> {
                    Log.e("RegisterError", "Помилка: " + error.getMessage());
                    Toast.makeText(this, "Реєстрація неуспішна!", Toast.LENGTH_LONG).show();
                });

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }
}