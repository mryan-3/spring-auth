package com.ryanm.auth.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import com.ryanm.auth.model.TaskShare.SharePermission;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)   
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Boolean completed = false;

    @Enumerated(EnumType.STRING)
    private Priority priority = Priority.MEDIUM;

    private LocalDateTime dueDate;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserModel user;
    
    public enum Priority {
        LOW, MEDIUM, HIGH
    }

    @Column(name = "is_shareable")
    private Boolean isShareable = false; 

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TaskShare> shares = new HashSet<>();

    public boolean hasAccess(UserModel user) {
        // Owner always has access
        if (this.user.equals(user)) {
            return true;
        }
        
        // Check if task is shared with user
        return shares.stream()
            .anyMatch(share -> share.getSharedWith().equals(user));
    }

    public SharePermission  getPermissionForUser(UserModel user) {
        // Owner has SHARE permission (highest)
        if (this.user.equals(user)) {
            return SharePermission.MANAGE;
        }
        
        // Find user's permission in shares
        return shares.stream()
            .filter(share -> share.getSharedWith().equals(user))
            .map(TaskShare::getPermission)
            .findFirst()
            .orElse(null);
    }
    
}
