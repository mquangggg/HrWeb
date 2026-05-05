package com.hrmanagement.hr_management.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.hrmanagement.hr_management.entity.Attendance;

import java.time.LocalDate;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    // Tìm kiếm record chấm công của một nhân viên trong 1 ngày cụ thể
    Optional<Attendance> findByEmployeeIdAndDate(Long employeeId, LocalDate date);

    // Lấy lịch sử chấm công của 1 nhân viên (có phân trang)
    Page<Attendance> findByEmployeeId(Long employeeId, Pageable pageable);

}
