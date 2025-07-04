package com.a0615.service;

import com.a0615.entity.Admin;
import com.a0615.entity.Employee;
import com.a0615.mapper.AdminMapper;
import com.a0615.mapper.EmployeeMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service // 标记这是一个 Spring Service 组件
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired // 自动注入 AdminMapper
    private AdminMapper adminMapper;

    @Autowired // 自动注入 EmployeeMapper
    private EmployeeMapper employeeMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 首先尝试作为管理员加载 (管理员使用 username 登录)
        QueryWrapper<Admin> adminQueryWrapper = new QueryWrapper<>();
        adminQueryWrapper.eq("username", username);
        Admin admin = adminMapper.selectOne(adminQueryWrapper);

        if (admin != null) {
            List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN")); // 为管理员添加 ROLE_ADMIN 权限
            // 返回 Spring Security 的 User 对象
            return new org.springframework.security.core.userdetails.User(
                    admin.getUsername(),
                    admin.getPwd(), // 使用 getPwd() 方法获取密码
                    authorities
            );
        }

        // 如果不是管理员，再尝试作为员工加载 (员工使用 emp_id 登录)
        QueryWrapper<Employee> employeeQueryWrapper = new QueryWrapper<>();
        employeeQueryWrapper.eq("emp_id", username); // 员工使用 empId 登录
        Employee employee = employeeMapper.selectOne(employeeQueryWrapper);

        if (employee != null) {
            List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_EMPLOYEE")); // 为员工添加 ROLE_EMPLOYEE 权限
            // 返回 Spring Security 的 User 对象
            return new org.springframework.security.core.userdetails.User(
                    employee.getEmpId(), // UserDetails 的 username 应该是员工的 empId
                    employee.getPwd(), // 使用 getPwd() 方法获取密码
                    authorities
            );
        }

        // 如果管理员和员工都找不到，抛出异常
        throw new UsernameNotFoundException("User not found with username: " + username);
    }
}