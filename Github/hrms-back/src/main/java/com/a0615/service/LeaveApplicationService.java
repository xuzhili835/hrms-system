package com.a0615.service;

import com.a0615.entity.LeaveApplication;
import com.a0615.mapper.LeaveApplicationMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 请假申请服务类，处理请假申请相关的业务逻辑。
 */
@Service
public class LeaveApplicationService {

    @Autowired
    private LeaveApplicationMapper leaveApplicationMapper;

    /**
     * 提交新的请假申请。
     * 在插入数据库之前，将请假申请的状态设置为"待审核"。
     *
     * @param leaveApplication 待提交的请假申请实体
     */
    @Transactional
    public void submitLeaveApplication(LeaveApplication leaveApplication) {
        leaveApplication.setStatus("待审核"); // PENDING -> 待审核
        leaveApplicationMapper.insert(leaveApplication);
    }

    /**
     * 获取所有请假申请。
     *
     * @return 包含所有请假申请的列表
     */
    public List<LeaveApplication> getAllLeaveApplications() {
        return leaveApplicationMapper.selectList(null);
    }

    /**
     * 根据请假申请状态获取请假申请列表。
     *
     * @param status 请假申请的状态（如 "待审核", "批准", "未批准"）
     * @return 匹配指定状态的所有请假申请列表
     */
    public List<LeaveApplication> getLeaveApplicationsByStatus(String status) {
        QueryWrapper<LeaveApplication> queryWrapper = new QueryWrapper<>();
        // 直接使用传入的中文状态进行查询
        queryWrapper.eq("status", status);
        return leaveApplicationMapper.selectList(queryWrapper);
    }

    /**
     * 员工根据自己的工号查看请假申请。
     *
     * @param empId 员工工号 (String)，用于筛选特定员工的请假申请
     * @return 该员工的所有请假申请列表
     */
    public List<LeaveApplication> getLeaveApplicationsByEmpId(String empId) {
        QueryWrapper<LeaveApplication> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("emp_id", empId);
        return leaveApplicationMapper.selectList(queryWrapper);
    }

    /**
     * 根据ID获取单个请假申请。
     *
     * @param id 请假申请的唯一ID
     * @return 匹配指定ID的请假申请实体，如果未找到则返回 null
     */
    public LeaveApplication getLeaveApplicationById(Long id) {
        return leaveApplicationMapper.selectById(id);
    }

    /**
     * 审核请假申请，并更新其状态。
     * 允许重新审核已审核的申请。
     *
     * @param id        待审核请假申请的ID
     * @param newStatus 新的状态（"批准" 或 "未批准"）
     * @return 如果审核成功并更新了状态，则返回 true；否则返回 false（例如：申请不存在或新状态无效）
     */
    @Transactional
    public boolean reviewLeaveApplication(Long id, String newStatus) {
        LeaveApplication existingApplication = leaveApplicationMapper.selectById(id);
        if (existingApplication == null) {
            System.out.println("请假申请ID: " + id + " 不存在。");
            return false;
        }

        // 检查新状态是否有效 (使用中文状态进行比较)
        if (!"批准".equalsIgnoreCase(newStatus) && !"未批准".equalsIgnoreCase(newStatus) && 
            !"待审核".equalsIgnoreCase(newStatus)) {
            System.out.println("无效的审核状态: " + newStatus + "。有效状态为：批准、未批准、待审核");
            return false;
        }

        String currentStatus = existingApplication.getStatus();
        System.out.println("请假申请ID: " + id + " 当前状态: " + currentStatus + " -> 新状态: " + newStatus);

        existingApplication.setStatus(newStatus); // 直接设置中文状态
        int result = leaveApplicationMapper.updateById(existingApplication);
        
        if (result > 0) {
            System.out.println("请假申请ID: " + id + " 审核成功，状态已更新为: " + newStatus);
        } else {
            System.out.println("请假申请ID: " + id + " 审核失败，数据库更新失败。");
        }
        
        return result > 0;
    }

    /**
     * 根据ID删除请假申请。
     *
     * @param id 待删除请假申请的唯一ID
     * @return 如果删除成功，则返回 true；否则返回 false
     */
    @Transactional
    public boolean deleteLeaveApplication(Long id) {
        int result = leaveApplicationMapper.deleteById(id);
        return result > 0;
    }

    /**
     * 获取待审批的请假申请数量
     *
     * @return 待审批的请假申请数量
     */
    public int getPendingApplicationsCount() {
        QueryWrapper<LeaveApplication> queryWrapper = new QueryWrapper<>();
        // 统计所有待审批的状态：包括"待审批"和"待审核"
        queryWrapper.in("status", "待审批", "待审核");
        return Math.toIntExact(leaveApplicationMapper.selectCount(queryWrapper));
    }
}