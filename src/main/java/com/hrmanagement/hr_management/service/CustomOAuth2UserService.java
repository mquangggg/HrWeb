package com.hrmanagement.hr_management.service;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService; //Lớp này xử lý lấy thông tin người dùng từ Google
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest; //Lớp này chứa thông tin về yêu cầu xác thực OAuth2
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.hrmanagement.hr_management.entity.Employee;
import com.hrmanagement.hr_management.enums.EmployeeStatus;
import com.hrmanagement.hr_management.enums.Role;
import com.hrmanagement.hr_management.repository.EmployeeRepository;
import com.hrmanagement.hr_management.security.CustomOAuth2User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String firstName = oAuth2User.getAttribute("given_name");
        String lastName = oAuth2User.getAttribute("family_name");
        
        if (firstName == null) firstName = name;
        if (lastName == null) lastName = "";

        Optional<Employee> employeeOptional = employeeRepository.findByEmail(email);
        Employee employee;
        
        if (employeeOptional.isPresent()) {
            employee = employeeOptional.get();
        } else {
            // Đăng ký mới nếu chưa có
            employee = new Employee();
            employee.setEmail(email);
            employee.setFirstName(firstName);
            employee.setLastName(lastName);
            // Tạo password ngẫu nhiên vì login bằng Google
            employee.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            employee.setRole(Role.EMPLOYEE);
            employee.setStatus(EmployeeStatus.active);
            employee.setStartDate(LocalDate.now());
            employeeRepository.save(employee);
        }

        return new CustomOAuth2User(oAuth2User, email);
    }
}
