package com.vs18.taskmanager;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vs18.taskmanager.databinding.ItemTaskLogBinding;

import java.util.List;

public class TaskLogAdapter extends RecyclerView.Adapter<TaskLogAdapter.TaskLogViewHolder>{

    private final List<TaskLog> taskLogList;

    public TaskLogAdapter(List<TaskLog> taskLogList){
        this.taskLogList = taskLogList;
    }

    @NonNull
    @Override
    public TaskLogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTaskLogBinding binding = ItemTaskLogBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new TaskLogViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskLogViewHolder holder, int position) {
        TaskLog taskLog = taskLogList.get(position);
        holder.binding.tvTitle.setText(taskLog.getTitle());
        holder.binding.tvAction.setText(taskLog.getAction());
        holder.binding.tvTimestamp.setText(taskLog.getTimestamp());
    }

    @Override
    public int getItemCount() {
        return taskLogList.size();
    }

    public static class TaskLogViewHolder extends RecyclerView.ViewHolder{
        ItemTaskLogBinding binding;

        public TaskLogViewHolder(ItemTaskLogBinding binding){
            super(binding.getRoot());
            this.binding = binding;
        }
    }

}
