package com.a0615.mapper;

import com.a0615.entity.Employee;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper // 标记这是一个 MyBatis Mapper 接口，Spring 会扫描并创建其代理实现
public interface EmployeeMapper extends BaseMapper<Employee> {
}
