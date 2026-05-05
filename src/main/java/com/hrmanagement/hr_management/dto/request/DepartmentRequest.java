package com.hrmanagement.hr_management.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DepartmentRequest {

    @NotBlank(message = "Tên phòng ban không được để trống")
    private String name;

    private String description;
    
    private Long managerId;
}
