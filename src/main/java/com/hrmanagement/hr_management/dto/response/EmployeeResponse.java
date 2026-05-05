package com.hrmanagement.hr_management.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.hrmanagement.hr_management.enums.EmployeeStatus;
import com.hrmanagement.hr_management.enums.Role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponse {
    // Dùng để trả về dữ liệu employee cho client

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private Role role;
    private BigDecimal baseSalary;
    private BigDecimal allowance;
    private LocalDate startDate;
    private EmployeeStatus status;
    
    // Thay vì trả về nguyên cả Object Department/Position (dễ bị đệ quy vòng lặp),
    // Ta trả về cả tên lẫn ID để frontend dùng
    private Long departmentId;
    private String departmentName;
    private Long positionId;
    private String positionName;
}
