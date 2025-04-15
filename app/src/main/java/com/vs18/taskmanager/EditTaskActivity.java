package com.vs18.taskmanager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.vs18.taskmanager.databinding.ActivityEditTaskBinding;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

public class EditTaskActivity extends AppCompatActivity {

    ActivityEditTaskBinding binding;
    int taskId;
    private static final String UPDATE_TASK_URL = "http://10.0.2.2:3000/tasks/";
    private static final int PICK_FILE_REQUEST = 1;
    private String filePath = null;
    private GoogleCalendarHelper googleCalendarHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditTaskBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        googleCalendarHelper = new GoogleCalendarHelper(this);
        googleCalendarHelper.signInIfNeeded();

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.status_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerStatus.setAdapter(adapter);

        Intent intent = getIntent();
        taskId = intent.getIntExtra("taskId", -1);
        binding.etTitle.setText(intent.getStringExtra("title"));
        binding.etDescription.setText(intent.getStringExtra("description"));
        String currentStatus = intent.getStringExtra("status");

        if (currentStatus != null) {
            int position = adapter.getPosition(currentStatus);
            binding.spinnerStatus.setSelection(position);
        }

        binding.etDeadline.setText(intent.getStringExtra("deadline"));
        binding.etFilePath.setText(intent.getStringExtra("attachments"));

        binding.btnSelectFile.setOnClickListener(v -> openFilePicker());
        binding.btnEditBook.setOnClickListener(v -> {
            try {
                updateTask();
            } catch (ParseException e) {
                Log.e("EditTaskActivity", "Error: " + e.getMessage());
            }
        });
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, PICK_FILE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == PICK_FILE_REQUEST) {
            assert data != null;
            Uri selectedFileUri = data.getData();
            if (selectedFileUri != null) {
                filePath = getFilePath(selectedFileUri);
                binding.etFilePath.setText(filePath);
                Log.d("EditTaskActivity", "Selected file path: " + filePath);
            }
        }
    }

    private String getFilePath(Uri uri) {
        String path = null;
        String[] projection = {OpenableColumns.DISPLAY_NAME};
        try (Cursor cursor = getContentResolver().query(uri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                path = cursor.getString(columnIndex);
            }
        } catch (Exception e) {
            Log.e("EditTaskActivity", "Error getting file path: " + e.getMessage());
        }
        return path;
    }

    private void updateTask() throws ParseException {
        String title = binding.etTitle.getText().toString();
        String description = binding.etDescription.getText().toString();
        String status = binding.spinnerStatus.getSelectedItem().toString();
        String deadline = binding.etDeadline.getText().toString();

        if (title.isEmpty() || description.isEmpty() || status.isEmpty() || deadline.isEmpty()) {
            Toast.makeText(this, "Заповніть всі поля!", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences preferences = getSharedPreferences("user_data", MODE_PRIVATE);
        String token = preferences.getString("token", null);
        Log.d("EditTaskActivity", "Токен: " + token);
        if (token == null) {
            Log.e("EditTaskActivity", "Токен не знайдено!");
            return;
        }

        try {
            String deadlineFormatted = formatDateToRFC3339(deadline);
            if (deadlineFormatted == null) {
                Log.e("EditTaskActivity", "❌ Invalid date format!");
                Toast.makeText(this, "Некоректний формат дати!", Toast.LENGTH_SHORT).show();
                return;
            }

            JSONObject taskData = new JSONObject();
            taskData.put("title", title);
            taskData.put("description", description);
            taskData.put("status", status);
            taskData.put("deadline", deadlineFormatted);

            if (filePath != null) {
                taskData.put("attachments", filePath);
            }

            @SuppressLint("SimpleDateFormat") JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, UPDATE_TASK_URL + taskId, taskData,
                    response -> {
                        Log.d("EditTaskActivity", "Server response: " + response.toString());
                        Toast.makeText(EditTaskActivity.this, "Завдання оновлено!", Toast.LENGTH_SHORT).show();

                        String newEndDateTime = null;
                        try {
                            newEndDateTime = formatDateToRFC3339(
                                    new SimpleDateFormat("yyyy-MM-dd").format(
                                            Objects.requireNonNull(new SimpleDateFormat("yyyy-MM-dd").parse(deadline)).getTime() + 3600000
                                    )
                            );
                        } catch (ParseException e) {
                            Log.e("EditTaskActivity", "Error: " + e.getMessage());
                        }

                        googleCalendarHelper.updateEventFromSavedId(deadlineFormatted, newEndDateTime);
                        finish();
                    },
                    error -> {
                        Log.e("EditTaskActivity", "Помилка оновлення: " + error.toString());
                        if (error.networkResponse != null) {
                            Log.e("EditTaskActivity", "Response code: " + error.networkResponse.statusCode);
                            Log.e("EditTaskActivity", "Response data: " + new String(error.networkResponse.data));
                        }
                        Toast.makeText(EditTaskActivity.this, "Помилка оновлення!", Toast.LENGTH_SHORT).show();
                    }) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + token);
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };

            RequestQueue queue = Volley.newRequestQueue(this);
            queue.add(request);

        } catch (JSONException e) {
            Log.e("EditTaskActivity", "❌ Помилка оновлення: " + e.getMessage());
            Toast.makeText(this, "Помилка обробки даних!", Toast.LENGTH_SHORT).show();
        }
    }

    private String formatDateToRFC3339(String inputDate) {
        try {
            @SuppressLint("SimpleDateFormat") SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date date = inputFormat.parse(inputDate);

            @SuppressLint({"SimpleDateFormat", "NewApi", "LocalSuppress"}) SimpleDateFormat outputFormat =
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
            outputFormat.setTimeZone(TimeZone.getDefault());
            return outputFormat.format(date);
        } catch (Exception e) {
            Log.e("EditTaskActivity", "❌ Error formatting date: " + e.getMessage());
            return null;
        }
    }
}