package com.hrmanagement.hr_management.dto.response;

import java.math.BigDecimal;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayrollResponse {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String email;
    private String departmentName;
    private Integer month;
    private Integer year;
    private BigDecimal workingDays;
    private BigDecimal baseSalary;
    private BigDecimal allowance;
    private BigDecimal deduction;
    private BigDecimal netSalary;
}
