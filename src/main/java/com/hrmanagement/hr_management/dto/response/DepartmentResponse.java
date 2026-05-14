package com.hrmanagement.hr_management.dto.response;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DepartmentResponse {
    private Long id;
    private String name;
    private String description;
    private Long managerId;
    private String managerName;
    private Integer employeeCount;
    private LocalDateTime createdAt;
}
