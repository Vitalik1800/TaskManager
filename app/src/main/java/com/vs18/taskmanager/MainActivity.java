package com.vs18.taskmanager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.vs18.taskmanager.databinding.ActivityMainBinding;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    TaskAdapter adapter;
    List<Task> taskList;
    RequestQueue queue;
    private static final String TASKS_URL = "http://10.0.2.2:3000/tasks";
    int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userId = getUserId();
        Log.d("MainActivity", "UserId: " + userId);

        binding.recyclerViewTasks.setLayoutManager(new LinearLayoutManager(this));

        taskList = new ArrayList<>();
        adapter = new TaskAdapter(this, taskList, this::showOptionsDialog);
        binding.recyclerViewTasks.setAdapter(adapter);

        queue = Volley.newRequestQueue(this);
        fetchTasks();

        binding.btnAddTask.setOnClickListener(v -> {
            if(userId != -1){
                Intent intent = new Intent(this, AddTaskActivity.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
            } else{
                Log.e("MainActivity", "Invalid userId!");
            }
        });

        binding.btnTaskLogs.setOnClickListener(v -> {
            if(userId != -1){
                Intent intent = new Intent(this, TaskLogsActivity.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
            } else{
                Log.e("MainActivity", "Invalid userId!");
            }
        });

        binding.btnTaskStatistics.setOnClickListener(v -> {
            if(userId != -1){
                Intent intent = new Intent(this, StatisticActivity.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
            } else{
                Log.e("MainActivity", "Invalid userId!");
            }
        });

        binding.btnLogout.setOnClickListener(v -> logout());
    }

    private void logout(){
        SharedPreferences preferences = getSharedPreferences("user_data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();

        Toast.makeText(this, "Ви вийшли з облікового запису", Toast.LENGTH_LONG).show();

        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private int getUserId(){
        SharedPreferences preferences = getSharedPreferences("user_data", Context.MODE_PRIVATE);
        return preferences.getInt("userId",-1);
    }

    private void showOptionsDialog(Task task){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Опції")
                .setMessage("Оберіть дію для: " + task.getTitle())
                .setPositiveButton("Редагувати", (dialog, which) -> editTask(task))
                .setNegativeButton("Видалити", (dialog, which) -> deleteTask(task))
                .setNeutralButton("Скасувати", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void editTask(Task task){
        Intent intent = new Intent(this, EditTaskActivity.class);
        intent.putExtra("taskId", task.getTaskId());
        intent.putExtra("title", task.getTitle());
        intent.putExtra("description", task.getDescription());
        intent.putExtra("status", task.getStatus());
        intent.putExtra("deadline", task.getDeadline());
        intent.putExtra("attachments", task.getAttachments());
        startActivity(intent);
    }

    private void deleteTask(Task task) {
        String url = "http://10.0.2.2:3000/tasks/" + task.getTaskId();

        SharedPreferences preferences = getSharedPreferences("user_data", MODE_PRIVATE);
        String token = preferences.getString("token", null);
        Log.d("EditTaskActivity", "Токен: " + token);
        if(token == null){
            Log.e("EditTaskActivity", "Токен не знайдено!");
            return;
        }

        JsonObjectRequest deleteRequest = new JsonObjectRequest(
                Request.Method.DELETE,
                url,
                null,
                response -> {
                    Toast.makeText(MainActivity.this, "Завдання успішно видалено", Toast.LENGTH_SHORT).show();
                    fetchTasks();
                },
                error -> {
                    Log.e("MainActivity", "Помилка при видаленні завдання: " + error.getMessage());
                    Toast.makeText(MainActivity.this, "Не вдалося видалити завдання!", Toast.LENGTH_SHORT).show();
                }
        ){
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        queue.add(deleteRequest);
    }

    public void fetchTasks() {
        String url = TASKS_URL + "?user_id=" + userId;

        @SuppressLint("NotifyDataSetChanged") JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                        taskList.clear();
                        Date now = new Date();

                        for (int i = 0; i < response.length(); i++) {
                            JSONObject taskObject = response.getJSONObject(i);
                            int id = taskObject.getInt("id");
                            String title = taskObject.getString("title");
                            String description = taskObject.getString("description");
                            String status = taskObject.getString("status");
                            String deadline = taskObject.getString("deadline");
                            String attachments = taskObject.getString("attachments");

                            Date deadlineDate = null;
                            try {
                                deadlineDate = dateFormat.parse(deadline);
                            } catch (ParseException e) {
                                Log.e("MainActivity", "Error parsing timestamp: " + e.getMessage());
                            }

                            if (deadlineDate != null && now.after(deadlineDate) && !status.equals("completed") && !status.equals("new") && !status.equals("in_progress")) {
                                status = "overdue";
                                updateTaskStatus(id, status);
                            }

                            String formattedDate = "";
                            if (deadlineDate != null) {
                                @SuppressLint("SimpleDateFormat") SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
                                outputFormat.setTimeZone(TimeZone.getDefault());
                                formattedDate = outputFormat.format(deadlineDate);
                            }

                            Task task = new Task(id, title, description, status, formattedDate, attachments);
                            taskList.add(task);

                            if ("overdue".equals(status)) {
                                TaskReminderScheduler.scheduleTaskReminder(this, task);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        Log.e("MainActivity", "JSON Error: " + e.getMessage());
                    }
                },
                error -> {
                    Log.e("MainActivity", "Volley Error: " + (error.getMessage() != null ? error.getMessage() : "Помилка сервера"));
                    Toast.makeText(this, "Помилка отримання даних!", Toast.LENGTH_LONG).show();
                }
        );

        queue.add(jsonArrayRequest);
    }


    private void updateTaskStatus(int taskId, String newStatus) {
        String url = "http://10.0.2.2:3000/tasks/" + taskId + "/status";

        SharedPreferences preferences = getSharedPreferences("user_data", MODE_PRIVATE);
        String token = preferences.getString("token", null);

        if (token == null) {
            Log.e("MainActivity", "Токен не знайдено!");
            return;
        }

        Map<String, String> params = new HashMap<>();
        params.put("status", newStatus);

        JSONObject jsonBody = new JSONObject(params);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                jsonBody,
                response -> Log.d("MainActivity", "Статус оновлено: " + newStatus),
                error -> Log.e("MainActivity", "Помилка оновлення статусу: " + error.getMessage())
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

    @Override
    protected void onResume() {
        super.onResume();
        fetchTasks();
    }
}