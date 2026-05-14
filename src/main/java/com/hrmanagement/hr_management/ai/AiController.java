package com.hrmanagement.hr_management.ai;

import java.security.Principal;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hrmanagement.hr_management.dto.request.ChatRequest;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @PostMapping("/chat")
    public ResponseEntity<?> chat(@RequestBody ChatRequest request, Principal principal) {
        if (request.getMessage() == null || request.getMessage().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Tin nhắn không được để trống"));
        }
        String email = principal.getName();
        String reply = aiService.chat(request.getMessage(), request.getHistory(), email);
        return ResponseEntity.ok(Map.of("reply", reply));
    }
}
