package com.vs18.taskmanager;

public class Task {

    int taskId;
    String title, description, status, deadline, attachments;

    public Task(int taskId, String title, String description, String status, String deadline, String attachments) {
        this.taskId = taskId;
        this.title = title;
        this.description = description;
        this.status = status;
        this.deadline = deadline;
        this.attachments = attachments;
    }

    public int getTaskId() {
        return taskId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public String getDeadline() {
        return deadline;
    }

    public String getAttachments() {
        return attachments;
    }
}

