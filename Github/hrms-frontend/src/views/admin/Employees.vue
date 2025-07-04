<!--
  员工管理页面
  
  功能说明：
  - 员工信息管理：查看、添加、编辑、删除员工信息
  - 搜索筛选：支持按姓名、部门、职位等条件筛选
  - 批量操作：批量删除、导出员工数据
  - 员工详情：查看员工详细信息和工作记录
  
  技术特点：
  - 响应式设计，适配各种屏幕尺寸
  - 表格分页，提升大数据量加载性能
  - 表单验证，确保数据完整性和准确性
  - 操作确认，防止误操作
-->

<template>
  <div class="employees-container">
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
      <div class="header-content">
        <div class="title-section">
          <h1 class="page-title">
            <el-icon><User /></el-icon>
            员工管理
          </h1>
          <p class="page-description">管理公司所有员工信息，包括基本资料、职位信息</p>
        </div>
        <div class="action-section">
          <el-button type="primary" @click="showAddDialog = true">
            <el-icon><Plus /></el-icon>
            添加员工
          </el-button>
          <el-button type="primary" @click="exportEmployees" :loading="loading">
            <el-icon><Download /></el-icon>
            导出数据
          </el-button>
        </div>
      </div>
    </div>

    <!-- 搜索筛选区域 关键词模糊查询功能实现-->
    <div class="search-section">
      <el-card shadow="never">
        <div class="search-form">
          <el-row :gutter="20">
            <el-col :xs="24" :sm="12" :md="6">
              <el-input
                v-model="searchKeyword"
                placeholder="搜索员工姓名或工号"
                clearable
                @input="handleSearch"
                @clear="handleSearch"
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6">
              <el-select
                v-model="selectedDepartment"
                placeholder="选择部门"
                clearable
                @change="handleSearch"
                @clear="handleSearch"
              >
                <el-option label="全部部门" value="" />
                <el-option label="技术部" value="技术部" />
                <el-option label="人事部" value="人事部" />
                <el-option label="财务部" value="财务部" />
                <el-option label="市场部" value="市场部" />
                <el-option label="销售部" value="销售部" />
                <el-option label="产品部" value="产品部" />
                <el-option label="行政部" value="行政部" />
                <el-option label="研发部" value="研发部" />
              </el-select>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6">
              <el-select
                v-model="selectedStatus"
                placeholder="员工状态"
                clearable
                @change="handleSearch"
                @clear="handleSearch"
              >
                <el-option label="全部状态" value="" />
                <el-option label="在职" value="在职" />
                <el-option label="离职" value="离职" />
                <el-option label="试用期" value="试用期" />
              </el-select>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6">
              <el-button type="primary" @click="handleSearchImmediate">
                <el-icon><Search /></el-icon>
                搜索
              </el-button>
              <el-button @click="resetSearch">
                <el-icon><Refresh /></el-icon>
                重置
              </el-button>
            </el-col>
          </el-row>
        </div>
      </el-card>
    </div>

    <!-- 员工列表区域 -->
    <div class="table-section">
      <el-card shadow="never">
        <!-- 批量操作工具栏 -->
        <div class="table-toolbar" v-if="selectedEmployees.length > 0">
          <div class="selected-info">
            已选择 <span class="selected-count">{{ selectedEmployees.length }}</span> 名员工
          </div>
          <div class="batch-actions">
            <el-button type="danger" @click="batchDelete">
              <el-icon><Delete /></el-icon>
              批量删除
            </el-button>
            <el-button type="primary" @click="batchExport" :loading="loading">
              <el-icon><Download /></el-icon>
              批量导出
            </el-button>
          </div>
        </div>

        <!-- 员工数据表格 -->
        <el-table
          :data="employees"
          v-loading="loading"
          @selection-change="handleSelectionChange"
          stripe
          style="width: 100%"
        >
          <el-table-column type="selection" width="55" />
          <el-table-column prop="empId" label="工号" width="140" />
          <el-table-column prop="name" label="姓名" width="140" />
          <el-table-column prop="dept" label="部门" width="150" />
          <el-table-column prop="pos" label="职位" min-width="160" />
          <el-table-column prop="entryTime" label="入职日期" width="140" />
          <el-table-column prop="status" label="状态" width="120">
            <template #default="{ row }">
              <el-tag
                :type="getStatusType(row.status)"
                size="small"
              >
                {{ row.status }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="220" fixed="right">
            <template #default="{ row }">
              <el-button
                type="primary"
                size="small"
                @click="viewEmployee(row)"
              >
                查看
              </el-button>
              <el-button
                size="small"
                @click="editEmployee(row)"
              >
                编辑
              </el-button>
              <el-button
                type="danger"
                size="small"
                :disabled="row.status !== '离职'"
                @click="deleteEmployee(row)"
                :title="row.status !== '离职' ? '只能删除离职状态的员工' : '删除员工'"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页组件 -->
        <div class="pagination-wrapper">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="totalEmployees"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </el-card>
    </div>

    <!-- 添加/编辑员工对话框 -->
    <el-dialog
      v-model="showAddDialog"
      :title="editingEmployee ? '编辑员工' : '添加员工'"
      width="600px"
      @close="resetForm"
    >
      <el-form
        ref="employeeFormRef"
        :model="employeeForm"
        :rules="formRules"
        label-width="100px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="employeeForm.name" placeholder="请输入员工姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="工号" prop="empId">
              <el-input v-model="employeeForm.empId" placeholder="请输入工号" :disabled="!!editingEmployee" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="部门" prop="dept">
              <el-select v-model="employeeForm.dept" placeholder="请选择部门" style="width: 100%">
                <el-option label="技术部" value="技术部" />
                <el-option label="人事部" value="人事部" />
                <el-option label="财务部" value="财务部" />
                <el-option label="市场部" value="市场部" />
                <el-option label="销售部" value="销售部" />
                <el-option label="产品部" value="产品部" />
                <el-option label="行政部" value="行政部" />
                <el-option label="研发部" value="研发部" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="职位" prop="pos">
              <el-input v-model="employeeForm.pos" placeholder="请输入职位" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="入职日期" prop="entryTime">
              <el-date-picker
                v-model="employeeForm.entryTime"
                type="date"
                placeholder="请选择入职日期"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="员工状态" prop="status">
              <el-select v-model="employeeForm.status" placeholder="请选择状态" style="width: 100%">
                <el-option label="在职" value="在职" />
                <el-option label="试用期" value="试用期" />
                <el-option label="离职" value="离职" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="密码" prop="pwd" v-if="!editingEmployee">
          <el-input
            v-model="employeeForm.pwd"
            type="password"
            placeholder="请输入初始密码"
            show-password
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="saveEmployee">确定</el-button>
      </template>
    </el-dialog>

    <!-- 员工详情对话框 -->
    <el-dialog
      v-model="showDetailDialog"
      title="员工详情"
      width="500px"
    >
      <div class="employee-detail" v-if="selectedEmployee">
        <div class="detail-header">
          <div class="basic-info">
            <h3>{{ selectedEmployee.name }}</h3>
            <p>{{ selectedEmployee.dept }} - {{ selectedEmployee.pos }}</p>
            <el-tag :type="getStatusType(selectedEmployee.status)">
              {{ selectedEmployee.status }}
            </el-tag>
          </div>
        </div>
        
        <el-descriptions :column="2" border>
          <el-descriptions-item label="工号">{{ selectedEmployee.empId }}</el-descriptions-item>
          <el-descriptions-item label="姓名">{{ selectedEmployee.name }}</el-descriptions-item>
          <el-descriptions-item label="部门">{{ selectedEmployee.dept }}</el-descriptions-item>
          <el-descriptions-item label="职位">{{ selectedEmployee.pos }}</el-descriptions-item>
          <el-descriptions-item label="入职日期">{{ selectedEmployee.entryTime }}</el-descriptions-item>
          <el-descriptions-item label="工作年限">{{ calculateWorkYears(selectedEmployee.entryTime) }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Plus, Download, Search, Refresh, Delete, ArrowLeft } from '@element-plus/icons-vue'
import { getEmployeeList, addEmployee, updateEmployee, deleteEmployee as deleteEmployeeAPI } from '@/api/employee'

// ==================== 响应式数据定义 ====================

/**
 * 路由实例
 */
const router = useRouter()

/**
 * 页面加载状态
 * 用于控制表格加载动画显示
 */
const loading = ref(false)

/**
 * 对话框显示状态控制
 * showAddDialog: 添加/编辑员工对话框
 * showDetailDialog: 员工详情查看对话框
 */
const showAddDialog = ref(false)
const showDetailDialog = ref(false)

/**
 * 搜索表单数据
 * 用于员工信息的筛选和搜索
 */
const searchKeyword = ref('')
const selectedDepartment = ref('')
const selectedStatus = ref('')

/**
 * 员工表单数据
 * 用于添加和编辑员工信息
 */
const employeeForm = reactive({
  name: '',
  empId: '',
  dept: '',
  pos: '',
  entryTime: '',
  status: '在职',
  pwd: ''
})

/**
 * 表单验证规则
 * 确保员工信息的完整性和准确性
 */
const formRules = {
  name: [
    { required: true, message: '请输入员工姓名', trigger: 'blur' }
  ],
  empId: [
    { required: true, message: '请输入工号', trigger: 'blur' }
  ],
  dept: [
    { required: true, message: '请选择部门', trigger: 'change' }
  ],
  pos: [
    { required: true, message: '请输入职位', trigger: 'blur' }
  ],
  entryTime: [
    { required: true, message: '请选择入职日期', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择员工状态', trigger: 'change' }
  ],
  pwd: [
    { required: true, message: '请输入初始密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

/**
 * 分页相关数据
 * 控制表格数据的分页显示
 */
const currentPage = ref(1)      // 当前页码
const pageSize = ref(20)        // 每页显示数量
const totalEmployees = ref(0)   // 员工总数

/**
 * 选中的员工数据
 * 用于批量操作和详情查看
 */
const selectedEmployees = ref([])  // 批量选中的员工列表
const selectedEmployee = ref(null) // 当前查看详情的员工
const editingEmployee = ref(null)  // 当前编辑的员工

/**
 * 员工数据列表
 */
const employees = ref([])

// ==================== 计算属性 ====================



// ==================== 方法定义 ====================

/**
 * 返回上一页
 */
const goBack = () => {
  router.go(-1)
}

/**
 * 获取状态对应的标签类型
 * @param {string} status - 员工状态
 * @returns {string} - Element Plus 标签类型
 */
const getStatusType = (status) => {
  const statusMap = {
    '在职': 'success',
    '试用期': 'warning', 
    '离职': 'danger'
  }
  return statusMap[status] || 'info'
}

/**
 * 计算工作年限
 * @param {string} hireDate - 入职日期
 * @returns {string} - 工作年限描述
 */
const calculateWorkYears = (hireDate) => {
  if (!hireDate) return '未知'
  
  const hire = new Date(hireDate)
  const now = new Date()
  const diffTime = Math.abs(now - hire)
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
  const years = Math.floor(diffDays / 365)
  const months = Math.floor((diffDays % 365) / 30)
  
  if (years > 0) {
    return `${years}年${months}个月`
  } else if (months > 0) {
    return `${months}个月`
  } else {
    return `${diffDays}天`
  }
}

// 搜索防抖定时器
let searchTimer = null

/**
 * 处理搜索
 */
const handleSearch = () => {
  // 清除之前的定时器
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
  
  // 设置防抖延迟
  searchTimer = setTimeout(() => {
    currentPage.value = 1
    loadEmployees()
  }, 300) // 300ms 防抖延迟
}

/**
 * 立即搜索（点击搜索按钮时使用）
 */
const handleSearchImmediate = () => {
  // 清除防抖定时器
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
  currentPage.value = 1
  loadEmployees()
}

/**
 * 重置搜索条件
 */
const resetSearch = () => {
  searchKeyword.value = ''
  selectedDepartment.value = ''
  selectedStatus.value = ''
  handleSearchImmediate()
}

/**
 * 处理表格选择变化
 * @param {Array} selection - 选中的员工列表
 */
const handleSelectionChange = (selection) => {
  selectedEmployees.value = selection
}

/**
 * 处理分页大小变化
 * @param {number} size - 新的分页大小
 */
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadEmployees()
}

/**
 * 处理当前页变化
 * @param {number} page - 新的页码
 */
const handleCurrentChange = (page) => {
  currentPage.value = page
  loadEmployees()
}

/**
 * 查看员工详情
 * @param {Object} employee - 员工对象
 */
const viewEmployee = (employee) => {
  selectedEmployee.value = employee
  showDetailDialog.value = true
}

/**
 * 编辑员工信息
 * @param {Object} employee - 员工对象
 */
const editEmployee = (employee) => {
  editingEmployee.value = employee
  
  // 填充表单数据
  Object.assign(employeeForm, {
    name: employee.name,
    empId: employee.empId,
    dept: employee.dept,
    pos: employee.pos,
    entryTime: employee.entryTime,
    status: employee.status,
    pwd: '' // 编辑时不显示密码
  })
  
  showAddDialog.value = true
}

/**
 * 生成员工Excel文件
 * @param {Array} data - 员工数据数组
 */
const generateEmployeeExcel = async (data) => {
  try {
    console.log('📝 开始生成Excel文件，数据量:', data.length)
    
    // 动态导入xlsx库
    const XLSX = await import('xlsx')
    
    // 表头定义
    const headers = [
      '员工工号',
      '员工姓名', 
      '部门',
      '职位',
      '入职日期',
      '员工状态',
      '工作年限',
      '导出时间'
    ]
    
    // 数据行处理
    const rows = data.map(employee => [
      employee.empId || '',
      employee.name || '',
      employee.dept || '',
      employee.pos || '',
      employee.entryTime || '',
      employee.status || '',
      calculateWorkYears(employee.entryTime),
      new Date().toLocaleString()
    ])
    
    // 创建工作簿
    const workbook = XLSX.utils.book_new()
    
    // 准备工作表数据（表头 + 数据行）
    const worksheetData = [headers, ...rows]
    
    // 创建工作表
    const worksheet = XLSX.utils.aoa_to_sheet(worksheetData)
    
    // 设置列宽
    const colWidths = [
      { wch: 15 }, // 员工工号
      { wch: 15 }, // 员工姓名
      { wch: 15 }, // 部门
      { wch: 20 }, // 职位
      { wch: 15 }, // 入职日期
      { wch: 12 }, // 员工状态
      { wch: 12 }, // 工作年限
      { wch: 20 }  // 导出时间
    ]
    worksheet['!cols'] = colWidths
    
    // 设置表头样式
    const headerRange = XLSX.utils.decode_range(worksheet['!ref'])
    for (let col = headerRange.s.c; col <= headerRange.e.c; col++) {
      const cellAddress = XLSX.utils.encode_cell({ r: 0, c: col })
      if (!worksheet[cellAddress]) continue
      
      worksheet[cellAddress].s = {
        font: { bold: true, color: { rgb: "FFFFFF" } },
        fill: { fgColor: { rgb: "4472C4" } },
        alignment: { horizontal: "center", vertical: "center" },
        border: {
          top: { style: "thin", color: { rgb: "000000" } },
          bottom: { style: "thin", color: { rgb: "000000" } },
          left: { style: "thin", color: { rgb: "000000" } },
          right: { style: "thin", color: { rgb: "000000" } }
        }
      }
    }
    
    // 设置数据行样式
    for (let row = 1; row <= headerRange.e.r; row++) {
      for (let col = headerRange.s.c; col <= headerRange.e.c; col++) {
        const cellAddress = XLSX.utils.encode_cell({ r: row, c: col })
        if (!worksheet[cellAddress]) continue
        
        worksheet[cellAddress].s = {
          alignment: { horizontal: "center", vertical: "center" },
          border: {
            top: { style: "thin", color: { rgb: "D0D0D0" } },
            bottom: { style: "thin", color: { rgb: "D0D0D0" } },
            left: { style: "thin", color: { rgb: "D0D0D0" } },
            right: { style: "thin", color: { rgb: "D0D0D0" } }
          }
        }
      }
    }
    
    // 添加工作表到工作簿
    XLSX.utils.book_append_sheet(workbook, worksheet, '员工信息')
    
    // 生成Excel文件
    const excelBuffer = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' })
    
    // 创建Blob对象
    const blob = new Blob([excelBuffer], { 
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' 
    })
    
    // 生成文件名（带时间戳）
    const now = new Date()
    const timestamp = `${now.getFullYear()}${(now.getMonth() + 1).toString().padStart(2, '0')}${now.getDate().toString().padStart(2, '0')}_${now.getHours().toString().padStart(2, '0')}${now.getMinutes().toString().padStart(2, '0')}`
    const filename = `员工信息表_${timestamp}.xlsx`
    
    // 创建下载链接
    const link = document.createElement('a')
    const url = URL.createObjectURL(blob)
    link.setAttribute('href', url)
    link.setAttribute('download', filename)
    link.style.visibility = 'hidden'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
    
    console.log('✅ Excel文件生成成功:', filename)
    
  } catch (error) {
    console.error('❌ 生成Excel文件失败:', error)
    throw new Error('生成Excel文件失败，请检查网络连接或联系系统管理员')
  }
}

/**
 * 删除员工
 * @param {Object} employee - 员工对象
 */
const deleteEmployee = async (employee) => {
  try {
    // 验证员工状态
    if (employee.status !== '离职') {
      ElMessage.warning('只能删除状态为"离职"的员工')
      return
    }
    
    await ElMessageBox.confirm(
      `确定要删除离职员工"${employee.name}"吗？此操作不可恢复。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
        dangerouslyUseHTMLString: true,
        message: `
          <div>
            <p>员工信息：</p>
            <ul style="margin: 10px 0; padding-left: 20px;">
              <li>姓名：${employee.name}</li>
              <li>工号：${employee.empId}</li>
              <li>部门：${employee.dept}</li>
              <li>状态：<span style="color: #f56c6c;">${employee.status}</span></li>
            </ul>
            <p style="color: #f56c6c; font-weight: bold;">注意：此操作不可恢复！</p>
          </div>
        `
      }
    )
    
    await deleteEmployeeAPI(employee.empId)
    ElMessage.success('离职员工删除成功')
    loadEmployees()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败：' + (error.message || '未知错误'))
    }
  }
}

/**
 * 保存员工信息
 */
const saveEmployee = async () => {
  try {
    // 表单验证
    await employeeFormRef.value.validate()
    
    if (editingEmployee.value) {
      // 编辑员工
      await updateEmployee(editingEmployee.value.empId, employeeForm)
      ElMessage.success('员工信息更新成功')
    } else {
      // 添加员工
      await addEmployee(employeeForm)
      ElMessage.success('员工添加成功')
    }
    
    showAddDialog.value = false
    loadEmployees()
  } catch (error) {
    ElMessage.error('保存失败：' + (error.message || '未知错误'))
  }
}

/**
 * 重置表单
 */
const resetForm = () => {
  editingEmployee.value = null
  Object.assign(employeeForm, {
    name: '',
    empId: '',
    dept: '',
    pos: '',
    entryTime: '',
    status: '在职',
    pwd: ''
  })
  
  // 清除表单验证
  if (employeeFormRef.value) {
    employeeFormRef.value.clearValidate()
  }
}

/**
 * 批量删除员工
 */
const batchDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedEmployees.value.length} 名员工吗？此操作不可恢复。`,
      '确认批量删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 这里应该调用批量删除API
    ElMessage.success(`已删除 ${selectedEmployees.value.length} 名员工`)
    selectedEmployees.value = []
    loadEmployees()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败')
    }
  }
}

/**
 * 导出员工数据
 */
const exportEmployees = async () => {
  try {
    loading.value = true
    console.log('🔄 开始导出员工数据...')
    
    // 获取当前搜索条件下的所有员工数据（不分页）
    const result = await getEmployeeList({
      page: 1,
      pageSize: 10000, // 获取所有数据
      name: searchKeyword.value,
      dept: selectedDepartment.value,
      status: selectedStatus.value
    })
    
    const exportData = result.data || []
    console.log('📊 获取到导出数据:', exportData.length, '条')
    
    if (exportData.length === 0) {
      ElMessage.warning('没有可导出的数据')
      return
    }
    
    // 生成Excel文件
    await generateEmployeeExcel(exportData)
    
    ElMessage.success(`成功导出 ${exportData.length} 条员工数据`)
    
  } catch (error) {
    console.error('❌ 导出员工数据失败:', error)
    ElMessage.error('导出失败：' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

/**
 * 批量导出选中员工数据
 */
const batchExport = async () => {
  if (selectedEmployees.value.length === 0) {
    ElMessage.warning('请先选择要导出的员工')
    return
  }
  
  try {
    loading.value = true
    console.log('🔄 开始批量导出员工数据...', selectedEmployees.value.length, '条')
    
    // 生成Excel文件
    await generateEmployeeExcel(selectedEmployees.value)
    
    ElMessage.success(`成功导出 ${selectedEmployees.value.length} 条员工数据`)
    
  } catch (error) {
    console.error('❌ 批量导出员工数据失败:', error)
    ElMessage.error('导出失败：' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// ==================== 生命周期钩子 ====================

/**
 * 加载员工列表数据
 */
const loadEmployees = async () => {
  try {
    loading.value = true
    const result = await getEmployeeList({
      page: currentPage.value,
      pageSize: pageSize.value,
      name: searchKeyword.value,
      dept: selectedDepartment.value,
      status: selectedStatus.value
    })
    
    employees.value = result.data || []
    totalEmployees.value = result.total || employees.value.length
  } catch (error) {
    console.error('加载员工列表失败:', error)
    ElMessage.error('加载员工列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 组件挂载时的初始化操作
 */
onMounted(() => {
  // 初始化数据加载
  loadEmployees()
})

/**
 * 组件卸载时清理定时器
 */
onUnmounted(() => {
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
})

// 表单引用
const employeeFormRef = ref()
</script>

<style scoped lang="scss">
/* ==================== 容器样式 ==================== */

.employees-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
  position: relative;
  
  .back-button-container {
    position: absolute;
    top: 20px;
    left: 20px;
    z-index: 15; // 提高层级确保在标题栏之上
    
    .back-button {
      background-color: rgba(255, 255, 255, 0.95);
      border: 1px solid rgba(255, 255, 255, 0.8);
      color: #495057;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
      backdrop-filter: blur(10px);
      transition: all 0.3s ease;
      
      &:hover {
        background-color: rgba(255, 255, 255, 1);
        border-color: rgba(255, 255, 255, 1);
        color: #212529;
        transform: translateY(-2px);
        box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);
      }
      
      &:active {
        transform: translateY(-1px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
      }
    }
  }
}

/* ==================== 页面标题区域 ==================== */

.page-header {
  margin-bottom: 20px;
  padding-left: 60px; // 为返回按钮留出空间
  
  .header-content {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    padding: 30px;
    border-radius: 12px;
    color: white;
    box-shadow: 0 8px 32px rgba(102, 126, 234, 0.3);
  }
  
  .title-section {
    .page-title {
      font-size: 28px;
      font-weight: 600;
      margin: 0 0 8px 0;
      display: flex;
      align-items: center;
      gap: 12px;
    }
    
    .page-description {
      font-size: 16px;
      opacity: 0.9;
      margin: 0;
      line-height: 1.5;
    }
  }
  
  .action-section {
    display: flex;
    gap: 12px;
    
    .el-button {
      border: 2px solid rgba(255, 255, 255, 0.3);
      background: rgba(255, 255, 255, 0.1);
      color: white;
      backdrop-filter: blur(10px);
      transition: all 0.3s ease;
      
      &:hover {
        background: rgba(255, 255, 255, 0.25);
        border-color: rgba(255, 255, 255, 0.6);
        transform: translateY(-2px);
        box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
      }
      
      &.el-button--primary {
        background: rgba(255, 255, 255, 0.25);
        border-color: rgba(255, 255, 255, 0.5);
        font-weight: 600;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        
        &:hover {
          background: rgba(255, 255, 255, 0.35);
          border-color: rgba(255, 255, 255, 0.7);
          transform: translateY(-2px);
          box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
        }
      }
    }
    
    // 隐藏可能在标题栏内的返回按钮
    .el-button[aria-label*="返回"],
    .el-button:has(.el-icon svg[data-icon="ArrowLeft"]),
    .el-button:has(.el-icon[class*="arrow-left"]) {
      display: none !important;
    }
  }
}

/* ==================== 搜索区域样式 ==================== */

.search-section {
  margin-bottom: 20px;
  
  .search-form {
    .el-row {
      align-items: flex-end;
    }
    
    .el-button {
      margin-left: 8px;
      
      &:first-child {
        margin-left: 0;
      }
    }
  }
}

/* ==================== 表格区域样式 ==================== */

.table-section {
  .table-toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 0;
    border-bottom: 1px solid #ebeef5;
    margin-bottom: 16px;
    
    .selected-info {
      color: #606266;
      font-size: 14px;
      
      .selected-count {
        color: #409eff;
        font-weight: 600;
      }
    }
    
    .batch-actions {
      display: flex;
      gap: 8px;
    }
  }
  
  .pagination-wrapper {
    display: flex;
    justify-content: center;
    margin-top: 20px;
  }
}

/* ==================== 对话框样式 ==================== */

.employee-detail {
  .detail-header {
    display: flex;
    align-items: center;
    gap: 20px;
    margin-bottom: 24px;
    padding: 20px;
    background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
    border-radius: 8px;
    
    .basic-info {
      h3 {
        margin: 0 0 8px 0;
        font-size: 20px;
        color: #303133;
      }
      
      p {
        margin: 0 0 8px 0;
        color: #606266;
        font-size: 14px;
      }
    }
  }
}

/* ==================== 响应式设计 ==================== */

@media (max-width: 768px) {
  .employees-container {
    padding: 10px;
  }
  
  .page-header .header-content {
    flex-direction: column;
    gap: 20px;
    text-align: center;
  }
  
  .action-section {
    justify-content: center;
  }
  
  .table-toolbar {
    flex-direction: column;
    gap: 12px;
    align-items: stretch !important;
    
    .batch-actions {
      justify-content: center;
    }
  }
  
  .employee-detail .detail-header {
    flex-direction: column;
    text-align: center;
  }
}

@media (max-width: 480px) {
  .page-header .header-content {
    padding: 20px;
  }
  
  .page-title {
    font-size: 24px !important;
  }
  
  .page-description {
    font-size: 14px !important;
  }
  
  .action-section {
    flex-direction: column;
    width: 100%;
    
    .el-button {
      width: 100%;
    }
  }
}

/* ==================== 表格优化 ==================== */

:deep(.el-table) {
  .el-table__header {
    th {
      background-color: #fafafa;
      color: #303133;
      font-weight: 600;
    }
  }
  
  .el-table__row {
    &:hover {
      background-color: #f5f7fa;
    }
  }
}

/* ==================== 卡片样式优化 ==================== */

:deep(.el-card) {
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  border: none;
  
  .el-card__body {
    padding: 24px;
  }
}

/* ==================== 按钮样式优化 ==================== */

.el-button {
  border-radius: 6px;
  font-weight: 500;
  
  &.el-button--small {
    padding: 5px 12px;
  }
}

/* ==================== 表单样式优化 ==================== */

:deep(.el-form-item) {
  margin-bottom: 20px;
  
  .el-form-item__label {
    font-weight: 500;
    color: #303133;
  }
}

:deep(.el-input), :deep(.el-select) {
  .el-input__inner, .el-select__wrapper {
    border-radius: 6px;
  }
}
</style> 