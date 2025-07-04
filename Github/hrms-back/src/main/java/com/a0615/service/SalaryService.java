package com.a0615.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.a0615.entity.Salary;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

    /**
 * 薪资服务接口
 */
public interface SalaryService extends IService<Salary> {

    /**
     * 根据员工ID获取薪资记录
     */
    List<Salary> getSalaryByEmpId(String empId);

    /**
     * 根据员工ID获取薪资记录列表（复数形式，用于EmployeeController）
     */
    List<Salary> getSalariesByEmpId(String empId);

    /**
     * 获取指定员工指定月份的薪资记录
     */
    Salary getSalaryByEmpIdAndMonth(String empId, String month);

    /**
     * 根据员工ID和年份获取薪资记录
     */
    List<Salary> getSalaryByEmpIdAndYear(String empId, String year);

    /**
     * 获取指定月份的薪资总额
     */
    BigDecimal getMonthlySalaryTotal(String month);

    /**
     * 获取部门薪资统计数据
     */
    List<Map<String, Object>> getDepartmentSalaryStatistics();

    /**
     * 获取月度薪资趋势数据
     */
    List<Map<String, Object>> getMonthlySalaryTrend();

    /**
     * 获取薪资等级分布数据
     */
    List<Map<String, Object>> getSalaryLevelDistribution();

    /**
     * 导入薪资记录
     */
    Map<String, Object> importSalaryRecords(org.springframework.web.multipart.MultipartFile file);
}