// ===================================================
//  DỮ LIỆU MẪU (mock data – sẽ thay bằng API sau)
// ===================================================
let employees = []; // Dữ liệu thật sẽ được load từ API

// Trạng thái phân trang nhân viên
let empPagination = { page: 0, size: 10, totalPages: 0, totalElements: 0 };
let empKeyword = '';
let empDeptId  = null;
let empPosId   = null;

let departments = []; // Dữ liệu thật sẽ được load từ API

let positions = []; // Dữ liệu thật sẽ được load từ API

let attendances = [];


let leaveRequests = [];


// ===================================================
//  ĐIỀU HƯỚNG – HIỆN / ẨN SECTION
// ===================================================
// Map tên section → tiêu đề hiển thị trên header
const sectionTitles = {
    dashboard:  'Dashboard',
    employee:   'Quản lý nhân viên',
    department: 'Quản lý phòng ban',
    position:   'Quản lý chức vụ',
    attendance: 'Chấm công',
    leave:      'Nghỉ phép',
    payroll:    'Bảng lương',
};

function showSection(name) {
    const token = localStorage.getItem('token');
    // Kiểm tra token ngầm với Server trước khi chuyển mục
    $.ajax({
        url: '/api/v1/auth/me',
        method: 'GET',
        headers: { 'Authorization': 'Bearer ' + token },
        success: function() {
            // Token còn hạn -> Chuyển mục mượt mà
            showSectionInternal(name);
            fetchDataForSection(name);
        },
        error: function(xhr) {
            // Token hết hạn hoặc lỗi -> Đá ra trang login
            if (xhr.status === 401 || xhr.status === 403 || xhr.status === 400) {
                handleLogout(false);
            } else {
                alert('Không thể kết nối tới máy chủ. Vui lòng thử lại sau.');
            }
        }
    });
}

function showSectionInternal(name) {
    // Lưu vào localStorage để ghi nhớ
    localStorage.setItem('active_section', name);

    // Cập nhật URL mà không reload trang (để F5 vẫn đúng chỗ)
    const newUrl = window.location.pathname + '?section=' + name;
    window.history.pushState({ section: name }, '', newUrl);

    // Ẩn tất cả sections
    $('.section').removeClass('active');
    // Hiện section được chọn
    $('#section-' + name).addClass('active');

    // Bỏ active tất cả nav links, active cái đang chọn
    $('.nav-item-link').removeClass('active');
    $('#nav-' + name).addClass('active');

    // Cập nhật tiêu đề header
    $('#header-title').text(sectionTitles[name] || name);
}

// Hàm bổ trợ để tải dữ liệu riêng cho từng section khi cần
function fetchDataForSection(name) {
    if (name === 'payroll') fetchPayrolls();
    if (name === 'dashboard') fetchDashboardStats();
    if (name === 'employee') fetchEmployees(0);
    if (name === 'department') fetchDepartments();
    if (name === 'position') fetchPositions();
    if (name === 'attendance') initAttendanceCalendar();
    if (name === 'leave') fetchLeaveRequests(0);
}

// ===================================================
//  ĐỒNG HỒ REALTIME
// ===================================================
function updateClock() {
    const now  = new Date();
    const pad  = n => String(n).padStart(2, '0');
    const days = ['Chủ nhật','Thứ hai','Thứ ba','Thứ tư','Thứ năm','Thứ sáu','Thứ bảy'];
    const time = `${pad(now.getHours())}:${pad(now.getMinutes())}:${pad(now.getSeconds())}`;
    const date = `${days[now.getDay()]}, ${pad(now.getDate())}/${pad(now.getMonth()+1)}/${now.getFullYear()}`;

    $('#clock').text(time);
    $('#header-date').text(date);
    $('#att-today-date').text(date);
    
    // Cập nhật banner chào mừng (chỉ cập nhật một lần hoặc định kỳ)
    if ($('#banner-date').text() === '...') {
        $('#banner-date').text(date);
        $('#banner-month-year').text(`Tháng ${pad(now.getMonth()+1)}, ${now.getFullYear()}`);
    }
}
updateClock();
setInterval(updateClock, 1000); // cập nhật mỗi giây

// ===================================================
//  LOAD THÔNG TIN USER TỪ LOCALSTORAGE VÀ URL
// ===================================================
function loadUserInfo() {
    // 1. Xác định section ngay lập tức để tránh bị nháy UI
    const urlParams = new URLSearchParams(window.location.search);
    const savedSection = localStorage.getItem('active_section');
    const section = urlParams.get('section') || savedSection || 'dashboard';
    showSectionInternal(section);

    // 2. Xử lý Token từ URL (Google Login)
    const urlToken = urlParams.get('token');
    if (urlToken) {
        localStorage.setItem('token', urlToken);
        window.history.replaceState({}, document.title, window.location.pathname);
    }

    const token = localStorage.getItem('token');
    if (!token) {
        window.location.href = '/pages/login.html';
        return;
    }

    // Gọi API lấy thông tin người dùng hiện tại
    $.ajax({
        url: '/api/v1/auth/me',
        method: 'GET',
        headers: { 'Authorization': 'Bearer ' + token },
        success: function(user) {
            // Lưu lại thông tin mới nhất
            localStorage.setItem('user', JSON.stringify(user));
            renderUserToDOM(user);
            applyRolePermissions(user.role);
            
            // CHỈ bắt đầu load dữ liệu khi đã xác thực user thành công
            fetchInitialData(user);
            
            // Tải dữ liệu riêng cho section hiện tại
            const section = localStorage.getItem('active_section') || 'dashboard';
            fetchDataForSection(section);
        },
        error: function(xhr) {
            if (xhr.status === 401 || xhr.status === 400 || xhr.status === 403) {
                console.warn('Phiên làm việc không hợp lệ.');
                handleLogout(false);
            } else {
                console.error('Không thể kết nối server, dùng dữ liệu cũ nếu có');
                const userStr = localStorage.getItem('user');
                if (userStr) {
                    const user = JSON.parse(userStr);
                    renderUserToDOM(user);
                    applyRolePermissions(user.role);
                }
            }
        }
    });
}

// Hàm tập hợp các lệnh load dữ liệu ban đầu để tránh bị rối và tối ưu tốc độ
function fetchInitialData(user) {
    // Set mặc định tháng hiện tại cho ô chọn tháng bảng lương
    const now = new Date();
    const currentMonth = now.getFullYear() + '-' + String(now.getMonth() + 1).padStart(2, '0');
    $('#payroll-month').val(currentMonth);

    fetchNotifications(); 
    if (user.role === 'ADMIN') {
        fetchDashboardStats();
    }
    initAttendanceCalendar(); // Khởi tạo lịch chấm công
    fetchEmployees(0);
    fetchDepartments();
    fetchPositions();
    fetchLeaveRequests(0);
}

function applyRolePermissions(role) {
    console.log('Applying permissions for role:', role);
    
    // Luôn hiện tất cả menu để nhân viên có thể vào xem (Read-only)
    $('#nav-employee, #nav-department, #nav-position, #nav-payroll').show();
    $('.nav-section-label').show();

    if (role === 'EMPLOYEE') {
        // Chỉ ẩn các nút hành động quản trị (Thêm, Sửa, Xóa, Tính lương, Duyệt)
        $('.admin-only').hide();
    } else if (role === 'MANAGER') {
        // Manager có thể duyệt phép và quản lý nhân viên cấp dưới,
        // NHƯNG không có quyền xem/tính lương tổng, không chỉnh sửa phòng ban/chức vụ.
        $('#section-dashboard .admin-only').hide(); // Thống kê, thông báo
        $('#section-department .admin-only').hide();
        $('#section-position .admin-only').hide();
        $('#section-payroll .admin-only').hide(); // Tính lương, Tổng hợp lương
        $('#section-employee .btn-indigo.admin-only').hide(); // Nút Thêm nhân viên
    }
}

function renderUserToDOM(user) {
    // Xử lý tên: Ưu tiên firstName và lastName từ API
    let fullName = '';
    if (user.firstName || user.lastName) {
        fullName = `${user.firstName || ''} ${user.lastName || ''}`.trim();
    } else {
        fullName = user.name || 'Người dùng';
    }
    
    const role = user.role || 'EMPLOYEE';

    // Lấy 2 chữ cái đầu làm avatar chữ (ví dụ: Trần Bình -> TB)
    const words = fullName.split(' ');
    let initials = '';
    if (words.length >= 2) {
        initials = (words[0].charAt(0) + words[words.length - 1].charAt(0)).toUpperCase();
    } else if (words.length === 1 && words[0].length >= 1) {
        initials = words[0].substring(0, 2).toUpperCase();
    } else {
        initials = 'NV';
    }

    $('#sidebar-avatar').text(initials);
    $('#sidebar-name').text(fullName);
    $('#sidebar-role').text(role);
    
    // Tên hiển thị ở banner chào mừng (Lấy từ cuối cùng)
    $('#welcome-name').text(words.pop() || fullName);
}

loadUserInfo();

// ===================================================
//  ĐĂNG XUẤT
// ===================================================
function handleLogout(confirmNeeded = true) {
    if (confirmNeeded && !confirm('Bạn có chắc muốn đăng xuất không?')) return;
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    localStorage.removeItem('active_section');
    const msg = !confirmNeeded ? '?error=session_expired' : '';
    window.location.href = '/pages/login.html' + msg;
}

function showToast(message, type = 'success') {
    const toastEl = $('#liveToast');
    const header = toastEl.find('.toast-header');
    const icon = $('#toast-icon');
    
    // Set màu sắc theo loại
    if (type === 'success') {
        header.css('background-color', '#d1e7dd').css('color', '#0f5132');
        icon.attr('class', 'fas fa-check-circle me-2');
    } else if (type === 'danger') {
        header.css('background-color', '#f8d7da').css('color', '#842029');
        icon.attr('class', 'fas fa-exclamation-circle me-2');
    } else {
        header.css('background-color', '#e2e3e5').css('color', '#41464b');
        icon.attr('class', 'fas fa-info-circle me-2');
    }
    
    $('#toast-body').text(message);
    const toast = new bootstrap.Toast(toastEl[0]);
    toast.show();
}

// ===================================================
//  HELPER: FORMAT TIỀN VIỆT NAM
// ===================================================
function formatVND(n) {
    return Number(n).toLocaleString('vi-VN') + ' ₫';
}

// ===================================================
//  HELPER: BADGE ROLE
// ===================================================
function roleBadge(role) {
    const colors = { ADMIN:'primary', MANAGER:'purple', EMPLOYEE:'info' };
    const color  = colors[role] || 'secondary';
    return `<span class="badge bg-${color}">${role}</span>`;
}

// ===================================================
//  HELPER: BADGE TRẠNG THÁI
// ===================================================
function statusBadge(status) {
    if (status === 'active')   return '<span class="badge bg-success">Hoạt động</span>';
    if (status === 'inactive') return '<span class="badge bg-danger">Vô hiệu hóa</span>';
    if (status === 'PENDING' || status === 'pending')  return '<span class="badge bg-warning text-dark">Chờ duyệt</span>';
    if (status === 'APPROVED' || status === 'approved') return '<span class="badge bg-success">Đã duyệt</span>';
    if (status === 'REJECTED' || status === 'rejected') return '<span class="badge bg-danger">Từ chối</span>';
    return `<span class="badge bg-secondary">${status}</span>`;
}

// ===================================================
//  AVATAR MÀU NGẪU NHIÊN
// ===================================================
function empAvatar(emp) {
    const initials = emp.name.split(' ').slice(-2).map(w => w[0]).join('').toUpperCase();
    return `<span class="emp-avatar me-2" style="background:${emp.color}">${initials}</span>`;
}

// ===================================================
//  AVATAR nhân viên – dùng initials từ firstName + lastName
// ===================================================
//  DASHBOARD – Thống kê (Stats)
// ===================================================
function fetchDashboardStats() {
    const token = localStorage.getItem('token');
    $.ajax({
        url: '/api/v1/dashboard/stats',
        method: 'GET',
        headers: { 'Authorization': 'Bearer ' + token },
        success: function(res) {
            const stats = res.data;
            const now = new Date();
            const month = now.getMonth() + 1;

            $('#stat-total-employees').text(stats.totalEmployees);
            $('#stat-present-today').text(stats.presentToday);
            $('#stat-on-leave').text(stats.onLeaveToday);
            $('#stat-total-salary').text(formatVND(stats.totalSalaryCurrentMonth));
            $('#stat-payroll-label').text(`Lương tháng ${month < 10 ? '0' + month : month}`);
        },
        error: function(xhr) {
            if (xhr.status === 401) handleLogout(false);
            console.error('Lỗi lấy thống kê dashboard:', xhr);
        }
    });
}

// ===================================================
function empAvatarFromApi(emp) {
    const colors = ['#6366f1','#8b5cf6','#06b6d4','#10b981','#f59e0b','#ec4899','#ef4444','#3b82f6'];
    const idx = (emp.id || 0) % colors.length;
    const initials = ((emp.firstName || '').charAt(0) + (emp.lastName || '').charAt(0)).toUpperCase() || 'NV';
    return `<span class="emp-avatar me-2" style="background:${colors[idx]}">${initials}</span>`;
}

// ===================================================
//  DASHBOARD – Thông báo (Notifications)
// ===================================================
function fetchNotifications() {
    const token = localStorage.getItem('token');
    $.ajax({
        url: '/api/v1/notifications',
        method: 'GET',
        headers: { 'Authorization': 'Bearer ' + token },
        success: function(res) {
            renderNotifications(res.data);
        },
        error: function(xhr) {
            if (xhr.status === 401) handleLogout(false);
            console.error('Lỗi lấy thông báo:', xhr);
        }
    });
}

function renderNotifications(list) {
    const user = JSON.parse(localStorage.getItem('user'));
    const isAdmin = user?.role === 'ADMIN';

    let html = '';
    list.forEach(function(n) {
        const date = new Date(n.publishedDate).toLocaleString('vi-VN', { 
            hour: '2-digit', minute: '2-digit', day: '2-digit', month: '2-digit' 
        });

        const iconMap = {
            info: 'info-circle text-info',
            success: 'check-circle text-success',
            warning: 'exclamation-triangle text-warning',
            danger: 'exclamation-circle text-danger'
        };
        const icon = iconMap[n.type] || iconMap.info;

        html += `
        <div class="d-flex align-items-start mb-3 border-bottom pb-3 notification-item">
            <div class="me-3 mt-1">
                <i class="fas fa-${icon} fa-lg"></i>
            </div>
            <div style="flex:1">
                <div class="d-flex justify-content-between align-items-center mb-1">
                    <h6 class="fw-bold mb-0" style="font-size:14px">${n.title}</h6>
                    <small class="text-muted" style="font-size:11px">${date}</small>
                </div>
                <p class="mb-1 text-muted" style="font-size:13px">${n.content}</p>
                <div class="d-flex justify-content-between align-items-center">
                    <small class="text-primary" style="font-size:11px">
                        <i class="fas fa-user-edit me-1"></i> ${n.publishedBy}
                    </small>
                    ${isAdmin ? `
                    <button class="btn btn-link btn-sm text-danger p-0" onclick="deleteNotification(${n.id})" title="Xóa thông báo">
                        <i class="fas fa-trash-alt" style="font-size:12px"></i>
                    </button>` : ''}
                </div>
            </div>
        </div>`;
    });
    $('#notification-feed').html(html || '<div class="text-center text-muted py-4">Chưa có thông báo nào</div>');
}

function openNotificationModal() {
    $('#notif-title, #notif-content').val('');
    $('#notif-type').val('info');
    new bootstrap.Modal($('#notification-modal')[0]).show();
}

function saveNotification() {
    const title = $('#notif-title').val().trim();
    const content = $('#notif-content').val().trim();
    const type = $('#notif-type').val();

    if (!title || !content) {
        alert('Vui lòng nhập đầy đủ tiêu đề và nội dung!');
        return;
    }

    const token = localStorage.getItem('token');
    $.ajax({
        url: '/api/v1/notifications',
        method: 'POST',
        headers: { 'Authorization': 'Bearer ' + token },
        contentType: 'application/json',
        data: JSON.stringify({ title, content, type }),
        success: function() {
            bootstrap.Modal.getInstance($('#notification-modal')[0]).hide();
            fetchNotifications();
        },
        error: function(xhr) {
            alert(xhr.responseJSON?.message || 'Lỗi khi đăng thông báo');
        }
    });
}

function deleteNotification(id) {
    if (!confirm('Bạn có chắc muốn xóa thông báo này?')) return;
    const token = localStorage.getItem('token');
    $.ajax({
        url: '/api/v1/notifications/' + id,
        method: 'DELETE',
        headers: { 'Authorization': 'Bearer ' + token },
        success: function() {
            fetchNotifications();
        },
        error: function(xhr) {
            alert(xhr.responseJSON?.message || 'Lỗi khi xóa thông báo');
        }
    });
}

// ===================================================
//  NHÂN VIÊN – Fetch từ API (có phân trang & tìm kiếm)
// ===================================================
// Biến trạng thái lọc (đã khai báo ở đầu file)

function fetchEmployees(page) {
    if (page !== undefined) empPagination.page = page;
    const token = localStorage.getItem('token');
    let url = `/api/v1/employees?page=${empPagination.page}&size=${empPagination.size}`;
    if (empKeyword) url += `&keyword=${encodeURIComponent(empKeyword)}`;
    if (empDeptId)  url += `&departmentId=${empDeptId}`;
    if (empPosId)   url += `&positionId=${empPosId}`;

    $.ajax({
        url: url,
        method: 'GET',
        headers: { 'Authorization': 'Bearer ' + token },
        success: function(pageData) {
            employees = pageData.data;
            empPagination.totalPages    = pageData.totalPages;
            empPagination.totalElements = pageData.totalElements;
            renderEmployees();
            renderEmpPagination();
        },
        error: function(xhr) {
            if (xhr.status === 401) handleLogout(false);
            console.error('Lỗi lấy danh sách nhân viên:', xhr);
        }
    });
}

// ===================================================
//  NHÂN VIÊN – Render bảng
// ===================================================
function renderEmployees() {
    let html = '';
    const user = JSON.parse(localStorage.getItem('user'));
    const role = user?.role || 'EMPLOYEE';

    employees.forEach(function(emp, i) {
        const fullName = `${emp.firstName || ''} ${emp.lastName || ''}`.trim();
        const rowNum   = empPagination.page * empPagination.size + i + 1;

        html += `<tr>
            <td class="ps-3 text-muted">${rowNum}</td>
            <td>
                <div class="d-flex align-items-center">
                    ${empAvatarFromApi(emp)}
                    <div>
                        <div class="fw-medium" style="font-size:13px">${fullName}</div>
                        <div class="text-muted" style="font-size:11px">${emp.email}</div>
                    </div>
                </div>
            </td>
            <td style="font-size:13px">${emp.phone || '—'}</td>
            <td style="font-size:13px">${emp.departmentName || '—'}</td>
            <td style="font-size:13px">${emp.positionName || '—'}</td>
            <td>${roleBadge(emp.role)}</td>
            <td style="font-size:13px">${role === 'ADMIN' ? formatVND(emp.baseSalary || 0) : '***'}</td>
            <td>${statusBadge(emp.status)}</td>
            ${role !== 'EMPLOYEE' ? `
            <td class="text-center admin-only">
                ${(role === 'ADMIN' || (role === 'MANAGER' && emp.departmentId === user.departmentId)) ? `
                <button class="btn btn-sm btn-outline-primary me-1" onclick="openEmpModal(${emp.id})">
                    <i class="fas fa-pen"></i>
                </button>
                <button class="btn btn-sm btn-outline-danger" onclick="deleteRow('employee', ${emp.id})">
                    <i class="fas fa-trash"></i>
                </button>
                ` : '—'}
            </td>` : ''}
        </tr>`;
    });
    $('#employee-tbody').html(html || '<tr><td colspan="9" class="text-center text-muted py-4">Không có dữ liệu</td></tr>');
}

// ===================================================
//  NHÂN VIÊN – Render phân trang
// ===================================================
function renderEmpPagination() {
    const total = empPagination.totalPages;
    const cur   = empPagination.page;
    if (total <= 1) { $('#emp-pagination').html(''); return; }

    let html = `<nav><ul class="pagination pagination-sm mb-0">`;
    html += `<li class="page-item ${cur === 0 ? 'disabled' : ''}">
        <a class="page-link" href="#" onclick="fetchEmployees(${cur - 1}); return false;">«</a></li>`;
    for (let i = 0; i < total; i++) {
        html += `<li class="page-item ${i === cur ? 'active' : ''}">
            <a class="page-link" href="#" onclick="fetchEmployees(${i}); return false;">${i + 1}</a></li>`;
    }
    html += `<li class="page-item ${cur === total - 1 ? 'disabled' : ''}">
        <a class="page-link" href="#" onclick="fetchEmployees(${cur + 1}); return false;">»</a></li>`;
    html += `</ul></nav>`;
    $('#emp-pagination').html(html);
}

// Tìm kiếm nhân viên → gọi API
// Lọc nhanh theo phòng ban
function filterByDept(id, name) {
    empDeptId = id;
    empPosId  = null; // Xóa lọc chức vụ nếu đang có
    showSection('employee');
    $('#emp-filter-info').show();
    $('#emp-filter-name').text('Phòng ban: ' + name);
    fetchEmployees(0);
}

// Lọc nhanh theo chức vụ
function filterByPos(id, name) {
    empPosId  = id;
    empDeptId = null; // Xóa lọc phòng ban nếu đang có
    showSection('employee');
    $('#emp-filter-info').show();
    $('#emp-filter-name').text('Chức vụ: ' + name);
    fetchEmployees(0);
}

// Xóa lọc
function clearEmpFilter() {
    empDeptId = null;
    empPosId  = null;
    empKeyword = '';
    $('#emp-search').val('');
    $('#emp-filter-info').hide();
    fetchEmployees(0);
}

function filterEmployees() {
    empKeyword = $('#emp-search').val().trim();
    fetchEmployees(0);
}

// fetchEmployees(); // Đã chuyển vào fetchInitialData

// ===================================================
//  NHÂN VIÊN – Mở modal thêm / sửa
// ===================================================
function openEmpModal(id) {
    // Reset form
    $('#emp-id').val('');
    $('#emp-firstname, #emp-lastname, #emp-email, #emp-password, #emp-phone, #emp-salary, #emp-allowance').val('');
    $('#emp-dept').val('');
    $('#emp-position').val('');
    $('#emp-role').val('EMPLOYEE');
    $('#emp-status').val('active');
    $('#emp-modal-title').text('Thêm nhân viên mới');
    $('#emp-password-group').show(); // Hiện ô password khi thêm mới

    // Nếu có id → đang sửa
    if (id) {
        const emp = employees.find(e => e.id === id);
        if (!emp) return;
        $('#emp-modal-title').text('Chỉnh sửa nhân viên');
        $('#emp-id').val(emp.id);
        $('#emp-firstname').val(emp.firstName);
        $('#emp-lastname').val(emp.lastName);
        $('#emp-email').val(emp.email);
        $('#emp-phone').val(emp.phone || '');
        $('#emp-salary').val(emp.baseSalary || 0);
        $('#emp-allowance').val(emp.allowance || 0);
        $('#emp-role').val(emp.role);
        $('#emp-status').val(emp.status);
        $('#emp-password-group').hide(); // Ẩn ô password khi chỉnh sửa

        // Chọn đúng phòng ban và chức vụ bằng ID
        if (emp.departmentId) $('#emp-dept').val(emp.departmentId);
        if (emp.positionId)   $('#emp-position').val(emp.positionId);
    }

    new bootstrap.Modal($('#employee-modal')[0]).show();
}

// ===================================================
//  NHÂN VIÊN – Lưu (gọi API POST hoặc PUT)
// ===================================================
function saveEmployee() {
    const id        = parseInt($('#emp-id').val()) || null;
    const firstName = $('#emp-firstname').val().trim();
    const lastName  = $('#emp-lastname').val().trim();
    const email     = $('#emp-email').val().trim();

    if (!firstName || !lastName || !email) {
        alert('Vui lòng nhập họ, tên và email!');
        return;
    }

    const token = localStorage.getItem('token');

    if (id) {
        // ---- CẬP NHẬT (PUT) ----
        const updateData = {
            firstName:    firstName,
            lastName:     lastName,
            phone:        $('#emp-phone').val().trim() || null,
            departmentId: parseInt($('#emp-dept').val()) || null,
            positionId:   parseInt($('#emp-position').val()) || null,
            role:         $('#emp-role').val(),
            baseSalary:   parseFloat($('#emp-salary').val()) || 0,
            allowance:    parseFloat($('#emp-allowance').val()) || 0,
            status:       $('#emp-status').val(),
        };
        $.ajax({
            url: '/api/v1/employees/' + id,
            method: 'PUT',
            contentType: 'application/json',
            data: JSON.stringify(updateData),
            headers: { 'Authorization': 'Bearer ' + token },
            success: function() {
                fetchEmployees();
                fetchDepartments(); // Cập nhật lại số lượng nhân viên ở tab Phòng ban
                fetchPositions();   // Cập nhật lại số lượng nhân viên ở tab Chức vụ (nếu có hiển thị)
                bootstrap.Modal.getInstance($('#employee-modal')[0]).hide();
            },
            error: function(xhr) {
                const msg = xhr.responseJSON?.message || JSON.stringify(xhr.responseJSON) || 'Có lỗi xảy ra';
                alert(msg);
            }
        });
    } else {
        // ---- THÊM MỚI (POST) ----
        const password = $('#emp-password').val();
        if (!password || password.length < 6) {
            alert('Mật khẩu phải có ít nhất 6 ký tự!');
            return;
        }
        const createData = {
            firstName:    firstName,
            lastName:     lastName,
            email:        email,
            password:     password,
            phone:        $('#emp-phone').val().trim() || null,
            departmentId: parseInt($('#emp-dept').val()) || null,
            positionId:   parseInt($('#emp-position').val()) || null,
            role:         $('#emp-role').val(),
            baseSalary:   parseFloat($('#emp-salary').val()) || 0,
            allowance:    parseFloat($('#emp-allowance').val()) || 0,
        };
        $.ajax({
            url: '/api/v1/employees',
            method: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(createData),
            headers: { 'Authorization': 'Bearer ' + token },
            success: function() {
                fetchEmployees();
                fetchDepartments();
                fetchPositions();
                bootstrap.Modal.getInstance($('#employee-modal')[0]).hide();
            },
            error: function(xhr) {
                const msg = xhr.responseJSON?.message || JSON.stringify(xhr.responseJSON) || 'Có lỗi xảy ra';
                alert(msg);
            }
        });
    }
}

// ===================================================
//  PHÒNG BAN – Render bảng
// ===================================================
function fetchDepartments() {
    const token = localStorage.getItem('token');
    $.ajax({
        url: '/api/v1/departments',
        method: 'GET',
        headers: { 'Authorization': 'Bearer ' + token },
        success: function(data) {
            departments = data;
            renderDepartments();
        },
        error: function(xhr) {
            if (xhr.status === 401) handleLogout(false);
            console.error('Lỗi lấy danh sách phòng ban:', xhr);
        }
    });
}

function renderDepartments() {
    let html = '';
    const user = JSON.parse(localStorage.getItem('user'));
    const role = user?.role || 'EMPLOYEE';

    departments.forEach(function(dept, i) {
        html += `<tr>
            <td class="ps-3 text-muted">${i + 1}</td>
            <td class="fw-medium">
                <a href="javascript:void(0)" class="text-decoration-none" onclick="filterByDept(${dept.id}, '${dept.name}')">
                    ${dept.name}
                </a>
            </td>
            <td style="font-size:13px">${dept.managerName || 'Chưa có'}</td>
            <td><span class="badge bg-light text-dark border">${dept.employeeCount || 0} người</span></td>
            <td class="text-muted" style="font-size:13px">${dept.description || ''}</td>
            ${role === 'ADMIN' ? `
            <td class="text-center admin-only">
                <button class="btn btn-sm btn-outline-primary me-1" onclick="openDeptModal(${dept.id})">
                    <i class="fas fa-pen"></i>
                </button>
                <button class="btn btn-sm btn-outline-danger" onclick="deleteRow('department', ${dept.id})">
                    <i class="fas fa-trash"></i>
                </button>
            </td>` : ''}
        </tr>`;
    });
    $('#department-tbody').html(html || '<tr><td colspan="6" class="text-center text-muted py-4">Không có dữ liệu</td></tr>');

    // Cập nhật dropdown phòng ban trong modal nhân viên (value = id để gửi lên API)
    const options = departments.map(d => `<option value="${d.id}">${d.name}</option>`).join('');
    $('#emp-dept').html('<option value="">-- Chọn phòng ban --</option>' + options);
}
// fetchDepartments(); // Đã chuyển vào fetchInitialData

// Mở modal phòng ban
function openDeptModal(id) {
    $('#dept-id').val('');
    $('#dept-name, #dept-manager, #dept-desc').val('');
    $('#dept-modal-title').text('Thêm phòng ban mới');

    if (id) {
        const dept = departments.find(d => d.id === id);
        if (!dept) return;
        $('#dept-modal-title').text('Chỉnh sửa phòng ban');
        $('#dept-id').val(dept.id);
        $('#dept-name').val(dept.name);
        $('#dept-manager').val(dept.managerName || ''); 
        $('#dept-desc').val(dept.description || '');
    }
    new bootstrap.Modal($('#department-modal')[0]).show();
}

// Lưu phòng ban
function saveDept() {
    const id   = parseInt($('#dept-id').val()) || null;
    const name = $('#dept-name').val().trim();
    if (!name) { alert('Vui lòng nhập tên phòng ban!'); return; }

    const data = {
        name: name,
        description: $('#dept-desc').val().trim(),
        // managerId: null -> Tạm thời chưa gán quản lý vì chưa có API nhân viên
    };

    const token = localStorage.getItem('token');
    const method = id ? 'PUT' : 'POST';
    const url = id ? '/api/v1/departments/' + id : '/api/v1/departments';

    $.ajax({
        url: url,
        method: method,
        contentType: 'application/json',
        data: JSON.stringify(data),
        headers: { 'Authorization': 'Bearer ' + token },
        success: function() {
            fetchDepartments(); // Gọi lại API để load danh sách mới
            bootstrap.Modal.getInstance($('#department-modal')[0]).hide();
        },
        error: function(xhr) {
            alert(xhr.responseJSON?.message || 'Có lỗi xảy ra');
        }
    });
}

// ===================================================
//  CHỨC VỤ – Render bảng
// ===================================================
function fetchPositions() {
    const token = localStorage.getItem('token');
    $.ajax({
        url: '/api/v1/positions',
        method: 'GET',
        headers: { 'Authorization': 'Bearer ' + token },
        success: function(data) {
            positions = data;
            renderPositions();
        },
        error: function(xhr) {
            if (xhr.status === 401) handleLogout(false);
            console.error('Lỗi lấy danh sách chức vụ:', xhr);
        }
    });
}

function renderPositions() {
    let html = '';
    const user = JSON.parse(localStorage.getItem('user'));
    const role = user?.role || 'EMPLOYEE';

    positions.forEach(function(pos, i) {
        html += `<tr>
            <td class="ps-3 text-muted">${i + 1}</td>
            <td class="fw-medium">
                <a href="javascript:void(0)" class="text-decoration-none" onclick="filterByPos(${pos.id}, '${pos.name}')">
                    ${pos.name}
                </a>
            </td>
            <td style="font-size:13px">${role === 'ADMIN' ? formatVND(pos.baseSalary || 0) : '***'}</td>
            ${role === 'ADMIN' ? `
            <td class="text-center admin-only">
                <button class="btn btn-sm btn-outline-primary me-1" onclick="openPosModal(${pos.id})">
                    <i class="fas fa-pen"></i>
                </button>
                <button class="btn btn-sm btn-outline-danger" onclick="deleteRow('position', ${pos.id})">
                    <i class="fas fa-trash"></i>
                </button>
            </td>` : ''}
        </tr>`;
    });
    $('#position-tbody').html(html || '<tr><td colspan="4" class="text-center text-muted py-4">Không có dữ liệu</td></tr>');

    // Cập nhật dropdown chức vụ trong modal nhân viên (value = id)
    const options = positions.map(p => `<option value="${p.id}">${p.name}</option>`).join('');
    $('#emp-position').html('<option value="">-- Chọn chức vụ --</option>' + options);
}
// fetchPositions(); // Đã chuyển vào fetchInitialData

function openPosModal(id) {
    $('#pos-id').val(''); 
    $('#pos-name, #pos-salary').val('');
    $('#pos-modal-title').text('Thêm chức vụ mới');
    if (id) {
        const pos = positions.find(p => p.id === id);
        if (!pos) return;
        $('#pos-modal-title').text('Chỉnh sửa chức vụ');
        $('#pos-id').val(pos.id);
        $('#pos-name').val(pos.name);
        $('#pos-salary').val(pos.baseSalary);
    }
    new bootstrap.Modal($('#position-modal')[0]).show();
}

function savePosition() {
    const id   = parseInt($('#pos-id').val()) || null;
    const name = $('#pos-name').val().trim();
    if (!name) { alert('Vui lòng nhập tên chức vụ!'); return; }
    
    const data = { 
        name: name, 
        baseSalary: parseInt($('#pos-salary').val()) || 0 
    };

    const token = localStorage.getItem('token');
    const method = id ? 'PUT' : 'POST';
    const url = id ? '/api/v1/positions/' + id : '/api/v1/positions';

    $.ajax({
        url: url,
        method: method,
        contentType: 'application/json',
        data: JSON.stringify(data),
        headers: { 'Authorization': 'Bearer ' + token },
        success: function() {
            fetchPositions(); // Gọi lại API để load danh sách mới
            bootstrap.Modal.getInstance($('#position-modal')[0]).hide();
        },
        error: function(xhr) {
            alert(xhr.responseJSON?.message || 'Có lỗi xảy ra');
        }
    });
}

// ===================================================
//  CHẤM CÔNG – Calendar & Table
// ===================================================
let attPagination = { page: 0, size: 10, totalPages: 0 };
let currentAttView = 'calendar'; // 'calendar' hoặc 'table'

function initAttendanceCalendar() {
    const user = JSON.parse(localStorage.getItem('user'));
    const isAdmin = user.role === 'ADMIN' || user.role === 'MANAGER';
    
    // Nếu là Admin, mặc định xem dạng Bảng vì Lịch sẽ bị ghi đè nhiều người
    if (isAdmin) {
        currentAttView = 'table';
        $('#attendance-calendar-container').hide();
        $('#attendance-table-container').show();
        $('#btn-toggle-att').html('<i class="fas fa-calendar-alt me-1"></i> Xem lịch cá nhân');
    }

    const now = new Date();
    const monthStr = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`;
    $('#att-month-calendar').val(monthStr);
    
    // Lắng nghe sự kiện đổi tháng
    $('#att-month-calendar').off('change').on('change', fetchAttendances);
    
    fetchAttendances();
}

function toggleAttView() {
    currentAttView = (currentAttView === 'calendar') ? 'table' : 'calendar';
    if (currentAttView === 'calendar') {
        $('#attendance-calendar-container').show();
        $('#attendance-table-container').hide();
        $('#btn-toggle-att').html('<i class="fas fa-table me-1"></i> Xem dạng bảng');
    } else {
        $('#attendance-calendar-container').hide();
        $('#attendance-table-container').show();
        $('#btn-toggle-att').html('<i class="fas fa-calendar-alt me-1"></i> Xem dạng lịch');
    }
    fetchAttendances();
}

function fetchAttendances() {
    const token = localStorage.getItem('token');
    const user = JSON.parse(localStorage.getItem('user'));
    const monthVal = $('#att-month-calendar').val();
    if (!monthVal) return;
    
    const [year, month] = monthVal.split('-');

    // Admin/Manager xem dạng bảng thì lấy tất cả.
    // Các trường hợp còn lại lấy theo tháng của cá nhân.
    const isAdmin = user.role === 'ADMIN' || user.role === 'MANAGER';
    
    // Luôn tải thống kê hôm nay khi vào mục chấm công
    fetchTodayAttendanceStats();

    if (currentAttView === 'calendar' || !isAdmin) {
        $.ajax({
            url: `/api/v1/attendances/calendar?month=${parseInt(month)}&year=${year}`,
            method: 'GET',
            headers: { 'Authorization': 'Bearer ' + token },
            success: function(list) {
                renderAttendanceCalendar(list, parseInt(month), parseInt(year));
                calculateMonthlyStats(list);
            }
        });
    } else {
        $.ajax({
            url: `/api/v1/attendances?page=0&size=50`,
            method: 'GET',
            headers: { 'Authorization': 'Bearer ' + token },
            success: function(res) {
                renderAttendanceTable(res.data);
            },
            error: function(xhr) {
                if (xhr.status === 401) handleLogout(false);
                console.error('Lỗi lấy danh sách chấm công:', xhr);
            }
        });
    }
}

// ===================================================
//  CHẤM CÔNG – Thống kê hôm nay
// ===================================================
function fetchTodayAttendanceStats() {
    const token = localStorage.getItem('token');
    $.ajax({
        url: '/api/v1/attendances/today-stats',
        method: 'GET',
        headers: { 'Authorization': 'Bearer ' + token },
        success: function(res) {
            $('#today-present').text(res.present);
            $('#progress-present').css('width', res.presentPercentage + '%');
            
            $('#today-late').text(res.late);
            $('#progress-late').css('width', res.latePercentage + '%');
            
            $('#today-absent').text(res.absent);
            $('#progress-absent').css('width', res.absentPercentage + '%');
        },
        error: function(xhr) {
            console.error('Lỗi khi tải thống kê chấm công hôm nay:', xhr);
        }
    });
}

function renderAttendanceCalendar(list, month, year) {
    const container = $('#attendance-calendar-container');
    container.empty();

    // Headers
    ['T2', 'T3', 'T4', 'T5', 'T6', 'T7', 'CN'].forEach(d => 
        container.append(`<div class="calendar-day-header">${d}</div>`));

    const firstDay = new Date(year, month - 1, 1).getDay();
    const daysInMonth = new Date(year, month, 0).getDate();
    const offset = (firstDay === 0) ? 6 : firstDay - 1;

    for (let i = 0; i < offset; i++) container.append('<div class="calendar-day empty"></div>');

    const today = new Date();
    for (let d = 1; d <= daysInMonth; d++) {
        const dateStr = `${year}-${String(month).padStart(2, '0')}-${String(d).padStart(2, '0')}`;
        const att = list.find(a => a.date === dateStr);
        const isToday = today.getFullYear() === year && (today.getMonth()+1) === month && today.getDate() === d;
        
        let content = '';
        if (att) {
            const isLate = att.checkIn && att.checkIn > '08:30:00';
            const isEarly = att.checkOut && att.checkOut < '17:30:00';
            content = `
                <div class="att-info">
                    <div class="time-in"><i class="far fa-clock"></i> ${att.checkIn ? att.checkIn.substring(0, 5) : '--:--'}</div>
                    <div class="time-out"><i class="fas fa-sign-out-alt"></i> ${att.checkOut ? att.checkOut.substring(0, 5) : '--:--'}</div>
                    ${isLate ? '<div class="badge-late mt-1">Đi muộn</div>' : ''}
                    ${isEarly ? '<div class="badge-early">Về sớm</div>' : ''}
                </div>`;
        }
        container.append(`<div class="calendar-day ${isToday ? 'today' : ''}"><div class="day-num">${d}</div>${content}</div>`);
    }
}

function renderAttendanceTable(list) {
    let html = '';
    list.forEach(function(att) {
        const statusColor = att.status === 'present' ? 'success' : att.status === 'late' ? 'warning' : 'danger';
        const statusText  = att.status === 'present' ? 'Đúng giờ' : att.status === 'late' ? 'Đi muộn' : 'Vắng mặt';
        let hours = '—';
        if (att.checkIn && att.checkOut) {
            const inTime = new Date(`2000-01-01T${att.checkIn}`);
            const outTime = new Date(`2000-01-01T${att.checkOut}`);
            hours = ((outTime - inTime) / (1000 * 60 * 60)).toFixed(1) + 'h';
        }
        html += `<tr>
            <td class="ps-3">
                <div class="fw-medium">${att.employeeName}</div>
                <div class="text-muted" style="font-size:11px">${att.email || ''}</div>
            </td>
            <td style="font-size:13px">${att.date}</td>
            <td style="font-size:13px" class="text-success fw-medium">${att.checkIn ? att.checkIn.substring(0, 5) : '--:--'}</td>
            <td style="font-size:13px" class="text-info fw-medium">${att.checkOut ? att.checkOut.substring(0, 5) : '--:--'}</td>
            <td style="font-size:13px">${hours}</td>
            <td><span class="badge bg-${statusColor}">${statusText}</span></td>
        </tr>`;
    });
    $('#attendance-tbody').html(html || '<tr><td colspan="6" class="text-center py-3">Không có dữ liệu</td></tr>');
}

function calculateMonthlyStats(list) {
    let totalWork = 0, lateCount = 0, earlyCount = 0, totalHours = 0;
    list.forEach(att => {
        if (att.checkIn) {
            totalWork++;
            if (att.checkIn > '08:30:00') lateCount++;
        }
        if (att.checkOut && att.checkOut < '17:30:00') earlyCount++;
        if (att.checkIn && att.checkOut) {
            totalHours += (new Date(`2000-01-01T${att.checkOut}`) - new Date(`2000-01-01T${att.checkIn}`)) / 3600000;
        }
    });
    $('#stat-att-total').text(totalWork);
    $('#stat-att-late').text(lateCount);
    $('#stat-att-early').text(earlyCount);
    $('#stat-att-avg').text((totalWork > 0 ? (totalHours / totalWork).toFixed(1) : 0) + 'h');
}

function doCheckIn() {
    const token = localStorage.getItem('token');
    $.ajax({
        url: '/api/v1/attendances/check-in',
        method: 'POST',
        headers: { 'Authorization': 'Bearer ' + token },
        success: function(res) {
            const time = res.checkIn.substring(0, 5);
            $('#checkin-status').html(`<span class="text-success"><i class="fas fa-check-circle me-1"></i>Đã check-in lúc <strong>${time}</strong></span>`);
            alert('Check-in thành công!');
            fetchAttendances(); // Reload lịch sử
        },
        error: function(xhr) {
            if (xhr.status === 401) handleLogout(false);
            alert(xhr.responseJSON?.message || 'Có lỗi xảy ra khi Check-in');
        }
    });
}

function doCheckOut() {
    const token = localStorage.getItem('token');
    $.ajax({
        url: '/api/v1/attendances/check-out',
        method: 'POST',
        headers: { 'Authorization': 'Bearer ' + token },
        success: function(res) {
            const time = res.checkOut.substring(0, 5);
            $('#checkin-status').html(`<span class="text-danger"><i class="fas fa-check-circle me-1"></i>Đã check-out lúc <strong>${time}</strong></span>`);
            alert('Check-out thành công!');
            fetchAttendances(); // Reload lịch sử
        },
        error: function(xhr) {
            if (xhr.status === 401) handleLogout(false);
            alert(xhr.responseJSON?.message || 'Có lỗi xảy ra khi Check-out');
        }
    });
}

// ===================================================
//  NGHỈ PHÉP – Render bảng
// ===================================================
let leavePagination = { page: 0, size: 10, totalPages: 0 };

function fetchLeaveRequests(page = 0) {
    const token = localStorage.getItem('token');
    // Luôn gọi API lấy toàn bộ đơn nghỉ phép cho tất cả mọi người
    const url = `/api/v1/leave-requests?page=${page}&size=${leavePagination.size}`;

    $.ajax({
        url: url,
        method: 'GET',
        headers: { 'Authorization': 'Bearer ' + token },
        success: function(res) {
            leavePagination.page = res.currentPage;
            leavePagination.totalPages = res.totalPages;
            const user = JSON.parse(localStorage.getItem('user'));
            renderLeaveRequests(res.data, user);
        },
        error: function(xhr) {
            if (xhr.status === 401) handleLogout(false);
            console.error('Lỗi khi tải lịch sử nghỉ phép:', xhr);
        }
    });
}

let currentLeaveList = [];

function renderLeaveRequests(list, user) {
    currentLeaveList = list;
    const role = user?.role || 'EMPLOYEE';
    const myDeptId = user?.departmentId || null;
    
    let pending = 0;
    let html    = '';
    list.forEach(function(leave, i) {
        if (leave.status === 'pending') pending++;
        
        // Logic hiển thị nút duyệt: ADMIN thấy hết, MANAGER thấy đơn cùng phòng ban
        // Không thể tự duyệt đơn của chính mình
        let canApprove = false;
        if (leave.status === 'pending' && leave.employeeId !== user?.id) {
            if (role === 'ADMIN') {
                canApprove = true;
            } else if (role === 'MANAGER' && leave.departmentId === myDeptId) {
                canApprove = true;
            }
        }

        html += `<tr style="cursor: pointer;" onclick="showLeaveDetail(${leave.id})">
            <td class="ps-3 text-muted">${i + 1 + (leavePagination.page * leavePagination.size)}</td>
            <td class="fw-medium">${leave.fullName || '—'}</td>
            <td style="font-size:13px">${leave.startDate}</td>
            <td style="font-size:13px">${leave.endDate}</td>
            <td><span class="badge bg-light text-dark border">${leave.days} ngày</span></td>
            <td class="text-muted" style="font-size:13px">
                <div class="fw-bold text-dark">${leave.reasonCategory || '—'}</div>
                ${leave.reason ? `<div class="text-truncate" style="max-width: 150px;" title="${leave.reason}">${leave.reason}</div>` : ''}
            </td>
            <td>${statusBadge(leave.status)}</td>
            ${role !== 'EMPLOYEE' ? `
            <td class="text-center admin-only">
                ${canApprove ? `
                <button class="btn btn-sm btn-success me-1" onclick="event.stopPropagation(); updateLeaveStatus(${leave.id}, 'approved')">
                    <i class="fas fa-check"></i>
                </button>
                <button class="btn btn-sm btn-danger" onclick="event.stopPropagation(); updateLeaveStatus(${leave.id}, 'rejected')">
                    <i class="fas fa-times"></i>
                </button>` : (leave.approverByName ? `<span class="small text-muted">Duyệt: ${leave.approverByName}</span>` : '—')}
            </td>` : ''}
        </tr>`;
    });
    $('#leave-tbody').html(html || '<tr><td colspan="8" class="text-center text-muted py-3">Không có đơn nghỉ phép nào</td></tr>');
    
    // Gọi render phân trang
    renderLeavePagination();

    // Badge số đơn chờ duyệt chỉ hiện cho ADMIN/MANAGER
    if (role === 'MANAGER' || role === 'ADMIN') {
        $('#leave-badge').text(pending).toggle(pending > 0);
    }
}
// Gọi lần đầu khi load - Đã chuyển vào fetchInitialData
// fetchLeaveRequests();

function renderLeavePagination() {
    const total = leavePagination.totalPages;
    const cur   = leavePagination.page;
    if (total <= 1) { $('#leave-pagination').html(''); return; }

    let html = `<nav><ul class="pagination pagination-sm mb-0">`;
    html += `<li class="page-item ${cur === 0 ? 'disabled' : ''}">
        <a class="page-link" href="#" onclick="fetchLeaveRequests(${cur - 1}); return false;">«</a></li>`;
    for (let i = 0; i < total; i++) {
        html += `<li class="page-item ${i === cur ? 'active' : ''}">
            <a class="page-link" href="#" onclick="fetchLeaveRequests(${i}); return false;">${i + 1}</a></li>`;
    }
    html += `<li class="page-item ${cur === total - 1 ? 'disabled' : ''}">
        <a class="page-link" href="#" onclick="fetchLeaveRequests(${cur + 1}); return false;">»</a></li>`;
    html += `</ul></nav>`;
    $('#leave-pagination').html(html);
}

// Mở modal tạo đơn – Ẩn dropdown nhân viên
function openLeaveModal() {
    $('#leave-employee').closest('.mb-3').addClass('d-none'); // Ẩn vì Backend tự nhận dạng qua Token
    $('#leave-from, #leave-to, #leave-reason, #leave-reason-category').val('');
    new bootstrap.Modal($('#leave-modal')[0]).show();
}

// Lưu đơn nghỉ mới
function saveLeave() {
    const from = $('#leave-from').val();
    const to   = $('#leave-to').val();
    const category = $('#leave-reason-category').val();
    const reason = $('#leave-reason').val().trim();
    if (!from || !to || !category) {
        alert('Vui lòng điền ngày và chọn Lý do chính!');
        return;
    }
    
    const token = localStorage.getItem('token');
    $.ajax({
        url: '/api/v1/leave-requests',
        method: 'POST',
        headers: { 'Authorization': 'Bearer ' + token },
        contentType: 'application/json',
        data: JSON.stringify({
            startDate: from,
            endDate: to,
            reasonCategory: category,
            reason: reason
        }),
        success: function() {
            alert('Tạo đơn nghỉ phép thành công!');
            fetchLeaveRequests();
            bootstrap.Modal.getInstance($('#leave-modal')[0]).hide();
        },
        error: function(xhr) {
            if (xhr.status === 401) handleLogout(false);
            alert(xhr.responseJSON?.message || 'Có lỗi xảy ra khi tạo đơn nghỉ phép');
        }
    });
}

// Duyệt / Từ chối đơn
function updateLeaveStatus(id, status) {
    if (!confirm(`Bạn có chắc muốn ${status === 'approved' ? 'duyệt' : 'từ chối'} đơn này?`)) return;
    const token = localStorage.getItem('token');
    $.ajax({
        url: `/api/v1/leave-requests/${id}/status?status=${status}`,
        method: 'PATCH',
        headers: { 'Authorization': 'Bearer ' + token },
        success: function() {
            fetchLeaveRequests();
        },
        error: function(xhr) {
            alert(xhr.responseJSON?.message || 'Có lỗi xảy ra');
        }
    });
}

function showLeaveDetail(id) {
    const leave = currentLeaveList.find(l => l.id === id);
    if (!leave) return;

    $('#detail-leave-employee').text(leave.fullName || '—');
    $('#detail-leave-time').text(`${leave.startDate} đến ${leave.endDate}`);
    $('#detail-leave-days').text(leave.days);
    $('#detail-leave-category').text(leave.reasonCategory || '—');
    $('#detail-leave-reason').text(leave.reason || '—');
    
    let statusText = 'Đang chờ';
    if (leave.status === 'approved') statusText = '<span class="text-success">Đã duyệt</span>';
    if (leave.status === 'rejected') statusText = '<span class="text-danger">Từ chối</span>';
    $('#detail-leave-status').html(statusText);
    
    $('#detail-leave-approver').text(leave.approverByName || '—');

    new bootstrap.Modal($('#leave-detail-modal')[0]).show();
}

// ===================================================
//  BẢNG LƯƠNG – Render
// ===================================================
let payrollPagination = { page: 0, size: 10, totalPages: 0 };

function fetchPayrolls(page = 0) {
    payrollPagination.page = page;
    const monthVal = $('#payroll-month').val(); // "YYYY-MM"
    if (!monthVal) return;

    const [year, month] = monthVal.split('-');
    const token = localStorage.getItem('token');

    $.ajax({
        url: `/api/v1/payrolls?month=${parseInt(month)}&year=${year}&page=${page}&size=${payrollPagination.size}`,
        method: 'GET',
        headers: { 'Authorization': 'Bearer ' + token },
        success: function(res) {
            payrollPagination.totalPages = res.data.totalPages;
            renderPayroll(res.data.data);
            updatePayrollStats(res.data.data);
            renderPayrollPagination();
        },
        error: function(xhr) {
            if (xhr.status === 401) handleLogout(false);
            console.error('Lỗi khi tải bảng lương:', xhr);
        }
    });
}

function renderPayroll(list) {
    let html = '';
    list.forEach(function(p) {
        const nameParts = p.employeeName.split(' ');
        const firstName = nameParts[0] || '';
        const lastName = nameParts.length > 1 ? nameParts[nameParts.length - 1] : '';

        html += `<tr>
            <td class="ps-3">
                <div class="d-flex align-items-center">
                    ${empAvatarFromApi({ id: p.employeeId, firstName: firstName, lastName: lastName })}
                    <div>
                        <div class="fw-medium" style="font-size:13px">${p.employeeName}</div>
                        <div class="text-muted" style="font-size:11px">${p.email}</div>
                    </div>
                </div>
            </td>
            <td style="font-size:13px">${p.departmentName}</td>
            <td style="font-size:13px">${formatVND(p.baseSalary)}</td>
            <td style="font-size:13px" class="fw-medium text-primary">${p.workingDays}</td>
            <td style="font-size:13px" class="text-success">+${formatVND(p.allowance)}</td>
            <td style="font-size:13px" class="text-danger">-${formatVND(p.deduction || 0)}</td>
            <td class="fw-bold">${formatVND(p.netSalary)}</td>
            <td><span class="badge bg-success">Đã tính</span></td>
        </tr>`;
    });
    const user = JSON.parse(localStorage.getItem('user'));
    const isAdmin = user?.role === 'ADMIN';
    const emptyMsg = isAdmin ? 'Chưa có dữ liệu lương tháng này. Nhấn "Tính lương" để bắt đầu.' : 'Chưa có dữ liệu lương tháng này.';
    $('#payroll-tbody').html(html || `<tr><td colspan="8" class="text-center text-muted py-3">${emptyMsg}</td></tr>`);
}

function updatePayrollStats(list) {
    let total = 0;
    list.forEach(p => total += p.netSalary);
    
    $('#total-salary').text(formatVND(total));
    $('#total-employees').text(list.length);
    
    const avg = list.length > 0 ? Math.round(total / list.length) : 0;
    $('#avg-salary').text(formatVND(avg));
}

function renderPayrollPagination() {
    const total = payrollPagination.totalPages;
    const cur   = payrollPagination.page;
    if (total <= 1) { $('#payroll-pagination').html(''); return; }

    let html = `<nav><ul class="pagination pagination-sm mb-0">`;
    html += `<li class="page-item ${cur === 0 ? 'disabled' : ''}">
        <a class="page-link" href="#" onclick="fetchPayrolls(${cur - 1}); return false;">«</a></li>`;
    for (let i = 0; i < total; i++) {
        html += `<li class="page-item ${i === cur ? 'active' : ''}">
            <a class="page-link" href="#" onclick="fetchPayrolls(${i}); return false;">${i + 1}</a></li>`;
    }
    html += `<li class="page-item ${cur === total - 1 ? 'disabled' : ''}">
        <a class="page-link" href="#" onclick="fetchPayrolls(${cur + 1}); return false;">»</a></li>`;
    html += `</ul></nav>`;
    $('#payroll-pagination').html(html);
}

// Tính lương
function calculatePayroll() {
    const monthVal = $('#payroll-month').val();
    if (!monthVal) return;

    const [year, month] = monthVal.split('-');
    const token = localStorage.getItem('token');

    if (!confirm(`Bạn có chắc muốn tính lại lương cho tháng ${month}/${year}?`)) return;

    $.ajax({
        url: `/api/v1/payrolls/calculate?month=${parseInt(month)}&year=${year}`,
        method: 'POST',
        headers: { 'Authorization': 'Bearer ' + token },
        success: function(res) {
            alert(res.message);
            fetchPayrolls(); // Reload lại bảng
        },
        error: function(xhr) {
            if (xhr.status === 401) handleLogout(false);
            alert(xhr.responseJSON?.message || 'Có lỗi xảy ra khi tính lương');
        }
    });
}

// Lắng nghe sự kiện đổi tháng
$('#payroll-month').on('change', function() {
    fetchPayrolls();
});

// ===================================================
//  XÓA DÒNG (dùng chung cho employee, department, position)
// ===================================================
function deleteRow(type, id) {
    // Hiện modal xác nhận
    $('#confirm-msg').text(`Bạn có chắc muốn xóa không? Hành động này không thể hoàn tác.`);

    // Xác nhận → thực hiện xóa
    $('#confirm-ok').off('click').on('click', function() {
        if (type === 'employee') {
            const token = localStorage.getItem('token');
            $.ajax({
                url: '/api/v1/employees/' + id,
                method: 'DELETE',
                headers: { 'Authorization': 'Bearer ' + token },
                success: function() {
                    fetchEmployees();
                    fetchDepartments();
                    fetchPositions();
                    bootstrap.Modal.getInstance($('#confirm-modal')[0]).hide();
                },
                error: function(xhr) {
                    alert(xhr.responseJSON?.message || 'Không thể xóa nhân viên');
                    bootstrap.Modal.getInstance($('#confirm-modal')[0]).hide();
                }
            });
            return;
        }
        if (type === 'department') {
            const token = localStorage.getItem('token');
            $.ajax({
                url: '/api/v1/departments/' + id,
                method: 'DELETE',
                headers: { 'Authorization': 'Bearer ' + token },
                success: function() {
                    fetchDepartments();
                    bootstrap.Modal.getInstance($('#confirm-modal')[0]).hide();
                },
                error: function(xhr) {
                    alert(xhr.responseJSON?.message || 'Không thể xóa phòng ban');
                    bootstrap.Modal.getInstance($('#confirm-modal')[0]).hide();
                }
            });
            return; // Dừng lại ở đây vì request bất đồng bộ
        }
        if (type === 'position') {
            const token = localStorage.getItem('token');
            $.ajax({
                url: '/api/v1/positions/' + id,
                method: 'DELETE',
                headers: { 'Authorization': 'Bearer ' + token },
                success: function() {
                    fetchPositions();
                    bootstrap.Modal.getInstance($('#confirm-modal')[0]).hide();
                },
                error: function(xhr) {
                    alert(xhr.responseJSON?.message || 'Không thể xóa chức vụ');
                    bootstrap.Modal.getInstance($('#confirm-modal')[0]).hide();
                }
            });
            return;
        }
    });

    new bootstrap.Modal($('#confirm-modal')[0]).show();
}
