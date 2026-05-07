package com.hrmanagement.hr_management.controller;

import com.hrmanagement.hr_management.dto.response.ApiResponse;
import com.hrmanagement.hr_management.dto.response.DashboardStatsResponse;
import com.hrmanagement.hr_management.enums.AttendanceStatus;
import com.hrmanagement.hr_management.enums.LeaveStatus;
import com.hrmanagement.hr_management.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final PayrollRepository payrollRepository;

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getStats() {
        LocalDate today = LocalDate.now();
        int month = today.getMonthValue();
        int year = today.getYear();

        long totalEmployees = employeeRepository.count();
        long presentToday = attendanceRepository.countByDateAndStatus(today, AttendanceStatus.present);
        long onLeaveToday = leaveRequestRepository.countByStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                LeaveStatus.approved, today, today);
        long pendingLeaves = leaveRequestRepository.countByStatus(LeaveStatus.pending);
        
        BigDecimal totalSalary = payrollRepository.findByMonthAndYear(month, year).stream()
                .map(p -> p.getNetSalary())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        DashboardStatsResponse stats = DashboardStatsResponse.builder()
                .totalEmployees(totalEmployees)
                .presentToday(presentToday)
                .onLeaveToday(onLeaveToday)
                .pendingLeaveRequests(pendingLeaves)
                .totalSalaryCurrentMonth(totalSalary)
                .build();

        return ResponseEntity.ok(ApiResponse.<DashboardStatsResponse>builder()
                .message("Lấy thống kê dashboard thành công")
                .data(stats)
                .build());
    }
}
