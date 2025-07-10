package com.ryanm.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ryanm.auth.model.TaskShare;
import com.ryanm.auth.model.UserModel;

import java.util.List;


public interface TaskShareRepository extends JpaRepository<TaskShare, Long> {
    List<TaskShare> findBySharedWith(UserModel sharedWith);
    void deleteByTaskIdAndSharedWith(Long taskId, UserModel sharedWith);
}
