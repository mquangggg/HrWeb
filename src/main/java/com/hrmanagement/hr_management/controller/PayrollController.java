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
    public ResponseEntity<ApiResponse<PageResponse<PayrollResponse>>> getPayrolls(
            @RequestParam int month,
            @RequestParam int year,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        
        if (month < 1 || month > 12) {
            throw new RuntimeException("Tháng không hợp lệ (1-12)");
        }
        if (year < 2000) {
            throw new RuntimeException("Năm không hợp lệ");
        }
        
        boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        String email = authentication.getName();
        
        PageResponse<PayrollResponse> results = payrollService.getPayrolls(month, year, page, size, email, isAdmin);
        
        return ResponseEntity.ok(ApiResponse.<PageResponse<PayrollResponse>>builder()
                .message(isAdmin ? "Lấy danh sách bảng lương tháng " + month + "/" + year 
                                 : "Lấy thông tin lương cá nhân tháng " + month + "/" + year)
                .data(results)
                .build());
    }
}
