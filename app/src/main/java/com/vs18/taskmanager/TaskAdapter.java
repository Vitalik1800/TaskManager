package com.vs18.taskmanager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.vs18.taskmanager.databinding.ItemTaskBinding;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder>{

    private final List<Task> taskList;
    Context context;
    private final OnTaskItemClickListener listener;

    public TaskAdapter(Context context, List<Task> taskList, OnTaskItemClickListener listener){
        this.context = context;
        this.taskList = taskList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTaskBinding binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new TaskViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.binding.tvTitle.setText(task.getTitle());
        holder.binding.tvDescription.setText(task.getDescription());
        holder.binding.tvStatus.setText(task.getStatus());
        holder.binding.tvDeadline.setText(task.getDeadline());
        holder.binding.tvAttachments.setText(task.getAttachments());

        holder.itemView.setOnClickListener(v -> {
            if(listener != null){
                listener.onTaskClick(task);
            }
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder{
        final ItemTaskBinding binding;

        public TaskViewHolder(ItemTaskBinding binding){
            super(binding.getRoot());
            this.binding = binding;
        }
    }

}
