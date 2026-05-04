package com.hrmanagement.hr_management.dto.response;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PositionResponse {
    private Long id;
    private String name;
    private BigDecimal baseSalary;
}
