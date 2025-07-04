package com.a0615.service;

import com.a0615.entity.Announcement;
import com.a0615.mapper.AnnouncementMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service // 标记这是一个 Spring Service 组件
public class AnnouncementService {

    @Autowired // 自动注入 AnnouncementMapper 接口
    private AnnouncementMapper announcementMapper;

    /**
     * 获取所有公告
     * @return 公告列表
     */
    public List<Announcement> getAllAnnouncements() {
        // 使用 BaseMapper 的 selectList 方法，传入 null 表示无查询条件
        return announcementMapper.selectList(null);
    }

    /**
     * 根据条件获取公告列表
     * @param status 状态筛选
     * @param title 标题筛选
     * @param author 作者筛选
     * @return 筛选后的公告列表
     */
    public List<Announcement> getAnnouncements(String status, String title, String author) {
        QueryWrapper<Announcement> queryWrapper = new QueryWrapper<>();
        
        // 状态筛选
        if (status != null && !status.trim().isEmpty()) {
            queryWrapper.eq("status", status.trim());
        }
        
        // 标题筛选 (模糊查询)
        if (title != null && !title.trim().isEmpty()) {
            queryWrapper.like("title", title.trim());
        }
        
        // 作者筛选
        if (author != null && !author.trim().isEmpty()) {
            queryWrapper.eq("pub_id", author.trim());
        }
        
        // 按发布日期倒序排列
        queryWrapper.orderByDesc("pub_date");
        
        return announcementMapper.selectList(queryWrapper);
    }

    /**
     * 根据 ID 获取公告信息
     * @param id 公告 ID
     * @return 公告对象
     */
    public Announcement getAnnouncementById(Long id) {
        // 使用 BaseMapper 的 selectById 方法
        return announcementMapper.selectById(id);
    }

    /**
     * 发布新公告
     * @param announcement 公告对象
     */
    @Transactional // 开启事务管理
    public void addAnnouncement(Announcement announcement) {
        // 设置发布日期为当前日期
        announcement.setPubDate(LocalDate.now());
        // 使用 BaseMapper 的 insert 方法
        announcementMapper.insert(announcement);
    }

    /**
     * 更新公告信息
     * @param announcement 公告对象
     */
    @Transactional // 开启事务管理
    public boolean updateAnnouncement(Announcement announcement) {
        // 确保 ID 存在，并且可以根据 ID 进行更新
        if (announcement.getId() == null || getAnnouncementById(announcement.getId()) == null) {
            return false;
        }
        // 使用 BaseMapper 的 updateById 方法（根据实体类中的 ID 更新）
        int result = announcementMapper.updateById(announcement);
        return result > 0;
    }

    /**
     * 删除公告
     * @param id 公告 ID
     */
    @Transactional // 开启事务管理
    public boolean deleteAnnouncement(Long id) {
        // 使用 BaseMapper 的 deleteById 方法
        int result = announcementMapper.deleteById(id);
        return result > 0;
    }
    /**
     * 根据发布者 ID 获取公告列表（如果需要，可以添加此方法）
     * @param pubId 发布者 ID
     * @return 该发布者的所有公告列表
     */
    public List<Announcement> getAnnouncementsByPubId(String pubId) {
        QueryWrapper<Announcement> queryWrapper = new QueryWrapper<>();
        // 对查询条件也进行 trim()
        queryWrapper.eq("pub_id", pubId != null ? pubId.trim() : null);
        return announcementMapper.selectList(queryWrapper);
    }
}