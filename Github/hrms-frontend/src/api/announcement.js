/**
 * 公告管理相关API接口
 */
import request from '@/utils/request';

/**
 * 获取公告列表
 */
export const getAnnouncements = async (params = {}) => {
  try {
    // 构建查询参数
    const queryParams = new URLSearchParams();
    if (params.title) queryParams.append('title', params.title);
    if (params.status) queryParams.append('status', params.status);
    if (params.author) queryParams.append('author', params.author);
    if (params.startDate) queryParams.append('startDate', params.startDate);
    if (params.endDate) queryParams.append('endDate', params.endDate);
    if (params.page) queryParams.append('page', params.page);
    if (params.pageSize) queryParams.append('pageSize', params.pageSize);
    
    const response = await request.get(`/announcements?${queryParams.toString()}`);
    return response;
  } catch (error) {
    console.error('获取公告列表失败:', error);
    throw error;
  }
};

/**
 * 获取公告详情
 */
export const getAnnouncementDetail = async (id, userId = null) => {
  try {
    const response = await request.get(`/announcements/${id}`);
    
    // 已移除阅读状态记录功能
    
    return response;
  } catch (error) {
    console.error('获取公告详情失败:', error);
    throw error;
  }
};

/**
 * 创建公告（管理员）
 */
export const createAnnouncement = async (announcementData) => {
  try {
    // 数据字段映射
    const requestData = {
      title: announcementData.title,
      content: announcementData.content,
      status: announcementData.status || '已发布', // 确保传递状态
      pubId: announcementData.author || announcementData.pubId || 'admin',
      pubDate: announcementData.pubDate || new Date().toISOString().split('T')[0]
    };
    
    const response = await request.post('/announcements', requestData);
    return response;
  } catch (error) {
    console.error('创建公告失败:', error);
    throw error;
  }
};

/**
 * 更新公告（管理员）
 */
export const updateAnnouncement = async (id, announcementData) => {
  try {
    // 数据字段映射
    const requestData = {
      title: announcementData.title,
      content: announcementData.content,
      status: announcementData.status, // 确保传递状态
      pubId: announcementData.author || announcementData.pubId || 'admin'
    };
    
    // 如果是发布草稿，更新发布日期
    if (announcementData.status === '已发布' && !announcementData.pubDate) {
      requestData.pubDate = new Date().toISOString().split('T')[0];
    }
    
    // 过滤掉undefined的字段
    Object.keys(requestData).forEach(key => {
      if (requestData[key] === undefined) {
        delete requestData[key];
      }
    });
    
    await request.put(`/announcements/${id}`, requestData);
    return { message: '公告更新成功' };
  } catch (error) {
    console.error('更新公告失败:', error);
    throw error;
  }
};

/**
 * 删除公告（管理员）
 */
export const deleteAnnouncement = async (id) => {
  try {
    await request.delete(`/announcements/${id}`);
    return { message: '公告删除成功' };
  } catch (error) {
    console.error('删除公告失败:', error);
    throw error;
  }
};

/**
 * 批量删除公告（管理员）
 */
export const batchDeleteAnnouncements = async (ids) => {
  try {
    // 逐个删除公告
    const promises = ids.map(id => deleteAnnouncement(id));
    await Promise.all(promises);
    return { message: '批量删除成功' };
  } catch (error) {
    console.error('批量删除公告失败:', error);
    throw error;
  }
};

/**
 * 发布公告（管理员）
 */
export const publishAnnouncement = async (id) => {
  try {
    // 更新公告状态为已发布
    await request.put(`/announcements/${id}`, {
      status: 'published',
      pubDate: new Date().toISOString()
    });
    return { message: '公告发布成功' };
  } catch (error) {
    console.error('发布公告失败:', error);
    throw error;
  }
};

/**
 * 归档公告（管理员）
 */
export const archiveAnnouncement = async (id) => {
  try {
    // 更新公告状态为已归档
    await request.put(`/announcements/${id}`, {
      status: 'archived'
    });
    return { message: '公告归档成功' };
  } catch (error) {
    console.error('归档公告失败:', error);
    throw error;
  }
};

/**
 * 设置公告置顶状态（管理员）
 */
export const setAnnouncementTop = async (id, isTop) => {
  try {
    // 更新公告置顶状态
    await request.put(`/announcements/${id}`, {
      isTop: isTop
    });
    return { message: isTop ? '公告置顶成功' : '取消置顶成功' };
  } catch (error) {
    console.error('设置公告置顶失败:', error);
    throw error;
  }
};

// 已移除标记已读和未读数量统计功能

/**
 * 获取公告统计数据（管理员）
 */
export const getAnnouncementStatistics = async (params = {}) => {
  try {
    // 获取所有公告进行统计
    const result = await getAnnouncements({ pageSize: 1000 });
    const announcements = result.data || [];
    
    // 按时间范围筛选
    let filteredAnnouncements = [...announcements];
    
    if (params.startDate) {
      filteredAnnouncements = filteredAnnouncements.filter(ann => 
        (ann.pubDate || ann.publishDate) >= params.startDate
      );
    }
    
    if (params.endDate) {
      filteredAnnouncements = filteredAnnouncements.filter(ann => 
        (ann.pubDate || ann.publishDate) <= params.endDate
      );
    }
    
    // 计算统计数据
    const totalAnnouncements = filteredAnnouncements.length;
    const publishedCount = filteredAnnouncements.filter(ann => ann.status === 'published').length;
    const draftCount = filteredAnnouncements.filter(ann => ann.status === 'draft').length;
    const archivedCount = filteredAnnouncements.filter(ann => ann.status === 'archived').length;
    const topCount = filteredAnnouncements.filter(ann => ann.isTop).length;
    
    // 按作者统计
    const authorStats = [];
    const authors = [...new Set(filteredAnnouncements.map(ann => ann.pubId || ann.author).filter(Boolean))];
    authors.forEach(author => {
      const authorAnnouncements = filteredAnnouncements.filter(ann => 
        (ann.pubId || ann.author) === author
      );
      authorStats.push({
        author,
        count: authorAnnouncements.length,
        publishedCount: authorAnnouncements.filter(ann => ann.status === 'published').length
      });
    });
    
    return {
      totalAnnouncements,
      publishedCount,
      draftCount,
      archivedCount,
      topCount,
      authorStats
    };
  } catch (error) {
    console.error('获取公告统计失败:', error);
    throw error;
  }
};

/**
 * 上传公告附件
 */
export const uploadAnnouncementAttachment = async (file) => {
  try {
    const formData = new FormData();
    formData.append('file', file);
    
    // 后端可能没有专门的上传接口，暂时返回模拟数据
    return {
      url: URL.createObjectURL(file),
      filename: file.name,
      size: file.size
    };
  } catch (error) {
    console.error('上传公告附件失败:', error);
    throw error;
  }
};

/**
 * 导出公告列表
 */
export const exportAnnouncements = async (params = {}) => {
  try {
    // 获取要导出的公告列表
    const result = await getAnnouncements(params);
    return result.data;
  } catch (error) {
    console.error('导出公告列表失败:', error);
    throw error;
  }
}; 