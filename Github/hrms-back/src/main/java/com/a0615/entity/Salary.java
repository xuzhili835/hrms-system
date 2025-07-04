package com.a0615.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 薪资实体类 - 严格按照数据库表结构定义
 */
@Data
@TableName("salaries")
public class Salary {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("emp_id")
    private String empId;
    
    @TableField("base_salary")
    private BigDecimal baseSalary;
    
    @TableField("allowance")
    private BigDecimal allowance;
    
    @TableField("bonus")
    private BigDecimal bonus;
    
    @TableField("deduction")
    private BigDecimal deduction;
    
    @TableField("total_salary")
    private BigDecimal totalSalary;
    
    @TableField("month")
    private String month; // 格式：YYYY-MM
    
    @TableField("status")
    private String status;
    
    @TableField("created_at")
    private LocalDateTime createdAt;
    
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}