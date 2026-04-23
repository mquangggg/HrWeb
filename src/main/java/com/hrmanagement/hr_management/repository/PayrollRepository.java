package com.hrmanagement.hr_management.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hrmanagement.hr_management.entity.Employee;
import com.hrmanagement.hr_management.entity.Payroll;

public interface PayrollRepository extends JpaRepository<Payroll, Long> {
    // Lấy bảng lương theo nhân viên
    List<Payroll> findByEmployee(Employee employee);

}
