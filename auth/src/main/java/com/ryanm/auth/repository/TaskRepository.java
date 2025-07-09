package com.ryanm.auth.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ryanm.auth.model.Task;
import com.ryanm.auth.model.UserModel;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByUserOrderByCreatedAtDesc(UserModel user);
    List<Task> findByUserAndCompleted(UserModel user, boolean completed);
    List<Task> findByUserAndPriority(UserModel user, Task.Priority priority);
    List<Task> findByUserAndDueDate(UserModel user, String dueDate);
    Optional<Task> findByIdAndUser(Long id, UserModel user);
    long countByUser(UserModel user);
    long countByUserAndCompleted(UserModel user, boolean completed);
}

