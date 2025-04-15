package com.vs18.taskmanager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.vs18.taskmanager.databinding.ActivityStatisticBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class StatisticActivity extends AppCompatActivity {

    ActivityStatisticBinding binding;
    RequestQueue queue;
    private static final String TASK_COUNT_URL = "http://10.0.2.2:3000/stats/tasks-count";
    private static final String TASK_STATUS_URL = "http://10.0.2.2:3000/stats/tasks-status";
    private static final String RECENT_TASKS_URL = "http://10.0.2.2:3000/stats/recent-tasks";

    int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStatisticBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userId = getUserId();
        Log.d("StatisticActivity", "User ID: " + userId);

        binding.recyclerViewRecentTasks.setLayoutManager(new LinearLayoutManager(this));
        queue = Volley.newRequestQueue(this);
        fetchStatistics();
    }

    private int getUserId(){
        SharedPreferences preferences = getSharedPreferences("user_data", Context.MODE_PRIVATE);
        return preferences.getInt("userId",-1);
    }

    private void fetchStatistics(){
        fetchTotalTasks();
        fetchTasksStatus();
        fetchRecentTasks();
    }

    private void fetchTotalTasks(){
        SharedPreferences preferences = getSharedPreferences("user_data", Context.MODE_PRIVATE);
        String token = preferences.getString("token", null); // тут передбачається, що токен зберігається під ключем "token"

        if (token == null) {
            Toast.makeText(StatisticActivity.this, "Token is missing", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                TASK_COUNT_URL,
                null,
                response -> {
                    try {
                        // Логування відповіді для дебагу
                        Log.d("StatisticActivity", "Response: " + response.toString());

                        StringBuilder statusText = new StringBuilder();
                        JSONObject total_tasks = response;

                        // Логування структури відповіді
                        Log.d("StatisticActivity", "Total tasks: " + total_tasks.toString());

                        // Використовуємо keys() для отримання ітератора
                        Iterator<String> keys = total_tasks.keys();
                        while (keys.hasNext()) {
                            String key = keys.next();
                            statusText.append(key)
                                    .append(": ")
                                    .append(total_tasks.getInt(key));
                        }

                        // Оновлення TextView для відображення статистики
                        binding.tvTotal.setText(statusText.toString());

                    } catch (JSONException e) {
                        Log.e("StatisticActivity", "Error: " + e.getMessage());
                        Toast.makeText(StatisticActivity.this, "Error fetching task status", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("StatisticActivity", "Error fetching task status: " + error.getMessage());
                    Toast.makeText(StatisticActivity.this, "Error fetching task status", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token); // Додавання токену в заголовок
                return headers;
            }
        };

        queue.add(jsonObjectRequest);
    }

    private void fetchTasksStatus() {
        // Отримання токена з SharedPreferences
        SharedPreferences preferences = getSharedPreferences("user_data", Context.MODE_PRIVATE);
        String token = preferences.getString("token", null); // тут передбачається, що токен зберігається під ключем "token"

        if (token == null) {
            Toast.makeText(StatisticActivity.this, "Token is missing", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                TASK_STATUS_URL,
                null,
                response -> {
                    try {
                        // Логування відповіді для дебагу
                        Log.d("StatisticActivity", "Response: " + response.toString());

                        StringBuilder statusText = new StringBuilder();
                        JSONObject statuses = response;

                        // Логування структури відповіді
                        Log.d("StatisticActivity", "Statuses: " + statuses.toString());

                        // Використовуємо keys() для отримання ітератора
                        Iterator<String> keys = statuses.keys();
                        while (keys.hasNext()) {
                            String key = keys.next();
                            statusText.append(key)
                                    .append(": ")
                                    .append(statuses.getInt(key))
                                    .append(" tasks\n\n");
                        }

                        // Оновлення TextView для відображення статистики
                        binding.tvTaskStatus.setText(statusText.toString());

                    } catch (JSONException e) {
                        Log.e("StatisticActivity", "Error: " + e.getMessage());
                        Toast.makeText(StatisticActivity.this, "Error fetching task status", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("StatisticActivity", "Error fetching task status: " + error.getMessage());
                    Toast.makeText(StatisticActivity.this, "Error fetching task status", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token); // Додавання токену в заголовок
                return headers;
            }
        };

        queue.add(jsonObjectRequest);
    }

    private void fetchRecentTasks() {
        SharedPreferences preferences = getSharedPreferences("user_data", Context.MODE_PRIVATE);
        String token = preferences.getString("token", null); // Отримання токена

        if (token == null) {
            Toast.makeText(StatisticActivity.this, "Token is missing", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                RECENT_TASKS_URL,
                null,
                response -> {
                    try {
                        List<String> tasks = new ArrayList<>();
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject task = response.getJSONObject(i);
                            String title = task.getString("title");
                            String status = task.getString("status");
                            tasks.add(title + " (" + status + ")");
                        }

                        // Встановлюємо адаптер для RecyclerView
                        StatisticAdapter adapter = new StatisticAdapter(tasks);
                        binding.recyclerViewRecentTasks.setAdapter(adapter);
                    } catch (JSONException e) {
                        Log.e("StatisticActivity", "Error parsing recent tasks: " + e.getMessage());
                        Toast.makeText(StatisticActivity.this, "Error fetching recent tasks", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(StatisticActivity.this, "Error fetching recent tasks", Toast.LENGTH_SHORT).show();
                    Log.e("StatisticActivity", "Error: " + error.getMessage());
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        queue.add(jsonArrayRequest);
    }






}