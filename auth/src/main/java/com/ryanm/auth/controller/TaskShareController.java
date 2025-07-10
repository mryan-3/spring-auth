package com.ryanm.auth.controller;

import com.ryanm.auth.dto.ApiResponse;
import com.ryanm.auth.dto.tasks.TaskShareRequest;
import com.ryanm.auth.service.TaskService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks/share")
@RequiredArgsConstructor
public class TaskShareController {
    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> shareTask(@RequestBody TaskShareRequest request) {
        try {
            taskService.shareTask(request);
            return ResponseEntity.ok(ApiResponse.success("Task shared successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{taskId}/user/{username}")
    public ResponseEntity<ApiResponse<Void>> removeShare(
            @PathVariable Long taskId,
            @PathVariable String username) {
        try {
            taskService.removeShare(taskId, username);
            return ResponseEntity.ok(ApiResponse.success("Share removed successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }
}