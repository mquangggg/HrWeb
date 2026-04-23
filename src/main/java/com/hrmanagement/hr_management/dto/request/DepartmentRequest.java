package com.hrmanagement.hr_management.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DepartmentRequest {
    
    @NotBlank(message = "Tên phòng ban không được để trống")
    private String name;
    
    private Long managerId; // Có thể null nếu phòng chưa có quản lý
}
