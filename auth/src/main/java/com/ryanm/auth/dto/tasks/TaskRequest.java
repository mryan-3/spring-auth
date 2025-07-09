package com.ryanm.auth.dto.tasks;

import com.ryanm.auth.model.Task.Priority;

import lombok.Data;

@Data
public class TaskRequest {
    private String title;
    private String description;
    private Priority priority;
    private String dueDate; // ISO 8601 format
}
