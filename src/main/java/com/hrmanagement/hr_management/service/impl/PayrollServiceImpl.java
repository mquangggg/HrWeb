package com.hrmanagement.hr_management.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

import com.hrmanagement.hr_management.dto.response.PageResponse;
import com.hrmanagement.hr_management.dto.response.PayrollResponse;
import com.hrmanagement.hr_management.entity.Attendance;
import com.hrmanagement.hr_management.entity.Employee;
import com.hrmanagement.hr_management.entity.Payroll;

import com.hrmanagement.hr_management.repository.AttendanceRepository;
import com.hrmanagement.hr_management.repository.EmployeeRepository;
import com.hrmanagement.hr_management.repository.PayrollRepository;
import com.hrmanagement.hr_management.service.PayrollService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class PayrollServiceImpl implements PayrollService {

    private final PayrollRepository payrollRepository;
    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;

    private static final int STANDARD_WORKING_DAYS = 22;

    @Override
    public List<PayrollResponse> calculatePayroll(int month, int year) {
        // Lấy tất cả nhân viên để đảm bảo tính lương cho cả người vừa nghỉ (inactive) nhưng có công
        List<Employee> allEmployees = employeeRepository.findAll();

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<PayrollResponse> results = new ArrayList<>();
        for (Employee emp : allEmployees) {
            // Lấy danh sách chấm công trong tháng
            List<Attendance> attendances = attendanceRepository.findByEmployeeIdAndDateBetween(
                    emp.getId(), startDate, endDate);

            // Tính tổng ngày công: mỗi ngày cap tối đa 1, tỉ lệ nếu < 8h
            BigDecimal totalEquivalentDays = BigDecimal.ZERO;

            for (Attendance att : attendances) {
                if (att.getCheckIn() == null || att.getCheckOut() == null) {
                    continue; // bỏ qua ngày quên check-out
                }

                Duration duration = Duration.between(att.getCheckIn(), att.getCheckOut());
                BigDecimal hoursWorked = BigDecimal.valueOf(duration.toMinutes())
                        .divide(new BigDecimal(60), 4, RoundingMode.HALF_UP);

                // >= 8 tiếng → 1 ngày công, < 8 tiếng → tính tỉ lệ
                BigDecimal dayValue = hoursWorked
                        .divide(new BigDecimal(8), 4, RoundingMode.HALF_UP)
                        .min(BigDecimal.ONE); // cap tối đa 1

                totalEquivalentDays = totalEquivalentDays.add(dayValue);
            }

            // Lấy baseSalary: ưu tiên từ employee, fallback về position
            BigDecimal baseSalary = emp.getBaseSalary();
            if (baseSalary == null || baseSalary.compareTo(BigDecimal.ZERO) == 0) {
                baseSalary = (emp.getPosition() != null && emp.getPosition().getBaseSalary() != null)
                        ? emp.getPosition().getBaseSalary()
                        : BigDecimal.ZERO;
            }
            BigDecimal allowance = emp.getAllowance() != null ? emp.getAllowance() : BigDecimal.ZERO;

            // Lương thực nhận = (baseSalary / 22) * ngày công + allowance cố định
            BigDecimal dailySalary = baseSalary
                    .divide(BigDecimal.valueOf(STANDARD_WORKING_DAYS), 4, RoundingMode.HALF_UP);
            
            BigDecimal netSalary = dailySalary
                    .multiply(totalEquivalentDays)
                    .add(allowance)
                    .setScale(2, RoundingMode.HALF_UP);


            if (netSalary.compareTo(BigDecimal.ZERO) <= 0) {
                netSalary = BigDecimal.ZERO;
            }

            // Lưu payroll
            Payroll payroll = payrollRepository
                    .findByEmployeeIdAndMonthAndYear(emp.getId(), month, year)
                    .orElse(new Payroll());

            payroll.setEmployee(emp);
            payroll.setMonth(month);
            payroll.setYear(year);
            payroll.setWorkingDays(totalEquivalentDays.setScale(2, RoundingMode.HALF_UP));
            payroll.setBaseSalary(baseSalary);
            payroll.setAllowance(allowance);
            payroll.setDeduction(BigDecimal.ZERO);
            payroll.setNetSalary(netSalary);

            results.add(mapToResponse(payrollRepository.save(payroll)));
        }

        return results;
    }

    @Override
    public PageResponse<PayrollResponse> getPayrolls(int month, int year, int page, int size, String email, boolean isAdmin) {
        if (isAdmin) {
            Pageable pageable = PageRequest.of(page, size);
            Page<Payroll> payrollPage = payrollRepository.findByMonthAndYear(month, year, pageable);
            
            List<PayrollResponse> content = payrollPage.getContent().stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
                    
            return PageResponse.<PayrollResponse>builder()
                    .data(content)
                    .currentPage(page)
                    .pageSize(size)
                    .totalElements(payrollPage.getTotalElements())
                    .totalPages(payrollPage.getTotalPages())
                    .build();
        } else {
            Employee emp = employeeRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));
            
            Optional<Payroll> payrollOpt = payrollRepository.findByEmployeeIdAndMonthAndYear(emp.getId(), month, year);
            
            List<PayrollResponse> content = new ArrayList<>();
            payrollOpt.ifPresent(p -> content.add(mapToResponse(p)));
            
            return PageResponse.<PayrollResponse>builder()
                    .data(content)
                    .currentPage(0)
                    .pageSize(size)
                    .totalElements(content.size())
                    .totalPages(content.isEmpty() ? 0 : 1)
                    .build();
        }
    }

    private PayrollResponse mapToResponse(Payroll payroll) {
        Employee emp = payroll.getEmployee();
        String firstName = emp.getFirstName() != null ? emp.getFirstName() : "";
        String lastName = emp.getLastName() != null ? emp.getLastName() : "";
        String fullName = (firstName + " " + lastName).trim();
        if (fullName.isEmpty()) fullName = "N/A";

        return PayrollResponse.builder()
                .id(payroll.getId())
                .employeeId(emp.getId())
                .employeeName(fullName)
                .email(emp.getEmail())
                .departmentName(emp.getDepartment() != null ? emp.getDepartment().getName() : "N/A")
                .month(payroll.getMonth())
                .year(payroll.getYear())
                .workingDays(payroll.getWorkingDays())
                .baseSalary(payroll.getBaseSalary())
                .allowance(payroll.getAllowance())
                .deduction(payroll.getDeduction())
                .netSalary(payroll.getNetSalary())
                .build();
    }
}
