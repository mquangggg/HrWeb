package com.hrmanagement.hr_management.dto.request;

import java.math.BigDecimal;

import com.hrmanagement.hr_management.enums.EmployeeStatus;
import com.hrmanagement.hr_management.enums.Role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EmployeeUpdateRequest {
    
    @NotBlank(message = "Họ không được để trống")
    private String firstName;
    
    @NotBlank(message = "Tên không được để trống")
    private String lastName;
    
    
    private String phone;
    
    private Long departmentId;
    
    private Long positionId;
    
    @NotNull(message = "Vai trò không được để trống")
    private Role role;
    
    private BigDecimal baseSalary;
    
    private BigDecimal allowance;
    
    //private LocalDate startDate; Thông tin mặc định khi tạo không nên sửa 
    
    @NotNull(message = "Trạng thái không được để trống") 
    private EmployeeStatus status;
}
