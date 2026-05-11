package com.hrmanagement.hr_management.service.impl;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hrmanagement.hr_management.dto.request.LeaveRequest;
import com.hrmanagement.hr_management.dto.response.LeaveResponse;
import com.hrmanagement.hr_management.dto.response.PageResponse;
import com.hrmanagement.hr_management.entity.Employee;
import com.hrmanagement.hr_management.enums.LeaveStatus;
import com.hrmanagement.hr_management.repository.EmployeeRepository;
import com.hrmanagement.hr_management.repository.LeaveRequestRepository;
import com.hrmanagement.hr_management.service.LeaveRequestService;

import java.time.LocalDate;
import java.util.Arrays;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class LeaveRequestServiceImpl implements LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    public LeaveResponse createLeaveRequest(String email, LeaveRequest request) {
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));

        // Phải xin nghỉ trước ít nhất 1 ngày
        if (!request.getStartDate().isAfter(LocalDate.now())) {
            throw new RuntimeException("Phải xin nghỉ phép trước ít nhất 1 ngày làm việc!");
        }

        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new RuntimeException("Ngày kết thúc phải sau ngày bắt đầu!");
        }

        // Kiểm tra overlap đơn nghỉ phép
        boolean isOverlapping = leaveRequestRepository.existsByEmployeeIdAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                employee.getId(),
                Arrays.asList(LeaveStatus.pending, LeaveStatus.approved),
                request.getEndDate(),
                request.getStartDate()
        );

        if (isOverlapping) {
            throw new RuntimeException("Bạn đã có đơn xin nghỉ phép (đang chờ hoặc đã duyệt) trùng với thời gian này!");
        }

        com.hrmanagement.hr_management.entity.LeaveRequest leaveRequest = new com.hrmanagement.hr_management.entity.LeaveRequest();
        leaveRequest.setEmployee(employee);
        leaveRequest.setStartDate(request.getStartDate());
        leaveRequest.setEndDate(request.getEndDate());
        leaveRequest.setReasonCategory(request.getReasonCategory());
        leaveRequest.setReason(request.getReason());
        leaveRequest.setStatus(LeaveStatus.pending);

        com.hrmanagement.hr_management.entity.LeaveRequest saved = leaveRequestRepository.save(leaveRequest);
        return mapToResponse(saved);
    }

    @Override
    public PageResponse<LeaveResponse> getMyLeaveRequests(String email, int page, int size) {
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<com.hrmanagement.hr_management.entity.LeaveRequest> pageData = leaveRequestRepository.findByEmployeeId(employee.getId(), pageable);

        return mapToPageResponse(pageData);
    }

    @Override
    public PageResponse<LeaveResponse> getAllLeaveRequests(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<com.hrmanagement.hr_management.entity.LeaveRequest> pageData = leaveRequestRepository.findAll(pageable);

        return mapToPageResponse(pageData);
    }

    @Override
    public LeaveResponse updateStatus(Long id, String email, LeaveStatus newStatus) {
        Employee approver = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người duyệt"));

        com.hrmanagement.hr_management.entity.LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn xin nghỉ phép"));

        if (leaveRequest.getStatus() != LeaveStatus.pending) {
            throw new RuntimeException("Đơn này đã được xử lý (không còn ở trạng thái pending)!");
        }

        // Không cho phép tự duyệt đơn của chính mình
        if (approver.getId().equals(leaveRequest.getEmployee().getId())) {
            throw new RuntimeException("Bạn không thể tự duyệt đơn nghỉ phép của chính mình!");
        }

        // --- Kiểm tra quyền duyệt ---
        String role = approver.getRole().name();
        if (role.equals("ADMIN")) {
            // ADMIN có toàn quyền
        } else if (role.equals("MANAGER")) {
            // MANAGER chỉ được duyệt nhân viên trong cùng phòng ban
            Long empDeptId = (leaveRequest.getEmployee().getDepartment() != null) ? leaveRequest.getEmployee().getDepartment().getId() : null;
            Long mgrDeptId = (approver.getDepartment() != null) ? approver.getDepartment().getId() : null;

            if (empDeptId == null || !empDeptId.equals(mgrDeptId)) {
                throw new RuntimeException("Bạn chỉ có quyền duyệt đơn của nhân viên thuộc cùng phòng ban của mình!");
            }
        } else {
            throw new RuntimeException("Bạn không có quyền duyệt đơn xin nghỉ phép!");
        }

        leaveRequest.setStatus(newStatus);
        leaveRequest.setApprovedBy(approver);

        com.hrmanagement.hr_management.entity.LeaveRequest updated = leaveRequestRepository.save(leaveRequest);
        return mapToResponse(updated);
    }

    private LeaveResponse mapToResponse(com.hrmanagement.hr_management.entity.LeaveRequest entity) {
        long daysBetween = ChronoUnit.DAYS.between(entity.getStartDate(), entity.getEndDate()) + 1;
        
        return LeaveResponse.builder()
                .id(entity.getId())
                .employeeId(entity.getEmployee().getId())
                .fullName(entity.getEmployee().getFirstName() + " " + entity.getEmployee().getLastName())
                .status(entity.getStatus().name())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .reasonCategory(entity.getReasonCategory())
                .reason(entity.getReason())
                .days((int) daysBetween)
                .approverByName(entity.getApprovedBy() != null ? 
                        entity.getApprovedBy().getFirstName() + " " + entity.getApprovedBy().getLastName() : null)
                .departmentId(entity.getEmployee().getDepartment() != null ? entity.getEmployee().getDepartment().getId() : null)
                .note("") // Có thể thêm logic lưu note khi từ chối nếu có
                .build();
    }

    private PageResponse<LeaveResponse> mapToPageResponse(Page<com.hrmanagement.hr_management.entity.LeaveRequest> pageData) {
        List<LeaveResponse> dtoList = pageData.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.<LeaveResponse>builder()
                .data(dtoList)
                .currentPage(pageData.getNumber())
                .pageSize(pageData.getSize())
                .totalElements(pageData.getTotalElements())
                .totalPages(pageData.getTotalPages())
                .build();
    }
}
