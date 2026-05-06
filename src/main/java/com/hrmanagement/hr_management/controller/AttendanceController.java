package com.hrmanagement.hr_management.controller;

import java.security.Principal;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    // Dành cho Manager/Admin xem tất cả (phân trang)
    @GetMapping
    // @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')") // Sẽ cấu hình sau ở Giai đoạn 6
    public ResponseEntity<PageResponse<AttendanceResponse>> getAllAttendances(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(attendanceService.getAllAttendances(page, size));
    }
}
