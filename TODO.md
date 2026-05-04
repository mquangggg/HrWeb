# 📌 TODO – Công việc cần triển khai tiếp theo

> **Nguyên tắc:** Làm theo thứ tự từ trên xuống vì các module sau phụ thuộc vào module trước.

---

## ✅ Đã hoàn thành

- [x] Cấu hình Spring Security + JWT
- [x] API Đăng nhập (`POST /api/v1/auth/login`)
- [x] API Đăng ký (`POST /api/v1/auth/register`)
- [x] API Lấy thông tin user (`GET /api/v1/auth/me`)
- [x] Tích hợp Google OAuth2
- [x] GlobalExceptionHandler trả về JSON lỗi
- [x] Giao diện Dashboard (mock data)
- [x] Giao diện Login / Register

---

## 🚀 Giai đoạn 1 – Backend CRUD cơ bản

### 1.1 Phòng ban (Department)
- [ ] Tạo `DepartmentService` interface + `DepartmentServiceImpl`
- [ ] Viết các API trong `DepartmentController`:
  - `GET /api/v1/departments` – Lấy danh sách
  - `GET /api/v1/departments/{id}` – Lấy chi tiết
  - `POST /api/v1/departments` – Thêm mới
  - `PUT /api/v1/departments/{id}` – Cập nhật
  - `DELETE /api/v1/departments/{id}` – Xóa (soft delete)
- [ ] Tạo `DepartmentRequest` DTO và `DepartmentResponse` DTO

### 1.2 Chức vụ (Position)
- [x] Tạo `PositionService` interface + `PositionServiceImpl`
- [x] Viết các API trong `PositionController`:
  - `GET /api/v1/positions` – Lấy danh sách
  - `POST /api/v1/positions` – Thêm mới
  - `PUT /api/v1/positions/{id}` – Cập nhật
  - `DELETE /api/v1/positions/{id}` – Xóa
- [x] Tạo `PositionRequest` DTO và `PositionResponse` DTO

### 1.3 Nhân viên (Employee)
- [ ] Tạo `EmployeeService` interface + `EmployeeServiceImpl`
- [ ] Viết các API trong `EmployeeController`:
  - `GET /api/v1/employees` – Lấy danh sách (có phân trang)
  - `GET /api/v1/employees/{id}` – Lấy chi tiết
  - `POST /api/v1/employees` – Thêm nhân viên mới
  - `PUT /api/v1/employees/{id}` – Cập nhật thông tin
  - `PATCH /api/v1/employees/{id}/status` – Kích hoạt / vô hiệu hóa
- [ ] Tạo `EmployeeRequest` DTO và `EmployeeResponse` DTO
- [ ] Kiểm tra email không trùng lặp khi thêm/sửa

---

## 🔗 Giai đoạn 2 – Kết nối Frontend với API thật

### 2.1 Module Phòng ban
- [ ] Sửa `dashboard.js`: Thay mảng `departments` mock bằng gọi `GET /api/v1/departments`
- [ ] Sửa hàm `saveDept()`: Gọi `POST` / `PUT` thay vì push vào mảng local
- [ ] Sửa nút xóa: Gọi `DELETE /api/v1/departments/{id}`

### 2.2 Module Chức vụ
- [ ] Sửa `dashboard.js`: Thay mảng `positions` mock bằng gọi `GET /api/v1/positions`
- [ ] Sửa hàm `savePosition()`: Gọi API thật
- [ ] Cập nhật dropdown chức vụ trong form nhân viên từ API

### 2.3 Module Nhân viên
- [ ] Sửa `dashboard.js`: Thay mảng `employees` mock bằng gọi `GET /api/v1/employees`
- [ ] Sửa hàm `saveEmployee()`: Gọi `POST` / `PUT` API
- [ ] Sửa nút xóa: Gọi `PATCH /api/v1/employees/{id}/status` (soft delete)
- [ ] Tìm kiếm nhân viên: Gọi API với query param thay vì filter local

---

## ⏰ Giai đoạn 3 – Chấm công (Attendance)

### 3.1 Backend
- [ ] Tạo `AttendanceService` + `AttendanceServiceImpl`
- [ ] Viết API:
  - `POST /api/v1/attendances/check-in` – Chấm công vào
  - `POST /api/v1/attendances/check-out` – Chấm công ra
  - `GET /api/v1/attendances/me` – Lịch sử của tôi
  - `GET /api/v1/attendances` – Tất cả (ADMIN/MANAGER)
- [ ] Kiểm tra không cho check-in 2 lần/ngày
- [ ] Tự động đánh dấu "Đi muộn" nếu check-in sau 8:30

### 3.2 Frontend
- [ ] Nút Check-in gọi `POST /api/v1/attendances/check-in` thật
- [ ] Nút Check-out gọi `POST /api/v1/attendances/check-out` thật
- [ ] Bảng lịch sử chấm công lấy từ `GET /api/v1/attendances/me`
- [ ] Thay số liệu thống kê (96 đúng giờ, 32 đi muộn...) bằng dữ liệu thật

---

## 🌴 Giai đoạn 4 – Nghỉ phép (Leave Request)

### 4.1 Backend
- [ ] Tạo `LeaveRequestService` + `LeaveRequestServiceImpl`
- [ ] Viết API:
  - `POST /api/v1/leave-requests` – Tạo đơn nghỉ
  - `GET /api/v1/leave-requests/me` – Đơn của tôi
  - `GET /api/v1/leave-requests` – Tất cả đơn (ADMIN/MANAGER)
  - `PATCH /api/v1/leave-requests/{id}/approve` – Duyệt
  - `PATCH /api/v1/leave-requests/{id}/reject` – Từ chối
- [ ] Kiểm tra không gửi đơn cho ngày đã qua
- [ ] Kiểm tra chỉ hủy được đơn PENDING

### 4.2 Frontend
- [ ] Form tạo đơn: Gọi `POST /api/v1/leave-requests` thật
- [ ] Bảng nghỉ phép: Lấy từ API thật
- [ ] Nút Duyệt/Từ chối: Gọi API thật
- [ ] Cập nhật badge số đơn chờ duyệt từ API

---

## 💰 Giai đoạn 5 – Tính lương (Payroll)

### 5.1 Backend
- [ ] Tạo `PayrollService` + `PayrollServiceImpl`
- [ ] Viết API:
  - `POST /api/v1/payrolls/calculate` – Tính lương tháng (ADMIN)
  - `GET /api/v1/payrolls/me` – Xem lương của tôi
  - `GET /api/v1/payrolls` – Tất cả bảng lương (ADMIN)
- [ ] Công thức: `Thực nhận = Lương cơ bản + Phụ cấp - Khấu trừ`
- [ ] Không tính lại nếu tháng đó đã có bảng lương

### 5.2 Frontend
- [ ] Nút "Tính lương": Gọi `POST /api/v1/payrolls/calculate` thật
- [ ] Bảng lương: Lấy từ `GET /api/v1/payrolls`
- [ ] Hiển thị số liệu tổng hợp (Tổng lương, Lương bình quân) từ API

---

## 🔑 Giai đoạn 6 – Phân quyền (RBAC)

- [ ] Thêm annotation `@PreAuthorize` vào các API cần giới hạn quyền:
  - ADMIN: toàn quyền
  - MANAGER: chỉ xem/sửa nhân viên trong phòng ban mình
  - EMPLOYEE: chỉ xem thông tin của bản thân
- [ ] Bật `@EnableMethodSecurity` trong `SecurityConfig`
- [ ] Ẩn/hiện các nút trên Dashboard theo role người dùng (frontend)

---

## 🧹 Giai đoạn 7 – Hoàn thiện & Kiểm thử

- [ ] Xóa toàn bộ mock data trong `dashboard.js`
- [ ] Thêm loading spinner khi gọi API (UX)
- [ ] Hiển thị toast notification thành công / thất bại
- [ ] Thêm phân trang cho các bảng dữ liệu lớn
- [ ] Kiểm tra lại toàn bộ luồng: Login → Dashboard → CRUD → Logout
- [ ] Test với tài khoản ADMIN, MANAGER, EMPLOYEE riêng biệt

---

## 📊 Tiến độ tổng thể

```
Authentication  ████████████████████ 100%
Department      ░░░░░░░░░░░░░░░░░░░░   0%
Position        ░░░░░░░░░░░░░░░░░░░░   0%
Employee        ░░░░░░░░░░░░░░░░░░░░   0%
Attendance      ░░░░░░░░░░░░░░░░░░░░   0%
Leave Request   ░░░░░░░░░░░░░░░░░░░░   0%
Payroll         ░░░░░░░░░░░░░░░░░░░░   0%
RBAC            ░░░░░░░░░░░░░░░░░░░░   0%
```

---

*Cập nhật lần cuối: 04/05/2026*
