package com.a0615.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.a0615.entity.Salary;
import com.a0615.entity.Employee;
import com.a0615.mapper.SalaryMapper;
import com.a0615.mapper.EmployeeMapper;
import com.a0615.service.SalaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 薪资服务实现类
 */
@Service
public class SalaryServiceImpl extends ServiceImpl<SalaryMapper, Salary> implements SalaryService {

    @Autowired
    private EmployeeMapper employeeMapper;

    @Override
    public List<Salary> getSalaryByEmpId(String empId) {
        QueryWrapper<Salary> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("emp_id", empId);
        queryWrapper.orderByDesc("month");
        return this.list(queryWrapper);
    }

    @Override
    public List<Salary> getSalariesByEmpId(String empId) {
        // 复数形式的方法，实现与getSalaryByEmpId相同
        return getSalaryByEmpId(empId);
    }

    @Override
    public Salary getSalaryByEmpIdAndMonth(String empId, String month) {
        QueryWrapper<Salary> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("emp_id", empId);
        queryWrapper.eq("month", month);
        return this.getOne(queryWrapper);
    }

    @Override
    public List<Salary> getSalaryByEmpIdAndYear(String empId, String year) {
        QueryWrapper<Salary> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("emp_id", empId);
        queryWrapper.likeRight("month", year); // 查询月份字段以指定年份开头的记录，如 "2024-01", "2024-02" 等
        queryWrapper.orderByAsc("month");
        return this.list(queryWrapper);
    }

    @Override
    public BigDecimal getMonthlySalaryTotal(String month) {
        QueryWrapper<Salary> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("month", month);
        List<Salary> salaries = this.list(queryWrapper);
        
        BigDecimal total = BigDecimal.ZERO;
        for (Salary salary : salaries) {
            if (salary.getTotalSalary() != null) {
                total = total.add(salary.getTotalSalary());
            }
        }
        
        return total;
    }

    @Override
    public List<Map<String, Object>> getDepartmentSalaryStatistics() {
        // 获取所有员工和薪资数据
        List<Employee> allEmployees = employeeMapper.selectList(null);
        List<Salary> allSalaries = this.list();
        
        System.out.println("=== 部门薪资统计调试开始 ===");
        System.out.println("总员工数: " + allEmployees.size());
        System.out.println("总薪资记录数: " + allSalaries.size());
        
        // 获取每个员工的最新薪资记录
        Map<String, Salary> latestSalariesByEmployee = new HashMap<>();
        for (Salary salary : allSalaries) {
            String empId = salary.getEmpId();
            Salary existing = latestSalariesByEmployee.get(empId);
            if (existing == null || salary.getMonth().compareTo(existing.getMonth()) > 0) {
                latestSalariesByEmployee.put(empId, salary);
            }
        }
        
        System.out.println("有最新薪资记录的员工数: " + latestSalariesByEmployee.size());
        
        // 计算总薪资用于计算比例
        BigDecimal totalAllLatestSalary = latestSalariesByEmployee.values().stream()
            .map(Salary::getTotalSalary)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 按部门分组统计员工
        Map<String, List<Employee>> employeesByDept = allEmployees.stream()
            .filter(emp -> emp.getDept() != null && !emp.getDept().trim().isEmpty())
            .filter(emp -> "在职".equals(emp.getStatus())) // 只统计在职员工
            .collect(Collectors.groupingBy(Employee::getDept));
        
        List<Map<String, Object>> departmentStats = new ArrayList<>();
        
        for (Map.Entry<String, List<Employee>> entry : employeesByDept.entrySet()) {
            String department = entry.getKey();
            List<Employee> deptEmployees = entry.getValue();
            
            // 获取该部门员工的最新薪资记录
            List<Salary> deptLatestSalaries = deptEmployees.stream()
                .map(Employee::getEmpId)
                .map(empId -> latestSalariesByEmployee.get(empId))
                .filter(salary -> salary != null)
                .collect(Collectors.toList());
            
            if (!deptLatestSalaries.isEmpty()) {
                Map<String, Object> deptStat = new HashMap<>();
                
                // 基础统计
                BigDecimal totalSalary = deptLatestSalaries.stream()
                    .map(Salary::getTotalSalary)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                BigDecimal averageSalary = totalSalary.divide(
                    BigDecimal.valueOf(deptLatestSalaries.size()), 2, RoundingMode.HALF_UP);
                
                BigDecimal highestSalary = deptLatestSalaries.stream()
                    .map(Salary::getTotalSalary)
                    .max(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);
                
                BigDecimal lowestSalary = deptLatestSalaries.stream()
                    .map(Salary::getTotalSalary)
                    .min(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);
                
                // 计算薪资占比
                BigDecimal salaryRatio = BigDecimal.ZERO;
                if (totalAllLatestSalary.compareTo(BigDecimal.ZERO) > 0) {
                    salaryRatio = totalSalary.multiply(BigDecimal.valueOf(100))
                        .divide(totalAllLatestSalary, 1, RoundingMode.HALF_UP);
                }
                
                deptStat.put("department", department);
                deptStat.put("employeeCount", deptEmployees.size());
                deptStat.put("totalSalary", totalSalary.intValue());
                deptStat.put("averageSalary", averageSalary.intValue());
                deptStat.put("highestSalary", highestSalary.intValue());
                deptStat.put("lowestSalary", lowestSalary.intValue());
                deptStat.put("salaryRatio", salaryRatio.intValue());
                
                System.out.println("部门: " + department + 
                    ", 员工数: " + deptEmployees.size() + 
                    ", 有薪资记录员工数: " + deptLatestSalaries.size() +
                    ", 总薪资: " + totalSalary.intValue());
                
                departmentStats.add(deptStat);
            }
        }
        
        // 按总薪资降序排序
        departmentStats.sort((a, b) -> 
            Integer.compare((Integer) b.get("totalSalary"), (Integer) a.get("totalSalary")));
        
        System.out.println("=== 部门薪资统计调试结束，共" + departmentStats.size() + "个部门 ===");
        
        return departmentStats;
    }

    @Override
    public List<Map<String, Object>> getMonthlySalaryTrend() {
        List<Salary> allSalaries = this.list();
        
        System.out.println("=== 月度薪资趋势调试开始 ===");
        System.out.println("总薪资记录数: " + allSalaries.size());
        
        // 按月份分组统计
        Map<String, List<Salary>> salariesByMonth = allSalaries.stream()
            .filter(salary -> salary.getMonth() != null)
            .collect(Collectors.groupingBy(Salary::getMonth));
        
        System.out.println("按月份分组后的月份数: " + salariesByMonth.size());
        
        List<Map<String, Object>> trendData = new ArrayList<>();
        
        for (Map.Entry<String, List<Salary>> entry : salariesByMonth.entrySet()) {
            String month = entry.getKey();
            List<Salary> monthSalaries = entry.getValue();
            
            BigDecimal totalSalary = monthSalaries.stream()
                .map(Salary::getTotalSalary)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal averageSalary = totalSalary.divide(
                BigDecimal.valueOf(monthSalaries.size()), 2, RoundingMode.HALF_UP);
            
            Map<String, Object> monthData = new HashMap<>();
            monthData.put("month", month);
            monthData.put("totalSalary", totalSalary.intValue());
            monthData.put("averageSalary", averageSalary.intValue());
            monthData.put("employeeCount", monthSalaries.size());
            
            System.out.println("月份: " + month + 
                ", 员工数: " + monthSalaries.size() + 
                ", 总薪资: " + totalSalary.intValue() +
                ", 平均薪资: " + averageSalary.intValue());
            
            trendData.add(monthData);
        }
        
        // 按月份排序
        trendData.sort(Comparator.comparing(data -> (String) data.get("month")));
        
        System.out.println("=== 月度薪资趋势调试结束，共" + trendData.size() + "个月份 ===");
        
        return trendData;
    }

    @Override
    public List<Map<String, Object>> getSalaryLevelDistribution() {
        // 获取所有员工的最新薪资记录（每个员工只取一条最新记录）
        List<Employee> allEmployees = employeeMapper.selectList(null);
        
        System.out.println("=== 薪资等级分布调试开始 ===");
        System.out.println("总员工数: " + allEmployees.size());
        
        // 为每个员工获取最新薪资
        Map<String, BigDecimal> employeeLatestSalary = new HashMap<>();
        for (Employee employee : allEmployees) {
            QueryWrapper<Salary> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("emp_id", employee.getEmpId());
            queryWrapper.orderByDesc("month");
            queryWrapper.last("LIMIT 1");
            
            Salary latestSalary = this.getOne(queryWrapper);
            if (latestSalary != null && latestSalary.getTotalSalary() != null) {
                employeeLatestSalary.put(employee.getEmpId(), latestSalary.getTotalSalary());
            }
        }
        
        System.out.println("有最新薪资记录的员工数: " + employeeLatestSalary.size());
        
        // 定义薪资等级区间
        List<Map<String, Object>> levels = new ArrayList<>();
        
        String[] levelNames = {"3K以下", "3K-5K", "5K-8K", "8K-12K", "12K-20K", "20K以上"};
        int[][] levelRanges = {{0, 3000}, {3000, 5000}, {5000, 8000}, {8000, 12000}, {12000, 20000}, {20000, Integer.MAX_VALUE}};
        
        for (int i = 0; i < levelNames.length; i++) {
            String levelName = levelNames[i];
            int minSalary = levelRanges[i][0];
            int maxSalary = levelRanges[i][1];
            
            long count = employeeLatestSalary.values().stream()
                .filter(salary -> {
                    int salaryValue = salary.intValue();
                    return salaryValue >= minSalary && (maxSalary == Integer.MAX_VALUE || salaryValue < maxSalary);
                })
                .count();
            
            Map<String, Object> levelData = new HashMap<>();
            levelData.put("level", levelName);
            levelData.put("count", (int) count);
            levelData.put("percentage", employeeLatestSalary.isEmpty() ? 0 : 
                Math.round((double) count * 100 / employeeLatestSalary.size()));
            
            System.out.println("等级: " + levelName + 
                ", 薪资范围: " + minSalary + "-" + (maxSalary == Integer.MAX_VALUE ? "无上限" : maxSalary) +
                ", 人数: " + count + 
                ", 占比: " + (employeeLatestSalary.isEmpty() ? 0 : Math.round((double) count * 100 / employeeLatestSalary.size())) + "%");
            
            levels.add(levelData);
        }
        
        System.out.println("=== 薪资等级分布调试结束 ===");
        
        return levels;
    }

    @Override
    public Map<String, Object> importSalaryRecords(MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        List<String> errorMessages = new ArrayList<>();
        List<Salary> successSalaries = new ArrayList<>();
        int totalRows = 0;
        int successCount = 0;
        int errorCount = 0;

        try (InputStream inputStream = file.getInputStream()) {
            Workbook workbook;
            String fileName = file.getOriginalFilename();
            
            // 根据文件扩展名创建对应的工作簿
            if (fileName != null && fileName.toLowerCase().endsWith(".xlsx")) {
                workbook = new XSSFWorkbook(inputStream);
            } else {
                workbook = new HSSFWorkbook(inputStream);
            }

            Sheet sheet = workbook.getSheetAt(0);
            totalRows = sheet.getPhysicalNumberOfRows();

            // 跳过标题行
            for (int i = 1; i < totalRows; i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }

                try {
                    Salary salary = parseRowToSalary(row, i + 1);
                    if (salary != null) {
                        // 验证员工是否存在
                        Employee employee = employeeMapper.selectOne(
                            new QueryWrapper<Employee>().eq("emp_id", salary.getEmpId())
                        );
                        if (employee == null) {
                            errorMessages.add("第" + (i + 1) + "行：员工工号 " + salary.getEmpId() + " 不存在");
                            errorCount++;
                            continue;
                        }

                        // 检查是否已存在相同员工相同月份的薪资记录
                        Salary existingSalary = this.getSalaryByEmpIdAndMonth(salary.getEmpId(), salary.getMonth());
                        if (existingSalary != null) {
                            errorMessages.add("第" + (i + 1) + "行：员工 " + salary.getEmpId() + " 在 " + salary.getMonth() + " 的薪资记录已存在");
                            errorCount++;
                            continue;
                        }

                        // 设置默认值
                        salary.setCreatedAt(LocalDateTime.now());
                        salary.setUpdatedAt(LocalDateTime.now());
                        if (salary.getStatus() == null || salary.getStatus().trim().isEmpty()) {
                            salary.setStatus("草稿");
                        }

                        successSalaries.add(salary);
                        successCount++;
                    }
                } catch (Exception e) {
                    errorMessages.add("第" + (i + 1) + "行：数据解析错误 - " + e.getMessage());
                    errorCount++;
                }
            }

            workbook.close();

            // 批量保存成功的记录
            if (!successSalaries.isEmpty()) {
                this.saveBatch(successSalaries);
            }

            result.put("success", true);
            result.put("message", "导入完成");
            result.put("totalRows", totalRows - 1); // 减去标题行
            result.put("successCount", successCount);
            result.put("errorCount", errorCount);
            result.put("errorMessages", errorMessages);

            System.out.println("薪资记录导入完成：总行数=" + (totalRows - 1) + "，成功=" + successCount + "，失败=" + errorCount);

        } catch (Exception e) {
            System.err.println("导入薪资记录时发生异常：" + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "导入失败：" + e.getMessage());
            result.put("totalRows", 0);
            result.put("successCount", 0);
            result.put("errorCount", 0);
            result.put("errorMessages", Arrays.asList("文件解析失败：" + e.getMessage()));
        }

        return result;
    }

    /**
     * 解析Excel行数据为Salary对象
     */
    private Salary parseRowToSalary(Row row, int rowNum) throws Exception {
        Salary salary = new Salary();

        try {
            // 员工工号 (A列)
            Cell empIdCell = row.getCell(0);
            if (empIdCell == null || getCellStringValue(empIdCell).trim().isEmpty()) {
                throw new Exception("员工工号不能为空");
            }
            salary.setEmpId(getCellStringValue(empIdCell).trim());

            // 月份 (B列) - 格式：YYYY-MM
            Cell monthCell = row.getCell(1);
            if (monthCell == null || getCellStringValue(monthCell).trim().isEmpty()) {
                throw new Exception("月份不能为空");
            }
            String monthValue = getCellStringValue(monthCell).trim();
            if (!monthValue.matches("\\d{4}-\\d{2}")) {
                throw new Exception("月份格式错误，应为YYYY-MM格式，如：2024-01");
            }
            salary.setMonth(monthValue);

            // 基本工资 (C列)
            Cell baseSalaryCell = row.getCell(2);
            if (baseSalaryCell == null) {
                throw new Exception("基本工资不能为空");
            }
            BigDecimal baseSalary = getCellNumericValue(baseSalaryCell);
            if (baseSalary.compareTo(BigDecimal.ZERO) < 0) {
                throw new Exception("基本工资不能为负数");
            }
            salary.setBaseSalary(baseSalary);

            // 津贴 (D列)
            Cell allowanceCell = row.getCell(3);
            BigDecimal allowance = allowanceCell != null ? getCellNumericValue(allowanceCell) : BigDecimal.ZERO;
            salary.setAllowance(allowance);

            // 奖金 (E列)
            Cell bonusCell = row.getCell(4);
            BigDecimal bonus = bonusCell != null ? getCellNumericValue(bonusCell) : BigDecimal.ZERO;
            salary.setBonus(bonus);

            // 扣除 (F列)
            Cell deductionCell = row.getCell(5);
            BigDecimal deduction = deductionCell != null ? getCellNumericValue(deductionCell) : BigDecimal.ZERO;
            salary.setDeduction(deduction);

            // 计算总薪资
            BigDecimal totalSalary = baseSalary.add(allowance).add(bonus).subtract(deduction);
            salary.setTotalSalary(totalSalary);

            // 状态 (G列) - 可选
            Cell statusCell = row.getCell(6);
            String status = statusCell != null ? getCellStringValue(statusCell).trim() : "草稿";
            if (!Arrays.asList("草稿", "已确认", "已发放").contains(status)) {
                throw new Exception("状态值错误，应为：草稿、已确认、已发放");
            }
            salary.setStatus(status);

            return salary;

        } catch (Exception e) {
            throw new Exception("第" + rowNum + "行数据格式错误：" + e.getMessage());
        }
    }

    /**
     * 获取单元格的字符串值
     */
    private String getCellStringValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    // 避免科学计数法
                    return String.valueOf((long) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    /**
     * 获取单元格的数值
     */
    private BigDecimal getCellNumericValue(Cell cell) throws Exception {
        if (cell == null) {
            return BigDecimal.ZERO;
        }
        
        switch (cell.getCellType()) {
            case NUMERIC:
                return BigDecimal.valueOf(cell.getNumericCellValue());
            case STRING:
                String value = cell.getStringCellValue().trim();
                if (value.isEmpty()) {
                    return BigDecimal.ZERO;
                }
                try {
                    return new BigDecimal(value);
                } catch (NumberFormatException e) {
                    throw new Exception("数值格式错误：" + value);
                }
            default:
                throw new Exception("单元格类型不支持转换为数值");
        }
    }
} 