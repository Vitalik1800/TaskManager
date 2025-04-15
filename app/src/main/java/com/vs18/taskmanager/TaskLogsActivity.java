package com.vs18.taskmanager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.vs18.taskmanager.databinding.ActivityTaskLogsBinding;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TaskLogsActivity extends AppCompatActivity {

    ActivityTaskLogsBinding binding;
    TaskLogAdapter adapter;
    List<TaskLog> taskLogList;
    RequestQueue queue;
    private static final String TASK_LOG_URL = "http://10.0.2.2:3000/task_logs";
    int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTaskLogsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userId = getUserId();
        Log.d("TaskLogsActivity", "UserId: " + userId);

        binding.recyclerViewTaskLogs.setLayoutManager(new LinearLayoutManager(this));

        taskLogList = new ArrayList<>();
        adapter = new TaskLogAdapter(taskLogList);
        binding.recyclerViewTaskLogs.setAdapter(adapter);

        queue = Volley.newRequestQueue(this);
        fetchTaskLogs();
    }

    private int getUserId(){
        SharedPreferences preferences = getSharedPreferences("user_data", Context.MODE_PRIVATE);
        return preferences.getInt("userId",-1);
    }

    public void fetchTaskLogs(){
        String url = TASK_LOG_URL + "?user_id=" + userId;

        @SuppressLint("NotifyDataSetChanged") JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try{
                        taskLogList.clear();
                        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                        for(int i = 0; i < response.length(); i++){
                            JSONObject taskLogObject = response.getJSONObject(i);
                            int id = taskLogObject.getInt("id");
                            String title = taskLogObject.getString("title");
                            String action = taskLogObject.getString("action");
                            String timestamp = taskLogObject.getString("timestamp");

                            Date date = null;
                            try {
                                date = dateFormat.parse(timestamp);
                            } catch (ParseException e) {
                                Log.e("UsersActivity", "Error parsing timestamp: " + e.getMessage());
                            }

                            String formattedDate = "";
                            if (date != null) {
                                @SuppressLint("SimpleDateFormat") SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // Desired output format
                                formattedDate = outputFormat.format(date);
                            }

                            taskLogList.add(new TaskLog(id, title, action, formattedDate));
                        }
                        adapter.notifyDataSetChanged();
                    }catch (JSONException e){
                        Log.e("TaskLogsActivity", "JSON Error: " + e.getMessage());
                    }
                },
                error -> {
                    Log.e("TaskLogsActivity", "Volley Error: " + (error.getMessage() != null ? error.getMessage() : "Помилка сервера"));
                    Toast.makeText(this, "Помилка отримання даних!", Toast.LENGTH_LONG).show();
                }
        );

        queue.add(jsonArrayRequest);
    }

}