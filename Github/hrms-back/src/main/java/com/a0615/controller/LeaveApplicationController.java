package com.a0615.controller;

import com.a0615.entity.LeaveApplication;
import com.a0615.service.LeaveApplicationService; // 导入 LeaveApplicationService
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // 导入 @PreAuthorize
import org.springframework.security.core.Authentication; // 导入 Authentication
import org.springframework.security.core.context.SecurityContextHolder; // 导入 SecurityContextHolder
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leave-applications") // 规范化为 /api 前缀
public class LeaveApplicationController {

    @Autowired
    private LeaveApplicationService leaveApplicationService;

    // 请注意：原代码中注入了 SalaryService 但未被使用，已移除。

    // --- 员工端接口 ---

    /**
     * 提交请假申请
     * POST /api/leave-applications
     * 仅限拥有 'EMPLOYEE' 角色的用户操作。
     * @param leaveApplication 请假申请实体（JSON 格式）
     * @return 成功状态（201 Created）和提交的请假申请信息，或错误状态（400 Bad Request / 500 Internal Server Error）。
     */
    @PostMapping
    @PreAuthorize("hasRole('EMPLOYEE')")
    // 员工只能为自己提交请假申请
    public ResponseEntity<LeaveApplication> submitLeaveApplication(@RequestBody LeaveApplication leaveApplication) {
        // 获取当前登录用户的用户名（即empId）
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentEmpId = authentication.getName();
        
        // 确保员工只能为自己提交请假申请
        leaveApplication.setEmpId(currentEmpId);
        // 校验请假日期逻辑
        if (leaveApplication.getStartDate().isAfter(leaveApplication.getEndDate())) {
            return ResponseEntity.badRequest().build();
        }

        try {
            leaveApplicationService.submitLeaveApplication(leaveApplication);
            return ResponseEntity.status(HttpStatus.CREATED).body(leaveApplication);
        } catch (Exception e) {
            // 记录异常，方便排查
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 员工查看自己的请假申请列表
     * GET /api/leave-applications/my-applications/{empId}
     * 仅限拥有 'EMPLOYEE' 角色的用户，且请求的 empId 必须与当前登录用户一致。
     * @param empId 员工工号 (String)
     * @return 该员工的请假申请列表（200 OK）。
     */
    @GetMapping("/my-applications/{empId}")
    @PreAuthorize("hasRole('EMPLOYEE') and #empId == authentication.principal.username")
    // 员工只能查看自己的请假申请
    public ResponseEntity<List<LeaveApplication>> getMyLeaveApplications(@PathVariable String empId) {
        // 由于 @PreAuthorize 已确保权限，此处直接调用服务
        List<LeaveApplication> applications = leaveApplicationService.getLeaveApplicationsByEmpId(empId);
        return ResponseEntity.ok(applications);
    }

    // --- 管理员端接口 ---

    /**
     * 获取所有请假申请列表（可根据状态筛选）
     * GET /api/leave-applications
     * 仅限拥有 'ADMIN' 角色的用户操作。
     * @param status 请假申请状态（可选，例如："待审核", "批准", "未批准"）
     * @return 符合条件的请假申请列表（200 OK）。
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')") // 只有管理员才能查看所有请假申请
    public ResponseEntity<List<LeaveApplication>> getAllLeaveApplications(@RequestParam(required = false) String status) {
        List<LeaveApplication> applications;
        if (status != null && !status.trim().isEmpty()) {
            // 对于中文状态，不需要 toUpperCase()。确保后端服务能处理中文状态。
            applications = leaveApplicationService.getLeaveApplicationsByStatus(status.trim());
        } else {
            applications = leaveApplicationService.getAllLeaveApplications();
        }
        return ResponseEntity.ok(applications);
    }

    /**
     * 根据ID获取单个请假申请详情（管理员）
     * GET /api/leave-applications/{id}
     * 仅限拥有 'ADMIN' 角色的用户操作。
     * @param id 请假申请ID (Long)
     * @return 请假申请对象（200 OK）或 404 Not Found。
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // 只有管理员才能根据ID获取请假申请
    public ResponseEntity<LeaveApplication> getLeaveApplicationById(@PathVariable Long id) {
        LeaveApplication application = leaveApplicationService.getLeaveApplicationById(id);
        if (application != null) {
            return ResponseEntity.ok(application);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 员工获取自己的请假申请详情
     * GET /api/leave-applications/my-applications/detail/{id}
     * 仅限拥有 'EMPLOYEE' 角色的用户，且只能查看自己的请假申请。
     * @param id 请假申请ID (Long)
     * @return 请假申请对象（200 OK）或 404 Not Found。
     */
    @GetMapping("/my-applications/detail/{id}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<LeaveApplication> getMyLeaveApplicationDetail(@PathVariable Long id) {
        // 获取当前登录用户的用户名（即empId）
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentEmpId = authentication.getName();
        
        LeaveApplication application = leaveApplicationService.getLeaveApplicationById(id);
        if (application == null) {
            return ResponseEntity.notFound().build();
        }
        
        // 验证是否是申请人本人
        if (!application.getEmpId().equals(currentEmpId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        return ResponseEntity.ok(application);
    }

    /**
     * 审核请假申请（更新请假申请状态）
     * PUT /api/leave-applications/{id}
     * 仅限拥有 'ADMIN' 角色的用户操作。
     * @param id 请假申请ID (Long)
     * @param requestBody 包含新状态的请求体（例如：{"status": "approved"} 或 {"status": "rejected"}）
     * @return 更新后的请假申请对象（200 OK），或错误状态（400 Bad Request）。
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // 只有管理员才能审核请假申请
    public ResponseEntity<LeaveApplication> updateLeaveApplication(@PathVariable Long id, @RequestBody LeaveApplication requestBody) {
        if (requestBody == null || requestBody.getStatus() == null || requestBody.getStatus().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        String newStatus = requestBody.getStatus().trim();

        // 校验传入的状态是否是有效状态
        if (!"approved".equals(newStatus) && !"rejected".equals(newStatus) && 
            !"pending".equals(newStatus) && !"批准".equals(newStatus) && !"未批准".equals(newStatus)) {
            return ResponseEntity.badRequest().body(null);
        }

        boolean success = leaveApplicationService.reviewLeaveApplication(id, newStatus);
        if (success) {
            LeaveApplication updatedApplication = leaveApplicationService.getLeaveApplicationById(id);
            return ResponseEntity.ok(updatedApplication);
        } else {
            // Service层如果返回false，通常表示ID不存在或状态不符合业务逻辑，这里统一返回400
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * 员工撤销自己的请假申请
     * DELETE /api/leave-applications/my-applications/{id}
     * 仅限拥有 'EMPLOYEE' 角色的用户，且只能撤销自己的待审批申请。
     * @param id 请假申请ID (Long)
     * @return 无内容响应（204 No Content），或错误状态。
     */
    @DeleteMapping("/my-applications/{id}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<String> cancelMyLeaveApplication(@PathVariable Long id) {
        // 获取当前登录用户的用户名（即empId）
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentEmpId = authentication.getName();
        
        LeaveApplication existingApplication = leaveApplicationService.getLeaveApplicationById(id);
        if (existingApplication == null) {
            return ResponseEntity.notFound().build();
        }
        
        // 验证是否是申请人本人
        if (!existingApplication.getEmpId().equals(currentEmpId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("只能撤销自己的请假申请");
        }
        
        // 验证申请状态是否允许撤销（支持多种状态表示）
        String status = existingApplication.getStatus();
        if (!"待审批".equals(status) && !"pending".equals(status) && !"待审核".equals(status)) {
            return ResponseEntity.badRequest().body("只能撤销待审批的请假申请，当前状态：" + status);
        }
        
        leaveApplicationService.deleteLeaveApplication(id);
        return ResponseEntity.ok("请假申请已撤销");
    }

    /**
     * 删除请假申请（管理员）
     * DELETE /api/leave-applications/{id}
     * 仅限拥有 'ADMIN' 角色的用户操作。
     * @param id 请假申请ID (Long)
     * @return 无内容响应（204 No Content），或 404 Not Found。
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // 只有管理员才能删除请假申请
    public ResponseEntity<Void> deleteLeaveApplication(@PathVariable Long id) {
        LeaveApplication existingApplication = leaveApplicationService.getLeaveApplicationById(id);
        if (existingApplication != null) {
            leaveApplicationService.deleteLeaveApplication(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}