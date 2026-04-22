package com.hrmanagement.hr_management.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "payrolls")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payroll {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Column(nullable = false)
    private Integer month; // 1 - 12

    @Column(nullable = false)
    private Integer year;

    @Column(name = "working_days")
    private Integer workingDays;

    @Column(name = "absent_days")
    @Builder.Default
    private Integer absentDays = 0;

    @Column(name = "base_salary", precision = 15, scale = 2)
    private BigDecimal baseSalary;

    @Column(precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal allowance = BigDecimal.ZERO;

    @Column(precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal deduction = BigDecimal.ZERO;

    @Column(name = "net_salary", precision = 15, scale = 2)
    private BigDecimal netSalary;
}
