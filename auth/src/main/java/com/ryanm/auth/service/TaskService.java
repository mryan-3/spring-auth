package com.ryanm.auth.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.ryanm.auth.dto.tasks.TaskFilterRequest;
import com.ryanm.auth.dto.tasks.TaskPageResponse;
import com.ryanm.auth.dto.tasks.TaskRequest;
import com.ryanm.auth.dto.tasks.TaskResponse;
import com.ryanm.auth.dto.tasks.TaskShareRequest;
import com.ryanm.auth.model.Task;
import com.ryanm.auth.model.TaskShare;
import com.ryanm.auth.model.UserModel;
import com.ryanm.auth.model.Task.Priority;
import com.ryanm.auth.model.TaskShare.SharePermission;
import com.ryanm.auth.repository.TaskRepository;
import com.ryanm.auth.repository.TaskShareRepository;
import com.ryanm.auth.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TaskShareRepository taskShareRepository;

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

    public TaskPageResponse getTasksWithFilters(TaskFilterRequest req) {
        UserModel user = getCurrentUser();
         Pageable pageable = createPageable(req);
        
        LocalDateTime dueBefore = parseDateTime(req.getDueBefore());
        LocalDateTime dueAfter = parseDateTime(req.getDueAfter());
        
        Page<Task> taskPage = taskRepository.findTasksWithFilters(
            user,
            req.getTitle(),
            req.getDescription(),
            req.getCompleted(),
            req.getPriority(),
            dueBefore,
            dueAfter,
            pageable
        );

        List<TaskResponse> taskResponses = taskPage.getContent().stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());

            return new TaskPageResponse(
                taskResponses,
            taskPage.getNumber(),           // Current page
            taskPage.getSize(),             // Page size
            taskPage.getTotalElements(),    // Total items
            taskPage.getTotalPages()
            );
    }



    private Pageable createPageable(TaskFilterRequest filterRequest) {
        Sort sort = Sort.by(
            Sort.Direction.fromString(filterRequest.getSortDirection()),
            filterRequest.getSortBy()
        );
        
        return PageRequest.of(
            filterRequest.getPage(), 
            filterRequest.getSize(), 
            sort
        );
    }
    private LocalDateTime parseDateTime(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateString);
        } catch (Exception e) {
            throw new RuntimeException("Invalid date format: " + dateString + ". Use ISO format: 2024-07-15T18:00:00");
        }
    }

   public TaskShare shareTask(Long taskId, String username, SharePermission permission) {
        UserModel currentUser = getCurrentUser();
        UserModel targetUser = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));

        Task task = taskRepository.findByIdAndUser(taskId, currentUser)
            .orElseThrow(() -> new RuntimeException("Task not found or access denied"));

        // Make task shareable if not already
        if (!task.getIsShareable()) {
            task.setIsShareable(true);
            taskRepository.save(task);
        }
        // Create it in the TaskShareRepository

        TaskShare share = new TaskShare();
        share.setTask(task);
        share.setSharedWith(targetUser);
        share.setPermission(permission);
        taskShareRepository.save(share);

        return share;
   }

    public void removeShare(Long taskId, String username) {
        UserModel currentUser = getCurrentUser();
        UserModel targetUser = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));

        taskRepository.findByIdAndUser(taskId, currentUser)
            .orElseThrow(() -> new RuntimeException("Task not found or access denied"));

        taskShareRepository.deleteByTaskIdAndSharedWith(taskId, targetUser);
    }

    public List<TaskResponse> getSharedTasks() {
        UserModel user = getCurrentUser();
        
        List<TaskShare> shares = taskShareRepository.findBySharedWith(user);
        // Check if user has any shared tasks
        if (shares.isEmpty()) {
            return List.of(); // Return empty list if no shared tasks

        }
        
        return shares.stream()
            .map(share -> convertToResponse(share.getTask()))
            .collect(Collectors.toList());
    }

    public TaskResponse editTask(Long taskId, TaskRequest request) {
        UserModel user = getCurrentUser();
        
        Task task = taskRepository.findByIdAndUser(taskId, user)
            .orElseThrow(() -> new RuntimeException("Task not found or access denied"));

        // Validate edit access
        if (!hasEditAccess(task, user)) {
            throw new RuntimeException("Access denied: You don't have permission to edit this task");
        }

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

    private boolean hasEditAccess(Task task, UserModel user) {
        // Owner has edit access
        if (task.getUser().equals(user)) {
            return true;
        }

        // Check if user has EDIT or MANAGE permission
        return task.getShares().stream()
            .filter(share -> share.getSharedWith().equals(user))
            .anyMatch(share -> 
                share.getPermission() == SharePermission.EDIT || 
                share.getPermission() == SharePermission.MANAGE);
    }
}