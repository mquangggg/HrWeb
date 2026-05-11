package com.hrmanagement.hr_management.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LeaveRequest {
    
    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDate startDate;
    
    @NotNull(message = "Ngày kết thúc không được để trống")
    private LocalDate endDate;
    
    @NotBlank(message = "Danh mục lý do không được để trống")
    private String reasonCategory;

    // Phần chi tiết có thể bỏ trống
    private String reason;
}
