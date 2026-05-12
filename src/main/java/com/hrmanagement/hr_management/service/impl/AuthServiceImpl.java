package com.hrmanagement.hr_management.service.impl;

import java.time.LocalDate;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.hrmanagement.hr_management.dto.request.LoginRequest;
import com.hrmanagement.hr_management.dto.request.RegisterRequest;
import com.hrmanagement.hr_management.dto.response.AuthResponse;
import com.hrmanagement.hr_management.dto.response.EmployeeResponse;
import com.hrmanagement.hr_management.entity.Employee;
import com.hrmanagement.hr_management.enums.EmployeeStatus;
import com.hrmanagement.hr_management.enums.Role;
import com.hrmanagement.hr_management.repository.EmployeeRepository;
import com.hrmanagement.hr_management.security.JwtTokenProvide;
import com.hrmanagement.hr_management.service.AuthService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvide jwtTokenProvide;
    private final AuthenticationManager authenticationManager;

    @Override
    public AuthResponse login(LoginRequest request) {
        // Xác thực qua AuthenticationManager
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Tạo JWT Token
        String jwt = jwtTokenProvide.generateToken(authentication);

        // Lấy thông tin User
        Employee employee = employeeRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        EmployeeResponse employeeResponse = EmployeeResponse.builder()
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

        return AuthResponse.builder()
                .token(jwt)
                .user(employeeResponse)
                .build();
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        // Kiểm tra email đã tồn tại chưa
        if (employeeRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email is already in use!");
        }

        // Mặc định role là EMPLOYEE khi đăng ký
        Role role = Role.EMPLOYEE;

        // Tạo Employee mới
        Employee employee = new Employee();
        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setEmail(request.getEmail());
        employee.setPassword(passwordEncoder.encode(request.getPassword()));
        employee.setRole(role);
        employee.setStatus(EmployeeStatus.active);
        employee.setStartDate(LocalDate.now());

        Employee savedEmployee = employeeRepository.save(employee);

        // Đăng nhập tự động sau khi đăng ký (Tạo JWT trực tiếp, không lộ password trong memory)
        String jwt = jwtTokenProvide.generateToken(savedEmployee.getEmail());

        EmployeeResponse employeeResponse = EmployeeResponse.builder()
                .id(savedEmployee.getId())
                .firstName(savedEmployee.getFirstName())
                .lastName(savedEmployee.getLastName())
                .email(savedEmployee.getEmail())
                .role(savedEmployee.getRole())
                .status(savedEmployee.getStatus())
                .startDate(savedEmployee.getStartDate())
                .build();

        return AuthResponse.builder()
                .token(jwt)
                .user(employeeResponse)
                .build();
    }

    @Override
    public EmployeeResponse getMe() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

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
