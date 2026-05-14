package com.hrmanagement.hr_management.service;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService; // Lớp này xử lý lấy thông tin người dùng từ Google
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest; // Lớp này chứa thông tin về yêu cầu xác thực OAuth2
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.hrmanagement.hr_management.entity.Employee;
import com.hrmanagement.hr_management.enums.EmployeeStatus;
import com.hrmanagement.hr_management.repository.EmployeeRepository;
import com.hrmanagement.hr_management.security.CustomOAuth2User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final EmployeeRepository employeeRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String firstName = oAuth2User.getAttribute("given_name");
        String lastName = oAuth2User.getAttribute("family_name");
        
        if (firstName == null) firstName = name;
        if (lastName == null) lastName = "";

        // Tìm nhân viên theo email
        Employee employee = employeeRepository.findByEmail(email).orElse(null);

        if (employee == null) {
            // Tự động tạo tài khoản nếu đăng nhập lần đầu bằng Google
            employee = new Employee();
            employee.setEmail(email);
            employee.setFirstName(firstName);
            employee.setLastName(lastName);
            employee.setRole(com.hrmanagement.hr_management.enums.Role.EMPLOYEE); // ← Mặc định là EMPLOYEE, Admin vào tự đổi
            employee.setStatus(EmployeeStatus.active);
            employee.setPassword("OAuth2_User"); // Password giả cho OAuth2 (không dùng để login thường)
            employee.setStartDate(java.time.LocalDate.now());
            employee = employeeRepository.save(employee);
        }

        // Kiểm tra tài khoản có bị vô hiệu hóa không
        if (employee.getStatus() == EmployeeStatus.inactive) {
            throw new OAuth2AuthenticationException("Tài khoản của bạn đã bị vô hiệu hóa.");
        }

        return new CustomOAuth2User(oAuth2User, email);
    }
}
