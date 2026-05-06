package com.hrmanagement.hr_management.service.impl;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hrmanagement.hr_management.dto.response.AttendanceResponse;
import com.hrmanagement.hr_management.dto.response.PageResponse;
import com.hrmanagement.hr_management.entity.Attendance;
import com.hrmanagement.hr_management.entity.Employee;
import com.hrmanagement.hr_management.enums.AttendanceStatus;
import com.hrmanagement.hr_management.repository.AttendanceRepository;
import com.hrmanagement.hr_management.repository.EmployeeRepository;
import com.hrmanagement.hr_management.service.AttendanceService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;

    // Giờ muộn mặc định là sau 08:30 AM
    private static final LocalTime LATE_THRESHOLD = LocalTime.of(8, 30);

    @Override
    public AttendanceResponse checkIn(String email) {
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        // Kiểm tra giờ check-in hợp lệ (ví dụ: không cho phép check-in từ 8 PM đến 6 AM)
        if (now.isBefore(LocalTime.of(6, 0)) || now.isAfter(LocalTime.of(20, 0))) {
            throw new RuntimeException("Giờ check-in không hợp lệ. Vui lòng check-in từ 06:00 AM đến 08:00 PM.");
        }

        // Kiểm tra xem hôm nay đã check-in chưa
        if (attendanceRepository.findByEmployeeIdAndDate(employee.getId(), today).isPresent()) {
            throw new RuntimeException("Bạn đã check-in trong ngày hôm nay rồi!");
        }

        Attendance attendance = new Attendance();
        attendance.setEmployee(employee);
        attendance.setDate(today);
        attendance.setCheckIn(now);

        // Đánh giá đi muộn hay đúng giờ
        if (now.isAfter(LATE_THRESHOLD)) {
            attendance.setStatus(AttendanceStatus.late);
        } else {
            attendance.setStatus(AttendanceStatus.present);
        }

        Attendance savedAttendance = attendanceRepository.save(attendance);
        return mapToResponse(savedAttendance);
    }

    @Override
    public AttendanceResponse checkOut(String email) {
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));

        LocalDate today = LocalDate.now();

        Attendance attendance = attendanceRepository.findByEmployeeIdAndDate(employee.getId(), today)
                .orElseThrow(() -> new RuntimeException("Bạn chưa check-in ngày hôm nay!"));

        if (attendance.getCheckOut() != null) {
            throw new RuntimeException("Bạn đã check-out rồi!");
        }

        attendance.setCheckOut(LocalTime.now());

        // Có thể bổ sung logic: nếu làm dưới 4 tiếng thì set status = half_day v.v.
        // Tạm thời giữ nguyên trạng thái khi check-in (present/late)

        Attendance savedAttendance = attendanceRepository.save(attendance);
        return mapToResponse(savedAttendance);
    }

    @Override
    public PageResponse<AttendanceResponse> getMyAttendances(String email, int page, int size) {
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));

        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());
        Page<Attendance> pageData = attendanceRepository.findByEmployeeId(employee.getId(), pageable);

        return mapToPageResponse(pageData);
    }

    @Override
    public PageResponse<AttendanceResponse> getAllAttendances(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());
        Page<Attendance> pageData = attendanceRepository.findAll(pageable);

        return mapToPageResponse(pageData);
    }

    private AttendanceResponse mapToResponse(Attendance attendance) {
        return AttendanceResponse.builder()
                .id(attendance.getId())
                .employeeId(attendance.getEmployee().getId())
                .employeeName(attendance.getEmployee().getFirstName() + " " + attendance.getEmployee().getLastName())
                .date(attendance.getDate())
                .checkIn(attendance.getCheckIn())
                .checkOut(attendance.getCheckOut())
                .status(attendance.getStatus())
                .build();
    }

    private PageResponse<AttendanceResponse> mapToPageResponse(Page<Attendance> pageData) {
        List<AttendanceResponse> dtoList = pageData.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.<AttendanceResponse>builder()
                .data(dtoList)
                .currentPage(pageData.getNumber())
                .pageSize(pageData.getSize())
                .totalElements(pageData.getTotalElements())
                .totalPages(pageData.getTotalPages())
                .build();
    }
}
