package com.vs18.taskmanager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.vs18.taskmanager.databinding.ActivityAddTaskBinding;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class AddTaskActivity extends AppCompatActivity {

    private static final int PICK_FILE_REQUEST = 1;
    private String filePath = null;
    ActivityAddTaskBinding binding;
    RequestQueue queue;
    String TASK_URL = "http://10.0.2.2:3000/tasks";
    int userId;
    private GoogleCalendarHelper googleCalendarHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddTaskBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        queue = Volley.newRequestQueue(this);
        userId = getUserId();
        googleCalendarHelper = new GoogleCalendarHelper(this);
        googleCalendarHelper.signInIfNeeded();

        Log.d("AddTaskActivity", "User ID: " + userId);
        binding.btnSelectFile.setOnClickListener(v -> openFilePicker());
        binding.btnAddTask.setOnClickListener(v -> saveTask());
    }

    private void openFilePicker(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, PICK_FILE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK && requestCode == PICK_FILE_REQUEST){
            assert data != null;
            Uri selectedFileUri = data.getData();
            if(selectedFileUri != null){
                filePath = getFilePath(selectedFileUri);
                binding.etFilePath.setText(filePath);
                Log.d("AddTaskActivity", "Selected file path: " + filePath);
            }
        }
    }

    private String getFilePath(Uri uri){
        String path = null;
        String [] projection = {OpenableColumns.DISPLAY_NAME};
        try(Cursor cursor = getContentResolver().query(uri, projection, null, null, null)){
            if(cursor != null && cursor.moveToFirst()){
                int columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                path = cursor.getString(columnIndex);
            }
        } catch (Exception e){
            Log.e("AddTaskActivity", "Error getting file path: " + e.getMessage());
        }
        return path;
    }

    private int getUserId() {
        SharedPreferences preferences = getSharedPreferences("user_data", Context.MODE_PRIVATE);
        return preferences.getInt("userId", -1);
    }

    private void saveTask() {
        JSONObject taskData = getTaskDataFromFields();
        if (taskData == null) {
            Log.e("AddTaskActivity", "❌ Failed to create JSON for request!");
            return;
        }

        SharedPreferences preferences = getSharedPreferences("user_data", MODE_PRIVATE);
        String token = preferences.getString("token", null);
        Log.d("AddTaskActivity", "Token: " + token);
        if (token == null) {
            Log.e("AddTaskActivity", "Token not found!");
            return;
        }

        Log.d("AddTaskActivity", "Sending data: " + taskData);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST, TASK_URL, taskData,
                response -> {
                    Log.d("AddTaskActivity", "✅ Task added successfully: " + response.toString());
                    try {
                        String title = taskData.getString("title");
                        String description = taskData.getString("description");
                        String deadline = taskData.getString("deadline");

                        String deadlineFormatted = formatDateToRFC3339(deadline);
                        if (deadlineFormatted != null) {
                            googleCalendarHelper.addTaskToCalendar(title, description, deadlineFormatted);
                        } else {
                            Log.e("AddTaskActivity", "❌ Invalid date format!");
                        }
                    } catch (JSONException e) {
                        Log.e("AddTaskActivity", "❌ Error retrieving data: " + e.getMessage());
                    }
                    finish();
                },
                error -> {
                    if (error.networkResponse != null) {
                        Log.e("AddTaskActivity", "❌ Error: " + new String(error.networkResponse.data));
                    } else {
                        Log.e("AddTaskActivity", "❌ Error adding task: " + error.getMessage());
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        queue.add(request);
    }

    private JSONObject getTaskDataFromFields() {
        String title = binding.etTitle.getText().toString();
        String description = binding.etDescription.getText().toString();
        String deadline = binding.etDeadline.getText().toString();

        if (title.isEmpty() || description.isEmpty() || deadline.isEmpty() || userId == -1) {
            Log.e("AddTaskActivity", "❌ Incorrect data: title='" + title + "', description='" + description + "', deadline='" + deadline + "', userId=" + userId);
            return null;
        }

        try {
            JSONObject taskObject = new JSONObject();
            taskObject.put("title", title);
            taskObject.put("description", description);
            taskObject.put("deadline", deadline);
            taskObject.put("assigned_to", userId);

            if(filePath != null){
               taskObject.put("attachments", filePath);
            }

            return taskObject;
        } catch (JSONException e) {
            Log.e("AddTaskActivity", "❌ Error creating JSON: " + e.getMessage());
            return null;
        }
    }

    private String formatDateToRFC3339(String inputDate) {
        try {
            @SuppressLint("SimpleDateFormat") SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date date = inputFormat.parse(inputDate);

            @SuppressLint("SimpleDateFormat") SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            outputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            return outputFormat.format(date);
        } catch (Exception e) {
            Log.e("AddTaskActivity", "❌ Error formatting date: " + e.getMessage());
            return null;
        }
    }
}
