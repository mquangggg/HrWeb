package com.hrmanagement.hr_management.dto.response;

import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
    private long totalEmployees;
    private long presentToday;
    private long onLeaveToday;
    private long pendingLeaveRequests;
    private BigDecimal totalSalaryCurrentMonth;
}
