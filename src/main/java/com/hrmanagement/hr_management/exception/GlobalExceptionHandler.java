package com.hrmanagement.hr_management.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// Bắt tất cả exception trong toàn bộ ứng dụng và trả về JSON
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Bắt lỗi sai email hoặc mật khẩu
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentials(BadCredentialsException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("message", "Email hoặc mật khẩu không đúng!");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    // Bắt lỗi không tìm thấy user
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleUserNotFound(UsernameNotFoundException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("message", "Email hoặc mật khẩu không đúng!");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    // Bắt lỗi tài khoản bị vô hiệu hóa
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<Map<String, String>> handleDisabled(DisabledException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("message", "Tài khoản của bạn đã bị vô hiệu hóa. Liên hệ Admin!");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    // Bắt lỗi validation từ @Valid trên các @RequestBody
    // Trả về map {fieldName: errorMessage} để frontend hiển thị đúng chỗ
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(err -> {
            String field = ((FieldError) err).getField();
            String message = err.getDefaultMessage();
            errors.put(field, message);
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    // Bắt RuntimeException chung (vd: Email not found trong AuthServiceImpl)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntime(RuntimeException ex) {
        Map<String, String> error = new HashMap<>();
        // Không để lộ thông tin nội bộ, chỉ trả về thông báo chung
        error.put("message", ex.getMessage() != null ? ex.getMessage() : "Đã xảy ra lỗi, vui lòng thử lại!");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}

