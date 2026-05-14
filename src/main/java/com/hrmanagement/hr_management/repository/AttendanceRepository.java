package com.hrmanagement.hr_management.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.hrmanagement.hr_management.entity.Attendance;

import com.hrmanagement.hr_management.enums.AttendanceStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    // Tìm kiếm record chấm công của một nhân viên trong 1 ngày cụ thể
    Optional<Attendance> findByEmployeeIdAndDate(Long employeeId, LocalDate date);

    // Lấy lịch sử chấm công của 1 nhân viên (có phân trang)
    Page<Attendance> findByEmployeeId(Long employeeId, Pageable pageable);

    // Đếm số ngày chấm công hợp lệ trong khoảng thời gian
    // Lấy danh sách chấm công trong khoảng thời gian
    java.util.List<Attendance> findByEmployeeIdAndDateBetween(Long employeeId, LocalDate start, LocalDate end);

    long countByDateAndStatus(LocalDate date, AttendanceStatus status);

    // Lấy lịch sử chấm công theo ID phòng ban (có phân trang)
    Page<Attendance> findByEmployeeDepartmentId(Long departmentId, Pageable pageable);

    // Đếm số lượng theo ngày, trạng thái và phòng ban
    long countByDateAndStatusAndEmployeeDepartmentId(LocalDate date, AttendanceStatus status, Long departmentId);
}
