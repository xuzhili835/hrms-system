package com.a0615.controller;

import com.a0615.entity.Admin;
import com.a0615.service.AdminService;
import com.a0615.service.EmployeeService;
import com.a0615.service.SalaryService;
import com.a0615.service.LeaveApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // 导入 @PreAuthorize
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/admins") // 规范化为 /api 前缀
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private EmployeeService employeeService;
    
    @Autowired
    private SalaryService salaryService;
    
    @Autowired
    private LeaveApplicationService leaveApplicationService;

    /**
     * 获取所有管理员列表 (管理员权限)
     * GET /api/admins
     * @return 管理员列表
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')") // 只有管理员才能获取所有管理员列表
    public ResponseEntity<List<Admin>> getAllAdmins() {
        List<Admin> admins = adminService.getAllAdmins();
        return ResponseEntity.ok(admins);
    }

    /**
     * 获取仪表板统计数据
     * GET /api/admins/dashboard-stats
     * @return 统计数据
     */
    @GetMapping("/dashboard-stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 获取员工总数
        int totalEmployees = employeeService.getAllEmployees().size();
        stats.put("totalEmployees", totalEmployees);
        
        // 获取当前月份薪资总支出
        String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        BigDecimal monthlySalary = salaryService.getMonthlySalaryTotal(currentMonth);
        stats.put("monthlySalary", monthlySalary != null ? monthlySalary : BigDecimal.ZERO);
        
        // 获取待审批的请假申请数量
        int pendingApplications = leaveApplicationService.getPendingApplicationsCount();
        stats.put("pendingApplications", pendingApplications);
        
        // 获取部门分布数据
        Map<String, Integer> departmentStats = employeeService.getDepartmentStats();
        stats.put("departmentStats", departmentStats);
        
        return ResponseEntity.ok(stats);
    }

    /**
     * 获取月度数据趋势
     * GET /api/admins/monthly-trends
     * @return 月度趋势数据
     */
    @GetMapping("/monthly-trends")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getMonthlyTrends() {
        Map<String, Object> trends = new HashMap<>();
        
        // 获取最近6个月的数据
        List<String> months = new ArrayList<>();
        List<Integer> employeeCounts = new ArrayList<>();
        List<BigDecimal> salaryTotals = new ArrayList<>();
        
        for (int i = 5; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusMonths(i);
            String month = date.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            String monthLabel = date.format(DateTimeFormatter.ofPattern("MM月"));
            
            months.add(monthLabel);
            
            // 获取该月的薪资总额
            BigDecimal salaryTotal = salaryService.getMonthlySalaryTotal(month);
            salaryTotals.add(salaryTotal != null ? salaryTotal : BigDecimal.ZERO);
            
            // 获取该月的员工数量（简化为当前员工总数，实际项目中可能需要历史数据表）
            employeeCounts.add(employeeService.getAllEmployees().size());
        }
        
        trends.put("months", months);
        trends.put("employeeCounts", employeeCounts);
        trends.put("salaryTotals", salaryTotals);
        
        return ResponseEntity.ok(trends);
    }

    /**
     * 搜索管理员 (管理员权限)
     * GET /api/admins/search?username=xxx&role=yyy
     * @param username 用户名关键词 (可选)
     * @param role 角色名称 (可选)
     * @return 符合条件的管理员列表
     */
    @GetMapping("/search") // 处理 GET /api/admins/search 请求
    @PreAuthorize("hasRole('ADMIN')") // 只有管理员才能搜索管理员
    public ResponseEntity<List<Admin>> searchAdmins(
            @RequestParam(required = false) String username, // 从请求参数获取 username，非必需
            @RequestParam(required = false) String role) { // 从请求参数获取 role，非必需
        List<Admin> admins = adminService.searchAdmins(username, role); // 调用 service 搜索管理员
        return ResponseEntity.ok(admins); // 返回 200 OK 和搜索结果
    }

    /**
     * 根据ID获取单个管理员信息 (管理员权限)
     * GET /api/admins/{id}
     * @param id 管理员ID
     * @return 管理员对象
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // 只有管理员才能根据ID获取管理员信息
    public ResponseEntity<Admin> getAdminById(@PathVariable Integer id) {
        Admin admin = adminService.getAdminById(id);
        if (admin != null) {
            return ResponseEntity.ok(admin);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 添加新管理员 (管理员权限)
     * POST /api/admins
     * @param admin 管理员信息（JSON格式）
     * @return 添加后的管理员信息
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')") // 只有管理员才能添加新管理员
    public ResponseEntity<Admin> addAdmin(@RequestBody Admin admin) {
        adminService.addAdmin(admin);
        return ResponseEntity.status(HttpStatus.CREATED).body(admin);
    }

    /**
     * 更新管理员信息 (管理员权限)
     * PUT /api/admins/{id}
     * @param id 管理员ID
     * @param admin 更新的管理员信息（JSON格式）
     * @return 更新后的管理员信息或 404 Not Found
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // 只有管理员才能更新管理员信息
    public ResponseEntity<Admin> updateAdmin(@PathVariable Integer id, @RequestBody Admin admin) {
        admin.setId(id);
        Admin existingAdmin = adminService.getAdminById(id);
        if (existingAdmin != null) {
            adminService.updateAdmin(admin);
            return ResponseEntity.ok(admin);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 删除管理员 (管理员权限)
     * DELETE /api/admins/{id}
     * @param id 管理员ID
     * @return 无内容响应
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // 只有管理员才能删除管理员
    public ResponseEntity<Void> deleteAdmin(@PathVariable Integer id) {
        Admin existingAdmin = adminService.getAdminById(id);
        if (existingAdmin != null) {
            adminService.deleteAdmin(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 管理员修改自己的密码
     * PUT /api/admins/change-password
     * @param passwordData 包含新密码的请求体
     * @return 无内容响应
     */
    @PutMapping("/change-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> changeAdminPassword(@RequestBody Map<String, String> passwordData) {
        try {
            String newPassword = passwordData.get("newPassword");
            if (newPassword == null || newPassword.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            // 从当前认证信息获取管理员用户名
            String username = org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication().getName();
            
            Admin admin = adminService.getAdminByUsername(username);
            if (admin == null) {
                return ResponseEntity.notFound().build();
            }

            // 更新密码
            admin.setPwd(newPassword);
            adminService.updateAdmin(admin);
            
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}