package com.hrmanagement.hr_management.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import com.hrmanagement.hr_management.enums.EmployeeStatus;
import com.hrmanagement.hr_management.enums.Role;

@Entity
@Table(name = "employees")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"password", "attendances", "leaveRequests", "approvedLeaveRequests", "payrolls", "manager", "subordinates" ,"department","position"})
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(length = 20)
    private String phone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id")
    private Position position;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name = "base_salary", precision = 15, scale = 2)
    private BigDecimal baseSalary = BigDecimal.ZERO;

    @Column(name = "allowance", precision = 15, scale = 2)
    private BigDecimal allowance = BigDecimal.ZERO;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmployeeStatus status = EmployeeStatus.active;

    // Quan hệ 1-nhiều với Attendance
    @OneToMany(mappedBy = "employee", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Attendance> attendances = new HashSet<>();
    // cascade = CascadeType.ALL => nhân viên được xóa thì nhân viên đó sẽ được xóa
    // các quan hệ khác
    // orphanRemoval = true => nhân viên đó sẽ được xóa các quan hệ khác

    // Nhân viên tạo đơn xin nghỉ phép
    @OneToMany(mappedBy = "employee", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<LeaveRequest> leaveRequests = new HashSet<>();

    // Nhân viên duyệt đơn xin nghỉ phép
    @OneToMany(mappedBy = "approvedBy", fetch = FetchType.LAZY)
    private Set<LeaveRequest> approvedLeaveRequests = new HashSet<>();

    // Nhân viên tạo bảng lương
    @OneToMany(mappedBy = "employee", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Payroll> payrolls = new HashSet<>();

    // Nhân viên quản lý department
    @OneToOne(mappedBy = "manager", fetch = FetchType.LAZY)
    private Department managedDepartment;

    // Quản lý trực tiếp (cấp trên)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private Employee manager;

    // Danh sách nhân viên cấp dưới
    @OneToMany(mappedBy = "manager", fetch = FetchType.LAZY)
    private Set<Employee> subordinates = new HashSet<>();
}
