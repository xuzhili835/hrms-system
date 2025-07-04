package com.a0615.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;

@Data
@TableName("employee") // 对应数据库表名
public class Employee implements UserDetails {
    @TableId(type = IdType.AUTO) // 主键，自增
    private Integer id;
    private String empId; // 员工编号
    private String name;  // 姓名
    private String dept;  // 部门
    private String pos;   // 职位
    private String pwd;   // 密码
    private String role;  // 角色 (例如：EMPLOYEE)
    private LocalDate entryTime; // 入职时间
    private String status;       // 员工状态 (例如："在职", "离职")

    // 以下是 Spring Security 相关的接口实现
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 根据角色设置权限。确保数据库中 role 字段存储的是 "EMPLOYEE" 而不是 "ROLE_EMPLOYEE"。
        if (role == null || role.isEmpty()) {
            return Collections.emptyList(); // 如果没有角色，返回空列表
        }
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getPassword() {
        return this.pwd;
    }

    @Override
    public String getUsername() { // 对于员工，username 应该是 empId
        return this.empId;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}