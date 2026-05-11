package com.hrmanagement.hr_management.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceStatsResponse {
    private long present;
    private long late;
    private long absent;
    private int presentPercentage;
    private int latePercentage;
    private int absentPercentage;
}
