package com.hrmanagement.hr_management.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.hrmanagement.hr_management.entity.Employee;
import com.hrmanagement.hr_management.entity.LeaveRequest;
import com.hrmanagement.hr_management.enums.LeaveStatus;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    // Tìm các đơn xin nghỉ phép theo nhân viên
    List<LeaveRequest> findByEmployee(Employee employee);
    
    // Tìm các đơn xin nghỉ phép theo trạng thái
    List<LeaveRequest> findByStatus(LeaveStatus status);
    
    // Tìm các đơn xin nghỉ phép được duyệt bởi
    List<LeaveRequest> findByApprovedBy(Employee approvedBy);

    // Tìm đơn xin nghỉ phép của 1 nhân viên (có phân trang)
    Page<LeaveRequest> findByEmployeeId(Long employeeId, Pageable pageable);
}
