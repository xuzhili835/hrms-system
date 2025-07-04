package com.a0615.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Data // Lombok 注解
@TableName("admin") // 映射到数据库的 admin 表
public class Admin implements UserDetails { // 实现 UserDetails 接口

    @TableId(type = IdType.AUTO) // ID 自增长
    private Integer id;
    private String username;
    private String pwd; // 密码字段
    private String role; // 角色字段，例如 "ADMIN"

    // --- 自定义方法 ---
    
    /**
     * 获取管理员显示名称
     * 由于Admin表没有单独的name字段，使用username作为显示名称
     * @return 管理员显示名称
     */
    public String getName() {
        return this.username;
    }

    // --- UserDetails 接口方法实现 ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 返回用户的权限集合。这里我们将 role 字段作为权限。
        // 确保数据库中 role 字段存储的是 "ADMIN" 而不是 "ROLE_ADMIN"。
        // Spring Security 自动添加 "ROLE_" 前缀进行匹配。
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
    public String getUsername() { // Spring Security 使用这个方法获取用户名
        return this.username;
    }

    // 账户是否未过期
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // 账户是否未锁定
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    // 凭证是否未过期
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // 账户是否启用
    @Override
    public boolean isEnabled() {
        return true;
    }
}