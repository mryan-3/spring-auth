package com.ryanm.auth.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.ryanm.auth.dto.tasks.TaskRequest;
import com.ryanm.auth.dto.tasks.TaskResponse;
import com.ryanm.auth.model.Task;
import com.ryanm.auth.model.UserModel;
import com.ryanm.auth.model.Task.Priority;
import com.ryanm.auth.repository.TaskRepository;
import com.ryanm.auth.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    private UserModel getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }


    public TaskResponse createTask(TaskRequest request) {
        UserModel user = getCurrentUser();

        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setPriority(request.getPriority() != null ? request.getPriority() : Priority.MEDIUM);
        task.setCompleted(false);
        task.setUser(user);

        if (request.getDueDate() != null && !request.getDueDate().isEmpty()) {
           try {
            LocalDateTime dueDate = LocalDateTime.parse(request.getDueDate());
            task.setDueDate(dueDate);
           } catch (Exception e) {
            throw new RuntimeException("Invalid due date format. Please use ISO-8601 format (e.g., 2023-10-01T10:15:30)");
           }
        }

        Task savedTask = taskRepository.save(task);
        
        return convertToResponse(savedTask);

    
    }
    private TaskResponse convertToResponse(Task task) {
        return new TaskResponse(
            task.getId(),
            task.getTitle(),
            task.getDescription(),
            task.getCompleted(),
            task.getPriority(),
            task.getDueDate(),
            task.getCreatedAt(),
            task.getUpdatedAt()
        );
    }

    public List<TaskResponse> getAllTasks() {
        UserModel user = getCurrentUser();
        List<Task> tasks = taskRepository.findByUserOrderByCreatedAtDesc(user);
        return tasks.stream()
            .map(this::convertToResponse)
            .toList();
    }

    public TaskResponse getTaskById(Long taskId) {
        UserModel user = getCurrentUser();
        Task task = taskRepository.findByIdAndUser(taskId, user)
            .orElseThrow(() -> new RuntimeException("Task not found"));
        return convertToResponse(task);
    }

    public TaskResponse toggleTaskCompletion(Long taskId) {
        UserModel user = getCurrentUser();
        
        Task task = taskRepository.findByIdAndUser(taskId, user)
            .orElseThrow(() -> new RuntimeException("Task not found or access denied"));
        
        // Business logic: toggle completion
        task.setCompleted(!task.getCompleted());
        
        Task savedTask = taskRepository.save(task);
        return convertToResponse(savedTask);
    }
    public TaskResponse updateTask(Long taskId, TaskRequest request) {
        UserModel user = getCurrentUser();
        
        Task task = taskRepository.findByIdAndUser(taskId, user)
            .orElseThrow(() -> new RuntimeException("Task not found or access denied"));
        
        // Update fields
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setPriority(request.getPriority() != null ? request.getPriority() : task.getPriority());
        
        // Update due date if provided
        if (request.getDueDate() != null && !request.getDueDate().isEmpty()) {
            try {
                LocalDateTime dueDate = LocalDateTime.parse(request.getDueDate());
                task.setDueDate(dueDate);
            } catch (Exception e) {
                throw new RuntimeException("Invalid due date format. Please use ISO-8601 format (e.g., 2023-10-01T10:15:30)");
            }
        }
        
        Task savedTask = taskRepository.save(task);
        return convertToResponse(savedTask);
    }

    // 🗑️ DELETE TASK
    public void deleteTask(Long taskId) {
        UserModel user = getCurrentUser();
        
        Task task = taskRepository.findByIdAndUser(taskId, user)
            .orElseThrow(() -> new RuntimeException("Task not found or access denied"));
        
        taskRepository.delete(task);
    }
}
