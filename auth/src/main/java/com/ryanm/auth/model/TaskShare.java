package com.ryanm.auth.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "task_shares")
@Getter @Setter
@NoArgsConstructor
public class TaskShare {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shared_with", nullable = false)
    private UserModel sharedWith;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SharePermission permission;

    @Column(nullable = false)
    private LocalDateTime sharedAt;

    @PrePersist
    protected void onCreate() {
        sharedAt = LocalDateTime.now();
    }

    public enum SharePermission {
        VIEW,    // Can only view the task
        EDIT,    // Can edit task details
        MANAGE   // Can edit and share with others
    }
}