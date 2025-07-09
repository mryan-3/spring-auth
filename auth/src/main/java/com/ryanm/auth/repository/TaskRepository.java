package com.ryanm.auth.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ryanm.auth.model.Task;
import com.ryanm.auth.model.UserModel;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByUserOrderByCreatedAtDesc(UserModel user);
    List<Task> findByUserAndCompleted(UserModel user, boolean completed);
    List<Task> findByUserAndPriority(UserModel user, Task.Priority priority);
    List<Task> findByUserAndDueDate(UserModel user, LocalDateTime dueDate);
    Optional<Task> findByIdAndUser(Long id, UserModel user);

    Page<Task> findByUserOrderByCreatedAtDesc(UserModel user, Pageable pageable);
    Page<Task> findByUserAndCompleted(UserModel user, boolean completed, Pageable pageable);
    Page<Task> findByUserAndPriority(UserModel user, Task.Priority priority, Pageable pageable);

    Page<Task> findByUserAndTitleContainingIgnoreCase(UserModel user, String title, Pageable pageable);
    Page<Task> findByUserAndDescriptionContainingIgnoreCase(UserModel user, String description, Pageable pageable);

     Page<Task> findByUserAndDueDateBetween(UserModel user, LocalDateTime start, LocalDateTime end, Pageable pageable);
    Page<Task> findByUserAndDueDateBefore(UserModel user, LocalDateTime before, Pageable pageable);
    Page<Task> findByUserAndDueDateAfter(UserModel user, LocalDateTime after, Pageable pageable);

    long countByUser(UserModel user);
    long countByUserAndCompleted(UserModel user, boolean completed);
    @Query("SELECT t FROM Task t WHERE t.user = :user " +
           "AND (:title IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :title, '%'))) " +
           "AND (:description IS NULL OR LOWER(t.description) LIKE LOWER(CONCAT('%', :description, '%'))) " +
           "AND (:completed IS NULL OR t.completed = :completed) " +
           "AND (:priority IS NULL OR t.priority = :priority) " +
           "AND (:dueBefore IS NULL OR t.dueDate <= :dueBefore) " +
           "AND (:dueAfter IS NULL OR t.dueDate >= :dueAfter)")
    Page<Task> findTasksWithFilters(
        @Param("user") UserModel user,
        @Param("title") String title,
        @Param("description") String description,
        @Param("completed") Boolean completed,
        @Param("priority") Task.Priority priority,
        @Param("dueBefore") LocalDateTime dueBefore,
        @Param("dueAfter") LocalDateTime dueAfter,
        Pageable pageable
    );
}

