package com.ryanm.auth.dto.tasks;

import com.ryanm.auth.model.Task.Priority;
import lombok.Data;

@Data
public class TaskFilterRequest {
    private String title;           
    private String description;     
    private Boolean completed;      
    private Priority priority;      
    private String dueBefore;       
    private String dueAfter;        
    private String search;          
    
    // Pagination parameters
    private Integer page = 0;       
    private Integer size = 10;     
    private String sortBy = "createdAt";  
    private String sortDirection = "desc";
}