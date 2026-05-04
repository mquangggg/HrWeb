// ===================================================
//  DỮ LIỆU MẪU (mock data – sẽ thay bằng API sau)
// ===================================================
let employees = [
    { id:1, name:'Nguyễn Văn An',   email:'an@company.com',     phone:'0901111111', dept:'Kỹ thuật',   position:'Developer',    role:'ADMIN',    salary:25000000, status:'active',   color:'#6366f1' },
    { id:2, name:'Trần Thị Bình',   email:'binh@company.com',   phone:'0902222222', dept:'Nhân sự',    position:'HR Specialist', role:'MANAGER', salary:18000000, status:'active',   color:'#8b5cf6' },
    { id:3, name:'Lê Minh Châu',    email:'chau@company.com',   phone:'0903333333', dept:'Kế toán',    position:'Accountant',   role:'EMPLOYEE', salary:14000000, status:'active',   color:'#06b6d4' },
    { id:4, name:'Phạm Thị Dung',   email:'dung@company.com',   phone:'0904444444', dept:'Marketing',  position:'Designer',     role:'EMPLOYEE', salary:13000000, status:'inactive', color:'#ec4899' },
    { id:5, name:'Hoàng Văn Em',    email:'em@company.com',     phone:'0905555555', dept:'Kỹ thuật',   position:'Developer',    role:'EMPLOYEE', salary:15000000, status:'active',   color:'#10b981' },
    { id:6, name:'Võ Thị Phương',   email:'phuong@company.com', phone:'0906666666', dept:'Marketing',  position:'Designer',     role:'MANAGER',  salary:20000000, status:'active',   color:'#f59e0b' },
];

let departments = []; // Dữ liệu thật sẽ được load từ API

let positions = []; // Dữ liệu thật sẽ được load từ API

let attendances = [
    { emp:'Nguyễn Văn An',  date:'21/04/2026', checkIn:'08:02', checkOut:'17:05', hours:9.05, status:'Đúng giờ' },
    { emp:'Trần Thị Bình',  date:'21/04/2026', checkIn:'08:45', checkOut:'17:00', hours:8.25, status:'Đi muộn'  },
    { emp:'Lê Minh Châu',   date:'21/04/2026', checkIn:'07:55', checkOut:'17:10', hours:9.25, status:'Đúng giờ' },
    { emp:'Hoàng Văn Em',   date:'21/04/2026', checkIn:'',      checkOut:'',      hours:0,    status:'Vắng mặt' },
    { emp:'Võ Thị Phương',  date:'21/04/2026', checkIn:'08:10', checkOut:'17:00', hours:8.83, status:'Đúng giờ' },
];

let leaveRequests = [
    { id:1, emp:'Trần Thị Bình', from:'22/04/2026', to:'23/04/2026', days:2, reason:'Việc gia đình',    status:'PENDING'  },
    { id:2, emp:'Lê Minh Châu',  from:'25/04/2026', to:'25/04/2026', days:1, reason:'Khám bệnh',       status:'PENDING'  },
    { id:3, emp:'Hoàng Văn Em',  from:'28/04/2026', to:'28/04/2026', days:1, reason:'Nghỉ cá nhân',    status:'PENDING'  },
    { id:4, emp:'Nguyễn Văn An', from:'15/04/2026', to:'15/04/2026', days:1, reason:'Việc cá nhân',    status:'APPROVED' },
    { id:5, emp:'Võ Thị Phương', from:'10/04/2026', to:'11/04/2026', days:2, reason:'Đám cưới anh họ', status:'REJECTED' },
];

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
}
updateClock();
setInterval(updateClock, 1000); // cập nhật mỗi giây

// ===================================================
//  LOAD THÔNG TIN USER TỪ LOCALSTORAGE VÀ URL
// ===================================================
function loadUserInfo() {
    // Nếu có token trên URL (từ đăng nhập Google), lưu vào localStorage
    const urlParams = new URLSearchParams(window.location.search);
    const urlToken = urlParams.get('token');
    if (urlToken) {
        localStorage.setItem('token', urlToken);
        // Xóa token khỏi URL để bảo mật
        window.history.replaceState({}, document.title, window.location.pathname);
    }

    const token = localStorage.getItem('token');
    if (!token) {
        // Chưa đăng nhập -> về trang login
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
        },
        error: function(xhr) {
            console.error('Không thể lấy thông tin user, dùng dữ liệu cũ');
            const userStr = localStorage.getItem('user');
            if (userStr) {
                renderUserToDOM(JSON.parse(userStr));
            } else {
                handleLogout();
            }
        }
    });
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
function handleLogout() {
    if (!confirm('Bạn có chắc muốn đăng xuất không?')) return;
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    window.location.href = '/pages/login.html';
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
    if (status === 'PENDING')  return '<span class="badge bg-warning text-dark">Chờ duyệt</span>';
    if (status === 'APPROVED') return '<span class="badge bg-success">Đã duyệt</span>';
    if (status === 'REJECTED') return '<span class="badge bg-danger">Từ chối</span>';
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
//  DASHBOARD – Hiện nhân viên gần đây (5 người)
// ===================================================
function renderDashboardEmployees() {
    const recent = employees.slice(0, 5); // Lấy 5 người đầu
    let html = '';
    recent.forEach(function(emp) {
        html += `<tr>
            <td class="ps-3">
                <div class="d-flex align-items-center">
                    ${empAvatar(emp)}
                    <div>
                        <div class="fw-medium" style="font-size:13px">${emp.name}</div>
                        <div class="text-muted" style="font-size:11px">${emp.email}</div>
                    </div>
                </div>
            </td>
            <td class="text-muted" style="font-size:13px">${emp.dept}</td>
            <td>${roleBadge(emp.role)}</td>
            <td>${statusBadge(emp.status)}</td>
        </tr>`;
    });
    $('#dashboard-emp-tbody').html(html);
}
renderDashboardEmployees();

// ===================================================
//  NHÂN VIÊN – Render bảng
// ===================================================
function renderEmployees(list) {
    // Dùng list nếu có (khi tìm kiếm), không thì dùng toàn bộ
    const data = list || employees;
    let html = '';
    data.forEach(function(emp, i) {
        html += `<tr>
            <td class="ps-3 text-muted">${i + 1}</td>
            <td>
                <div class="d-flex align-items-center">
                    ${empAvatar(emp)}
                    <div>
                        <div class="fw-medium" style="font-size:13px">${emp.name}</div>
                        <div class="text-muted" style="font-size:11px">${emp.email}</div>
                    </div>
                </div>
            </td>
            <td style="font-size:13px">${emp.phone}</td>
            <td style="font-size:13px">${emp.dept}</td>
            <td style="font-size:13px">${emp.position}</td>
            <td>${roleBadge(emp.role)}</td>
            <td style="font-size:13px">${formatVND(emp.salary)}</td>
            <td>${statusBadge(emp.status)}</td>
            <td class="text-center">
                <button class="btn btn-sm btn-outline-primary me-1" onclick="openEmpModal(${emp.id})">
                    <i class="fas fa-pen"></i>
                </button>
                <button class="btn btn-sm btn-outline-danger" onclick="deleteRow('employee', ${emp.id})">
                    <i class="fas fa-trash"></i>
                </button>
            </td>
        </tr>`;
    });
    $('#employee-tbody').html(html || '<tr><td colspan="9" class="text-center text-muted py-4">Không có dữ liệu</td></tr>');
}
renderEmployees();

// Tìm kiếm nhân viên theo tên hoặc email
function filterEmployees() {
    const keyword = $('#emp-search').val().toLowerCase().trim();
    const filtered = employees.filter(function(emp) {
        return emp.name.toLowerCase().includes(keyword) ||
               emp.email.toLowerCase().includes(keyword);
    });
    renderEmployees(filtered);
}

// ===================================================
//  NHÂN VIÊN – Mở modal thêm / sửa
// ===================================================
function openEmpModal(id) {
    // Reset form trước
    $('#emp-id').val('');
    $('#emp-name, #emp-email, #emp-phone, #emp-salary').val('');
    $('#emp-dept, #emp-position').val('');
    $('#emp-role').val('EMPLOYEE');
    $('#emp-status').val('active');
    $('#emp-modal-title').text('Thêm nhân viên mới');

    // Nếu có id → đang sửa → điền dữ liệu vào form
    if (id) {
        const emp = employees.find(e => e.id === id);
        if (!emp) return;
        $('#emp-modal-title').text('Chỉnh sửa nhân viên');
        $('#emp-id').val(emp.id);
        $('#emp-name').val(emp.name);
        $('#emp-email').val(emp.email);
        $('#emp-phone').val(emp.phone);
        $('#emp-salary').val(emp.salary);
        $('#emp-role').val(emp.role);
        $('#emp-status').val(emp.status);
        // Tìm và chọn dept, position
        $('#emp-dept option').filter(function() {
            return $(this).text() === emp.dept;
        }).prop('selected', true);
        $('#emp-position option').filter(function() {
            return $(this).text() === emp.position;
        }).prop('selected', true);
    }

    new bootstrap.Modal($('#employee-modal')[0]).show();
}

// ===================================================
//  NHÂN VIÊN – Lưu (thêm mới hoặc cập nhật)
// ===================================================
function saveEmployee() {
    const id   = parseInt($('#emp-id').val()) || null;
    const name = $('#emp-name').val().trim();
    const email= $('#emp-email').val().trim();

    if (!name || !email) {
        alert('Vui lòng nhập họ tên và email!');
        return;
    }

    const empData = {
        name:     name,
        email:    email,
        phone:    $('#emp-phone').val().trim(),
        dept:     $('#emp-dept').val(),
        position: $('#emp-position').val(),
        role:     $('#emp-role').val(),
        salary:   parseInt($('#emp-salary').val()) || 0,
        status:   $('#emp-status').val(),
        color:    '#' + Math.floor(Math.random()*16777215).toString(16).padStart(6,'0'), // màu ngẫu nhiên
    };

    if (id) {
        // Cập nhật nhân viên đã có
        const index = employees.findIndex(e => e.id === id);
        employees[index] = { ...employees[index], ...empData };
    } else {
        // Thêm nhân viên mới với id tự tăng
        empData.id = Date.now();
        employees.push(empData);
    }

    // Render lại bảng và đóng modal
    renderEmployees();
    renderDashboardEmployees();
    bootstrap.Modal.getInstance($('#employee-modal')[0]).hide();
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
            console.error('Lỗi lấy danh sách phòng ban:', xhr);
        }
    });
}

function renderDepartments() {
    let html = '';
    departments.forEach(function(dept, i) {
        html += `<tr>
            <td class="ps-3 text-muted">${i + 1}</td>
            <td class="fw-medium">${dept.name}</td>
            <td style="font-size:13px">${dept.managerName || 'Chưa có'}</td>
            <td><span class="badge bg-light text-dark border">${dept.count || 0} người</span></td>
            <td class="text-muted" style="font-size:13px">${dept.description || ''}</td>
            <td class="text-center">
                <button class="btn btn-sm btn-outline-primary me-1" onclick="openDeptModal(${dept.id})">
                    <i class="fas fa-pen"></i>
                </button>
                <button class="btn btn-sm btn-outline-danger" onclick="deleteRow('department', ${dept.id})">
                    <i class="fas fa-trash"></i>
                </button>
            </td>
        </tr>`;
    });
    $('#department-tbody').html(html || '<tr><td colspan="6" class="text-center text-muted py-4">Không có dữ liệu</td></tr>');

    // Cập nhật dropdown chọn phòng ban trong các modal khác
    const options = departments.map(d => `<option value="${d.name}">${d.name}</option>`).join('');
    $('#emp-dept, #pos-dept').html('<option value="">-- Chọn phòng ban --</option>' + options);
}
fetchDepartments();

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
            console.error('Lỗi lấy danh sách chức vụ:', xhr);
        }
    });
}

function renderPositions() {
    let html = '';
    positions.forEach(function(pos, i) {
        html += `<tr>
            <td class="ps-3 text-muted">${i + 1}</td>
            <td class="fw-medium">${pos.name}</td>
            <td style="font-size:13px">${formatVND(pos.baseSalary || 0)}</td>
            <td class="text-center">
                <button class="btn btn-sm btn-outline-primary me-1" onclick="openPosModal(${pos.id})">
                    <i class="fas fa-pen"></i>
                </button>
                <button class="btn btn-sm btn-outline-danger" onclick="deleteRow('position', ${pos.id})">
                    <i class="fas fa-trash"></i>
                </button>
            </td>
        </tr>`;
    });
    $('#position-tbody').html(html || '<tr><td colspan="4" class="text-center text-muted py-4">Không có dữ liệu</td></tr>');

    // Cập nhật dropdown chức vụ trong modal thêm nhân viên
    const options = positions.map(p => `<option value="${p.name}">${p.name}</option>`).join('');
    $('#emp-position').html('<option value="">-- Chọn chức vụ --</option>' + options);
}
fetchPositions();

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
//  CHẤM CÔNG – Render bảng
// ===================================================
function renderAttendances() {
    let html = '';
    attendances.forEach(function(att) {
        const statusColor = att.status === 'Đúng giờ' ? 'success' :
                            att.status === 'Đi muộn'  ? 'warning' : 'danger';
        html += `<tr>
            <td class="ps-3 fw-medium">${att.emp}</td>
            <td style="font-size:13px">${att.date}</td>
            <td style="font-size:13px">${att.checkIn  || '—'}</td>
            <td style="font-size:13px">${att.checkOut || '—'}</td>
            <td style="font-size:13px">${att.hours ? att.hours + 'h' : '—'}</td>
            <td><span class="badge bg-${statusColor}">${att.status}</span></td>
        </tr>`;
    });
    $('#attendance-tbody').html(html);
}
renderAttendances();

// Giả lập check-in / check-out
function doCheckIn() {
    const time = new Date().toLocaleTimeString('vi-VN', {hour:'2-digit', minute:'2-digit'});
    $('#checkin-status').html(`<span class="text-success"><i class="fas fa-check-circle me-1"></i>Đã check-in lúc <strong>${time}</strong></span>`);
    $('#btn-checkin').prop('disabled', true).addClass('opacity-50');
}
function doCheckOut() {
    const time = new Date().toLocaleTimeString('vi-VN', {hour:'2-digit', minute:'2-digit'});
    $('#checkin-status').html(`<span class="text-danger"><i class="fas fa-check-circle me-1"></i>Đã check-out lúc <strong>${time}</strong></span>`);
    $('#btn-checkout').prop('disabled', true).addClass('opacity-50');
}

// ===================================================
//  NGHỈ PHÉP – Render bảng
// ===================================================
function renderLeaveRequests() {
    let pending = 0;
    let html    = '';
    leaveRequests.forEach(function(leave, i) {
        if (leave.status === 'PENDING') pending++;
        const canApprove = leave.status === 'PENDING';
        html += `<tr>
            <td class="ps-3 text-muted">${i + 1}</td>
            <td class="fw-medium">${leave.emp}</td>
            <td style="font-size:13px">${leave.from}</td>
            <td style="font-size:13px">${leave.to}</td>
            <td><span class="badge bg-light text-dark border">${leave.days} ngày</span></td>
            <td class="text-muted" style="font-size:13px">${leave.reason}</td>
            <td>${statusBadge(leave.status)}</td>
            <td class="text-center">
                ${canApprove ? `
                <button class="btn btn-sm btn-success me-1" onclick="approveLeave(${leave.id})">
                    <i class="fas fa-check"></i>
                </button>
                <button class="btn btn-sm btn-danger" onclick="rejectLeave(${leave.id})">
                    <i class="fas fa-times"></i>
                </button>` : '—'}
            </td>
        </tr>`;
    });
    $('#leave-tbody').html(html);
    // Cập nhật số badge
    $('#leave-badge').text(pending).toggle(pending > 0);
}
renderLeaveRequests();

// Mở modal tạo đơn – điền dropdown nhân viên
function openLeaveModal() {
    $('#leave-employee').html('<option value="">-- Chọn nhân viên --</option>' +
        employees.map(e => `<option>${e.name}</option>`).join(''));
    $('#leave-from, #leave-to, #leave-reason').val('');
    new bootstrap.Modal($('#leave-modal')[0]).show();
}

// Lưu đơn nghỉ mới
function saveLeave() {
    const from = $('#leave-from').val();
    const to   = $('#leave-to').val();
    const emp  = $('#leave-employee').val();
    if (!emp || !from || !to || !$('#leave-reason').val()) {
        alert('Vui lòng điền đầy đủ thông tin!');
        return;
    }
    // Tính số ngày (đơn giản)
    const diff = Math.ceil((new Date(to) - new Date(from)) / 86400000) + 1;

    leaveRequests.push({
        id:     Date.now(),
        emp:    emp,
        from:   from.split('-').reverse().join('/'),
        to:     to.split('-').reverse().join('/'),
        days:   diff,
        reason: $('#leave-reason').val().trim(),
        status: 'PENDING',
    });
    renderLeaveRequests();
    bootstrap.Modal.getInstance($('#leave-modal')[0]).hide();
}

// Duyệt đơn
function approveLeave(id) {
    const leave = leaveRequests.find(l => l.id === id);
    if (leave) { leave.status = 'APPROVED'; renderLeaveRequests(); }
}

// Từ chối đơn
function rejectLeave(id) {
    const leave = leaveRequests.find(l => l.id === id);
    if (leave) { leave.status = 'REJECTED'; renderLeaveRequests(); }
}

// ===================================================
//  BẢNG LƯƠNG – Render
// ===================================================
function renderPayroll() {
    let html = '';
    employees.forEach(function(emp) {
        const bonus   = Math.round(emp.salary * 0.1);          // Phụ cấp 10%
        const deduct  = emp.status === 'inactive' ? emp.salary : 0; // Trừ nếu inactive
        const total   = emp.salary + bonus - deduct;
        const isPaid  = emp.status === 'active';
        html += `<tr>
            <td class="ps-3">
                <div class="d-flex align-items-center">
                    ${empAvatar(emp)}
                    <div>
                        <div class="fw-medium" style="font-size:13px">${emp.name}</div>
                        <div class="text-muted" style="font-size:11px">${emp.email}</div>
                    </div>
                </div>
            </td>
            <td style="font-size:13px">${emp.dept}</td>
            <td style="font-size:13px">${formatVND(emp.salary)}</td>
            <td style="font-size:13px" class="text-success">+${formatVND(bonus)}</td>
            <td style="font-size:13px" class="text-danger">-${formatVND(deduct)}</td>
            <td class="fw-bold">${formatVND(total)}</td>
            <td>${isPaid
                ? '<span class="badge bg-warning text-dark">Chưa trả</span>'
                : '<span class="badge bg-danger">Đã khấu trừ</span>'}</td>
        </tr>`;
    });
    $('#payroll-tbody').html(html);
}
renderPayroll();

// Tính lương (giả lập – sẽ gọi API /api/v1/payrolls/calculate sau)
function calculatePayroll() {
    const month = $('#payroll-month').val();
    alert(`Đang tính lương cho tháng ${month}...\n(Backend chưa sẵn sàng)`);
}

// ===================================================
//  XÓA DÒNG (dùng chung cho employee, department, position)
// ===================================================
function deleteRow(type, id) {
    // Hiện modal xác nhận
    $('#confirm-msg').text(`Bạn có chắc muốn xóa không? Hành động này không thể hoàn tác.`);

    // Xác nhận → thực hiện xóa
    $('#confirm-ok').off('click').on('click', function() {
        if (type === 'employee') {
            employees   = employees.filter(e => e.id !== id);
            renderEmployees();
            renderDashboardEmployees();
            bootstrap.Modal.getInstance($('#confirm-modal')[0]).hide();
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
