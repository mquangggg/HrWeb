package com.hrmanagement.hr_management.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<ApiResponse<List<PayrollResponse>>> calculatePayroll(
            @RequestParam int month,
            @RequestParam int year) {
        
        List<PayrollResponse> results = payrollService.calculatePayroll(month, year);
        
        return ResponseEntity.ok(ApiResponse.<List<PayrollResponse>>builder()
                .message("Tính lương thành công cho tháng " + month + "/" + year)
                .data(results)
                .build());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PayrollResponse>>> getPayrolls(
            @RequestParam int month,
            @RequestParam int year) {
        
        List<PayrollResponse> results = payrollService.getPayrolls(month, year);
        
        return ResponseEntity.ok(ApiResponse.<List<PayrollResponse>>builder()
                .message("Lấy danh sách bảng lương tháng " + month + "/" + year)
                .data(results)
                .build());
    }
}
