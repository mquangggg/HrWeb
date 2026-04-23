package com.hrmanagement.hr_management.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // Nếu data là null thì không in ra JSON
public class ApiResponse<T> {
    // Class này dùng để trả về dữ liệu cho client
    
    @Builder.Default
    private int code = 200; // 200 là thành công
    
    private String message;
    
    private T data; // Dữ liệu trả về (có thể là EmployeeResponse, List<...>, v.v.)
}
