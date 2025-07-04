/**
 * 薪资管理相关API接口
 */
import request from '@/utils/request';

/**
 * 获取薪资记录列表（管理员）
 */
export const getSalaryRecords = async (params = {}) => {
  try {
    // 构建查询参数
    const queryParams = new URLSearchParams();
    if (params.empId) queryParams.append('empId', params.empId);
    if (params.month) queryParams.append('month', params.month);
    if (params.year) queryParams.append('year', params.year);
    if (params.status) queryParams.append('status', params.status);
    if (params.page) queryParams.append('page', params.page);
    if (params.pageSize) queryParams.append('pageSize', params.pageSize);
    
    const response = await request.get(`/salaries?${queryParams.toString()}`);
    return response;
  } catch (error) {
    console.error('获取薪资记录失败:', error);
    throw error;
  }
};

/**
 * 获取员工薪资历史记录
 * @param {string} empId 员工ID
 * @param {object} params 查询参数
 * @returns {Promise}
 */
export function getEmployeeSalaryHistory(empId, params = {}) {
  console.log('🔄 调用薪资API:', { empId, params });
  return request({
    url: `/salaries/my-salaries/${empId}`,
    method: 'get',
    params
  }).then(response => {
    console.log('✅ 薪资API响应:', response);
    return response;
  }).catch(error => {
    console.error('❌ 薪资API错误:', error);
    throw error;
  });
}

/**
 * 获取员工指定月份薪资记录
 * @param {string} empId 员工ID
 * @param {string} month 月份 (YYYY-MM格式)
 * @returns {Promise}
 */
export function getEmployeeSalaryByMonth(empId, month) {
  return request({
    url: `/salaries/employee/${empId}/month/${month}`,
    method: 'get'
  })
}

/**
 * 根据ID获取薪资详情
 * @param {number} id 薪资记录ID
 * @returns {Promise}
 */
export function getSalaryDetail(id) {
  return request({
    url: `/salaries/${id}`,
    method: 'get'
  })
}

/**
 * 获取所有薪资记录（管理员用）
 * @returns {Promise}
 */
export function getAllSalaries() {
  return request({
    url: '/salaries',
    method: 'get'
  })
}

/**
 * 获取薪资列表
 */
export function getSalaryList(params) {
  return request({
    url: '/salaries',
    method: 'get',
    params
  })
}

/**
 * 获取员工薪资详情
 */
export function getEmployeeSalary(empId) {
  return request({
    url: `/salaries/employee/${empId}`,
    method: 'get'
  })
}

/**
 * 获取员工年度总收入
 */
export function getEmployeeYearlyIncome(empId, year) {
  return request({
    url: `/salaries/employee/${empId}/yearly/${year}`,
    method: 'get'
  })
}

/**
 * 获取员工当前月薪资
 */
export function getEmployeeCurrentSalary(empId) {
  return request({
    url: `/salaries/employee/${empId}/current`,
    method: 'get'
  })
}

/**
 * 添加薪资记录
 */
export function addSalary(data) {
  return request({
    url: '/salaries',
    method: 'post',
    data
  })
}

/**
 * 更新薪资记录
 */
export function updateSalary(id, data) {
  return request({
    url: `/salaries/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除薪资记录
 */
export function deleteSalary(id) {
  return request({
    url: `/salaries/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除薪资记录
 */
export function batchDeleteSalary(ids) {
  return request({
    url: '/salaries/batch',
    method: 'delete',
    data: ids
  })
}

/**
 * 导入薪资记录
 */
export function importSalaryRecords(file) {
  const formData = new FormData();
  formData.append('file', file);
  
  return request({
    url: '/salaries/import',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 获取薪资统计数据
 */
export function getSalaryStatistics() {
  return request({
    url: '/salaries/statistics',
    method: 'get'
  })
}

/**
 * 获取部门薪资统计
 */
export function getDepartmentSalaryStats() {
  return request({
    url: '/salaries/department-stats',
    method: 'get'
  })
}

/**
 * 批量导入薪资
 */
export function batchImportSalary(data) {
  return request({
    url: '/salaries/batch-import',
    method: 'post',
    data
  })
}

/**
 * 创建薪资记录（管理员）
 */
export const createSalaryRecord = async (salaryData) => {
  try {
    const response = await request.post('/salaries', salaryData);
    return response;
  } catch (error) {
    console.error('创建薪资记录失败:', error);
    throw error;
  }
};

/**
 * 更新薪资记录（管理员）
 */
export const updateSalaryRecord = async (id, salaryData) => {
  try {
    const response = await request.put(`/salaries/${id}`, salaryData);
    return response;
  } catch (error) {
    console.error('更新薪资记录失败:', error);
    throw error;
  }
};

/**
 * 删除薪资记录（管理员）
 */
export const deleteSalaryRecord = async (id) => {
  try {
    const response = await request.delete(`/salaries/${id}`);
    return response;
  } catch (error) {
    console.error('删除薪资记录失败:', error);
    throw error;
  }
};

/**
 * 确认薪资记录（管理员）
 */
export const confirmSalaryRecord = async (id) => {
  try {
    await request.put(`/salaries/${id}`, {
      status: '已确认'
    });
    return { message: '薪资记录确认成功' };
  } catch (error) {
    console.error('确认薪资记录失败:', error);
    throw error;
  }
};

/**
 * 发放薪资（管理员）
 */
export const paySalary = async (id) => {
  try {
    await request.put(`/salaries/${id}`, {
      status: '已发放'
    });
    return { message: '薪资发放成功' };
  } catch (error) {
    console.error('发放薪资失败:', error);
    throw error;
  }
};

/**
 * 批量发放薪资（管理员）
 */
export const batchPaySalary = async (ids) => {
  try {
    const promises = ids.map(id => paySalary(id));
    await Promise.all(promises);
    return { message: '批量发放薪资成功' };
  } catch (error) {
    console.error('批量发放薪资失败:', error);
    throw error;
  }
};

/**
 * 获取岗位薪资标准（管理员）
 */
export const getPositionSalaryStandards = async (params = {}) => {
  try {
    // 后端可能没有专门的薪资标准接口，返回默认标准
    const defaultStandards = [
      {
        id: 1,
        position: '前端开发工程师',
        department: '技术部',
        minSalary: 8000,
        maxSalary: 18000,
        baseSalary: 12000,
        level: '中级',
        description: '负责前端页面开发和维护'
      },
      {
        id: 2,
        position: '人事专员',
        department: '人事部',
        minSalary: 6000,
        maxSalary: 12000,
        baseSalary: 8000,
        level: '初级',
        description: '负责人事管理相关工作'
      },
      {
        id: 3,
        position: '会计',
        department: '财务部',
        minSalary: 7000,
        maxSalary: 15000,
        baseSalary: 10000,
        level: '中级',
        description: '负责财务核算和报表制作'
      }
    ];
    
    return defaultStandards;
  } catch (error) {
    console.error('获取岗位薪资标准失败:', error);
    throw error;
  }
};

/**
 * 创建岗位薪资标准（管理员）
 */
export const createPositionSalaryStandard = async (standardData) => {
  try {
    throw new Error('岗位薪资标准创建功能暂未实现，请联系系统管理员');
  } catch (error) {
    console.error('创建岗位薪资标准失败:', error);
    throw error;
  }
};

/**
 * 更新岗位薪资标准（管理员）
 */
export const updatePositionSalaryStandard = async (id, standardData) => {
  try {
    throw new Error('岗位薪资标准更新功能暂未实现，请联系系统管理员');
  } catch (error) {
    console.error('更新岗位薪资标准失败:', error);
    throw error;
  }
};

/**
 * 删除岗位薪资标准（管理员）
 */
export const deletePositionSalaryStandard = async (id) => {
  try {
    throw new Error('岗位薪资标准删除功能暂未实现，请联系系统管理员');
  } catch (error) {
    console.error('删除岗位薪资标准失败:', error);
    throw error;
  }
};

/**
 * 导出薪资报表（管理员）
 */
export const exportSalaryReport = async (params = {}) => {
  try {
    // 获取薪资记录数据
    const salaryData = await getSalaryRecords(params);
    
    // 构建Excel数据
    const excelData = generateExcelData(salaryData);
    
    // 创建并下载Excel文件
    await downloadExcel(excelData, '薪资记录报表.xlsx');
    
    return {
      success: true,
      message: '导出成功',
      count: Array.isArray(salaryData) ? salaryData.length : (salaryData.data ? salaryData.data.length : 0)
    };
  } catch (error) {
    console.error('导出薪资报表失败:', error);
    throw error;
  }
};

/**
 * 生成Excel数据格式
 */
const generateExcelData = (salaryData) => {
  let records = [];
  
  // 处理响应数据格式
  if (Array.isArray(salaryData)) {
    records = salaryData;
  } else if (salaryData.data && Array.isArray(salaryData.data)) {
    records = salaryData.data;
  } else {
    records = [];
  }
  
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
  const rows = records.map(record => [
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
  
  return {
    headers,
    rows,
    sheetName: '薪资记录'
  };
};

/**
 * 下载Excel文件（真正的Excel格式）
 */
const downloadExcel = async (data, filename) => {
  try {
    // 动态导入xlsx库
    const XLSX = await import('xlsx');
    
    // 创建工作簿
    const workbook = XLSX.utils.book_new();
    
    // 准备工作表数据（表头 + 数据行）
    const worksheetData = [data.headers, ...data.rows];
    
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
    XLSX.utils.book_append_sheet(workbook, worksheet, data.sheetName || '薪资记录');
    
    // 生成Excel文件
    const excelBuffer = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' });
    
    // 创建Blob对象
    const blob = new Blob([excelBuffer], { 
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' 
    });
    
    // 生成文件名（带时间戳）
    const now = new Date();
    const timestamp = `${now.getFullYear()}${(now.getMonth() + 1).toString().padStart(2, '0')}${now.getDate().toString().padStart(2, '0')}_${now.getHours().toString().padStart(2, '0')}${now.getMinutes().toString().padStart(2, '0')}`;
    const finalFilename = filename.replace('.xlsx', `_${timestamp}.xlsx`);
    
    // 创建下载链接
    const link = document.createElement('a');
    const url = URL.createObjectURL(blob);
    link.setAttribute('href', url);
    link.setAttribute('download', finalFilename);
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

/**
 * 格式化数字（用于导出）
 */
const formatNumber = (value) => {
  if (value == null || value === '') return 0;
  const num = Number(value);
  return isNaN(num) ? 0 : num;
};

/**
 * 获取员工姓名（用于导出）
 */
const getEmployeeName = (empId) => {
  // 这里需要从员工列表中获取姓名，暂时返回占位符
  return `员工${empId}`;
};

/**
 * 获取状态文本（用于导出）
 */
const getStatusText = (status) => {
  // 数据库中存储的就是中文状态，直接返回
  return status || '草稿';
};

/**
 * 获取月度薪资趋势数据
 */
export const getMonthlySalaryTrend = () => {
  return request({
    url: '/salaries/monthly-trend',
    method: 'get'
  });
};

/**
 * 获取薪资等级分布数据
 */
export const getSalaryLevelDistribution = () => {
  return request({
    url: '/salaries/level-distribution',
    method: 'get'
  });
}; 