package com.hrmanagement.hr_management.service;

import com.hrmanagement.hr_management.dto.request.EmployeeCreateRequest;
import com.hrmanagement.hr_management.dto.request.EmployeeUpdateRequest;
import com.hrmanagement.hr_management.dto.response.EmployeeResponse;
import com.hrmanagement.hr_management.dto.response.PageResponse;
import com.hrmanagement.hr_management.enums.EmployeeStatus;

public interface EmployeeService {

    // Lấy danh sách nhân viên có phân trang và tìm kiếm (hỗ trợ lọc theo phòng ban, chức vụ)
    PageResponse<EmployeeResponse> getAllEmployees(int page, int size, String keyword, Long deptId, Long posId);

    // Lấy chi tiết một nhân viên theo id
    EmployeeResponse getEmployeeById(Long id);

    // Thêm nhân viên mới (ADMIN thêm, có password mặc định)
    EmployeeResponse createEmployee(EmployeeCreateRequest request);

    // Cập nhật thông tin nhân viên
    EmployeeResponse updateEmployee(Long id, EmployeeUpdateRequest request);

    // Kích hoạt / vô hiệu hóa nhân viên (soft delete)
    EmployeeResponse updateStatus(Long id, EmployeeStatus status);

    // Xóa nhân viên vĩnh viễn (chỉ ADMIN)
    void deleteEmployee(Long id);
}
