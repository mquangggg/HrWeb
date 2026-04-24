package com.hrmanagement.hr_management.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.hrmanagement.hr_management.enums.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;

@Data
@ToString(exclude = "password")
public class EmployeeCreateRequest {
    
    @NotBlank(message = "Họ không được để trống")
    private String firstName;
    
    @NotBlank(message = "Tên không được để trống")
    private String lastName;
    
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;
    
    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;
    
    private String phone;
    
    private Long departmentId;
    
    private Long positionId;
    
    @NotNull(message = "Vai trò không được để trống")
    private Role role;
    
    private BigDecimal baseSalary;
    
    private BigDecimal allowance;
    
    private LocalDate startDate;
}
