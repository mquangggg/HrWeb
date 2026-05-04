# Hướng dẫn chi tiết: Xây dựng Module Chức vụ (Position CRUD)

Để hoàn thiện phần Chức vụ (Position), bạn hãy làm lần lượt theo các bước sau. Lưu ý rằng bảng `positions` trong Database thực tế của bạn chỉ chứa: `id`, `name`, `base_salary`. 

Vì vậy, mình sẽ hướng dẫn bạn code để khớp chính xác với Database.

---

### Bước 1: Tạo các DTO (Data Transfer Object)
Tạo 2 file để truyền và nhận dữ liệu.

**1. `src/main/java/com/hrmanagement/hr_management/dto/request/PositionRequest.java`**
```java
package com.hrmanagement.hr_management.dto.request;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class PositionRequest {
    private String name;
    private BigDecimal baseSalary;
}
```

**2. `src/main/java/com/hrmanagement/hr_management/dto/response/PositionResponse.java`**
```java
package com.hrmanagement.hr_management.dto.response;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PositionResponse {
    private Long id;
    private String name;
    private BigDecimal baseSalary;
}
```

---

### Bước 2: Cập nhật Repository
Mở file `PositionRepository.java` và thêm hàm kiểm tra trùng tên:

```java
package com.hrmanagement.hr_management.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.hrmanagement.hr_management.entity.Position;

public interface PositionRepository extends JpaRepository<Position, Long> {
    boolean existsByName(String name);
}
```

---

### Bước 3: Tạo Service
**1. Interface `PositionService.java`**
```java
package com.hrmanagement.hr_management.service;

import java.util.List;
import com.hrmanagement.hr_management.dto.request.PositionRequest;
import com.hrmanagement.hr_management.dto.response.PositionResponse;

public interface PositionService {
    List<PositionResponse> getAllPositions();
    PositionResponse getPositionById(Long id);
    PositionResponse createPosition(PositionRequest request);
    PositionResponse updatePosition(Long id, PositionRequest request);
    void deletePosition(Long id);
}
```

**2. Class `PositionServiceImpl.java`**
(Nằm trong thư mục `impl`)
```java
package com.hrmanagement.hr_management.service.impl;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import com.hrmanagement.hr_management.dto.request.PositionRequest;
import com.hrmanagement.hr_management.dto.response.PositionResponse;
import com.hrmanagement.hr_management.entity.Position;
import com.hrmanagement.hr_management.repository.PositionRepository;
import com.hrmanagement.hr_management.service.PositionService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PositionServiceImpl implements PositionService {

    private final PositionRepository positionRepository;

    @Override
    public List<PositionResponse> getAllPositions() {
        return positionRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PositionResponse getPositionById(Long id) {
        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chức vụ"));
        return mapToResponse(position);
    }

    @Override
    public PositionResponse createPosition(PositionRequest request) {
        if (positionRepository.existsByName(request.getName())) {
            throw new RuntimeException("Tên chức vụ đã tồn tại!");
        }
        Position position = new Position();
        position.setName(request.getName());
        position.setBaseSalary(request.getBaseSalary());
        return mapToResponse(positionRepository.save(position));
    }

    @Override
    public PositionResponse updatePosition(Long id, PositionRequest request) {
        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chức vụ"));

        if (!position.getName().equalsIgnoreCase(request.getName()) && positionRepository.existsByName(request.getName())) {
            throw new RuntimeException("Tên chức vụ đã tồn tại!");
        }

        position.setName(request.getName());
        position.setBaseSalary(request.getBaseSalary());
        return mapToResponse(positionRepository.save(position));
    }

    @Override
    public void deletePosition(Long id) {
        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chức vụ"));
        positionRepository.delete(position);
    }

    private PositionResponse mapToResponse(Position position) {
        return PositionResponse.builder()
                .id(position.getId())
                .name(position.getName())
                .baseSalary(position.getBaseSalary())
                .build();
    }
}
```

---

### Bước 4: Tạo Controller
Tạo file `PositionController.java` để khai báo các đường dẫn API:

```java
package com.hrmanagement.hr_management.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.hrmanagement.hr_management.dto.request.PositionRequest;
import com.hrmanagement.hr_management.dto.response.PositionResponse;
import com.hrmanagement.hr_management.service.PositionService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/positions")
@RequiredArgsConstructor
public class PositionController {

    private final PositionService positionService;

    @GetMapping
    public ResponseEntity<List<PositionResponse>> getAllPositions() {
        return ResponseEntity.ok(positionService.getAllPositions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PositionResponse> getPositionById(@PathVariable Long id) {
        return ResponseEntity.ok(positionService.getPositionById(id));
    }

    @PostMapping
    public ResponseEntity<PositionResponse> createPosition(@RequestBody PositionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(positionService.createPosition(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PositionResponse> updatePosition(@PathVariable Long id, @RequestBody PositionRequest request) {
        return ResponseEntity.ok(positionService.updatePosition(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePosition(@PathVariable Long id) {
        positionService.deletePosition(id);
        return ResponseEntity.noContent().build();
    }
}
```

---

### Bước 5: Cập nhật giao diện HTML
Mở `dashboard.html`, tìm đến ID `#position-modal`. Thay thế toàn bộ thẻ `<div class="modal-body">...</div>` bằng code sau (để xóa Min/Max/Phòng ban, thay bằng Lương cơ bản):

```html
<div class="modal-body">
    <input type="hidden" id="pos-id">
    <div class="mb-3">
        <label class="form-label fw-medium">Tên chức vụ <span class="text-danger">*</span></label>
        <input type="text" class="form-control" id="pos-name" placeholder="VD: Senior Developer">
    </div>
    <div class="mb-3">
        <label class="form-label fw-medium">Lương cơ bản (VNĐ)</label>
        <input type="number" class="form-control" id="pos-salary" placeholder="VD: 15000000">
    </div>
</div>
```
*Gợi ý: Nhớ sửa cả tiêu đề cột `<th class="ps-3">...</th>` ở khu vực bảng hiển thị danh sách Chức vụ trong `dashboard.html` để khớp với Lương cơ bản nhé.*

---

### Bước 6: Cập nhật JavaScript
Mở `dashboard.js`, thay thế toàn bộ logic quản lý Chức vụ bằng AJAX gọi API (giống hệt cách chúng ta làm với Phòng ban lúc nãy). 
- Xóa biến `let positions = [...]` cứng đi.
- Viết hàm `fetchPositions()` gọi `GET /api/v1/positions`.
- Sửa `savePosition()` để lấy ID từ `#pos-salary` thay vì `#pos-min`, `#pos-max` và gọi `POST` hoặc `PUT`.

👉 **Khi nào bạn copy xong các bước Backend và sửa HTML, nếu cần mình sẽ gửi nốt phần code `dashboard.js` cho bạn nhé!**
