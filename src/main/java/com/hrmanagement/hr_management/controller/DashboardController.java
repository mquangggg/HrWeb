package com.hrmanagement.hr_management.controller;

import com.hrmanagement.hr_management.dto.response.ApiResponse;
import com.hrmanagement.hr_management.dto.response.DashboardStatsResponse;
import com.hrmanagement.hr_management.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getStats() {
        DashboardStatsResponse stats = dashboardService.getStats();

        return ResponseEntity.ok(ApiResponse.<DashboardStatsResponse>builder()
                .message("Lấy thống kê dashboard thành công")
                .data(stats)
                .build());
    }
}
