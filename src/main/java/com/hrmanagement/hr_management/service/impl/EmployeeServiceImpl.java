package com.hrmanagement.hr_management.service.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setPhone(request.getPhone());
        employee.setRole(request.getRole());
        employee.setBaseSalary(request.getBaseSalary());
        employee.setAllowance(request.getAllowance());
        employee.setStatus(request.getStatus());

        // Cập nhật phòng ban (null = bỏ gán)
        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException(
                            "Không tìm thấy phòng ban với id: " + request.getDepartmentId()));
            employee.setDepartment(department);
        } else {
            employee.setDepartment(null);
        }

        // Cập nhật chức vụ (null = bỏ gán)
        if (request.getPositionId() != null) {
            Position position = positionRepository.findById(request.getPositionId())
                    .orElseThrow(() -> new RuntimeException(
                            "Không tìm thấy chức vụ với id: " + request.getPositionId()));
            employee.setPosition(position);
        } else {
            employee.setPosition(null);
        }

        return mapToResponse(employeeRepository.save(employee));
    }

    @Override
    public EmployeeResponse updateStatus(Long id, EmployeeStatus status) {
        Employee employee = findEmployeeById(id);
        employee.setStatus(status);
        return mapToResponse(employeeRepository.save(employee));
    }

    @Override
    public void deleteEmployee(Long id) {
        Employee employee = findEmployeeById(id);
        employeeRepository.delete(employee);
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
