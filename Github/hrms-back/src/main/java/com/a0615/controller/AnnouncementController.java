package com.a0615.controller;

import com.a0615.entity.Announcement;
import com.a0615.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController // 标记这是一个 RESTful 控制器，返回 JSON 或 XML 数据
@RequestMapping("/api/announcements") // 修改为/api/announcements，与前端调用保持一致
public class AnnouncementController {

    @Autowired // 自动注入 AnnouncementService
    private AnnouncementService announcementService;

    // --- 管理员端接口 ---

    /**
     * 管理员：发布新公告
     * POST /api/announcements
     * @param announcement 公告信息 (JSON 格式)
     * @return 发布后的公告信息
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')") // 只有管理员可以发布公告
    public ResponseEntity<Announcement> addAnnouncement(@RequestBody Announcement announcement) {
        announcementService.addAnnouncement(announcement);
        // 通常，成功创建资源后返回 201 Created 状态码
        return ResponseEntity.status(HttpStatus.CREATED).body(announcement);
    }

    /**
     * 管理员：更新公告信息
     * PUT /api/announcements/{id}
     * @param id 公告 ID
     * @param announcement 更新的公告信息 (JSON 格式)
     * @return 更新后的公告信息或 404 Not Found
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // 只有管理员可以更新公告
    public ResponseEntity<Announcement> updateAnnouncement(@PathVariable Long id, @RequestBody Announcement announcement) {
        // 确保 ID 一致，防止路径 ID 和请求体 ID 不匹配
        announcement.setId(id);
        boolean success = announcementService.updateAnnouncement(announcement);
        if (success) {
            // 更新成功返回 200 OK
            return ResponseEntity.ok(announcement);
        } else {
            // 公告不存在返回 404 Not Found
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 管理员：删除公告
     * DELETE /api/announcements/{id}
     * @param id 公告 ID
     * @return 无内容响应
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // 只有管理员可以删除公告
    public ResponseEntity<Void> deleteAnnouncement(@PathVariable Long id) {
        boolean success = announcementService.deleteAnnouncement(id);
        if (success) {
            // 删除成功返回 204 No Content
            return ResponseEntity.noContent().build();
        } else {
            // 公告不存在返回 404 Not Found
            return ResponseEntity.notFound().build();
        }
    }

    // --- 员工和管理员共享接口 ---

    /**
     * 获取公告列表 (需要认证)
     * GET /api/announcements
     * @param status 状态筛选 (可选)
     * @param title 标题筛选 (可选) 
     * @param author 作者筛选 (可选)
     * @return 公告列表
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')") // 需要认证
    public ResponseEntity<List<Announcement>> getAllAnnouncements(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author) {
        List<Announcement> announcements = announcementService.getAnnouncements(status, title, author);
        return ResponseEntity.ok(announcements); // 返回 200 OK 状态码和公告列表
    }

    /**
     * 根据 ID 获取单个公告信息 (需要认证)
     * GET /api/announcements/{id}
     * @param id 公告 ID
     * @return 公告对象
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')") // 需要认证
    public ResponseEntity<Announcement> getAnnouncementById(@PathVariable Long id) {
        Announcement announcement = announcementService.getAnnouncementById(id);
        if (announcement != null) {
            return ResponseEntity.ok(announcement); // 找到返回 200 OK
        } else {
            return ResponseEntity.notFound().build(); // 未找到返回 404 Not Found
        }
    }
}