package com.hrmanagement.hr_management.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PositionRequest {

    @NotBlank(message = "Tên chức vụ không được để trống")
    private String name;

    @NotNull(message = "Lương cơ bản không được để trống")
    @Positive(message = "Lương cơ bản phải là số dương")
    private BigDecimal baseSalary;
}
