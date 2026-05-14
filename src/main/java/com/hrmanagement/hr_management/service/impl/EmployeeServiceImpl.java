package com.hrmanagement.hr_management.service.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hrmanagement.hr_management.dto.request.EmployeeCreateRequest;
import com.hrmanagement.hr_management.dto.request.EmployeeUpdateRequest;
import com.hrmanagement.hr_management.dto.response.EmployeeResponse;
import com.hrmanagement.hr_management.dto.response.PageResponse;
import com.hrmanagement.hr_management.entity.Department;
import com.hrmanagement.hr_management.entity.Employee;
import com.hrmanagement.hr_management.entity.Position;
import com.hrmanagement.hr_management.enums.EmployeeStatus;
import com.hrmanagement.hr_management.repository.DepartmentRepository;
import com.hrmanagement.hr_management.repository.EmployeeRepository;
import com.hrmanagement.hr_management.repository.PositionRepository;
import com.hrmanagement.hr_management.service.EmployeeService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<EmployeeResponse> getAllEmployees(int page, int size, String keyword, Long deptId, Long posId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("lastName").ascending());
        
        // Gọi repository với các tham số lọc linh hoạt
        Page<Employee> employeePage = employeeRepository.searchEmployees(keyword, deptId, posId, pageable);

        List<EmployeeResponse> data = employeePage.getContent()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.<EmployeeResponse>builder()
                .currentPage(page)
                .totalPages(employeePage.getTotalPages())
                .pageSize(size)
                .totalElements(employeePage.getTotalElements())
                .data(data)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(Long id) {
        return mapToResponse(findEmployeeById(id));
    }

    @Override
    public EmployeeResponse createEmployee(EmployeeCreateRequest request) {
        // Kiểm tra email trùng lặp
        if (employeeRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email '" + request.getEmail() + "' đã được sử dụng!");
        }

        Employee employee = new Employee();
        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setEmail(request.getEmail());
        employee.setPassword(passwordEncoder.encode(request.getPassword()));
        employee.setPhone(request.getPhone());
        employee.setRole(request.getRole());
        employee.setBaseSalary(request.getBaseSalary());
        employee.setAllowance(request.getAllowance());
        employee.setStartDate(request.getStartDate() != null ? request.getStartDate() : LocalDate.now());
        employee.setStatus(EmployeeStatus.active);

        // Gán phòng ban nếu có
        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException(
                            "Không tìm thấy phòng ban với id: " + request.getDepartmentId()));
            employee.setDepartment(department);
        }

        // Gán chức vụ nếu có
        if (request.getPositionId() != null) {
            Position position = positionRepository.findById(request.getPositionId())
                    .orElseThrow(() -> new RuntimeException(
                            "Không tìm thấy chức vụ với id: " + request.getPositionId()));
            employee.setPosition(position);
        }

        return mapToResponse(employeeRepository.save(employee));
    }

    @Override
    public EmployeeResponse updateEmployee(Long id, EmployeeUpdateRequest request) {
        Employee employee = findEmployeeById(id);
        checkManagerPermission(employee);

        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setPhone(request.getPhone());
        
        // Chỉ Admin mới được đổi Role/Lương/Phòng ban
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Employee currentUser = employeeRepository.findByEmail(currentUserEmail).orElse(null);
        
        if (currentUser != null && currentUser.getRole().name().equals("ADMIN")) {
            // [BẢO VỆ] Ngăn Admin tự hạ quyền hoặc tự vô hiệu hóa chính mình
            if (currentUser.getId().equals(employee.getId())) {
                if (!request.getRole().name().equals("ADMIN")) {
                    throw new RuntimeException("Bạn không thể tự hạ quyền Admin của chính mình để tránh khóa hệ thống!");
                }
                if (request.getStatus() != com.hrmanagement.hr_management.enums.EmployeeStatus.active) {
                    throw new RuntimeException("Bạn không thể tự vô hiệu hóa tài khoản Admin của chính mình!");
                }
            }

            employee.setRole(request.getRole());
            employee.setBaseSalary(request.getBaseSalary());
            employee.setAllowance(request.getAllowance());
            
            if (request.getDepartmentId() != null) {
                Department department = departmentRepository.findById(request.getDepartmentId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng ban"));
                employee.setDepartment(department);
            } else {
                employee.setDepartment(null);
            }
        }

        employee.setStatus(request.getStatus());

        if (request.getPositionId() != null) {
            Position position = positionRepository.findById(request.getPositionId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy chức vụ"));
            employee.setPosition(position);
        } else {
            employee.setPosition(null);
        }

        return mapToResponse(employeeRepository.save(employee));
    }

    @Override
    public EmployeeResponse updateStatus(Long id, EmployeeStatus status) {
        Employee employee = findEmployeeById(id);
        checkManagerPermission(employee);
        employee.setStatus(status);
        return mapToResponse(employeeRepository.save(employee));
    }

    @Override
    public void deleteEmployee(Long id) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Employee currentUser = employeeRepository.findByEmail(currentUserEmail).orElse(null);
        
        if (currentUser != null && currentUser.getId().equals(id)) {
            throw new RuntimeException("Bạn không thể tự xóa tài khoản của chính mình!");
        }

        Employee employee = findEmployeeById(id);
        checkManagerPermission(employee);
        employeeRepository.delete(employee);
    }

    private void checkManagerPermission(Employee targetEmployee) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Employee currentUser = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Người dùng hiện tại không tồn tại"));

        if (currentUser.getRole().name().equals("ADMIN")) return;

        if (currentUser.getRole().name().equals("MANAGER")) {
            if (currentUser.getDepartment() == null || targetEmployee.getDepartment() == null ||
                !currentUser.getDepartment().getId().equals(targetEmployee.getDepartment().getId())) {
                throw new RuntimeException("Bạn không có quyền thao tác trên nhân viên ngoài phòng ban của mình!");
            }
        } else {
            throw new RuntimeException("Bạn không có quyền thực hiện hành động này!");
        }
    }

    // ===========================
    // PRIVATE HELPERS
    // ===========================

    private Employee findEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với id: " + id));
    }

    private EmployeeResponse mapToResponse(Employee employee) {
        return EmployeeResponse.builder()
                .id(employee.getId())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .email(employee.getEmail())
                .phone(employee.getPhone())
                .role(employee.getRole())
                .baseSalary(employee.getBaseSalary())
                .allowance(employee.getAllowance())
                .startDate(employee.getStartDate())
                .status(employee.getStatus())
                .departmentId(employee.getDepartment() != null ? employee.getDepartment().getId() : null)
                .departmentName(employee.getDepartment() != null ? employee.getDepartment().getName() : null)
                .positionId(employee.getPosition() != null ? employee.getPosition().getId() : null)
                .positionName(employee.getPosition() != null ? employee.getPosition().getName() : null)
                .build();
    }
}
