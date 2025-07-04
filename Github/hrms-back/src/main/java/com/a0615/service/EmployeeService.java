package com.a0615.service;

import com.a0615.entity.Employee;
import com.a0615.mapper.EmployeeMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Map;
import java.util.HashMap;

// !!! 确保导入这些 DTOs !!!
import com.a0615.dto.EmployeeCreateDTO;
import com.a0615.dto.EmployeeUpdateDTO;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    @Autowired
    private PasswordEncoder passwordEncoder; // 注入密码编码器接口

    /**
     * 获取所有员工列表。
     * 此方法本身不进行权限检查，权限检查由Controller层的@PreAuthorize完成。
     * @return 员工列表。
     */
    public List<Employee> getAllEmployees() {
        return employeeMapper.selectList(null);
    }

    /**
     * 根据员工工号获取单个员工信息。
     * 此方法本身不进行权限检查，权限检查由Controller层的@PreAuthorize完成。
     * @param empId 员工工号。
     * @return 员工对象，如果不存在则返回null。
     */
    public Employee getEmployeeByEmpId(String empId) {
        QueryWrapper<Employee> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("emp_id", empId);
        return employeeMapper.selectOne(queryWrapper);
    }

    /**
     * 添加新员工。
     * 从 DTO 接收数据，进行默认值设置和密码加密。
     * @param createDto 待添加的员工信息 DTO。
     * @throws IllegalArgumentException 如果员工工号已存在，或密码为空。
     */
    @Transactional // 事务管理
    public void addEmployee(EmployeeCreateDTO createDto) { // <-- 参数类型改为 EmployeeCreateDTO
        // 检查 empId 是否已存在
        if (getEmployeeByEmpId(createDto.getEmpId()) != null) {
            throw new IllegalArgumentException("员工工号 " + createDto.getEmpId() + " 已存在。");
        }

        // 将 DTO 转换为实体
        Employee employee = new Employee();
        employee.setEmpId(createDto.getEmpId());
        employee.setName(createDto.getName());
        employee.setDept(createDto.getDept());
        employee.setPos(createDto.getPos());

        // 密码处理：对密码进行加密
        if (createDto.getPwd() != null && !createDto.getPwd().isEmpty()) {
            employee.setPwd(passwordEncoder.encode(createDto.getPwd()));
        } else {
            // 如果密码为空，抛出异常
            throw new IllegalArgumentException("员工密码不能为空。");
        }

        // 角色设置：如果 DTO 提供了角色，使用 DTO 的，否则默认为 EMPLOYEE
        employee.setRole(createDto.getRole() != null && !createDto.getRole().isEmpty() ? createDto.getRole() : "EMPLOYEE");

        // 入职时间设置：如果未设置，则默认为当前日期
        employee.setEntryTime(createDto.getEntryTime() != null ? createDto.getEntryTime() : LocalDate.now());

        // 状态设置：如果未设置，则默认为"在职"
        employee.setStatus(createDto.getStatus() != null && !createDto.getStatus().isEmpty() ? createDto.getStatus() : "在职");

        employeeMapper.insert(employee);
    }

    /**
     * 根据员工工号更新员工信息。
     * 从 DTO 接收更新数据，并应用到现有实体。
     * @param empId 待更新员工的工号。
     * @param updateDto 包含更新信息的员工 DTO。
     * @return 如果更新成功返回true，否则返回false（员工不存在或未作任何修改）。
     * @throws IllegalArgumentException 如果员工不存在。
     */
    @Transactional
    public boolean updateEmployeeByEmpId(String empId, EmployeeUpdateDTO updateDto) { // <-- 参数类型改为 EmployeeUpdateDTO
        Employee existingEmployee = getEmployeeByEmpId(empId);
        if (existingEmployee == null) {
            throw new IllegalArgumentException("员工工号 " + empId + " 不存在。");
        }

        boolean changed = false; // 标记是否有字段被修改

        // 仅当新值与旧值不同时才更新并标记为修改
        if (updateDto.getName() != null && !Objects.equals(updateDto.getName(), existingEmployee.getName())) {
            existingEmployee.setName(updateDto.getName());
            changed = true;
        }
        if (updateDto.getDept() != null && !Objects.equals(updateDto.getDept(), existingEmployee.getDept())) {
            existingEmployee.setDept(updateDto.getDept());
            changed = true;
        }
        if (updateDto.getPos() != null && !Objects.equals(updateDto.getPos(), existingEmployee.getPos())) {
            existingEmployee.setPos(updateDto.getPos());
            changed = true;
        }
        // 密码更新：只有当新密码不为空且与旧密码不同时才更新并加密
        // 注意：这里需要判断传入的密码是否已经加密过，通常前端会发送明文密码
        if (updateDto.getPwd() != null && !updateDto.getPwd().isEmpty()) {
            // 只有当新密码与旧的加密密码不匹配时才重新加密和更新
            // 如果 existingEmployee.getPwd() 为 null 或空，则直接加密新密码
            if (existingEmployee.getPwd() == null || existingEmployee.getPwd().isEmpty() || !passwordEncoder.matches(updateDto.getPwd(), existingEmployee.getPwd())) {
                existingEmployee.setPwd(passwordEncoder.encode(updateDto.getPwd()));
                changed = true;
            }
        }
        if (updateDto.getEntryTime() != null && !Objects.equals(updateDto.getEntryTime(), existingEmployee.getEntryTime())) {
            existingEmployee.setEntryTime(updateDto.getEntryTime());
            changed = true;
        }
        // 状态更新：此处不再进行角色判断，由Controller层的@PreAuthorize保证只有管理员能调用此方法。
        // Service层只负责执行传入的状态更新。
        if (updateDto.getStatus() != null && !Objects.equals(updateDto.getStatus(), existingEmployee.getStatus())) {
            existingEmployee.setStatus(updateDto.getStatus());
            changed = true;
        }
        // 角色更新：通常只由管理员修改，这里Service层负责执行。
        if (updateDto.getRole() != null && !Objects.equals(updateDto.getRole(), existingEmployee.getRole())) {
            existingEmployee.setRole(updateDto.getRole());
            changed = true;
        }


        if (changed) {
            int rowsAffected = employeeMapper.updateById(existingEmployee); // 使用 updateById 更新整个实体
            return rowsAffected > 0;
        }
        return false; // 没有字段被修改
    }


    /**
     * 根据员工工号删除状态为"离职"的员工。
     * @param empId 待删除员工的工号。
     * @return 如果成功删除返回true，否则返回false（员工不存在或状态不是"离职"）。
     * @throws IllegalArgumentException 如果员工不存在或状态不是"离职"。
     */
    @Transactional
    public boolean deleteResignedEmployee(String empId) {
        Employee employeeToDelete = getEmployeeByEmpId(empId);
        if (employeeToDelete == null) {
            throw new IllegalArgumentException("员工工号 " + empId + " 不存在，无法删除。");
        }
        // 检查员工状态是否为"离职"
        if ("离职".equals(employeeToDelete.getStatus())) {
            int rowsAffected = employeeMapper.deleteById(employeeToDelete.getId());
            return rowsAffected > 0;
        } else {
            throw new IllegalArgumentException("只有状态为'离职'的员工才能被删除，当前员工状态为: " + employeeToDelete.getStatus());
        }
    }

    /**
     * 搜索员工。
     * @param name 姓名关键词（模糊匹配）。
     * @param dept 部门名称（精确匹配）。
     * @return 符合条件的员工列表。
     */
    public List<Employee> searchEmployees(String name, String dept) {
        QueryWrapper<Employee> queryWrapper = new QueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            queryWrapper.like("name", name); // 姓名模糊查询
        }
        if (dept != null && !dept.isEmpty()) {
            queryWrapper.eq("dept", dept); // 部门精确查询
        }
        return employeeMapper.selectList(queryWrapper);
    }

    /**
     * 获取部门统计数据
     * @return 各部门的员工数量统计
     */
    public Map<String, Integer> getDepartmentStats() {
        List<Employee> allEmployees = getAllEmployees();
        Map<String, Integer> departmentStats = new HashMap<>();
        
        for (Employee employee : allEmployees) {
            String dept = employee.getDept();
            if (dept != null && !dept.isEmpty()) {
                departmentStats.put(dept, departmentStats.getOrDefault(dept, 0) + 1);
            }
        }
        
        return departmentStats;
    }
}