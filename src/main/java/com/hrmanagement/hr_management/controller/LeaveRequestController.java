package com.hrmanagement.hr_management.controller;

import java.security.Principal;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hrmanagement.hr_management.dto.request.LeaveRequest;
import com.hrmanagement.hr_management.dto.response.LeaveResponse;
import com.hrmanagement.hr_management.dto.response.PageResponse;
import com.hrmanagement.hr_management.enums.LeaveStatus;
import com.hrmanagement.hr_management.service.LeaveRequestService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/leave-requests")
@RequiredArgsConstructor
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    // Tạo đơn nghỉ phép mới
    @PostMapping
    public ResponseEntity<LeaveResponse> createLeaveRequest(
            Principal principal,
            @Valid @RequestBody LeaveRequest request) {
        String email = principal.getName();
        return ResponseEntity.ok(leaveRequestService.createLeaveRequest(email, request));
    }

    // Xem đơn của tôi
    @GetMapping("/me")
    public ResponseEntity<PageResponse<LeaveResponse>> getMyLeaveRequests(
            Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        String email = principal.getName();
        return ResponseEntity.ok(leaveRequestService.getMyLeaveRequests(email, page, size));
    }

    // Xem tất cả đơn (Dành cho Manager/Admin)
    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<PageResponse<LeaveResponse>> getAllLeaveRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(leaveRequestService.getAllLeaveRequests(page, size));
    }

    // Duyệt/Từ chối đơn (Dành cho Manager/Admin)
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<LeaveResponse> updateLeaveStatus(
            @PathVariable Long id,
            @RequestParam LeaveStatus status,
            Principal principal) {
        String email = principal.getName();
        return ResponseEntity.ok(leaveRequestService.updateStatus(id, email, status));
    }
}
