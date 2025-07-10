package com.ryanm.auth.dto.tasks;

import java.time.LocalDateTime;

import com.ryanm.auth.model.Task.Priority;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TaskResponse {
    private Long id;
    private String title;
    private String description;
    private Boolean completed;
    private Priority priority;
    private LocalDateTime dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
