package com.ryanm.auth.service;

import com.ryanm.auth.model.Task;
import com.ryanm.auth.model.TaskShare.SharePermission;
import com.ryanm.auth.model.UserModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TaskPermissionService {

    public boolean canView(Task task, UserModel user) {
        return task.hasAccess(user);
    }

    public boolean canEdit(Task task, UserModel user) {
        SharePermission permission = task.getPermissionForUser(user);
        return permission == SharePermission.EDIT || permission == SharePermission.MANAGE;
    }

    public boolean canShare(Task task, UserModel user) {
        SharePermission permission = task.getPermissionForUser(user);
        return permission == SharePermission.MANAGE;
    }

    public boolean canShareTask(Task task, UserModel user) {
        // Check if user can share AND task is shareable
        return task.getIsShareable() && canShare(task, user);
    }

    public void validateViewAccess(Task task, UserModel user) {
        if (!canView(task, user)) {
            throw new RuntimeException("Access denied: You don't have permission to view this task");
        }
    }

    public void validateEditAccess(Task task, UserModel user) {
        if (!canEdit(task, user)) {
            throw new RuntimeException("Access denied: You don't have permission to edit this task");
        }
    }

    public void validateShareAccess(Task task, UserModel user) {
        if (!canShareTask(task, user)) {
            throw new RuntimeException("Access denied: You don't have permission to share this task or task is not shareable");
        }
    }
}