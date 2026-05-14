package com.hrmanagement.hr_management.ai;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hrmanagement.hr_management.entity.Attendance;
import com.hrmanagement.hr_management.entity.Employee;
import com.hrmanagement.hr_management.entity.LeaveRequest;
import com.hrmanagement.hr_management.entity.Notification;
import com.hrmanagement.hr_management.entity.Payroll;
import com.hrmanagement.hr_management.enums.AttendanceStatus;
import com.hrmanagement.hr_management.enums.LeaveStatus;
import com.hrmanagement.hr_management.enums.Role;
import com.hrmanagement.hr_management.repository.AttendanceRepository;
import com.hrmanagement.hr_management.repository.DepartmentRepository;
import com.hrmanagement.hr_management.repository.EmployeeRepository;
import com.hrmanagement.hr_management.repository.LeaveRequestRepository;
import com.hrmanagement.hr_management.repository.NotificationRepository;
import com.hrmanagement.hr_management.repository.PayrollRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AiService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final AttendanceRepository attendanceRepository;
    private final PayrollRepository payrollRepository;
    private final NotificationRepository notificationRepository;
    private final ObjectMapper objectMapper;

    private RestClient restClient = RestClient.create();

    @Value("${gemini.api.key}")
    private String apiKey;

    private static final String GEMINI_URL =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";

    private static final String SYSTEM_PROMPT = """
Bạn là trợ lý AI của hệ thống quản lý nhân sự (HR Management System) tại Việt Nam.
Bạn có thể:
1. Trả lời câu hỏi về chính sách nhân sự, quy trình làm việc
2. Tra cứu thông tin nhân viên, phòng ban, chức vụ
3. Xem thông tin chấm công, nghỉ phép, bảng lương
4. Hỗ trợ check-in / check-out, tạo đơn xin nghỉ phép
5. Duyệt hoặc từ chối đơn nghỉ phép (chỉ dành cho MANAGER và ADMIN)
6. Cung cấp thông tin thông báo từ công ty

LUÔN trả lời bằng tiếng Việt, lịch sự và chuyên nghiệp.
Nếu người dùng hỏi điều gì ngoài phạm vi HR, hãy từ chối nhẹ nhàng.
Khi cần tra cứu dữ liệu, hãy sử dụng các công cụ có sẵn.
""";

    private static final int MAX_HISTORY = 10;
    private static final int MAX_FUNCTION_STEPS = 5;

    @Transactional
    public String chat(String message, List<Map<String, String>> history, String userEmail) {
        Employee employee = employeeRepository.findByEmail(userEmail).orElse(null);
        if (employee == null) {
            return "Không tìm thấy thông tin tài khoản của bạn.";
        }

        try {
            String userContext = buildUserContext(employee);
            List<Map<String, Object>> contents = buildContents(history, message);
            String reply = runAiLoop(contents, userContext, employee);
            return reply;
        } catch (Exception e) {
            String msg = e.getMessage();
            e.printStackTrace();
            if (msg != null && msg.contains("Gemini API lỗi")) {
                return "Không thể kết nối với AI. Chi tiết: " + msg;
            }
            return "Xin lỗi, đã có lỗi xảy ra. Vui lòng thử lại sau.";
        }
    }

    private String buildUserContext(Employee emp) {
        return String.format("""
            THÔNG TIN NGƯỜI DÙNG HIỆN TẠI:
            - Họ tên: %s %s
            - Email: %s
            - Vai trò: %s
            - Phòng ban: %s
            - Chức vụ: %s
            - Ngày hôm nay: %s
            """,
            emp.getFirstName() != null ? emp.getFirstName() : "",
            emp.getLastName() != null ? emp.getLastName() : "",
            emp.getEmail() != null ? emp.getEmail() : "",
            emp.getRole() != null ? emp.getRole().name() : "",
            emp.getDepartment() != null ? emp.getDepartment().getName() : "Chưa có",
            emp.getPosition() != null ? emp.getPosition().getName() : "Chưa có",
            LocalDate.now().toString()
        );
    }

    private List<Map<String, Object>> buildContents(List<Map<String, String>> history, String message) {
        List<Map<String, Object>> contents = new ArrayList<>();

        if (history != null) {
            int start = Math.max(0, history.size() - MAX_HISTORY);
            for (int i = start; i < history.size(); i++) {
                Map<String, String> msg = history.get(i);
                contents.add(makeTextEntry(msg.get("role"), msg.get("content")));
            }
        }

        contents.add(makeTextEntry("user", message));
        return contents;
    }

    private Map<String, Object> makeTextEntry(String role, String text) {
        Map<String, Object> entry = new HashMap<>();
        entry.put("role", role);
        List<Map<String, Object>> parts = new ArrayList<>();
        Map<String, Object> part = new HashMap<>();
        part.put("text", text);
        parts.add(part);
        entry.put("parts", parts);
        return entry;
    }

    /**
     * Vòng lặp xử lý function calls. Gemini có thể yêu cầu nhiều function calls
     * liên tiếp. Vòng lặp tối đa MAX_FUNCTION_STEPS lần để tránh vô hạn.
     */
    private String runAiLoop(List<Map<String, Object>> contents, String userContext, Employee employee) throws JsonProcessingException {
        List<Map<String, Object>> currentContents = new ArrayList<>(contents);

        for (int step = 0; step < MAX_FUNCTION_STEPS; step++) {
            JsonNode response = callGemini(currentContents, userContext);

            JsonNode candidate = response.path("candidates").get(0);
            if (candidate == null) {
                JsonNode promptFeedback = response.path("promptFeedback");
                if (promptFeedback.has("blockReason")) {
                    return "Tin nhắn bị chặn do: " + promptFeedback.path("blockReason").asText();
                }
                return "Xin lỗi, không thể kết nối với AI. Vui lòng kiểm tra API key.";
            }

            JsonNode parts = candidate.path("content").path("parts");
            JsonNode functionCall = findFunctionCall(parts);

            if (functionCall == null) {
                return extractText(response);
            }

            // Có function call — thực thi và thêm kết quả vào history
            String functionName = functionCall.path("name").asText();
            JsonNode args = functionCall.path("args");
            ObjectNode functionResult = executeFunction(functionName, args, employee);

            // Thêm phản hồi của model (function call) vào contents
            Map<String, Object> modelEntry = new HashMap<>();
            modelEntry.put("role", "model");
            List<Map<String, Object>> modelParts = new ArrayList<>();
            Map<String, Object> fcPart = new HashMap<>();
            Map<String, Object> fcMap = new HashMap<>();
            fcMap.put("name", functionName);
            fcMap.put("args", (args != null && !args.isNull()) ? args : objectMapper.createObjectNode());
            fcPart.put("functionCall", fcMap);
            modelParts.add(fcPart);
            modelEntry.put("parts", modelParts);
            currentContents.add(modelEntry);

            // Thêm kết quả function vào contents
            Map<String, Object> funcEntry = new HashMap<>();
            funcEntry.put("role", "function");
            List<Map<String, Object>> funcParts = new ArrayList<>();
            Map<String, Object> funcPart = new HashMap<>();
            funcPart.put("functionResponse", Map.of(
                "name", functionName,
                "response", Map.of("result", functionResult)
            ));
            funcParts.add(funcPart);
            funcEntry.put("parts", funcParts);
            currentContents.add(funcEntry);
        }

        return "Xin lỗi, yêu cầu quá phức tạp để xử lý trong một lần. Vui lòng thử lại.";
    }

    private JsonNode callGemini(List<Map<String, Object>> contents, String userContext) {
        ObjectNode requestBody = objectMapper.createObjectNode();

        // Đưa userContext vào systemInstruction để AI biết người dùng là ai
        ObjectNode systemInstruction = requestBody.putObject("systemInstruction");
        systemInstruction.putArray("parts").addObject()
            .put("text", SYSTEM_PROMPT + "\n\n" + userContext);

        ArrayNode contentsArray = requestBody.putArray("contents");
        for (Map<String, Object> content : contents) {
            ObjectNode contentNode = objectMapper.createObjectNode();
            contentNode.put("role", (String) content.get("role"));
            ArrayNode partsArray = contentNode.putArray("parts");

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            for (Map<String, Object> part : parts) {
                ObjectNode partNode = objectMapper.createObjectNode();
                if (part.containsKey("text")) {
                    partNode.put("text", (String) part.get("text"));
                } else if (part.containsKey("functionCall")) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> fc = (Map<String, Object>) part.get("functionCall");
                    ObjectNode fcNode = objectMapper.createObjectNode();
                    fcNode.put("name", (String) fc.get("name"));
                    try {
                        fcNode.set("args", objectMapper.readTree(objectMapper.writeValueAsString(fc.get("args"))));
                    } catch (JsonProcessingException e) {
                        fcNode.set("args", objectMapper.createObjectNode());
                    }
                    partNode.set("functionCall", fcNode);
                } else if (part.containsKey("functionResponse")) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> fr = (Map<String, Object>) part.get("functionResponse");
                    ObjectNode frNode = objectMapper.createObjectNode();
                    frNode.put("name", (String) fr.get("name"));
                    try {
                        frNode.set("response", objectMapper.readTree(objectMapper.writeValueAsString(fr.get("response"))));
                    } catch (JsonProcessingException e) {
                        frNode.set("response", objectMapper.createObjectNode());
                    }
                    partNode.set("functionResponse", frNode);
                }
                partsArray.add(partNode);
            }
            contentsArray.add(contentNode);
        }

        // Khai báo tất cả functions
        ObjectNode tool = requestBody.putArray("tools").addObject();
        ArrayNode functionDeclarations = tool.putArray("functionDeclarations");

        functionDeclarations.add(buildSimpleFunction("get_my_info", "Lấy thông tin chi tiết của người dùng hiện tại"));
        functionDeclarations.add(buildFunctionDecl("get_employees_by_department",
            "Lấy danh sách nhân viên theo tên phòng ban",
            buildParamProps().set("departmentName", buildStringParam("Tên phòng ban"))));
        functionDeclarations.add(buildSimpleFunction("get_leave_balance", "Lấy số ngày phép còn lại của người dùng"));
        functionDeclarations.add(buildSimpleFunction("get_my_leave_requests", "Lấy danh sách đơn xin nghỉ phép của người dùng"));
        functionDeclarations.add(buildFunctionDecl("get_attendance_summary", "Lấy tổng kết chấm công theo tháng",
            buildParamProps()
                .<ObjectNode>set("month", buildIntParam("Tháng (1-12)"))
                .<ObjectNode>set("year", buildIntParam("Năm"))));
        functionDeclarations.add(buildFunctionDecl("get_payroll_info", "Lấy thông tin lương theo tháng",
            buildParamProps()
                .<ObjectNode>set("month", buildIntParam("Tháng (1-12)"))
                .<ObjectNode>set("year", buildIntParam("Năm"))));
        functionDeclarations.add(buildFunctionDecl("create_leave_request", "Tạo đơn xin nghỉ phép mới",
            buildParamProps()
                .<ObjectNode>set("startDate", buildStringParam("Ngày bắt đầu nghỉ (YYYY-MM-DD)"))
                .<ObjectNode>set("endDate", buildStringParam("Ngày kết thúc nghỉ (YYYY-MM-DD)"))
                .<ObjectNode>set("reasonCategory", buildStringParam("Lý do chính: Ốm đau, Có việc gia đình, Du lịch / Nghỉ mát, Khám thai / Sinh con, Khác"))
                .<ObjectNode>set("reason", buildStringParam("Chi tiết lý do"))));
        functionDeclarations.add(buildSimpleFunction("get_company_policies", "Lấy danh sách thông báo/chính sách công ty"));
        functionDeclarations.add(buildSimpleFunction("get_departments", "Lấy danh sách tất cả phòng ban và số nhân viên"));
        functionDeclarations.add(buildSimpleFunction("check_in", "Thực hiện check-in (chấm công vào làm) cho ngày hôm nay"));
        functionDeclarations.add(buildSimpleFunction("check_out", "Thực hiện check-out (chấm công ra về) cho ngày hôm nay"));
        functionDeclarations.add(buildFunctionDecl("approve_leave_request",
            "Duyệt đơn xin nghỉ phép (chỉ MANAGER và ADMIN mới dùng được)",
            buildParamProps().set("leaveRequestId", buildIntParam("ID của đơn xin nghỉ phép cần duyệt"))));
        functionDeclarations.add(buildFunctionDecl("reject_leave_request",
            "Từ chối đơn xin nghỉ phép (chỉ MANAGER và ADMIN mới dùng được)",
            buildParamProps()
                .<ObjectNode>set("leaveRequestId", buildIntParam("ID của đơn xin nghỉ phép cần từ chối"))
                .<ObjectNode>set("reason", buildStringParam("Lý do từ chối (tùy chọn)"))));

        String jsonBody;
        try {
            jsonBody = objectMapper.writeValueAsString(requestBody);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Lỗi serialize request", e);
        }

        String url = GEMINI_URL + "?key=" + apiKey;
        String responseBody = restClient.post()
            .uri(url)
            .header("Content-Type", "application/json")
            .body(jsonBody)
            .retrieve()
            .onStatus(status -> status.isError(), (request, response) -> {
                try {
                    String body = new String(response.getBody().readAllBytes());
                    throw new RuntimeException("Gemini API lỗi " + response.getStatusCode() + ": " + body);
                } catch (java.io.IOException ex) {
                    throw new RuntimeException("Gemini API lỗi " + response.getStatusCode());
                }
            })
            .body(String.class);

        try {
            return objectMapper.readTree(responseBody);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Lỗi parse Gemini response", e);
        }
    }

    private ObjectNode buildFunctionDecl(String name, String description, ObjectNode properties) {
        ObjectNode decl = objectMapper.createObjectNode();
        decl.put("name", name);
        decl.put("description", description);
        ObjectNode parameters = objectMapper.createObjectNode();
        parameters.put("type", "OBJECT");
        parameters.set("properties", properties);
        decl.set("parameters", parameters);
        return decl;
    }

    private ObjectNode buildSimpleFunction(String name, String description) {
        ObjectNode decl = objectMapper.createObjectNode();
        decl.put("name", name);
        decl.put("description", description);
        ObjectNode parameters = objectMapper.createObjectNode();
        parameters.put("type", "OBJECT");
        parameters.set("properties", objectMapper.createObjectNode());
        decl.set("parameters", parameters);
        return decl;
    }

    private ObjectNode buildParamProps() {
        return objectMapper.createObjectNode();
    }

    private ObjectNode buildStringParam(String description) {
        ObjectNode p = objectMapper.createObjectNode();
        p.put("type", "STRING");
        p.put("description", description);
        return p;
    }

    private ObjectNode buildIntParam(String description) {
        ObjectNode p = objectMapper.createObjectNode();
        p.put("type", "INTEGER");
        p.put("description", description);
        return p;
    }

    private JsonNode findFunctionCall(JsonNode parts) {
        if (parts == null || !parts.isArray()) return null;
        for (JsonNode part : parts) {
            if (part.has("functionCall")) {
                return part.get("functionCall");
            }
        }
        return null;
    }

    private String extractText(JsonNode response) {
        JsonNode candidates = response.path("candidates");
        if (!candidates.isArray() || candidates.isEmpty()) {
            JsonNode promptFeedback = response.path("promptFeedback");
            if (promptFeedback.has("blockReason")) {
                return "Tin nhắn bị chặn do: " + promptFeedback.path("blockReason").asText();
            }
            return "Xin lỗi, tôi không thể xử lý yêu cầu này.";
        }

        StringBuilder text = new StringBuilder();
        JsonNode parts = candidates.get(0).path("content").path("parts");
        for (JsonNode part : parts) {
            if (part.has("text")) {
                text.append(part.get("text").asText());
            }
        }
        String result = text.toString().trim();
        return result.isEmpty() ? "Xin lỗi, tôi không thể xử lý yêu cầu này." : result;
    }

    private ObjectNode executeFunction(String functionName, JsonNode args, Employee employee) {
        switch (functionName) {
            case "get_my_info": return getMyInfo(employee);
            case "get_employees_by_department": return getEmployeesByDepartment(args);
            case "get_leave_balance": return getLeaveBalance(employee);
            case "get_my_leave_requests": return getMyLeaveRequests(employee);
            case "get_attendance_summary": return getAttendanceSummary(args, employee);
            case "get_payroll_info": return getPayrollInfo(args, employee);
            case "create_leave_request": return createLeaveRequest(args, employee);
            case "get_company_policies": return getCompanyPolicies();
            case "get_departments": return getDepartments();
            case "check_in": return checkIn(employee);
            case "check_out": return checkOut(employee);
            case "approve_leave_request": return updateLeaveStatus(args, employee, LeaveStatus.approved);
            case "reject_leave_request": return updateLeaveStatus(args, employee, LeaveStatus.rejected);
            default:
                ObjectNode result = objectMapper.createObjectNode();
                result.put("error", "Chức năng không được hỗ trợ: " + functionName);
                return result;
        }
    }

    // ===== Function implementations =====

    private ObjectNode getMyInfo(Employee emp) {
        ObjectNode result = objectMapper.createObjectNode();
        result.put("hoTen", (emp.getFirstName() != null ? emp.getFirstName() : "") + " " + (emp.getLastName() != null ? emp.getLastName() : ""));
        result.put("email", emp.getEmail());
        result.put("vaiTro", emp.getRole() != null ? emp.getRole().name() : "");
        result.put("soDienThoai", emp.getPhone() != null ? emp.getPhone() : "Chưa cập nhật");
        result.put("phongBan", emp.getDepartment() != null ? emp.getDepartment().getName() : "Chưa có");
        result.put("chucVu", emp.getPosition() != null ? emp.getPosition().getName() : "Chưa có");
        result.put("luongCoBan", emp.getBaseSalary() != null ? emp.getBaseSalary().toString() : "0");
        result.put("phuCap", emp.getAllowance() != null ? emp.getAllowance().toString() : "0");
        result.put("trangThai", emp.getStatus() != null ? emp.getStatus().name() : "");
        result.put("ngayBatDau", emp.getStartDate() != null ? emp.getStartDate().toString() : "");
        return result;
    }

    private ObjectNode getEmployeesByDepartment(JsonNode args) {
        ObjectNode result = objectMapper.createObjectNode();
        String deptName = args.has("departmentName") ? args.get("departmentName").asText() : "";

        var departments = departmentRepository.findAll();
        var dept = departments.stream()
            .filter(d -> d.getName().toLowerCase().contains(deptName.toLowerCase()))
            .findFirst();

        if (dept.isEmpty()) {
            result.put("message", "Không tìm thấy phòng ban: " + deptName);
            ArrayNode list = result.putArray("phongBanCoSan");
            departments.forEach(d -> list.add(d.getName()));
            return result;
        }

        var employees = employeeRepository.findByDepartment(dept.get());
        result.put("phongBan", dept.get().getName());
        result.put("soLuong", employees.size());
        ArrayNode empList = result.putArray("danhSach");
        employees.forEach(e -> {
            ObjectNode emp = empList.addObject();
            emp.put("hoTen", e.getFirstName() + " " + e.getLastName());
            emp.put("email", e.getEmail());
            emp.put("chucVu", e.getPosition() != null ? e.getPosition().getName() : "Chưa có");
            emp.put("vaiTro", e.getRole().name());
            emp.put("trangThai", e.getStatus().name());
        });
        return result;
    }

    private ObjectNode getLeaveBalance(Employee emp) {
        ObjectNode result = objectMapper.createObjectNode();
        long approvedLeaveDays = leaveRequestRepository.findByEmployee(emp).stream()
            .filter(lr -> lr.getStatus() == LeaveStatus.approved)
            .filter(lr -> lr.getStartDate().getYear() == LocalDate.now().getYear())
            .mapToLong(lr -> java.time.temporal.ChronoUnit.DAYS.between(lr.getStartDate(), lr.getEndDate()) + 1)
            .sum();
        long pendingDays = leaveRequestRepository.findByEmployee(emp).stream()
            .filter(lr -> lr.getStatus() == LeaveStatus.pending)
            .filter(lr -> lr.getStartDate().getYear() == LocalDate.now().getYear())
            .mapToLong(lr -> java.time.temporal.ChronoUnit.DAYS.between(lr.getStartDate(), lr.getEndDate()) + 1)
            .sum();
        result.put("tongNgayPhepHangNam", 12);
        result.put("daDung", approvedLeaveDays);
        result.put("dangChoDuyet", pendingDays);
        result.put("conLai", Math.max(0, 12 - approvedLeaveDays));
        return result;
    }

    private ObjectNode getMyLeaveRequests(Employee emp) {
        ObjectNode result = objectMapper.createObjectNode();
        var requests = leaveRequestRepository.findByEmployee(emp);
        ArrayNode list = result.putArray("danhSach");
        requests.forEach(lr -> {
            ObjectNode item = list.addObject();
            item.put("id", lr.getId());
            item.put("ngayBatDau", lr.getStartDate().toString());
            item.put("ngayKetThuc", lr.getEndDate().toString());
            item.put("soNgay", java.time.temporal.ChronoUnit.DAYS.between(lr.getStartDate(), lr.getEndDate()) + 1);
            item.put("lyDo", lr.getReasonCategory() != null ? lr.getReasonCategory() : "");
            item.put("chiTiet", lr.getReason() != null ? lr.getReason() : "");
            item.put("trangThai", lr.getStatus().name());
            item.put("nguoiDuyet", lr.getApprovedBy() != null ?
                lr.getApprovedBy().getFirstName() + " " + lr.getApprovedBy().getLastName() : "Chưa duyệt");
        });
        return result;
    }

    private ObjectNode getAttendanceSummary(JsonNode args, Employee emp) {
        int month = args.has("month") ? args.get("month").asInt() : LocalDate.now().getMonthValue();
        int year = args.has("year") ? args.get("year").asInt() : LocalDate.now().getYear();

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        var attendances = attendanceRepository.findByEmployeeIdAndDateBetween(emp.getId(), start, end);

        ObjectNode result = objectMapper.createObjectNode();
        result.put("thang", month);
        result.put("nam", year);
        result.put("tongNgayCong", attendances.size());
        result.put("diDungGio", attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.present).count());
        result.put("diMuon", attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.late).count());

        double totalHours = attendances.stream()
            .filter(a -> a.getCheckIn() != null && a.getCheckOut() != null)
            .mapToDouble(a -> {
                long minutes = java.time.Duration.between(a.getCheckIn(), a.getCheckOut()).toMinutes();
                return Math.max(0, minutes) / 60.0;
            })
            .sum();
        result.put("tongGioLam", Math.round(totalHours * 10.0) / 10.0);

        return result;
    }

    private ObjectNode getPayrollInfo(JsonNode args, Employee emp) {
        int month = args.has("month") ? args.get("month").asInt() : LocalDate.now().getMonthValue();
        int year = args.has("year") ? args.get("year").asInt() : LocalDate.now().getYear();

        ObjectNode result = objectMapper.createObjectNode();
        var payrollOpt = payrollRepository.findByEmployeeIdAndMonthAndYear(emp.getId(), month, year);
        if (payrollOpt.isPresent()) {
            Payroll p = payrollOpt.get();
            result.put("thang", p.getMonth());
            result.put("nam", p.getYear());
            result.put("luongCoBan", p.getBaseSalary() != null ? p.getBaseSalary().toString() : "0");
            result.put("phuCap", p.getAllowance() != null ? p.getAllowance().toString() : "0");
            result.put("khauTru", p.getDeduction() != null ? p.getDeduction().toString() : "0");
            result.put("thucNhan", p.getNetSalary() != null ? p.getNetSalary().toString() : "0");
            result.put("ngayCong", p.getWorkingDays() != null ? p.getWorkingDays().toString() : "0");
        } else {
            result.put("message", "Chưa có dữ liệu lương tháng " + month + "/" + year);
        }
        return result;
    }

    private ObjectNode createLeaveRequest(JsonNode args, Employee emp) {
        ObjectNode result = objectMapper.createObjectNode();
        try {
            String startDateStr = args.get("startDate").asText();
            String endDateStr = args.get("endDate").asText();
            String reasonCategory = args.has("reasonCategory") ? args.get("reasonCategory").asText() : "Khác";
            String reason = args.has("reason") ? args.get("reason").asText() : "";

            LocalDate startDate = LocalDate.parse(startDateStr);
            LocalDate endDate = LocalDate.parse(endDateStr);

            if (!startDate.isAfter(LocalDate.now())) {
                result.put("error", "Ngày bắt đầu phải sau hôm nay ít nhất 1 ngày.");
                result.put("success", false);
                return result;
            }
            if (startDate.isAfter(endDate)) {
                result.put("error", "Ngày kết thúc phải sau ngày bắt đầu.");
                result.put("success", false);
                return result;
            }

            LeaveRequest request = new LeaveRequest();
            request.setEmployee(emp);
            request.setStartDate(startDate);
            request.setEndDate(endDate);
            request.setReasonCategory(reasonCategory);
            request.setReason(reason);
            request.setStatus(LeaveStatus.pending);

            leaveRequestRepository.save(request);

            result.put("success", true);
            result.put("message", "Đã tạo đơn xin nghỉ phép thành công từ " + startDate + " đến " + endDate);
            result.put("lyDo", reasonCategory);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "Lỗi khi tạo đơn: " + e.getMessage());
        }
        return result;
    }

    private ObjectNode getCompanyPolicies() {
        ObjectNode result = objectMapper.createObjectNode();
        List<Notification> notifications = notificationRepository.findAllByOrderByPublishedDateDesc();
        ArrayNode list = result.putArray("thongBao");
        notifications.forEach(n -> {
            ObjectNode item = list.addObject();
            item.put("tieuDe", n.getTitle());
            item.put("noiDung", n.getContent());
            item.put("ngayDang", n.getPublishedDate() != null ? n.getPublishedDate().toString() : "");
            item.put("nguoiDang", n.getPublishedBy() != null ? n.getPublishedBy() : "");
            item.put("loai", n.getType() != null ? n.getType() : "info");
        });
        result.put("soLuong", notifications.size());
        return result;
    }

    private ObjectNode getDepartments() {
        ObjectNode result = objectMapper.createObjectNode();
        var departments = departmentRepository.findAll();
        ArrayNode list = result.putArray("danhSach");
        departments.forEach(d -> {
            ObjectNode item = list.addObject();
            item.put("id", d.getId());
            item.put("ten", d.getName());
            item.put("moTa", d.getDescription() != null ? d.getDescription() : "");
            long count = employeeRepository.countByDepartmentId(d.getId());
            item.put("soNhanVien", count);
        });
        result.put("soLuong", departments.size());
        return result;
    }

    private ObjectNode checkIn(Employee emp) {
        ObjectNode result = objectMapper.createObjectNode();
        try {
            LocalDate today = LocalDate.now();
            LocalTime now = LocalTime.now();

            if (now.isBefore(LocalTime.of(6, 0)) || now.isAfter(LocalTime.of(20, 0))) {
                result.put("success", false);
                result.put("error", "Giờ check-in không hợp lệ. Vui lòng check-in từ 06:00 đến 20:00.");
                return result;
            }

            if (attendanceRepository.findByEmployeeIdAndDate(emp.getId(), today).isPresent()) {
                result.put("success", false);
                result.put("error", "Bạn đã check-in ngày hôm nay rồi!");
                return result;
            }

            Attendance attendance = new Attendance();
            attendance.setEmployee(emp);
            attendance.setDate(today);
            attendance.setCheckIn(now);

            LocalTime lateThreshold = LocalTime.of(8, 30);
            attendance.setStatus(now.isAfter(lateThreshold) ? AttendanceStatus.late : AttendanceStatus.present);

            attendanceRepository.save(attendance);

            result.put("success", true);
            result.put("thoiGian", now.toString());
            result.put("trangThai", attendance.getStatus() == AttendanceStatus.late ? "Đi muộn" : "Đúng giờ");
            result.put("message", "Check-in thành công lúc " + now.getHour() + ":" + String.format("%02d", now.getMinute()));
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "Lỗi khi check-in: " + e.getMessage());
        }
        return result;
    }

    private ObjectNode checkOut(Employee emp) {
        ObjectNode result = objectMapper.createObjectNode();
        try {
            LocalDate today = LocalDate.now();
            LocalTime now = LocalTime.now();

            var attendanceOpt = attendanceRepository.findByEmployeeIdAndDate(emp.getId(), today);
            if (attendanceOpt.isEmpty()) {
                result.put("success", false);
                result.put("error", "Bạn chưa check-in ngày hôm nay!");
                return result;
            }

            Attendance attendance = attendanceOpt.get();
            if (attendance.getCheckOut() != null) {
                result.put("success", false);
                result.put("error", "Bạn đã check-out rồi lúc " + attendance.getCheckOut() + "!");
                return result;
            }

            attendance.setCheckOut(now);
            attendanceRepository.save(attendance);

            long minutes = java.time.Duration.between(attendance.getCheckIn(), now).toMinutes();
            double hours = Math.max(0, minutes) / 60.0;

            result.put("success", true);
            result.put("thoiGian", now.toString());
            result.put("soGioLam", Math.round(hours * 10.0) / 10.0);
            result.put("message", "Check-out thành công lúc " + now.getHour() + ":" + String.format("%02d", now.getMinute())
                + ". Tổng giờ làm: " + String.format("%.1f", hours) + " giờ.");
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "Lỗi khi check-out: " + e.getMessage());
        }
        return result;
    }

    private ObjectNode updateLeaveStatus(JsonNode args, Employee approver, LeaveStatus newStatus) {
        ObjectNode result = objectMapper.createObjectNode();
        try {
            // Chỉ MANAGER và ADMIN mới được duyệt/từ chối
            if (approver.getRole() == Role.EMPLOYEE) {
                result.put("success", false);
                result.put("error", "Bạn không có quyền duyệt đơn xin nghỉ phép. Chỉ MANAGER và ADMIN mới có quyền này.");
                return result;
            }

            if (!args.has("leaveRequestId")) {
                result.put("success", false);
                result.put("error", "Thiếu ID đơn xin nghỉ phép.");
                return result;
            }

            Long leaveRequestId = args.get("leaveRequestId").asLong();
            var leaveOpt = leaveRequestRepository.findById(leaveRequestId);
            if (leaveOpt.isEmpty()) {
                result.put("success", false);
                result.put("error", "Không tìm thấy đơn xin nghỉ phép với ID: " + leaveRequestId);
                return result;
            }

            LeaveRequest leaveRequest = leaveOpt.get();

            if (leaveRequest.getStatus() != LeaveStatus.pending) {
                result.put("success", false);
                result.put("error", "Đơn này đã được xử lý (trạng thái hiện tại: " + leaveRequest.getStatus().name() + ")");
                return result;
            }

            if (approver.getId().equals(leaveRequest.getEmployee().getId())) {
                result.put("success", false);
                result.put("error", "Bạn không thể tự duyệt đơn nghỉ phép của chính mình!");
                return result;
            }

            // MANAGER chỉ được duyệt nhân viên cùng phòng ban
            if (approver.getRole() == Role.MANAGER) {
                Long empDeptId = leaveRequest.getEmployee().getDepartment() != null
                    ? leaveRequest.getEmployee().getDepartment().getId() : null;
                Long mgrDeptId = approver.getDepartment() != null ? approver.getDepartment().getId() : null;
                if (empDeptId == null || !empDeptId.equals(mgrDeptId)) {
                    result.put("success", false);
                    result.put("error", "Bạn chỉ có quyền duyệt đơn của nhân viên trong cùng phòng ban của mình.");
                    return result;
                }
            }

            leaveRequest.setStatus(newStatus);
            leaveRequest.setApprovedBy(approver);
            leaveRequestRepository.save(leaveRequest);

            String action = newStatus == LeaveStatus.approved ? "duyệt" : "từ chối";
            String employeeName = leaveRequest.getEmployee().getFirstName() + " " + leaveRequest.getEmployee().getLastName();
            result.put("success", true);
            result.put("message", "Đã " + action + " đơn xin nghỉ phép của " + employeeName
                + " (từ " + leaveRequest.getStartDate() + " đến " + leaveRequest.getEndDate() + ")");
            result.put("trangThai", newStatus.name());
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "Lỗi khi xử lý đơn: " + e.getMessage());
        }
        return result;
    }
}
