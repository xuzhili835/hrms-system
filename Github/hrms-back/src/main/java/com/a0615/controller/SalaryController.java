package com.a0615.controller;

import com.a0615.entity.Salary;
import com.a0615.service.SalaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collection;

/**
 * 薪资控制器
 */
@RestController
@RequestMapping("/api/salaries")
@CrossOrigin(origins = "*")
public class SalaryController {

    @Autowired
    private SalaryService salaryService;

    /**
     * 获取薪资统计数据 - 必须放在最前面避免路由冲突
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getSalaryStatistics() {
        try {
            // 获取所有员工和薪资数据
            List<Salary> allSalaries = salaryService.list();
            Map<String, Object> statistics = new HashMap<>();
            
            if (allSalaries.isEmpty()) {
                statistics.put("totalSalary", BigDecimal.ZERO);
                statistics.put("averageSalary", BigDecimal.ZERO);
                statistics.put("highestSalary", BigDecimal.ZERO);
                statistics.put("totalEmployees", 0);
                statistics.put("departmentStats", new ArrayList<>());
                return ResponseEntity.ok(statistics);
            }
            
            // 获取每个员工的最新薪资记录（用于统计概览）
            Map<String, Salary> latestSalariesByEmployee = new HashMap<>();
            for (Salary salary : allSalaries) {
                String empId = salary.getEmpId();
                Salary existing = latestSalariesByEmployee.get(empId);
                if (existing == null || salary.getMonth().compareTo(existing.getMonth()) > 0) {
                    latestSalariesByEmployee.put(empId, salary);
                }
            }
            
            Collection<Salary> latestSalaries = latestSalariesByEmployee.values();
            
            // 基于最新薪资计算统计数据
            BigDecimal totalLatestSalary = latestSalaries.stream()
                .map(Salary::getTotalSalary)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // 计算平均薪资（基于员工数量，而不是薪资记录数量）
            BigDecimal averageSalary = latestSalaries.isEmpty() ? BigDecimal.ZERO :
                totalLatestSalary.divide(BigDecimal.valueOf(latestSalaries.size()), 2, BigDecimal.ROUND_HALF_UP);
            
            // 计算最高薪资（基于最新薪资）
            BigDecimal highestSalary = latestSalaries.stream()
                .map(Salary::getTotalSalary)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
            
            // 统计员工数量
            int totalEmployees = latestSalaries.size();
            
            // 对于统计卡片，显示的是基于最新薪资的数据
            statistics.put("totalSalary", totalLatestSalary);
            statistics.put("averageSalary", averageSalary);
            statistics.put("highestSalary", highestSalary);
            statistics.put("totalEmployees", totalEmployees);
            
            // 部门统计 - 基于真实数据库查询
            List<Map<String, Object>> departmentStats = salaryService.getDepartmentSalaryStatistics();
            statistics.put("departmentStats", departmentStats);
            
            System.out.println("=== 薪资统计数据调试 ===");
            System.out.println("总薪资记录数: " + allSalaries.size());
            System.out.println("有薪资记录的员工数: " + totalEmployees);
            System.out.println("最新薪资总和: " + totalLatestSalary);
            System.out.println("平均薪资: " + averageSalary);
            System.out.println("最高薪资: " + highestSalary);
            
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            System.err.println("获取薪资统计数据失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取部门薪资统计 - 也放在前面避免路由冲突
     */
    @GetMapping("/department-stats")
    public ResponseEntity<List<Map<String, Object>>> getDepartmentSalaryStats() {
        try {
            // 获取真实部门薪资统计数据
            List<Map<String, Object>> departmentStats = salaryService.getDepartmentSalaryStatistics();
            return ResponseEntity.ok(departmentStats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取员工薪资历史记录
     */
    @GetMapping("/employee/{empId}")
    public ResponseEntity<List<Salary>> getEmployeeSalaryHistory(@PathVariable String empId) {
        try {
            List<Salary> salaries = salaryService.getSalaryByEmpId(empId);
            return ResponseEntity.ok(salaries);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取员工薪资历史记录 - 前端调用的接口
     */
    @GetMapping("/my-salaries/{empId}")
    public ResponseEntity<List<Salary>> getMyEmployeeSalaryHistory(@PathVariable String empId) {
        try {
            List<Salary> salaries = salaryService.getSalaryByEmpId(empId);
            return ResponseEntity.ok(salaries);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取员工年度总收入
     */
    @GetMapping("/employee/{empId}/yearly/{year}")
    public ResponseEntity<Map<String, Object>> getEmployeeYearlyIncome(
            @PathVariable String empId, 
            @PathVariable String year) {
        try {
            List<Salary> salaries = salaryService.getSalaryByEmpIdAndYear(empId, year);
            
            BigDecimal totalIncome = salaries.stream()
                .map(Salary::getTotalSalary)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            Map<String, Object> result = new HashMap<>();
            result.put("totalIncome", totalIncome);
            result.put("year", year);
            result.put("empId", empId);
            result.put("salaryCount", salaries.size());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取员工当前月薪资
     */
    @GetMapping("/employee/{empId}/current")
    public ResponseEntity<Salary> getEmployeeCurrentSalary(@PathVariable String empId) {
        try {
            // 获取当前年月，格式：YYYY-MM
            String currentMonth = java.time.LocalDate.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM")
            );
            
            Salary salary = salaryService.getSalaryByEmpIdAndMonth(empId, currentMonth);
            if (salary != null) {
                return ResponseEntity.ok(salary);
            } else {
                // 如果当前月没有薪资记录，返回最新的一条记录
                List<Salary> salaries = salaryService.getSalaryByEmpId(empId);
                if (!salaries.isEmpty()) {
                    // 假设列表是按时间排序的，取最后一条
                    Salary latestSalary = salaries.get(salaries.size() - 1);
                    return ResponseEntity.ok(latestSalary);
                }
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取指定员工指定月份的薪资记录
     */
    @GetMapping("/employee/{empId}/month/{month}")
    public ResponseEntity<Salary> getEmployeeSalaryByMonth(
            @PathVariable String empId, 
            @PathVariable String month) {
        try {
            Salary salary = salaryService.getSalaryByEmpIdAndMonth(empId, month);
            if (salary != null) {
                return ResponseEntity.ok(salary);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取所有薪资记录（管理员用）
     */
    @GetMapping("/all")
    public ResponseEntity<List<Salary>> getAllSalaries() {
        try {
            List<Salary> salaries = salaryService.list();
            return ResponseEntity.ok(salaries);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取薪资记录列表（管理员用）
     */
    @GetMapping
    public ResponseEntity<List<Salary>> getSalaryRecords(
            @RequestParam(required = false) String empId,
            @RequestParam(required = false) String month,
            @RequestParam(required = false) String year,
            @RequestParam(required = false) String status) {
        try {
            List<Salary> salaries;
            if (empId != null && !empId.isEmpty()) {
                salaries = salaryService.getSalaryByEmpId(empId);
            } else {
                salaries = salaryService.list();
            }
            return ResponseEntity.ok(salaries);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 根据ID获取薪资详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Salary> getSalaryById(@PathVariable Long id) {
        try {
            Salary salary = salaryService.getById(id);
            if (salary != null) {
                return ResponseEntity.ok(salary);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 创建薪资记录（管理员用）
     */
    @PostMapping
    public ResponseEntity<Salary> createSalaryRecord(@RequestBody Salary salary) {
        try {
            // 设置创建时间
            salary.setCreatedAt(java.time.LocalDateTime.now());
            salary.setUpdatedAt(java.time.LocalDateTime.now());
            
            // 如果没有设置状态，默认为草稿
            if (salary.getStatus() == null || salary.getStatus().isEmpty()) {
                salary.setStatus("草稿");
            }
            
            // 保存薪资记录
            boolean saved = salaryService.save(salary);
            if (saved) {
                return ResponseEntity.ok(salary);
            } else {
                return ResponseEntity.internalServerError().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 更新薪资记录（管理员用）
     */
    @PutMapping("/{id}")
    public ResponseEntity<Salary> updateSalaryRecord(@PathVariable Long id, @RequestBody Salary salary) {
        try {
            // 设置ID和更新时间
            salary.setId(id);
            salary.setUpdatedAt(java.time.LocalDateTime.now());
            
            // 更新薪资记录
            boolean updated = salaryService.updateById(salary);
            if (updated) {
                Salary updatedSalary = salaryService.getById(id);
                return ResponseEntity.ok(updatedSalary);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 删除薪资记录（管理员用）
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSalaryRecord(@PathVariable Long id) {
        try {
            boolean deleted = salaryService.removeById(id);
            if (deleted) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 批量删除薪资记录
     */
    @DeleteMapping("/batch")
    public ResponseEntity<Map<String, Object>> batchDeleteSalaryRecords(@RequestBody List<Long> ids) {
        try {
            System.out.println("批量删除薪资记录，ID列表: " + ids);
            
            if (ids == null || ids.isEmpty()) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "请选择要删除的记录");
                return ResponseEntity.badRequest().body(result);
            }
            
            boolean result = salaryService.removeByIds(ids);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", result);
            response.put("message", result ? "删除成功" : "删除失败");
            response.put("deletedCount", result ? ids.size() : 0);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("批量删除薪资记录失败: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "删除失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * 导入薪资记录
     */
    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importSalaryRecords(@RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        try {
            System.out.println("开始导入薪资记录，文件名: " + file.getOriginalFilename());
            
            // 文件类型检查
            String fileName = file.getOriginalFilename();
            if (fileName == null || (!fileName.toLowerCase().endsWith(".xlsx") && !fileName.toLowerCase().endsWith(".xls"))) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "请上传Excel文件（.xlsx或.xls格式）");
                return ResponseEntity.badRequest().body(result);
            }
            
            // 文件大小检查
            if (file.getSize() > 10 * 1024 * 1024) { // 10MB限制
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "文件大小不能超过10MB");
                return ResponseEntity.badRequest().body(result);
            }
            
            Map<String, Object> importResult = salaryService.importSalaryRecords(file);
            return ResponseEntity.ok(importResult);
            
        } catch (Exception e) {
            System.err.println("导入薪资记录失败: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "导入失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * 获取月度薪资趋势数据
     */
    @GetMapping("/monthly-trend")
    public ResponseEntity<List<Map<String, Object>>> getMonthlySalaryTrend() {
        try {
            List<Map<String, Object>> trendData = salaryService.getMonthlySalaryTrend();
            return ResponseEntity.ok(trendData);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取薪资等级分布数据
     */
    @GetMapping("/level-distribution")
    public ResponseEntity<List<Map<String, Object>>> getSalaryLevelDistribution() {
        try {
            List<Map<String, Object>> levelData = salaryService.getSalaryLevelDistribution();
            return ResponseEntity.ok(levelData);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}