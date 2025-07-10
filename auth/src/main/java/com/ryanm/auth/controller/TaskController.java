package com.ryanm.auth.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ryanm.auth.dto.ApiResponse;
import com.ryanm.auth.dto.tasks.TaskFilterRequest;
import com.ryanm.auth.dto.tasks.TaskPageResponse;
import com.ryanm.auth.dto.tasks.TaskRequest;
import com.ryanm.auth.dto.tasks.TaskResponse;
import com.ryanm.auth.model.Task.Priority;
import com.ryanm.auth.service.TaskService;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;




@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    // Create a new task
    @PostMapping
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(@RequestBody TaskRequest request) {
        try {
            TaskResponse response = taskService.createTask(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                                 .body(ApiResponse.success("Task created successfully", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("An unexpected error occurred"));
        } 
    }
    
    // Get all tasks
    @GetMapping
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getAllTasks() {
        try {
            List<TaskResponse> tasks = taskService.getAllTasks();
            return ResponseEntity.status(HttpStatus.OK)
                                 .body(ApiResponse.success("Tasks retrieved successfully", tasks));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("An unexpected error occurred"));
        }
    }
    
    @GetMapping("/{taskId}")
    public ResponseEntity<ApiResponse<TaskResponse>> getTask(@PathVariable Long taskId) {
        try {
            TaskResponse task = taskService.getTaskById(taskId);
            
            return ResponseEntity.ok(ApiResponse.success("Task retrieved successfully", task));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Task not found: " + e.getMessage()));
        }
    }

    @PatchMapping("/{taskId}/toggle")
    public ResponseEntity<ApiResponse<TaskResponse>> toggleTaskCompletion(@PathVariable Long taskId) {
        try {
            TaskResponse updatedTask = taskService.toggleTaskCompletion(taskId);
            
            return ResponseEntity.ok(ApiResponse.success("Task status updated successfully", updatedTask));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Failed to update task: " + e.getMessage()));
        }
    }
    

    @PutMapping("/{taskId}")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(
            @PathVariable Long taskId, 
            @RequestBody TaskRequest request) {
        try {
            TaskResponse updatedTask = taskService.updateTask(taskId, request);
            
            return ResponseEntity.ok(ApiResponse.success("Task updated successfully", updatedTask));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Failed to update task: " + e.getMessage()));
        }
    }
    @DeleteMapping("/{taskId}")
    public ResponseEntity<ApiResponse<String>> deleteTask(@PathVariable Long taskId) {
        try {
            taskService.deleteTask(taskId);
            
            return ResponseEntity.ok(ApiResponse.success("Task deleted successfully", null));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Failed to delete task: " + e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<TaskPageResponse>> searchTasks(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Boolean completed,
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) String dueBefore,
            @RequestParam(required = false) String dueAfter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        
        try {
            TaskFilterRequest filterRequest = new TaskFilterRequest();
            filterRequest.setTitle(title);
            filterRequest.setDescription(description);
            filterRequest.setCompleted(completed);
            filterRequest.setPriority(priority);
            filterRequest.setDueBefore(dueBefore);
            filterRequest.setDueAfter(dueAfter);
            filterRequest.setPage(page);
            filterRequest.setSize(size);
            filterRequest.setSortBy(sortBy);
            filterRequest.setSortDirection(sortDirection);
            
            TaskPageResponse result = taskService.getTasksWithFilters(filterRequest);
            
            return ResponseEntity.ok(ApiResponse.success("Tasks retrieved successfully", result));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Search failed: " + e.getMessage()));
        }
    }
    

}
