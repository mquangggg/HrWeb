package com.hrmanagement.hr_management.controller;

import java.security.Principal;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import com.hrmanagement.hr_management.dto.response.AttendanceResponse;
import com.hrmanagement.hr_management.dto.response.PageResponse;
import com.hrmanagement.hr_management.service.AttendanceService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/attendances")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    // Chấm công vào
    @PostMapping("/check-in")
    public ResponseEntity<AttendanceResponse> checkIn(Principal principal) {
        // Lấy email từ token (hoặc OAuth2)
        String email = principal.getName();
        return ResponseEntity.ok(attendanceService.checkIn(email));
    }

    // Chấm công ra
    @PostMapping("/check-out")
    public ResponseEntity<AttendanceResponse> checkOut(Principal principal) {
        String email = principal.getName();
        return ResponseEntity.ok(attendanceService.checkOut(email));
    }

    // Lấy lịch sử chấm công của bản thân (phân trang)
    @GetMapping("/me")
    public ResponseEntity<PageResponse<AttendanceResponse>> getMyAttendances(
            Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        String email = principal.getName();
        return ResponseEntity.ok(attendanceService.getMyAttendances(email, page, size));
    }

    // Lấy dữ liệu chấm công theo tháng để hiển thị Lịch
    @GetMapping("/calendar")
    public ResponseEntity<java.util.List<AttendanceResponse>> getAttendanceCalendar(
            Principal principal,
            @RequestParam int month,
            @RequestParam int year) {
        String email = principal.getName();
        return ResponseEntity.ok(attendanceService.getAttendanceByMonth(email, month, year));
    }

    // Dành cho mọi người xem tất cả (phân trang)
    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN', 'EMPLOYEE')")
    public ResponseEntity<PageResponse<AttendanceResponse>> getAllAttendances(
            Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            org.springframework.security.core.Authentication authentication) {
            
        boolean isAdmin = authentication.getAuthorities().contains(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN"));
        String email = principal.getName();
        return ResponseEntity.ok(attendanceService.getAllAttendances(page, size, email, isAdmin));
    }

    // Lấy thống kê hôm nay
    @GetMapping("/today-stats")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN', 'EMPLOYEE')")
    public ResponseEntity<com.hrmanagement.hr_management.dto.response.AttendanceStatsResponse> getTodayStats(
            Principal principal,
            org.springframework.security.core.Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().contains(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN"));
        return ResponseEntity.ok(attendanceService.getTodayStats(principal.getName(), isAdmin));
    }
}
