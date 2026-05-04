package com.hrmanagement.hr_management.dto.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PositionRequest {
    private String name;
    private BigDecimal baseSalary;
}
