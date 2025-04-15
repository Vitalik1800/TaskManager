package com.vs18.taskmanager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StatisticAdapter extends RecyclerView.Adapter<StatisticAdapter.StatisticViewHolder> {

    private List<String> tasks;

    public StatisticAdapter(List<String> tasks) {
        this.tasks = tasks;
    }

    @Override
    public StatisticViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Інфлюємо власну XML-розмітку
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task_statistic, parent, false);
        return new StatisticViewHolder(view);
    }

    @Override
    public void onBindViewHolder(StatisticViewHolder holder, int position) {
        // Встановлюємо дані для кожного елемента
        holder.textView.setText(tasks.get(position));
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public static class StatisticViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public StatisticViewHolder(View itemView) {
            super(itemView);
            // Прив'язуємо TextView з елемента
            textView = itemView.findViewById(R.id.taskTextView);
        }
    }
}
