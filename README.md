# 📋 README – HR Management System

**Version:** 1.0 | **Stack:** Spring Boot 4.x · MySQL 8 · Vanilla JS

---

## 🚀 Tổng quan

Hệ thống quản lý nhân sự tích hợp các chức năng: đăng nhập, quản lý nhân viên, chấm công, nghỉ phép và tính lương. Phân quyền theo 3 vai trò: **ADMIN**, **MANAGER**, **EMPLOYEE**.

---

## 📁 Cấu trúc thư mục

```
src/main/
├── java/com/hrmanagement/hr_management/
│   ├── config/          → SecurityConfig, JwtConfig, CorsConfig
│   ├── controller/      → REST API controllers
│   ├── dto/
│   │   ├── request/     → DTO nhận input từ client
│   │   └── response/    → DTO trả về client
│   ├── entity/          → JPA Entities
│   ├── enums/           → Enums (Gender, Role, Status...)
│   ├── exception/       → GlobalExceptionHandler
│   ├── repository/      → JPA Repositories
│   ├── security/        → JWT filter, UserDetailsService
│   ├── service/
│   │   └── impl/        → Service implementations
│   └── util/            → Helper classes
└── resources/
    ├── application.properties
    └── static/
        ├── pages/       → HTML pages
        ├── css/         → Stylesheets
        ├── js/          → JavaScript
        └── images/      → Assets
```

---

## ⚙️ Cấu hình & Khởi chạy

```properties
# application.properties
spring.datasource.url=jdbc:mysql://localhost:3306/db_hr_management

server.port=8082
```

```bash
# Chạy project
./mvnw spring-boot:run
```

---

## 🔐 UC-01: Đăng nhập hệ thống

### Mô tả
Người dùng (ADMIN / MANAGER / EMPLOYEE) đăng nhập bằng email và mật khẩu. Hệ thống xác thực và trả về JWT token.

### Công nghệ
- Spring Security + JWT
- Axios (frontend call API)

### API
```
POST /api/v1/auth/login
POST /api/v1/auth/register
POST /api/v1/auth/logout
```

### Input / Output
| Trường | Type | Mô tả |
|--------|------|--------|
| email | String | Email đăng nhập |
| password | String | Mật khẩu |

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": { "id": 1, "name": "Nguyễn Văn A", "role": "ADMIN" }
}
```

### Luồng hoạt động
1. User nhập email/password → Frontend gửi POST `/api/v1/auth/login`
2. Backend xác thực credentials
3. Trả về JWT token (24h)
4. Frontend lưu token vào `localStorage`
5. Redirect dashboard theo role

### Lỗi thường gặp
| Mã lỗi | Mô tả |
|--------|-------|
| 401 | Sai tài khoản/mật khẩu |
| 403 | Tài khoản đã bị vô hiệu hóa |
| 400 | Thiếu dữ liệu input |

---

## 👤 UC-02: Quản lý nhân viên (CRUD)

### Mô tả
ADMIN và MANAGER quản lý thông tin nhân viên trong hệ thống.

### API
```
GET    /api/v1/employees           → Danh sách nhân viên
GET    /api/v1/employees/{id}      → Chi tiết nhân viên
POST   /api/v1/employees           → Thêm nhân viên mới
PUT    /api/v1/employees/{id}      → Cập nhật thông tin
PATCH  /api/v1/employees/{id}/status → Kích hoạt/vô hiệu hóa
```

### Dữ liệu chính
| Trường | Type | Mô tả |
|--------|------|--------|
| full_name | String | Họ và tên |
| email | String | Email (unique) |
| phone | String | Số điện thoại |
| department_id | Long | Mã phòng ban |
| position_id | Long | Mã chức vụ |
| role | Enum | ADMIN / MANAGER / EMPLOYEE |
| base_salary | Decimal | Lương cơ bản |

### Rule
- ✅ Email không được trùng lặp
- ✅ Không xóa cứng – chỉ **soft delete** (status = INACTIVE)
- ✅ ADMIN: toàn quyền | MANAGER: chỉ phòng ban mình

---

## 🕐 UC-03: Chấm công

### Mô tả
Nhân viên check-in / check-out để hệ thống ghi nhận thời gian làm việc.

### API
```
POST /api/v1/attendances/check-in    → Chấm công vào
POST /api/v1/attendances/check-out   → Chấm công ra
GET  /api/v1/attendances/me          → Lịch sử chấm công
GET  /api/v1/attendances/report      → Báo cáo tổng hợp (ADMIN/MANAGER)
```

### Luồng
```
Check-in:  Nhấn nút → Lưu check_in = NOW()
Check-out: Nhấn nút → Lưu check_out = NOW() → Tính số giờ làm
```

### Rule
- ✅ Không được check-in 2 lần/ngày
- ✅ IP phải nằm trong whitelist (nếu bật)
- ✅ **LATE** nếu check-in sau giờ quy định

---

## 🌴 UC-04: Xin nghỉ phép

### Mô tả
Nhân viên gửi đơn nghỉ phép, Manager/Admin duyệt hoặc từ chối.

### API
```
POST   /api/v1/leave-requests                → Tạo đơn nghỉ
GET    /api/v1/leave-requests/me             → Đơn của tôi
GET    /api/v1/leave-requests                → Tất cả đơn (ADMIN/MANAGER)
PATCH  /api/v1/leave-requests/{id}/approve   → Duyệt đơn
PATCH  /api/v1/leave-requests/{id}/reject    → Từ chối đơn
```

### Trạng thái đơn
```
PENDING → (APPROVED | REJECTED)
```

### Rule
- ✅ Không vượt quá số ngày phép được cấp
- ✅ Không gửi đơn cho ngày đã qua
- ✅ Chỉ hủy được đơn khi còn ở trạng thái PENDING

---

## 💰 UC-05: Tính lương

### Mô tả
Hệ thống tự động tính lương dựa trên dữ liệu chấm công và nghỉ phép theo tháng.

### API
```
POST /api/v1/payrolls/calculate    → Tính lương (ADMIN)
GET  /api/v1/payrolls/me           → Xem lương của tôi
GET  /api/v1/payrolls              → Tất cả bảng lương (ADMIN)
```

### Công thức tính lương
```
Lương thực nhận = Lương cơ bản + Phụ cấp - Khấu trừ

Khấu trừ = Số ngày vắng × (Lương cơ bản / Số ngày làm việc trong tháng)
```

### Rule
- ✅ Không tính lại nếu payroll tháng đó đã tồn tại (trừ khi có flag `override=true`)
- ✅ Nếu thiếu dữ liệu chấm công → lương = 0

---

## 🔑 Phân quyền (RBAC)

| Chức năng | ADMIN | MANAGER | EMPLOYEE |
|-----------|-------|---------|----------|
| Xem nhân viên | ✅ Tất cả | ✅ Phòng ban mình | ❌ |
| CRUD nhân viên | ✅ | ✅ Phòng ban mình | ❌ |
| Xem chấm công | ✅ | ✅ Phòng ban mình | ✅ Của mình |
| Xem nghỉ phép | ✅ | ✅ Phòng ban mình | ✅ Của mình |
| Duyệt nghỉ phép | ✅ | ✅ | ❌ |
| Tính lương | ✅ | ❌ | ❌ |
| Xem lương | ✅ | ✅ Phòng ban | ✅ Của mình |

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Spring Boot 4.x |
| Security | Spring Security + JWT |
| ORM | JPA + Hibernate |
| Database | MySQL 8 |
| Frontend | HTML / CSS / Vanilla JS |
| Build tool | Maven |

---

## 📌 Ghi chú

- JWT token hết hạn sau **24 giờ**
- Mọi request (trừ login/register) phải có header: `Authorization: Bearer <token>`
- API prefix: `/api/v1/`

---
*HR Management System – Internal Use Only*
