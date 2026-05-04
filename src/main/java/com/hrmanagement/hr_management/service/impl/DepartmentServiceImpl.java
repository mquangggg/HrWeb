package com.hrmanagement.hr_management.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.hrmanagement.hr_management.dto.request.DepartmentRequest;
import com.hrmanagement.hr_management.dto.response.DepartmentResponse;
import com.hrmanagement.hr_management.entity.Department;
import com.hrmanagement.hr_management.entity.Employee;
import com.hrmanagement.hr_management.repository.DepartmentRepository;
import com.hrmanagement.hr_management.repository.EmployeeRepository;
import com.hrmanagement.hr_management.service.DepartmentService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    public List<DepartmentResponse> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public DepartmentResponse getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng ban với ID: " + id));
        return mapToResponse(department);
    }

    @Override
    public DepartmentResponse createDepartment(DepartmentRequest request) {
        if (departmentRepository.existsByName(request.getName())) {
            throw new RuntimeException("Tên phòng ban đã tồn tại!");
        }

        Department department = new Department();
        department.setName(request.getName());
        department.setDescription(request.getDescription());

        // Gán quản lý nếu có truyền ID
        if (request.getManagerId() != null) {
            Employee manager = employeeRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên quản lý"));
            department.setManager(manager);
        }

        Department savedDepartment = departmentRepository.save(department);
        return mapToResponse(savedDepartment);
    }

    @Override
    public DepartmentResponse updateDepartment(Long id, DepartmentRequest request) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng ban với ID: " + id));

        // Kiểm tra tên trùng lặp (nếu đổi tên mới)
        if (!department.getName().toLowerCase().equals(request.getName().toLowerCase()) && departmentRepository.existsByName(request.getName())) {
            throw new RuntimeException("Tên phòng ban đã tồn tại!");
        }

        department.setName(request.getName());
        department.setDescription(request.getDescription());

        // Cập nhật người quản lý
        if (request.getManagerId() != null) {
            Employee manager = employeeRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên quản lý"));
            department.setManager(manager);
        } else {
            department.setManager(null); // Gỡ quản lý nếu không truyền
        }

        Department updatedDepartment = departmentRepository.save(department);
        return mapToResponse(updatedDepartment);
    }

    @Override
    public void deleteDepartment(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng ban với ID: " + id));
        
        // Cần lưu ý nếu phòng ban đang có nhân viên thì DB sẽ văng lỗi ConstraintViolationException.
        // Tạm thời mình cứ xóa thẳng, GlobalExceptionHandler sẽ bắt nếu có lỗi.
        departmentRepository.delete(department);
    }

    // Hàm tiện ích để map Entity sang DTO
    private DepartmentResponse mapToResponse(Department department) {
        return DepartmentResponse.builder()
                .id(department.getId())
                .name(department.getName())
                .description(department.getDescription())
                .managerId(department.getManager() != null ? department.getManager().getId() : null)
                .managerName(department.getManager() != null ? 
                        department.getManager().getFirstName() + " " + department.getManager().getLastName() : null)
                .createdAt(department.getCreatedAt())
                .build();
    }
}
