package com.ryanm.auth.dto.tasks;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class TaskPageResponse {
    private List<TaskResponse> tasks;    // Task data
    private int currentPage;             // Current page number (0-based)
    private int pageSize;                // Number of items per page
    private long totalElements;          // Total number of tasks
    private int totalPages;              // Total number of pages
    private boolean first;               // Is this the first page?
    private boolean last;                // Is this the last page?
}