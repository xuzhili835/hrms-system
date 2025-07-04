package com.a0615.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("leave_application")
public class LeaveApplication {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("emp_id")
    private String empId;
    
    @TableField("type")
    private String type;
    
    @TableField("start_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    
    @TableField("end_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    
    @TableField("days")
    private Integer days;
    
    @TableField("reason")
    private String reason;
    
    @TableField("status")
    private String status = "待审批";
    
    @TableField("approver")
    private String approver;
    
    @TableField("approve_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime approveTime;
    
    @TableField("remarks")
    private String remarks;
    
    @TableField("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @TableField("updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}