package com.hrmanagement.hr_management.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PayrollCalculateRequest {

    @NotNull(message = "ID nhân viên không được để trống")
    private Long employeeId;

    @NotNull(message = "Tháng không được để trống")
    @Min(value = 1, message = "Tháng phải từ 1 đến 12")
    @Max(value = 12, message = "Tháng phải từ 1 đến 12")
    private Integer month;

    @NotNull(message = "Năm không được để trống")
    @Min(value = 2000, message = "Năm không hợp lệ")
    private Integer year;
    
    // Số ngày nghỉ, tiền thưởng, tiền phạt có thể truyền từ client hoặc tự tính toán
    private Integer absentDays;
    private Double allowance;
    private Double deduction;
}
