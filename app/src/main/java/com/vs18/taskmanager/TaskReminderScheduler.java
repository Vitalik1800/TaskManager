package com.vs18.taskmanager;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TaskReminderScheduler {

    @SuppressLint("ScheduleExactAlarm")
    public static void scheduleTaskReminder(Context context, Task task){
        try{
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date deadLineDate = sdf.parse(task.getDeadline());

            if(deadLineDate == null) return;

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(deadLineDate);
            calendar.add(Calendar.MINUTE,-30);

            Intent intent = new Intent(context, TaskReminderReceiver.class);
            intent.putExtra("taskTitle", task.getTitle());

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context, task.getTaskId(), intent, PendingIntent.FLAG_UPDATE_CURRENT
            );

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if(alarmManager != null){
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            }

        }catch (Exception e){
            Log.e("TaskReminderScheduler", "Error: " + e.getMessage());
        }
    }

}
