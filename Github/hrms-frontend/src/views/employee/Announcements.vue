<template>
  <div class="employee-announcements">
    <!-- 返回按钮 -->
    <div class="back-button-container">
      <el-button 
        @click="goBack" 
        class="back-button"
        :icon="ArrowLeft"
        circle
        size="large"
      />
    </div>
    <!-- 页面标题区域 -->
    <div class="page-header">
      <h1 class="page-title">公司公告</h1>
      <p class="page-desc">查看最新的公司公告和通知信息</p>
    </div>

    <!-- 筛选和搜索区域 -->
    <div class="filter-section">
      <el-card class="filter-card">
        <div class="filter-content">
          <div class="filter-left">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索公告标题或内容"
              size="default"
              style="width: 300px"
              @input="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </div>

          <div class="filter-right">
            <!-- 已移除公告类型筛选功能 -->
          </div>
        </div>
      </el-card>
    </div>

    <!-- 公告列表 -->
    <div class="announcements-list">
      <el-card class="list-card">
        <div class="list-content">
          <!-- 公告项目 -->
          <div 
            v-for="announcement in paginatedAnnouncements" 
            :key="announcement.id"
            class="announcement-item"
            @click="viewAnnouncement(announcement)"
          >
            <!-- 公告头部信息 -->
            <div class="announcement-header">
              <div class="header-left">
                <h3 class="announcement-title">
                  {{ announcement.title }}
                </h3>
              </div>

              <div class="header-right">
                <span class="publish-time">{{ formatTime(announcement.publishTime) }}</span>
                <el-icon class="arrow-icon"><ArrowRight /></el-icon>
              </div>
            </div>

            <!-- 公告摘要 -->
            <div class="announcement-summary">
              <p class="summary-text">{{ announcement.summary }}</p>
            </div>

            <!-- 公告底部信息 -->
            <div class="announcement-footer">
              <div class="footer-left">
                <span class="publisher">发布人：{{ announcement.publisher }}</span>
                <span class="department">{{ announcement.department }}</span>
              </div>

              <div class="footer-right">
                <el-tag 
                  v-if="announcement.isImportant" 
                  type="danger" 
                  size="small"
                  effect="plain"
                >
                  重要
                </el-tag>
                <el-tag 
                  v-if="announcement.isTop" 
                  type="warning" 
                  size="small"
                  effect="plain"
                >
                  置顶
                </el-tag>
              </div>
            </div>
          </div>

          <!-- 空状态 -->
          <div v-if="filteredAnnouncements.length === 0" class="empty-state">
            <el-empty description="暂无公告信息" />
          </div>
        </div>

        <!-- 分页 -->
        <div class="pagination-wrapper">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[5, 10, 20, 30]"
            :total="filteredAnnouncements.length"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </el-card>
    </div>

    <!-- 公告详情弹窗 -->
    <el-dialog
      v-model="showDetailDialog"
      :title="selectedAnnouncement?.title"
      width="800px"
      class="announcement-dialog"
    >
      <div v-if="selectedAnnouncement" class="announcement-detail">
        <!-- 公告信息 -->
        <div class="detail-header">
          <div class="detail-meta">
            <span class="publish-info">
              {{ selectedAnnouncement.publisher }} · {{ selectedAnnouncement.department }}
            </span>
            
            <span class="publish-time">
              {{ formatTime(selectedAnnouncement.publishTime) }}
            </span>
          </div>

          <div class="detail-tags">
            <el-tag 
              v-if="selectedAnnouncement.isImportant" 
              type="danger" 
              size="small"
            >
              重要公告
            </el-tag>
            <el-tag 
              v-if="selectedAnnouncement.isTop" 
              type="warning" 
              size="small"
            >
              置顶公告
            </el-tag>
          </div>
        </div>

        <!-- 公告内容 -->
        <div class="detail-content">
          <div class="content-text" v-html="selectedAnnouncement.content"></div>
          
          <!-- 附件列表 -->
          <div v-if="selectedAnnouncement.attachments?.length" class="attachments">
            <h4>附件下载</h4>
            <div class="attachment-list">
              <div 
                v-for="attachment in selectedAnnouncement.attachments" 
                :key="attachment.id"
                class="attachment-item"
                @click="downloadAttachment(attachment)"
              >
                <el-icon><Document /></el-icon>
                <span class="attachment-name">{{ attachment.name }}</span>
                <span class="attachment-size">({{ attachment.size }})</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showDetailDialog = false">关闭</el-button>
                  <!-- 已移除标记为已读功能 -->
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Search, ArrowRight, Document, ArrowLeft } from '@element-plus/icons-vue';

// 响应式数据定义
const router = useRouter();
const route = useRoute();
// 已移除筛选类型和筛选状态功能
const searchKeyword = ref(''); // 搜索关键词
const currentPage = ref(1); // 当前页码
const pageSize = ref(10); // 每页显示数量
const showDetailDialog = ref(false); // 显示详情弹窗
const selectedAnnouncement = ref(null); // 选中的公告
const loading = ref(false); // 加载状态
const announcements = ref([]); // 公告列表

// 筛选参数
const filters = reactive({
  type: '',
  title: ''
});

// 返回上一页
const goBack = () => {
  // 如果有历史记录且不是登录页面，则返回上一页
  if (window.history.length > 1 && !document.referrer.includes('/login')) {
    router.back();
  } else {
    // 否则返回员工仪表板
    router.push('/employee/dashboard');
  }
};

// 获取公告列表
const fetchAnnouncements = async () => {
  try {
    loading.value = true;
    
    console.log('🔄 获取公告列表', { filters });
    
    // 调用真实的API接口
    const { getAnnouncements } = await import('@/api/announcement');
    
    // 构建查询参数
    const params = {
      type: filters.type || undefined,
      title: filters.title || undefined,
      status: '已发布' // 员工只能看到已发布的公告
    };
    
    // 过滤掉undefined的参数
    Object.keys(params).forEach(key => {
      if (params[key] === undefined) {
        delete params[key];
      }
    });
    
    const response = await getAnnouncements(params);
    
    console.log('✅ 公告列表获取成功:', response);
    
    // 处理响应数据
    if (response && Array.isArray(response)) {
      announcements.value = response.map(announcement => ({
        id: announcement.id,
        title: announcement.title,
        type: announcement.type || 'general',
        summary: announcement.content ? announcement.content.substring(0, 100) + '...' : '',
        content: announcement.content,
        publisher: announcement.pubId || '系统管理员',
        department: announcement.department || '管理部门',
        publishTime: announcement.pubDate || announcement.createTime,
        // 已移除阅读状态功能
        isImportant: announcement.isImportant || false,
        isTop: announcement.isTop || false,
        attachments: announcement.attachments || []
      }));
      
      // 按发布时间倒序排列（最新公告在前）
      announcements.value.sort((a, b) => {
        const dateA = new Date(a.publishTime);
        const dateB = new Date(b.publishTime);
        return dateB - dateA; // 倒序排列
      });
      
      console.log('📅 公告排序后的时间顺序:', announcements.value.slice(0, 5).map(a => ({
        id: a.id,
        title: a.title.substring(0, 20) + '...',
        publishTime: a.publishTime
      })));
      
      // 不需要设置pagination.total，直接使用filteredAnnouncements.length
    } else if (response && response.data) {
      // 如果返回的是分页数据
      announcements.value = response.data.map(announcement => ({
        id: announcement.id,
        title: announcement.title,
        type: announcement.type || 'general',
        summary: announcement.content ? announcement.content.substring(0, 100) + '...' : '',
        content: announcement.content,
        publisher: announcement.pubId || '系统管理员',
        department: announcement.department || '管理部门',
        publishTime: announcement.pubDate || announcement.createTime,
        // 已移除阅读状态功能
        isImportant: announcement.isImportant || false,
        isTop: announcement.isTop || false,
        attachments: announcement.attachments || []
      }));
      
      // 按发布时间倒序排列（最新公告在前）
      announcements.value.sort((a, b) => {
        const dateA = new Date(a.publishTime);
        const dateB = new Date(b.publishTime);
        return dateB - dateA; // 倒序排列
      });
      
      // 不需要设置pagination.total，直接使用filteredAnnouncements.length
    } else {
          console.warn('⚠️ API返回数据格式异常:', response);
    announcements.value = [];
    }
    
  } catch (error) {
    console.error('❌ 获取公告列表失败:', error);
    ElMessage.error('获取公告列表失败：' + (error.message || '未知错误'));
    
    // 出错时清空列表
    announcements.value = [];
  } finally {
    loading.value = false;
  }
};

// 计算属性：筛选后的公告列表
const filteredAnnouncements = computed(() => {
  let filtered = announcements.value;

  // 已移除按类型筛选功能
  // 已移除按阅读状态筛选功能

  // 按关键词搜索
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase();
    filtered = filtered.filter(item => 
      item.title.toLowerCase().includes(keyword) ||
      item.summary.toLowerCase().includes(keyword)
    );
  }

  // 确保结果按发布时间倒序排列（最新公告在前）
  filtered.sort((a, b) => {
    const dateA = new Date(a.publishTime);
    const dateB = new Date(b.publishTime);
    return dateB - dateA; // 倒序排列
  });

  return filtered;
});

// 分页后的公告列表
const paginatedAnnouncements = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  return filteredAnnouncements.value.slice(start, end);
});

/**
 * 页面挂载时执行
 * 初始化数据
 */
onMounted(async () => {
  console.log('📰 公告页面初始化');
  
  // 获取公告列表
  await fetchAnnouncements();
  
  // 检查是否有从仪表板传来的公告ID
  const announcementId = route.query.id;
  if (announcementId) {
    console.log('🎯 检测到公告ID参数:', announcementId);
    
    // 等待公告列表加载完成后查找对应公告
    const targetAnnouncement = announcements.value.find(
      ann => ann.id.toString() === announcementId.toString()
    );
    
    if (targetAnnouncement) {
      console.log('✅ 找到目标公告，自动打开详情:', targetAnnouncement);
      viewAnnouncement(targetAnnouncement);
    } else {
      console.warn('⚠️ 未找到指定ID的公告:', announcementId);
      ElMessage.warning('未找到指定的公告');
    }
  }
  
  totalRecords.value = announcements.value.length;
});

/**
 * 获取公告类型名称
 * @param {string} type - 公告类型
 * @returns {string} 类型名称
 */
const getTypeName = (type) => {
  const typeMap = {
    'important': '重要通知',
    'hr': '人事公告',
    'policy': '制度更新',
    'activity': '活动通知',
    'system': '系统维护'
  };
  return typeMap[type] || '其他';
};

/**
 * 获取公告类型对应的颜色
 * @param {string} type - 公告类型
 * @returns {string} 颜色类型
 */
const getTypeColor = (type) => {
  const colorMap = {
    'important': 'danger',
    'hr': 'primary',
    'policy': 'warning',
    'activity': 'success',
    'system': 'info'
  };
  return colorMap[type] || 'info';
};

/**
 * 格式化时间显示
 * @param {string} time - 时间字符串
 * @returns {string} 格式化后的时间
 */
const formatTime = (time) => {
  const date = new Date(time);
  const now = new Date();
  const diff = now - date;
  const days = Math.floor(diff / (1000 * 60 * 60 * 24));

  if (days === 0) {
    return '今天';
  } else if (days === 1) {
    return '昨天';
  } else if (days < 7) {
    return `${days}天前`;
  } else {
    return date.toLocaleDateString();
  }
};

/**
 * 处理筛选条件变化
 */
const handleFilterChange = () => {
  currentPage.value = 1;
  // 这里可以添加数据重新加载逻辑
};

/**
 * 处理搜索
 */
const handleSearch = () => {
  currentPage.value = 1;
  // 这里可以添加搜索逻辑
};

/**
 * 查看公告详情
 * @param {Object} announcement - 公告对象
 */
const viewAnnouncement = (announcement) => {
  selectedAnnouncement.value = announcement;
  showDetailDialog.value = true;
  
  // 已移除自动标记为已读功能
};

// 已移除标记为已读功能

/**
 * 下载附件
 * @param {Object} attachment - 附件对象
 */
const downloadAttachment = (attachment) => {
  ElMessage.success(`开始下载：${attachment.name}`);
  // 这里应该实现实际的下载逻辑
};

/**
 * 处理每页显示数量变化
 * @param {number} size - 新的每页显示数量
 */
const handleSizeChange = (size) => {
  pageSize.value = size;
  currentPage.value = 1;
};

/**
 * 处理当前页码变化
 * @param {number} page - 新的页码
 */
const handleCurrentChange = (page) => {
  currentPage.value = page;
};
</script>

<style lang="scss" scoped>
/* 页面容器样式 */
.employee-announcements {
  width: 100%;
  margin: 0 auto;
  padding: 24px;
  
  // 全局隐藏可能在彩色标题栏内的返回按钮
  .page-header, .header-content, .action-section, .header-actions,
  .el-tabs, .el-card, .table-header, .card-header {
    .el-button[aria-label*="返回"],
    .el-button:has(.el-icon svg[data-icon="ArrowLeft"]),
    .el-button:has(.el-icon[class*="arrow-left"]),
    .el-button:has(.el-icon.el-icon--arrow-left),
    .el-button .el-icon--arrow-left,
    .el-icon.el-icon--arrow-left,
    .el-button:contains("返回"),
    .back-btn,
    .return-btn {
      display: none !important;
      visibility: hidden !important;
    }
  }
  
  // 返回按钮样式已移至全局样式，此处删除局部样式以避免冲突
}

/* 页面标题区域 */
.page-header {
  margin-bottom: 24px;
  padding-left: 60px; // 为返回按钮留出空间
  
  .page-title {
    font-size: 1.8rem;
    font-weight: 600;
    color: #2c3e50;
    margin-bottom: 8px;
  }
  
  .page-desc {
    color: #7f8c8d;
    font-size: 1rem;
    margin: 0;
  }
}

/* 筛选区域 */
.filter-section {
  margin-bottom: 24px;
  
  .filter-card {
    border-radius: 12px;
    border: none;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  }
  
  .filter-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .filter-left {
      display: flex;
      gap: 12px;
    }
  }
}

/* 公告列表 */
.announcements-list {
  .list-card {
    border-radius: 12px;
    border: none;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  }
  
  .list-content {
    .announcement-item {
      padding: 20px;
      border-bottom: 1px solid #f0f0f0;
      cursor: pointer;
      transition: all 0.3s ease;
      
      &:last-child {
        border-bottom: none;
      }
      
      &:hover {
        background-color: #f8f9fa;
      }
      
      /* 已移除未读状态样式 */
      
      .announcement-header {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
        margin-bottom: 12px;
        
        .header-left {
          display: flex;
          align-items: center;
          gap: 12px;
          flex: 1;
          
          .type-tag {
            flex-shrink: 0;
          }
          
          .announcement-title {
            font-size: 1.1rem;
            font-weight: 600;
            color: #2c3e50;
            margin: 0;
            display: flex;
            align-items: center;
            gap: 8px;
          }
        }
        
        .header-right {
          display: flex;
          align-items: center;
          gap: 8px;
          flex-shrink: 0;
          
          .publish-time {
            font-size: 0.9rem;
            color: #7f8c8d;
          }
          
          .arrow-icon {
            color: #bdc3c7;
            transition: transform 0.3s ease;
          }
        }
      }
      
      .announcement-summary {
        margin-bottom: 12px;
        
        .summary-text {
          font-size: 0.95rem;
          color: #7f8c8d;
          line-height: 1.5;
          margin: 0;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          line-clamp: 2; /* 标准属性，用于兼容性 */
          -webkit-box-orient: vertical;
          overflow: hidden;
        }
      }
      
      .announcement-footer {
        display: flex;
        justify-content: space-between;
        align-items: center;
        
        .footer-left {
          display: flex;
          gap: 16px;
          
          .publisher,
          .department {
            font-size: 0.85rem;
            color: #95a5a6;
          }
        }
        
        .footer-right {
          display: flex;
          gap: 8px;
        }
      }
      
      &:hover .arrow-icon {
        transform: translateX(4px);
      }
    }
    
    .empty-state {
      padding: 40px 0;
      text-align: center;
    }
  }
  
  .pagination-wrapper {
    margin-top: 20px;
    display: flex;
    justify-content: center;
  }
}

/* 公告详情弹窗 */
.announcement-dialog {
  :deep(.el-dialog__body) {
    padding: 0 20px 20px;
  }
  
  .announcement-detail {
    .detail-header {
      padding: 20px 0;
      border-bottom: 1px solid #f0f0f0;
      margin-bottom: 20px;
      
      .detail-meta {
        display: flex;
        align-items: center;
        gap: 12px;
        margin-bottom: 12px;
        
        .publish-info {
          font-size: 0.9rem;
          color: #7f8c8d;
        }
        
        .publish-time {
          font-size: 0.9rem;
          color: #95a5a6;
        }
      }
      
      .detail-tags {
        display: flex;
        gap: 8px;
      }
    }
    
    .detail-content {
      .content-text {
        font-size: 1rem;
        line-height: 1.6;
        color: #2c3e50;
        margin-bottom: 20px;
        
        :deep(p) {
          margin-bottom: 12px;
        }
        
        :deep(ul) {
          padding-left: 20px;
          
          li {
            margin-bottom: 8px;
          }
        }
        
        :deep(strong) {
          font-weight: 600;
          color: #2c3e50;
        }
      }
      
      .attachments {
        border-top: 1px solid #f0f0f0;
        padding-top: 20px;
        
        h4 {
          font-size: 1rem;
          font-weight: 600;
          color: #2c3e50;
          margin-bottom: 12px;
        }
        
        .attachment-list {
          .attachment-item {
            display: flex;
            align-items: center;
            gap: 8px;
            padding: 8px 12px;
            background: #f8f9fa;
            border-radius: 6px;
            cursor: pointer;
            transition: background-color 0.3s ease;
            margin-bottom: 8px;
            
            &:hover {
              background-color: #e9ecef;
            }
            
            .attachment-name {
              font-size: 0.9rem;
              color: #2c3e50;
            }
            
            .attachment-size {
              font-size: 0.8rem;
              color: #7f8c8d;
            }
          }
        }
      }
    }
  }
  
  .dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .employee-announcements {
    padding: 16px;
  }
  
  .page-header {
    .page-title {
      font-size: 1.5rem;
    }
  }
  
  .filter-content {
    flex-direction: column !important;
    gap: 16px;
    align-items: stretch !important;
    
    .filter-left {
      justify-content: space-between;
    }
    
    .filter-right {
      width: 100%;
      
      .el-input {
        width: 100% !important;
      }
    }
  }
  
  .announcement-header {
    flex-direction: column !important;
    gap: 12px;
    align-items: flex-start !important;
    
    .header-left {
      width: 100%;
    }
    
    .header-right {
      width: 100%;
      justify-content: space-between;
    }
  }
  
  .announcement-footer {
    flex-direction: column !important;
    gap: 12px;
    align-items: flex-start !important;
  }
}

@media (max-width: 480px) {
  .header-left {
    flex-direction: column !important;
    gap: 8px !important;
    align-items: flex-start !important;
  }
  
  .footer-left {
    flex-direction: column !important;
    gap: 4px !important;
  }
  
  .announcement-dialog {
    :deep(.el-dialog) {
      width: 95% !important;
      margin: 5vh auto !important;
    }
  }
}
</style> 