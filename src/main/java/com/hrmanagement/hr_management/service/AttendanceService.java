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
    PageResponse<AttendanceResponse> getAllAttendances(int page, int size, String email, boolean isAdmin);

    // Lấy dữ liệu chấm công theo tháng của 1 nhân viên
    java.util.List<AttendanceResponse> getAttendanceByMonth(String email, int month, int year);

    // Lấy thống kê chấm công hôm nay
    com.hrmanagement.hr_management.dto.response.AttendanceStatsResponse getTodayStats(String email, boolean isAdmin);
}
