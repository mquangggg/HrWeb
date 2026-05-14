package com.hrmanagement.hr_management.dto.response;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveResponse {
    private Long id;
    private Long employeeId;
    private String fullName;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reasonCategory;
    private String reason;
    private Integer days;
    private String note;
    private String approverByName;
    private Long departmentId;
}
