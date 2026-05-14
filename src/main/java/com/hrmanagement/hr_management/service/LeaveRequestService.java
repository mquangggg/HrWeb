package com.hrmanagement.hr_management.service;

import com.hrmanagement.hr_management.dto.request.LeaveRequest;
import com.hrmanagement.hr_management.dto.response.LeaveResponse;
import com.hrmanagement.hr_management.dto.response.PageResponse;
import com.hrmanagement.hr_management.enums.LeaveStatus;

public interface LeaveRequestService {
    // Tạo đơn nghỉ phép mới
    LeaveResponse createLeaveRequest(String email, LeaveRequest request);

    // Lấy danh sách đơn nghỉ phép của bản thân
    PageResponse<LeaveResponse> getMyLeaveRequests(String email, int page, int size);

    // Lấy danh sách tất cả đơn nghỉ phép (dành cho Admin/Manager)
    PageResponse<LeaveResponse> getAllLeaveRequests(int page, int size);

    // Cập nhật trạng thái đơn (duyệt/từ chối)
    LeaveResponse updateStatus(Long id, String email, LeaveStatus newStatus);
}
