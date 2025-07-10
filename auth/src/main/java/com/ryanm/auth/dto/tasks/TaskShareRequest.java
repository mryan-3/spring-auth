package com.ryanm.auth.dto.tasks;

import com.ryanm.auth.model.TaskShare;

import lombok.Data;

@Data
public class TaskShareRequest {
    private Long taskId;
    private String username;  // Username to share with
    private TaskShare.SharePermission permission;  // Permission level for the shared user
}
