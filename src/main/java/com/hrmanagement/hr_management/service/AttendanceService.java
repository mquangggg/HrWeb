package com.hrmanagement.hr_management.service;

import com.hrmanagement.hr_management.dto.response.AttendanceResponse;
import com.hrmanagement.hr_management.dto.response.PageResponse;

public interface AttendanceService {
    
    // Chấm công vào
    AttendanceResponse checkIn(String email);

    // Chấm công ra
    AttendanceResponse checkOut(String email);

    // Lấy lịch sử chấm công của bản thân
    PageResponse<AttendanceResponse> getMyAttendances(String email, int page, int size);

    // Lấy lịch sử chấm công của tất cả nhân viên (dành cho Admin/Manager)
    PageResponse<AttendanceResponse> getAllAttendances(int page, int size);
}
