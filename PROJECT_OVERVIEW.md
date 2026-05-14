# 📑 BÁO CÁO TỔNG QUAN DỰ ÁN QUẢN LÝ NHÂN SỰ (HR MANAGEMENT SYSTEM)

Dự án là một hệ thống quản trị nhân sự toàn diện, được thiết kế để tối ưu hóa quy trình quản lý nhân viên, chấm công, nghỉ phép và tính lương trong doanh nghiệp.

---

## 🚀 1. Công nghệ sử dụng (Technology Stack)
Hệ thống được xây dựng trên những công nghệ hiện đại, đảm bảo tính hiệu năng và khả năng mở rộng:
*   **Backend:** Java 25 (Phiên bản mới nhất), Spring Boot 4.0.5, Spring Data JPA, Hibernate.
*   **Database:** MySQL (Lưu trữ dữ liệu có cấu trúc, đảm bảo tính toàn vẹn dữ liệu).
*   **Security:** Spring Security, JWT (JSON Web Token), OAuth2 (Google Login).
*   **Frontend:** HTML5, Vanilla CSS (Thiết kế hiện đại, responsive), JavaScript (ES6+), Bootstrap 5, jQuery (Xử lý AJAX).

## 🏗 2. Kiến trúc Hệ thống (Architecture)
Hệ thống tuân thủ nghiêm ngặt các nguyên tắc thiết kế phần mềm chuyên nghiệp:
*   **Kiến trúc 3 lớp (3-Layer Architecture):**
    *   **Controller Layer:** Tiếp nhận yêu cầu HTTP và điều hướng.
    *   **Service Layer:** Xử lý nghiệp vụ chính (Business Logic).
    *   **Repository Layer:** Tương tác trực tiếp với Database qua Spring Data JPA.
*   **Mô hình RESTful API:** Cung cấp các Endpoint chuẩn hóa, trả về dữ liệu định dạng JSON, giúp Frontend dễ dàng giao tiếp và xử lý dữ liệu động.
*   **DTO (Data Transfer Object):** Sử dụng các Request/Response DTO để bảo mật cấu trúc bảng dữ liệu bên dưới và tối ưu hóa lượng thông tin truyền tải.

## 🛡 3. Cơ chế Bảo mật & Phân quyền (Security & RBAC)
Hệ thống áp dụng mô hình bảo mật đa tầng mạnh mẽ:
*   **Stateless Authentication với JWT:** Sử dụng Token để xác thực người dùng, giúp hệ thống hoạt động mượt mà mà không cần lưu Session trên Server.
*   **Tích hợp OAuth2 Social Login:** Cho phép người dùng đăng nhập nhanh chóng bằng tài khoản Google.
*   **Phân quyền dựa trên vai trò (RBAC):**
    *   Sử dụng **`@PreAuthorize`** và **`@EnableMethodSecurity`** để kiểm soát quyền truy cập chi tiết đến từng hàm xử lý.
    *   Phân chia rõ rệt 3 vai trò: **ADMIN** (Quản trị hệ thống), **MANAGER** (Quản lý bộ phận), **EMPLOYEE** (Nhân viên).

## 💻 4. Các Module chức năng chính
1.  **Quản lý Nhân viên:** CRUD đầy đủ thông tin nhân viên, hỗ trợ tìm kiếm nâng cao theo tên, phòng ban, chức vụ và phân trang dữ liệu.
2.  **Quản lý Cơ cấu (Department & Position):** Xây dựng sơ đồ tổ chức thông qua quản lý Phòng ban và Chức vụ.
3.  **Hệ thống Chấm công (Attendance):**
    *   Hỗ trợ Check-in/Check-out thời gian thực.
    *   Giao diện Lịch (Calendar View) trực quan để theo dõi lịch sử làm việc.
    *   Tự động thống kê số ngày công, đi muộn, về sớm.
4.  **Hệ thống Nghỉ phép (Leave Request):** Quy trình gửi đơn và duyệt đơn trực tuyến giữa Nhân viên và Quản lý/Admin.
5.  **Module Tính lương (Payroll):** Tự động tính toán lương thực nhận dựa trên ngày công thực tế, lương cơ bản, phụ cấp và các khoản khấu trừ.
6.  **Hệ thống Thông báo:** Đăng tin và cập nhật thông báo nội bộ cho toàn thể nhân viên.

## 📈 5. Đặc điểm nổi bật
*   **Xử lý ngoại lệ tập trung (Global Exception Handling):** Đảm bảo mọi lỗi hệ thống đều được phản hồi dưới dạng JSON thống nhất.
*   **Validation:** Kiểm tra dữ liệu đầu vào nghiêm ngặt (Email duy nhất, định dạng số điện thoại, bắt buộc nhập).
*   **Giao diện Dashboard động:** Hiển thị biểu đồ thống kê, đồng hồ thời gian thực và các chỉ số vận hành quan trọng.

---
*Tài liệu này mô tả trạng thái kỹ thuật hiện tại của dự án HR Management.*
