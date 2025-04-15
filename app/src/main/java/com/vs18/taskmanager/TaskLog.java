package com.vs18.taskmanager;

public class TaskLog {

    private final int id;
    private final String title, action, timestamp;

    public TaskLog(int id, String title, String action, String timestamp) {
        this.id = id;
        this.title = title;
        this.action = action;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAction() {
        return action;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
