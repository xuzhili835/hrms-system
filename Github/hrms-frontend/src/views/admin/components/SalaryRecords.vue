<template>
  <div class="salary-records">
    <!-- 操作栏 -->
    <div class="action-bar">
      <el-button type="primary" @click="showCreateDialog = true">
        <el-icon><Plus /></el-icon>
        新建薪资记录
      </el-button>
      
      <el-button @click="showImportDialog = true">
        <el-icon><Upload /></el-icon>
        导入报表
      </el-button>
      
      <el-button @click="exportSalary">
        <el-icon><Download /></el-icon>
        导出报表
      </el-button>
      
      <el-button 
        type="danger" 
        :disabled="selectedIds.length === 0"
        @click="handleBatchDelete"
      >
        <el-icon><Delete /></el-icon>
        批量删除 ({{ selectedIds.length }})
      </el-button>
    </div>

    <!-- 筛选区域 -->
    <el-card class="filter-card">
      <el-form :model="filters" inline>
        <el-form-item label="员工工号">
          <el-input 
            v-model="filters.empId" 
            placeholder="请输入员工工号"
            clearable
            style="width: 160px"
            @input="handleSearch"
            @clear="handleSearch"
          />
        </el-form-item>
        
        <el-form-item label="月份">
          <el-date-picker
            v-model="filters.month"
            type="month"
            placeholder="选择月份"
            format="YYYY-MM"
            value-format="YYYY-MM"
            style="width: 140px"
            @change="handleSearch"
          />
        </el-form-item>
        
        <el-form-item label="状态">
          <el-select 
            v-model="filters.status" 
            placeholder="选择状态"
            clearable
            style="width: 120px"
            @change="handleSearch"
          >
            <el-option label="草稿" value="草稿" />
            <el-option label="已确认" value="已确认" />
            <el-option label="已发放" value="已发放" />
          </el-select>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="resetFilters">
            <el-icon><RefreshRight /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 薪资记录表格 -->
    <el-card class="table-card">
      <div class="table-header" v-if="hasActiveFilters">
        <span class="search-info">
          <el-icon><Search /></el-icon>
          已筛选，共 {{ pagination.total }} 条结果
        </span>
      </div>
      
      <el-table 
        v-loading="loading" 
        :data="salaryList" 
        stripe
        style="width: 100%"
        :table-layout="'auto'"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column prop="empId" label="员工工号" min-width="100" align="center" />
        <el-table-column prop="employeeName" label="员工姓名" min-width="110" align="center">
          <template #default="{ row }">
            <span class="employee-name">{{ getEmployeeName(row.empId) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="month" label="月份" min-width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small" type="primary">{{ row.month }}</el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="baseSalary" label="基本工资" min-width="110" align="right">
          <template #default="{ row }">
            <span class="salary-item">¥{{ formatNumber(row.baseSalary) }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="allowance" label="津贴" min-width="90" align="right">
          <template #default="{ row }">
            <span class="salary-item allowance">¥{{ formatNumber(row.allowance) }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="bonus" label="奖金" min-width="90" align="right">
          <template #default="{ row }">
            <span class="salary-item bonus">¥{{ formatNumber(row.bonus) }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="deduction" label="扣除" min-width="90" align="right">
          <template #default="{ row }">
            <span class="salary-item deduction">¥{{ formatNumber(row.deduction) }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="totalSalary" label="总薪资" min-width="120" align="right">
          <template #default="{ row }">
            <span class="total-salary">¥{{ formatNumber(row.totalSalary) }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="status" label="状态" min-width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" min-width="200" align="center" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button type="primary" link size="small" @click="viewDetail(row)">
                <el-icon><View /></el-icon>
                查看
              </el-button>
              <el-button 
                v-if="row.status !== '已发放'" 
                type="warning" 
                link 
                size="small"
                @click="editRecord(row)"
              >
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button 
                v-if="row.status === '草稿'" 
                type="success" 
                link 
                size="small"
                @click="confirmRecord(row)"
              >
                <el-icon><Check /></el-icon>
                确认
              </el-button>
              <el-button 
                v-if="row.status === '已确认'" 
                type="warning" 
                link 
                size="small"
                @click="payRecord(row)"
              >
                <el-icon><Money /></el-icon>
                发放
              </el-button>
              <el-button 
                type="danger" 
                link 
                size="small"
                @click="handleSingleDelete(row)"
              >
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[5, 10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          background
        />
      </div>
    </el-card>

    <!-- 创建/编辑对话框 -->
    <el-dialog 
      v-model="showCreateDialog" 
      :title="isEdit ? '编辑薪资记录' : '新建薪资记录'" 
      width="800px"
      :close-on-click-modal="false"
    >
      <SalaryForm 
        :form-data="currentRecord"
        :is-edit="isEdit"
        @submit="handleSubmit"
        @cancel="handleCancel"
      />
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog 
      v-model="showDetailDialog" 
      title="薪资详情" 
      width="700px"
    >
      <SalaryDetail 
        v-if="currentDetail"
        :salary-data="currentDetail"
      />
    </el-dialog>

    <!-- 导入对话框 -->
    <el-dialog 
      v-model="showImportDialog" 
      title="导入薪资记录" 
      width="600px"
      :close-on-click-modal="false"
    >
      <div class="import-content">
        <el-alert
          title="导入说明"
          type="info"
          show-icon
          :closable="false"
        >
          <template #default>
            <ul style="margin: 0; padding-left: 20px;">
              <li>请上传Excel文件（.xlsx或.xls格式）</li>
              <li>第一行为标题行，数据从第二行开始</li>
              <li>列顺序：工号、月份(YYYY-MM)、基本工资、津贴、奖金、扣除、状态</li>
              <li>状态值：草稿、已确认、已发放（可选，默认为草稿）</li>
              <li>文件大小不超过10MB</li>
            </ul>
          </template>
        </el-alert>

        <el-upload
          ref="uploadRef"
          :auto-upload="false"
          :show-file-list="true"
          :limit="1"
          accept=".xlsx,.xls"
          :on-change="handleFileChange"
          :on-remove="handleFileRemove"
          drag
          class="upload-demo"
          style="margin: 20px 0;"
        >
          <el-icon class="el-icon--upload"><upload-filled /></el-icon>
          <div class="el-upload__text">
            将文件拖到此处，或<em>点击上传</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">
              只能上传 xlsx/xls 文件，且不超过 10MB
            </div>
          </template>
        </el-upload>

        <div class="import-progress" v-if="importProgress.show">
          <el-progress 
            :percentage="importProgress.percentage" 
            :status="importProgress.status"
          />
          <div class="progress-text">{{ importProgress.text }}</div>
        </div>

        <div class="import-result" v-if="importResult">
          <el-alert
            :title="importResult.success ? '导入完成' : '导入失败'"
            :type="importResult.success ? 'success' : 'error'"
            show-icon
            :closable="false"
          >
            <template #default>
              <div v-if="importResult.success">
                <p>总行数：{{ importResult.totalRows }}</p>
                <p>成功：{{ importResult.successCount }}</p>
                <p>失败：{{ importResult.errorCount }}</p>
                <div v-if="importResult.errorMessages && importResult.errorMessages.length > 0">
                  <p style="color: #e6a23c; margin-top: 10px;">错误详情：</p>
                  <ul style="margin: 5px 0; padding-left: 20px; max-height: 200px; overflow-y: auto;">
                    <li v-for="(error, index) in importResult.errorMessages" :key="index" style="color: #e6a23c;">
                      {{ error }}
                    </li>
                  </ul>
                </div>
              </div>
              <div v-else>
                {{ importResult.message }}
              </div>
            </template>
          </el-alert>
        </div>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showImportDialog = false">取消</el-button>
          <el-button 
            type="primary" 
            @click="handleImport"
            :loading="importing"
            :disabled="!selectedFile"
          >
            开始导入
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { 
  Plus, Download, Upload, Search, RefreshRight, View, Edit, Check, Money, Delete, UploadFilled
} from '@element-plus/icons-vue';
import { 
  getSalaryRecords, 
  createSalaryRecord,
  updateSalaryRecord,
  confirmSalaryRecord,
  paySalary,
  exportSalaryReport,
  getSalaryDetail,
  deleteSalary,
  batchDeleteSalary,
  importSalaryRecords
} from '@/api/salary';
import { getEmployeeList } from '@/api/employee';
import SalaryForm from './SalaryForm.vue';
import SalaryDetail from './SalaryDetail.vue';

// 响应式数据
const loading = ref(false);
const showCreateDialog = ref(false);
const showDetailDialog = ref(false);
const showImportDialog = ref(false);
const isEdit = ref(false);

const salaryList = ref([]);
const currentRecord = ref({});
const currentDetail = ref(null);
const departments = ref(['技术部', '人事部', '财务部', '市场部', '行政部']);
const employeeList = ref([]);

// 批量选择和删除相关
const selectedIds = ref([]);

// 导入相关
const selectedFile = ref(null);
const importing = ref(false);
const uploadRef = ref(null);
const importProgress = reactive({
  show: false,
  percentage: 0,
  status: '',
  text: ''
});
const importResult = ref(null);

// 分页
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
});

// 筛选条件
const filters = reactive({
  empId: '',
  month: '',
  status: ''
});

// 获取员工列表
const fetchEmployees = async () => {
  try {
    const response = await getEmployeeList();
    if (Array.isArray(response)) {
      employeeList.value = response;
    } else if (response.data && Array.isArray(response.data)) {
      employeeList.value = response.data;
    }
    console.log('📋 员工列表:', employeeList.value.length, '人');
  } catch (error) {
    console.error('❌ 获取员工列表失败:', error);
  }
};

// 根据工号获取员工姓名
const getEmployeeName = (empId) => {
  if (!empId) return '未知员工';
  
  const employee = employeeList.value.find(emp => emp.empId === empId);
  if (employee && employee.name) {
    return employee.name;
  }
  
  // 如果员工列表还没加载完成，显示加载提示
  if (employeeList.value.length === 0) {
    return '加载中...';
  }
  
  // 如果找不到员工，返回工号并记录警告
  console.warn(`未找到员工信息: ${empId}，员工列表:`, employeeList.value.map(e => e.empId));
  return `未知员工(${empId})`;
};

// 检测是否有活跃的筛选条件
const hasActiveFilters = computed(() => {
  return filters.empId || filters.month || filters.status;
});

// 格式化数字
const formatNumber = (value) => {
  if (value == null || value === '') return '0';
  const num = Number(value);
  return isNaN(num) ? '0' : num.toLocaleString();
};

// 全部薪资记录缓存
const allSalaryRecords = ref([]);

// 获取薪资记录列表
const fetchSalaryRecords = async () => {
  try {
    loading.value = true;
    
    console.log('🔄 获取薪资记录');
    const response = await getSalaryRecords();
    console.log('✅ 薪资记录API响应:', response);
    
    // 处理响应数据
    let allRecords = [];
    if (Array.isArray(response)) {
      allRecords = response;
    } else if (response.data && Array.isArray(response.data)) {
      allRecords = response.data;
    } else {
      console.warn('⚠️ 意外的响应格式:', response);
      allRecords = [];
    }
    
    // 缓存全部数据
    allSalaryRecords.value = allRecords;
    
    // 应用筛选和分页
    applyFiltersAndPagination();
    
    console.log('📊 薪资记录处理完成:', {
      全部记录: allSalaryRecords.value.length,
      当前页显示: salaryList.value.length,
      筛选条件: filters
    });
    
  } catch (error) {
    console.error('❌ 获取薪资记录失败:', error);
    ElMessage.error('获取薪资记录失败：' + (error.message || '未知错误'));
    allSalaryRecords.value = [];
    salaryList.value = [];
    pagination.total = 0;
  } finally {
    loading.value = false;
  }
};

// 应用筛选和分页
const applyFiltersAndPagination = () => {
  let filteredRecords = [...allSalaryRecords.value];
  
  // 按员工工号筛选
  if (filters.empId) {
    filteredRecords = filteredRecords.filter(record => 
      record.empId && record.empId.includes(filters.empId)
    );
  }
  
  // 按月份筛选
  if (filters.month) {
    filteredRecords = filteredRecords.filter(record => 
      record.month === filters.month
    );
  }
  
  // 按状态筛选
  if (filters.status) {
    filteredRecords = filteredRecords.filter(record => 
      record.status === filters.status
    );
  }
  
  // 更新总数
  pagination.total = filteredRecords.length;
  
  // 分页处理
  const startIndex = (pagination.page - 1) * pagination.pageSize;
  const endIndex = startIndex + pagination.pageSize;
  salaryList.value = filteredRecords.slice(startIndex, endIndex);
  
  console.log('🔍 筛选结果:', {
    筛选后总数: pagination.total,
    当前页: pagination.page,
    每页大小: pagination.pageSize,
    当前页数据: salaryList.value.length
  });
};

// 查看详情
const viewDetail = async (row) => {
  try {
    loading.value = true;
    console.log('🔍 查看薪资详情:', row);
    
    // 构造详情数据，确保包含员工信息
    const detailData = {
      ...row,
      // 确保有员工姓名信息
      employeeName: getEmployeeName(row.empId),
      // 添加格式化后的数据
      baseSalaryFormatted: formatNumber(row.baseSalary || row.base_salary),
      allowanceFormatted: formatNumber(row.allowance),
      bonusFormatted: formatNumber(row.bonus),
      deductionFormatted: formatNumber(row.deduction),
      totalSalaryFormatted: formatNumber(row.totalSalary || row.total_salary)
    };
    
    currentDetail.value = detailData;
    showDetailDialog.value = true;
  } catch (error) {
    console.error('❌ 获取详情失败:', error);
    ElMessage.error('获取详情失败：' + (error.message || '未知错误'));
  } finally {
    loading.value = false;
  }
};

// 编辑记录
const editRecord = (row) => {
  if (!row.id) {
    ElMessage.error('薪资记录ID缺失，无法编辑');
    return;
  }
  
  console.log('✏️ 编辑薪资记录:', row);
  
  // 构造编辑数据，确保字段映射正确
  currentRecord.value = {
    id: row.id,
    empId: row.empId,
    empName: getEmployeeName(row.empId), // 添加员工姓名方便显示
    baseSalary: row.baseSalary || row.base_salary || 0,
    allowance: row.allowance || 0,
    bonus: row.bonus || 0,
    deduction: row.deduction || 0,
    totalSalary: row.totalSalary || row.total_salary || 0,
    month: row.month,
    status: row.status || '草稿'
  };
  
  isEdit.value = true;
  showCreateDialog.value = true;
};

// 确认记录
const confirmRecord = async (row) => {
  if (!row.id) {
    ElMessage.error('薪资记录ID缺失，无法确认');
    return;
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要确认 ${getEmployeeName(row.empId)} 的薪资记录吗？`,
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    );
    
    loading.value = true;
    console.log('✅ 确认薪资记录:', row.id);
    
    await confirmSalaryRecord(row.id);
    ElMessage.success('薪资记录已确认');
    
    // 刷新列表
    await fetchSalaryRecords();
  } catch (error) {
    if (error !== 'cancel') {
      console.error('❌ 确认失败:', error);
      ElMessage.error('确认失败：' + (error.message || error.response?.data?.message || '未知错误'));
    }
  } finally {
    loading.value = false;
  }
};

// 发放薪资
const payRecord = async (row) => {
  if (!row.id) {
    ElMessage.error('薪资记录ID缺失，无法发放');
    return;
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要发放 ${getEmployeeName(row.empId)} 的薪资吗？发放后将无法修改。`,
      '确认发放',
      {
        confirmButtonText: '确定发放',
        cancelButtonText: '取消',
        type: 'warning'
      }
    );
    
    loading.value = true;
    console.log('💰 发放薪资:', row.id);
    
    await paySalary(row.id);
    ElMessage.success('薪资已发放');
    
    // 刷新列表
    await fetchSalaryRecords();
  } catch (error) {
    if (error !== 'cancel') {
      console.error('❌ 发放失败:', error);
      ElMessage.error('发放失败：' + (error.message || error.response?.data?.message || '未知错误'));
    }
  } finally {
    loading.value = false;
  }
};

// 导出薪资报表
const exportSalary = async () => {
  try {
    loading.value = true;
    
    // 构建导出参数，包含当前筛选条件
    const exportParams = {
      empId: filters.empId || '',
      month: filters.month || '',
      status: filters.status || ''
    };
    
    console.log('🔄 开始导出薪资报表，参数:', exportParams);
    
    // 获取要导出的数据（使用当前筛选结果）
    let exportData = [];
    if (hasActiveFilters.value) {
      // 如果有筛选条件，导出筛选结果
      const filteredRecords = allSalaryRecords.value.filter(record => {
        let match = true;
        if (exportParams.empId) {
          match = match && record.empId && record.empId.includes(exportParams.empId);
        }
        if (exportParams.month) {
          match = match && record.month === exportParams.month;
        }
        if (exportParams.status) {
          match = match && record.status === exportParams.status;
        }
        return match;
      });
      exportData = filteredRecords;
    } else {
      // 没有筛选条件，导出全部数据
      exportData = allSalaryRecords.value;
    }
    
    if (exportData.length === 0) {
      ElMessage.warning('没有可导出的数据');
      return;
    }
    
    // 生成导出文件
    await generateSalaryExcel(exportData);
    
    ElMessage.success(`成功导出 ${exportData.length} 条薪资记录`);
    
  } catch (error) {
    console.error('❌ 导出失败:', error);
    ElMessage.error('导出失败：' + (error.message || '未知错误'));
  } finally {
    loading.value = false;
  }
};

// 生成薪资Excel文件（真正的Excel格式）
const generateSalaryExcel = async (data) => {
  try {
    // 动态导入xlsx库
    const XLSX = await import('xlsx');
    
    // 表头
    const headers = [
      '员工工号',
      '员工姓名',
      '月份',
      '基本工资(元)',
      '津贴(元)',
      '奖金(元)',
      '扣除(元)',
      '总薪资(元)',
      '状态',
      '导出时间'
    ];
    
    // 数据行
    const rows = data.map(record => [
      record.empId || '',
      getEmployeeName(record.empId) || '未知员工',
      record.month || '',
      Number(record.baseSalary) || 0,
      Number(record.allowance) || 0,
      Number(record.bonus) || 0,
      Number(record.deduction) || 0,
      Number(record.totalSalary) || 0,
      getStatusText(record.status) || '草稿',
      new Date().toLocaleString()
    ]);
    
    // 创建工作簿
    const workbook = XLSX.utils.book_new();
    
    // 准备工作表数据（表头 + 数据行）
    const worksheetData = [headers, ...rows];
    
    // 创建工作表
    const worksheet = XLSX.utils.aoa_to_sheet(worksheetData);
    
    // 设置列宽
    const colWidths = [
      { wch: 12 }, // 员工工号
      { wch: 15 }, // 员工姓名
      { wch: 12 }, // 月份
      { wch: 15 }, // 基本工资
      { wch: 12 }, // 津贴
      { wch: 12 }, // 奖金
      { wch: 12 }, // 扣除
      { wch: 15 }, // 总薪资
      { wch: 10 }, // 状态
      { wch: 20 }  // 导出时间
    ];
    worksheet['!cols'] = colWidths;
    
    // 设置表头样式
    const headerRange = XLSX.utils.decode_range(worksheet['!ref']);
    for (let col = headerRange.s.c; col <= headerRange.e.c; col++) {
      const cellAddress = XLSX.utils.encode_cell({ r: 0, c: col });
      if (!worksheet[cellAddress]) continue;
      
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
      };
    }
    
    // 设置数据行样式
    for (let row = 1; row <= headerRange.e.r; row++) {
      for (let col = headerRange.s.c; col <= headerRange.e.c; col++) {
        const cellAddress = XLSX.utils.encode_cell({ r: row, c: col });
        if (!worksheet[cellAddress]) continue;
        
        worksheet[cellAddress].s = {
          alignment: { horizontal: col >= 3 && col <= 7 ? "right" : "center", vertical: "center" },
          border: {
            top: { style: "thin", color: { rgb: "D0D0D0" } },
            bottom: { style: "thin", color: { rgb: "D0D0D0" } },
            left: { style: "thin", color: { rgb: "D0D0D0" } },
            right: { style: "thin", color: { rgb: "D0D0D0" } }
          }
        };
        
        // 薪资列设置数字格式
        if (col >= 3 && col <= 7) {
          worksheet[cellAddress].t = 'n';
          worksheet[cellAddress].z = '#,##0';
        }
      }
    }
    
    // 添加工作表到工作簿
    XLSX.utils.book_append_sheet(workbook, worksheet, '薪资记录');
    
    // 生成Excel文件
    const excelBuffer = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' });
    
    // 创建Blob对象
    const blob = new Blob([excelBuffer], { 
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' 
    });
    
    // 生成文件名（带时间戳）
    const now = new Date();
    const timestamp = `${now.getFullYear()}${(now.getMonth() + 1).toString().padStart(2, '0')}${now.getDate().toString().padStart(2, '0')}_${now.getHours().toString().padStart(2, '0')}${now.getMinutes().toString().padStart(2, '0')}`;
    const filename = `薪资记录报表_${timestamp}.xlsx`;
    
    // 创建下载链接
    const link = document.createElement('a');
    const url = URL.createObjectURL(blob);
    link.setAttribute('href', url);
    link.setAttribute('download', filename);
    link.style.visibility = 'hidden';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
    
  } catch (error) {
    console.error('生成Excel文件失败:', error);
    throw new Error('生成Excel文件失败，请检查网络连接或联系系统管理员');
  }
};

// 处理表单提交
const handleSubmit = async (formData) => {
  try {
    console.log('📝 提交薪资记录数据:', formData);
    
    if (isEdit.value) {
      const response = await updateSalaryRecord(currentRecord.value.id, formData);
      console.log('✅ 更新薪资记录成功:', response);
      ElMessage.success('薪资记录更新成功');
    } else {
      const response = await createSalaryRecord(formData);
      console.log('✅ 创建薪资记录成功:', response);
      ElMessage.success('薪资记录创建成功');
    }
    
    showCreateDialog.value = false;
    
    // 重新获取数据并重置分页
    pagination.page = 1;
    await fetchSalaryRecords();
    
  } catch (error) {
    console.error('❌ 薪资记录操作失败:', error);
    ElMessage.error('操作失败：' + (error.message || error.response?.data?.message || '未知错误'));
  }
};

// 处理取消
const handleCancel = () => {
  showCreateDialog.value = false;
  currentRecord.value = {};
  isEdit.value = false;
};

// 搜索
const handleSearch = () => {
  pagination.page = 1;
  applyFiltersAndPagination();
};

// 重置筛选
const resetFilters = () => {
  Object.assign(filters, {
    empId: '',
    month: '',
    status: ''
  });
  pagination.page = 1;
  applyFiltersAndPagination();
};

// 分页处理
const handleSizeChange = (val) => {
  pagination.pageSize = val;
  pagination.page = 1;
  applyFiltersAndPagination();
};

const handleCurrentChange = (val) => {
  pagination.page = val;
  applyFiltersAndPagination();
};

// 状态处理方法
const getStatusTagType = (status) => {
  const statusMap = {
    '草稿': 'info',
    'draft': 'info',
    '已确认': 'warning',
    'confirmed': 'warning', 
    '已发放': 'success',
    'paid': 'success',
    '已取消': 'danger',
    'cancelled': 'danger'
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status) => {
  const statusMap = {
    'draft': '草稿',
    'confirmed': '已确认',
    'paid': '已发放',
    'cancelled': '已取消'
  }
  return statusMap[status] || status || '草稿'
}

// 批量选择处理
const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id);
  console.log('选中的薪资记录ID:', selectedIds.value);
};

// 单个删除
const handleSingleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除员工 ${getEmployeeName(row.empId)} ${row.month} 的薪资记录吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    );

    await deleteSalary(row.id);
    ElMessage.success('删除成功');
    await fetchSalaryRecords();
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除薪资记录失败:', error);
      ElMessage.error('删除失败：' + (error.message || '未知错误'));
    }
  }
};

// 批量删除
const handleBatchDelete = async () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请选择要删除的记录');
    return;
  }

  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedIds.value.length} 条薪资记录吗？`,
      '批量删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    );

    const response = await batchDeleteSalary(selectedIds.value);
    if (response.success) {
      ElMessage.success(`成功删除 ${response.deletedCount} 条记录`);
      selectedIds.value = [];
      await fetchSalaryRecords();
    } else {
      ElMessage.error('删除失败：' + response.message);
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除薪资记录失败:', error);
      ElMessage.error('删除失败：' + (error.message || '未知错误'));
    }
  }
};

// 文件上传处理
const handleFileChange = (file, fileList) => {
  selectedFile.value = file.raw;
  importResult.value = null;
  importProgress.show = false;
  console.log('选择的文件:', file);
};

const handleFileRemove = () => {
  selectedFile.value = null;
  importResult.value = null;
  importProgress.show = false;
};

// 导入处理
const handleImport = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请选择要导入的文件');
    return;
  }

  importing.value = true;
  importProgress.show = true;
  importProgress.percentage = 0;
  importProgress.status = '';
  importProgress.text = '正在上传文件...';
  importResult.value = null;

  try {
    // 模拟进度
    const progressInterval = setInterval(() => {
      if (importProgress.percentage < 90) {
        importProgress.percentage += 10;
      }
    }, 200);

    const response = await importSalaryRecords(selectedFile.value);
    
    clearInterval(progressInterval);
    importProgress.percentage = 100;
    importProgress.status = response.success ? 'success' : 'exception';
    importProgress.text = response.success ? '导入完成' : '导入失败';
    
    importResult.value = response;
    
    if (response.success) {
      ElMessage.success('导入完成');
      // 如果有成功导入的记录，刷新列表
      if (response.successCount > 0) {
        await fetchSalaryRecords();
      }
    } else {
      ElMessage.error('导入失败：' + response.message);
    }
  } catch (error) {
    clearInterval(progressInterval);
    importProgress.percentage = 100;
    importProgress.status = 'exception';
    importProgress.text = '导入失败';
    
    console.error('导入薪资记录失败:', error);
    const errorMessage = error.response?.data?.message || error.message || '导入失败';
    importResult.value = {
      success: false,
      message: errorMessage
    };
    ElMessage.error('导入失败：' + errorMessage);
  } finally {
    importing.value = false;
  }
};

// 页面挂载时确保先加载员工列表
onMounted(async () => {
  console.log('🚀 薪资记录页面初始化');
  
  try {
    // 先加载员工列表，再加载薪资记录
    await fetchEmployees();
    console.log('✅ 员工列表加载完成，共', employeeList.value.length, '人');
    
    // 等待一下确保员工列表已处理完成
    await new Promise(resolve => setTimeout(resolve, 100));
    
    await fetchSalaryRecords();
    console.log('✅ 薪资记录加载完成');
  } catch (error) {
    console.error('❌ 页面初始化失败:', error);
    ElMessage.error('页面初始化失败，请刷新重试');
  }
});
</script>

<style lang="scss" scoped>
.salary-records {
  .action-bar {
    margin-bottom: 16px;
    
    .el-button {
      margin-right: 12px;
    }
  }
  
  .filter-card {
    margin-bottom: 16px;
    border-radius: 8px;
    border: none;
    
    .filter-row {
      display: flex;
      gap: 16px;
      align-items: center;
      flex-wrap: wrap;
      
      .el-select {
        width: 120px;
      }
    }
  }
  
  .table-card {
    border-radius: 8px;
    border: none;
    
    .table-header {
      margin-bottom: 16px;
      
      .search-info {
        display: flex;
        align-items: center;
        gap: 8px;
        color: #606266;
        font-size: 14px;
        
        .el-icon {
          color: #409eff;
        }
      }
    }
    
    // 员工姓名样式
    .employee-name {
      font-weight: 500;
      color: #303133;
    }
    
    // 薪资项目样式
    .salary-item {
      font-weight: 500;
      
      &.allowance {
        color: #67c23a;
      }
      
      &.bonus {
        color: #e6a23c;
      }
      
      &.deduction {
        color: #f56c6c;
      }
    }
    
    // 总薪资样式
    .total-salary {
      font-weight: 600;
      color: #27ae60;
      font-size: 15px;
    }
    
    // 操作按钮样式
    .action-buttons {
      display: flex;
      gap: 8px;
      align-items: center;
      justify-content: center;
      flex-wrap: wrap;
      
      .el-button {
        padding: 4px 8px;
        
        .el-icon {
          margin-right: 4px;
        }
      }
    }
    
    .pagination-wrapper {
      margin-top: 20px;
      display: flex;
      justify-content: center;
    }
  }
  
  // 表格全局样式
  :deep(.el-table) {
    .el-table__body-wrapper {
      .el-table__row {
        &:hover {
          .salary-item {
            transform: scale(1.02);
            transition: transform 0.2s ease;
          }
        }
      }
    }
    
    .el-table__header {
      th {
        background-color: #f8f9fa;
        font-weight: 600;
        color: #303133;
      }
    }
  }
  
  // 导入对话框样式
  .import-content {
    .upload-demo {
      :deep(.el-upload) {
        border: 2px dashed #d9d9d9;
        border-radius: 6px;
        cursor: pointer;
        position: relative;
        overflow: hidden;
        transition: all 0.3s ease;
        
        &:hover {
          border-color: #409eff;
        }
      }
      
      :deep(.el-upload-dragger) {
        background-color: #fafafa;
        border: none;
        border-radius: 6px;
        width: 100%;
        height: 180px;
        text-align: center;
        cursor: pointer;
        position: relative;
        overflow: hidden;
        transition: all 0.3s ease;
        
        &:hover {
          background-color: #f5f7fa;
        }
      }
    }
    
    .import-progress {
      margin: 20px 0;
      
      .progress-text {
        text-align: center;
        margin-top: 8px;
        color: #606266;
        font-size: 14px;
      }
    }
    
    .import-result {
      margin-top: 20px;
      
      p {
        margin: 5px 0;
        font-size: 14px;
      }
    }
  }
}

@media (max-width: 768px) {
  .salary-records {
    .filter-row {
      flex-direction: column;
      align-items: stretch;
      
      .el-select {
        width: 100%;
        margin-bottom: 12px;
      }
    }
  }
}
</style> 