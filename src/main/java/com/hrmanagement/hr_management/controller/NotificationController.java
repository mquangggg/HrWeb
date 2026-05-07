package com.hrmanagement.hr_management.controller;

import com.hrmanagement.hr_management.dto.request.NotificationRequest;
import com.hrmanagement.hr_management.dto.response.ApiResponse;
import com.hrmanagement.hr_management.dto.response.NotificationResponse;
import com.hrmanagement.hr_management.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getAllNotifications() {
        return ResponseEntity.ok(ApiResponse.<List<NotificationResponse>>builder()
                .message("Lấy danh sách thông báo thành công")
                .data(notificationService.getAllNotifications())
                .build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<NotificationResponse>> createNotification(
            @RequestBody NotificationRequest request,
            Principal principal) {
        return ResponseEntity.ok(ApiResponse.<NotificationResponse>builder()
                .message("Đăng thông báo thành công")
                .data(notificationService.createNotification(request, principal.getName()))
                .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .message("Xóa thông báo thành công")
                .build());
    }
}
