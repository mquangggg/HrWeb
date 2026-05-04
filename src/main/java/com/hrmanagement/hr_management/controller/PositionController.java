package com.hrmanagement.hr_management.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable; // @PathVariable: Lấy tham số từ URL
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hrmanagement.hr_management.dto.request.PositionRequest;
import com.hrmanagement.hr_management.dto.response.PositionResponse;
import com.hrmanagement.hr_management.service.PositionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/v1/positions")
@RequiredArgsConstructor
public class PositionController {
    private final PositionService positionService;

    @GetMapping()
    public ResponseEntity<List<PositionResponse>> getAllPositions() {
        return ResponseEntity.ok(positionService.getAllPositions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PositionResponse> getPositionById(@PathVariable Long id) {
        return ResponseEntity.ok(positionService.getPositionById(id));
    }

    @PostMapping()
    public ResponseEntity<PositionResponse> createPosition(@RequestBody PositionRequest request) {
        return ResponseEntity.ok(positionService.createPosition(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PositionResponse> updatePosition(@PathVariable Long id, @RequestBody PositionRequest request) {
        return ResponseEntity.ok(positionService.updatePosition(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<PositionResponse> deletePosition(@PathVariable Long id) {
        positionService.deletePosition(id);
        return ResponseEntity.noContent().build();
    }

}