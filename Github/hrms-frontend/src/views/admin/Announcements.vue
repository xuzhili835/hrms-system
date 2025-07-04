<template>
  <div class="announcements-container">
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
    
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>公告管理</h1>
      <el-button type="primary" @click="showAddDialog = true">
        <el-icon><Plus /></el-icon>
        发布公告
      </el-button>
    </div>

    <!-- 搜索区域 -->
    <el-card class="search-card">
      <el-row :gutter="20">
        <el-col :span="14">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索公告标题或内容"
            clearable
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-col>
        <el-col :span="6">
          <el-select v-model="selectedStatus" placeholder="公告状态" clearable>
            <el-option label="全部状态" value="" />
            <el-option label="草稿" value="草稿" />
            <el-option label="已发布" value="已发布" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-button type="primary" @click="handleSearch">搜索</el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 公告列表 -->
    <el-card class="table-card">
      <el-table :data="paginatedAnnouncements" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="title" label="公告标题" min-width="280" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="author" label="发布人" width="120" />
        <el-table-column prop="publishDate" label="发布时间" width="180" />
        <el-table-column label="操作" width="200" align="center">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-tooltip content="查看详情" placement="top">
                <el-button
                  type="primary"
                  :icon="View"
                  size="small"
                  @click="viewAnnouncement(row)"
                />
              </el-tooltip>
              
              <el-tooltip content="编辑公告" placement="top">
                <el-button
                  type="warning"
                  :icon="Edit"
                  size="small"
                  @click="editAnnouncement(row)"
                />
              </el-tooltip>
              
              <el-tooltip v-if="row.status === '草稿'" content="发布公告" placement="top">
                <el-button
                  type="success"
                  size="small"
                  @click="publishDraft(row)"
                >
                  发布
                </el-button>
              </el-tooltip>
              
              <el-tooltip content="删除公告" placement="top">
                <el-button
                  type="danger"
                  :icon="Delete"
                  size="small"
                  @click="deleteAnnouncement(row)"
                />
              </el-tooltip>
            </div>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页组件 -->
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

    <!-- 添加/编辑公告对话框 -->
    <el-dialog
      v-model="showAddDialog"
      :title="editingAnnouncement ? '编辑公告' : '发布公告'"
      width="700px"
    >
      <el-form ref="announcementFormRef" :model="announcementForm" :rules="formRules" label-width="100px">
        <el-form-item label="公告标题" prop="title">
          <el-input
            v-model="announcementForm.title"
            placeholder="请输入公告标题"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="公告内容" prop="content">
          <el-input
            v-model="announcementForm.content"
            type="textarea"
            :rows="6"
            placeholder="请输入公告内容..."
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button @click="saveDraft">保存草稿</el-button>
        <el-button type="primary" @click="publishAnnouncement">
          {{ editingAnnouncement ? '更新并发布' : '发布公告' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 公告详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="公告详情" width="600px">
      <div class="announcement-detail" v-if="selectedAnnouncement">
        <div class="detail-header">
          <h2>{{ selectedAnnouncement.title }}</h2>
          <div class="meta-info">
            <el-tag :type="getStatusType(selectedAnnouncement.status)">
              {{ selectedAnnouncement.status }}
            </el-tag>
          </div>
          <div class="publish-info">
            <span>发布人：{{ selectedAnnouncement.author }}</span>
            <span>发布时间：{{ selectedAnnouncement.publishDate }}</span>
          </div>
        </div>
        <div class="detail-content">
          <div class="content-text">{{ selectedAnnouncement.content }}</div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, ArrowLeft, View, Edit, Delete } from '@element-plus/icons-vue'
import { getAnnouncements, createAnnouncement, updateAnnouncement, deleteAnnouncement as deleteAnnouncementAPI } from '@/api/announcement'

// 响应式数据
const router = useRouter()
const showAddDialog = ref(false)
const showDetailDialog = ref(false)
const searchKeyword = ref('')
const selectedStatus = ref('')
const editingAnnouncement = ref(null)
const selectedAnnouncement = ref(null)
const loading = ref(false)
const currentPage = ref(1) // 当前页码
const pageSize = ref(10) // 每页显示数量

// 返回上一页
const goBack = () => {
  router.back()
}

// 公告表单数据
const announcementForm = reactive({
  title: '',
  content: ''
})

// 表单验证规则
const formRules = {
  title: [{ required: true, message: '请输入公告标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入公告内容', trigger: 'blur' }]
}

// 公告数据
const announcements = ref([])

// 获取公告列表
const fetchAnnouncements = async () => {
  try {
    loading.value = true
    const response = await getAnnouncements()
    console.log('公告数据:', response)
    
    // 处理响应数据，将数据库字段映射到前端需要的格式
    if (response && Array.isArray(response)) {
      announcements.value = response.map(item => ({
        id: item.id,
        title: item.title,
        content: item.content,
        status: item.status || '已发布',
        author: item.pubId || '管理员',
        publishDate: item.pubDate || item.createdAt
      }))
    } else if (response && response.data && Array.isArray(response.data)) {
      announcements.value = response.data.map(item => ({
        id: item.id,
        title: item.title,
        content: item.content,
        status: item.status || '已发布',
        author: item.pubId || '管理员',
        publishDate: item.pubDate || item.createdAt
      }))
    }
    
    console.log('处理后的公告数据:', announcements.value)
  } catch (error) {
    console.error('获取公告列表失败:', error)
    ElMessage.error('获取公告列表失败')
  } finally {
    loading.value = false
  }
}

// 组件挂载时获取数据
onMounted(() => {
  fetchAnnouncements()
})

// 计算属性 - 筛选后的公告列表
const filteredAnnouncements = computed(() => {
  let result = announcements.value
  
  if (searchKeyword.value) {
    result = result.filter(ann => 
      ann.title.includes(searchKeyword.value) || 
      ann.content.includes(searchKeyword.value)
    )
  }
  
  if (selectedStatus.value) {
    result = result.filter(ann => ann.status === selectedStatus.value)
  }
  
  // 按发布时间倒序排序
  result.sort((a, b) => {
    return new Date(b.publishDate) - new Date(a.publishDate)
  })
  
  return result
})

// 分页后的公告列表
const paginatedAnnouncements = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredAnnouncements.value.slice(start, end)
})

// 方法定义
const getStatusType = (status) => {
  const statusMap = {
    '已发布': 'success',
    '草稿': 'info'
  }
  return statusMap[status] || 'info'
}

const handleSearch = () => {
  // 重置到第一页
  currentPage.value = 1
  // 搜索逻辑已在计算属性中处理
}

// 分页方法
const handleSizeChange = (newSize) => {
  pageSize.value = newSize
  currentPage.value = 1 // 重置到第一页
}

const handleCurrentChange = (newPage) => {
  currentPage.value = newPage
}

const viewAnnouncement = (announcement) => {
  selectedAnnouncement.value = announcement
  showDetailDialog.value = true
}

const editAnnouncement = (announcement) => {
  editingAnnouncement.value = announcement
  Object.assign(announcementForm, { ...announcement })
  showAddDialog.value = true
}

const deleteAnnouncement = async (announcement) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除公告 "${announcement.title}" 吗？`,
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 调用API删除公告
    await deleteAnnouncementAPI(announcement.id)
    
    // 从本地数组中移除
    const index = announcements.value.findIndex(ann => ann.id === announcement.id)
    if (index > -1) {
      announcements.value.splice(index, 1)
    }
    
    ElMessage.success('公告删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除公告失败:', error)
      ElMessage.error('删除公告失败')
    } else {
      ElMessage.info('已取消删除')
    }
  }
}

const saveDraft = async () => {
  try {
    const data = {
      title: announcementForm.title,
      content: announcementForm.content,
      status: '草稿',
      pubId: 'admin'
    }
    
    if (editingAnnouncement.value) {
      await updateAnnouncement(editingAnnouncement.value.id, data)
      ElMessage.success('草稿保存成功')
    } else {
      data.pubDate = new Date().toISOString().split('T')[0]
      await createAnnouncement(data)
      ElMessage.success('草稿保存成功')
    }
    
    showAddDialog.value = false
    resetForm()
    await fetchAnnouncements()
  } catch (error) {
    console.error('保存草稿失败:', error)
    ElMessage.error('保存草稿失败')
  }
}

const publishAnnouncement = async () => {
  try {
    const data = {
      title: announcementForm.title,
      content: announcementForm.content,
      status: '已发布',
      pubId: 'admin'
    }
    
    if (editingAnnouncement.value) {
      await updateAnnouncement(editingAnnouncement.value.id, data)
      ElMessage.success('公告更新并发布成功')
    } else {
      data.pubDate = new Date().toISOString().split('T')[0]
      await createAnnouncement(data)
      ElMessage.success('公告发布成功')
    }
    
    showAddDialog.value = false
    resetForm()
    await fetchAnnouncements()
  } catch (error) {
    console.error('发布公告失败:', error)
    ElMessage.error('发布公告失败')
  }
}

const publishDraft = async (announcement) => {
  try {
    await ElMessageBox.confirm(
      `确定要发布公告 "${announcement.title}" 吗？`,
      '发布确认',
      {
        confirmButtonText: '确定发布',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const updateData = {
      title: announcement.title,
      content: announcement.content,
      status: '已发布',
      pubId: 'admin'
    }
    
    await updateAnnouncement(announcement.id, updateData)
    ElMessage.success('公告发布成功')
    await fetchAnnouncements()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('发布公告失败:', error)
      ElMessage.error('发布公告失败')
    }
  }
}

const resetForm = () => {
  Object.assign(announcementForm, {
    title: '',
    content: ''
  })
  editingAnnouncement.value = null
}
</script>

<style scoped>
.announcements-container {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.back-button-container {
  margin-bottom: 20px;
}

.back-button {
  background-color: #409eff;
  border-color: #409eff;
  color: white;
}

.back-button:hover {
  background-color: #66b1ff;
  border-color: #66b1ff;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.page-header h1 {
  margin: 0;
  font-size: 24px;
  color: #303133;
}

.search-card {
  margin-bottom: 20px;
}

.table-card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.action-buttons {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.action-buttons .el-button {
  margin: 0;
  border-radius: 6px;
  transition: all 0.3s ease;
  font-size: 12px;
  padding: 8px 12px;
}

.action-buttons .el-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

/* 为不同类型的按钮添加特定的悬停效果 */
.action-buttons .el-button--primary:hover {
  background-color: #409eff;
  border-color: #409eff;
}

.action-buttons .el-button--warning:hover {
  background-color: #e6a23c;
  border-color: #e6a23c;
}

.action-buttons .el-button--success:hover {
  background-color: #67c23a;
  border-color: #67c23a;
}

.action-buttons .el-button--danger:hover {
  background-color: #f56c6c;
  border-color: #f56c6c;
}

/* 分页样式 */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
  padding: 16px 0;
  border-top: 1px solid #ebeef5;
}

.action-buttons .el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.action-buttons .el-button.is-circle {
  width: 32px;
  height: 32px;
  padding: 0;
  border-radius: 50%;
}

/* 响应式设计 - 小屏幕适配 */
@media (max-width: 768px) {
  .action-buttons {
    gap: 8px;
  }
  
  .action-buttons .el-button.is-circle {
    width: 28px;
    height: 28px;
  }
}

/* 表格行悬停效果优化 */
.el-table tbody tr:hover .action-buttons .el-button {
  opacity: 1;
  visibility: visible;
}

.action-buttons .el-button {
  opacity: 0.8;
  transition: all 0.3s ease;
}

.action-buttons .el-button:hover {
  opacity: 1;
}

.announcement-detail {
  padding: 20px 0;
}

.detail-header {
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e4e7ed;
}

.detail-header h2 {
  margin: 0 0 10px 0;
  color: #303133;
  font-size: 20px;
}

.meta-info {
  margin-bottom: 10px;
}

.publish-info {
  color: #909399;
  font-size: 14px;
}

.publish-info span {
  margin-right: 20px;
}

.detail-content {
  line-height: 1.6;
}

.content-text {
  white-space: pre-wrap;
  color: #606266;
  font-size: 14px;
}
</style> 