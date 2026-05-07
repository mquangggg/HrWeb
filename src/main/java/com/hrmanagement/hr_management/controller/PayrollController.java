package com.hrmanagement.hr_management.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.stream.Collectors;

import com.hrmanagement.hr_management.dto.response.ApiResponse;
import com.hrmanagement.hr_management.dto.response.PageResponse;
import com.hrmanagement.hr_management.dto.response.PayrollResponse;
import com.hrmanagement.hr_management.service.PayrollService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/payrolls")
@RequiredArgsConstructor
public class PayrollController {

    private final PayrollService payrollService;

    @PostMapping("/calculate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<PayrollResponse>>> calculatePayroll(
            @RequestParam int month,
            @RequestParam int year) {
        
        if (month < 1 || month > 12) {
            throw new RuntimeException("Tháng không hợp lệ (1-12)");
        }
        if (year < 2000) {
            throw new RuntimeException("Năm không hợp lệ");
        }
        
        List<PayrollResponse> results = payrollService.calculatePayroll(month, year);
        
        return ResponseEntity.ok(ApiResponse.<List<PayrollResponse>>builder()
                .message("Tính lương thành công cho tháng " + month + "/" + year)
                .data(results)
                .build());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<ApiResponse<List<PayrollResponse>>> getPayrolls(
            @RequestParam int month,
            @RequestParam int year,
            Authentication authentication) {
        
        if (month < 1 || month > 12) {
            throw new RuntimeException("Tháng không hợp lệ (1-12)");
        }
        if (year < 2000) {
            throw new RuntimeException("Năm không hợp lệ");
        }
        
        List<PayrollResponse> results = payrollService.getPayrolls(month, year);
        
        // Lọc dữ liệu nếu không phải là ADMIN
        boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        
        if (!isAdmin) {
            String currentEmail = authentication.getName();
            results = results.stream()
                    .filter(p -> p.getEmail().equals(currentEmail))
                    .collect(Collectors.toList());
        }
        
        return ResponseEntity.ok(ApiResponse.<List<PayrollResponse>>builder()
                .message(isAdmin ? "Lấy danh sách bảng lương tháng " + month + "/" + year 
                                 : "Lấy thông tin lương cá nhân tháng " + month + "/" + year)
                .data(results)
                .build());
    }
}
