package com.hrmanagement.hr_management.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hrmanagement.hr_management.entity.Employee;
import com.hrmanagement.hr_management.entity.Payroll;

public interface PayrollRepository extends JpaRepository<Payroll, Long> {
    // Lấy bảng lương theo nhân viên
    List<Payroll> findByEmployee(Employee employee);

    // Lấy danh sách bảng lương theo tháng và năm
    List<Payroll> findByMonthAndYear(Integer month, Integer year);

    // Lấy danh sách bảng lương theo tháng và năm (có phân trang)
    org.springframework.data.domain.Page<Payroll> findByMonthAndYear(Integer month, Integer year, org.springframework.data.domain.Pageable pageable);

    // Tìm bảng lương của 1 nhân viên trong 1 tháng cụ thể
    Optional<Payroll> findByEmployeeIdAndMonthAndYear(Long employeeId, Integer month, Integer year);
}
