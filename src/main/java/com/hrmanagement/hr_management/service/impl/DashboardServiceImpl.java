package com.hrmanagement.hr_management.service.impl;

import com.hrmanagement.hr_management.dto.response.DashboardStatsResponse;
import com.hrmanagement.hr_management.enums.AttendanceStatus;
import com.hrmanagement.hr_management.enums.LeaveStatus;
import com.hrmanagement.hr_management.repository.AttendanceRepository;
import com.hrmanagement.hr_management.repository.EmployeeRepository;
import com.hrmanagement.hr_management.repository.LeaveRequestRepository;
import com.hrmanagement.hr_management.repository.PayrollRepository;
import com.hrmanagement.hr_management.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final PayrollRepository payrollRepository;

    @Override
    public DashboardStatsResponse getStats() {
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

        return DashboardStatsResponse.builder()
                .totalEmployees(totalEmployees)
                .presentToday(presentToday)
                .onLeaveToday(onLeaveToday)
                .pendingLeaveRequests(pendingLeaves)
                .totalSalaryCurrentMonth(totalSalary)
                .build();
    }
}
