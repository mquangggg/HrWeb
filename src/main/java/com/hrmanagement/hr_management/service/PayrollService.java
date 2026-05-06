package com.hrmanagement.hr_management.service;

import java.util.List;
import com.hrmanagement.hr_management.dto.response.PageResponse;
import com.hrmanagement.hr_management.dto.response.PayrollResponse;

public interface PayrollService {
    // Tính lương cho tất cả nhân viên trong tháng/năm
    List<PayrollResponse> calculatePayroll(int month, int year);

    // Lấy bảng lương đã tính
    List<PayrollResponse> getPayrolls(int month, int year);
}
