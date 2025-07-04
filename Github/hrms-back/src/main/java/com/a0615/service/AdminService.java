package com.a0615.service;

import com.a0615.entity.Admin;
import com.a0615.mapper.AdminMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils; // 导入 Spring 的 StringUtils 工具类

import java.util.List;

@Service
public class AdminService {

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 获取所有管理员信息
     * @return 管理员列表
     */
    public List<Admin> getAllAdmins() {
        return adminMapper.selectList(null);
    }

    /**
     * @param id 管理员ID
     * 根据ID获取管理员信息
     * @return 管理员对象
     */
    public Admin getAdminById(Integer id) {
        return adminMapper.selectById(id);
    }

    /**
     * 根据用户名获取管理员信息 (用于登录验证等)
     * @param username 用户名
     * @return 管理员对象
     */
    public Admin getAdminByUsername(String username) {
        QueryWrapper<Admin> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return adminMapper.selectOne(queryWrapper);
    }

    /**
     * 添加新管理员
     * 密码会进行加密处理
     * @param admin 管理员对象
     */
    @Transactional
    public void addAdmin(Admin admin) {
        admin.setPwd(passwordEncoder.encode(admin.getPwd()));
        adminMapper.insert(admin);
    }

    /**
     * 更新管理员信息
     * 如果密码不为空，则对密码进行加密更新
     * @param admin 管理员对象
     */
    @Transactional
    public void updateAdmin(Admin admin) {
        if (admin.getPwd() != null && !admin.getPwd().isEmpty()) {
            admin.setPwd(passwordEncoder.encode(admin.getPwd()));
        }
        adminMapper.updateById(admin);
    }

    /**
     * 删除管理员
     * @param id 管理员ID
     */
    @Transactional
    public void deleteAdmin(Integer id) {
        adminMapper.deleteById(id);
    }

    /**
     * 根据用户名和角色搜索管理员。
     * 如果用户名或角色为空，则不作为查询条件。
     * @param username 用户名关键词（可选）。
     * @param role 角色名称（可选）。
     * @return 符合条件的管理员列表。
     */
    public List<Admin> searchAdmins(String username, String role) {
        QueryWrapper<Admin> queryWrapper = new QueryWrapper<>();

        // 根据 username 条件动态构建查询
        if (StringUtils.hasText(username)) { // 检查字符串是否非空且不全为空白字符
            queryWrapper.like("username", username); // 模糊匹配用户名
        }

        // 根据 role 条件动态构建查询
        if (StringUtils.hasText(role)) {
            queryWrapper.eq("role", role); // 精确匹配角色
        }

        // 执行查询并返回结果
        return adminMapper.selectList(queryWrapper);
    }
}
