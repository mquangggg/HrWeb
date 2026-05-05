package com.hrmanagement.hr_management.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;

import com.hrmanagement.hr_management.enums.AttendanceStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AttendanceResponse {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private LocalDate date;
    private LocalTime checkIn;
    private LocalTime checkOut;
    private AttendanceStatus status;
}
