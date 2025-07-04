package com.a0615.controller;

import com.a0615.entity.Employee;
import com.a0615.entity.Salary;
import com.a0615.service.EmployeeService;
import com.a0615.service.SalaryService;
import com.a0615.dto.EmployeeCreateDTO; // 导入 DTO
import com.a0615.dto.EmployeeUpdateDTO; // 导入 DTO

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication; // 导入 Authentication
import org.springframework.security.core.context.SecurityContextHolder; // 导入 SecurityContextHolder
import org.springframework.web.bind.annotation.*;
// import jakarta.validation.Valid; // 用于启用验证，如果您的DTO有验证注解，请取消注释

import java.util.List;

@RestController // 标记这是一个 RESTful 控制器
@RequestMapping("/api/employees") // 定义基础请求路径，添加 /api/ 前缀规范
public class EmployeeController {
    @Autowired // 自动注入 EmployeeService
    private EmployeeService employeeService;

    @Autowired // 自动注入 SalaryService
    private SalaryService salaryService;

    /**
     * 获取所有员工列表 (管理员权限)
     * GET /api/employees
     * @return 员工列表
     */
    @GetMapping // 处理 GET /api/employees 请求
    @PreAuthorize("hasRole('ADMIN')") // 只有管理员才能访问此接口
    public ResponseEntity<List<Employee>> getAllEmployees() {
        List<Employee> employees = employeeService.getAllEmployees(); // 调用 service 获取所有员工
        return ResponseEntity.ok(employees); // 返回 200 OK 和员工列表
    }

    /**
     * 根据员工工号获取单个员工信息 (管理员可以获取所有，员工只能获取自己的)
     * GET /api/employees/byEmpId/{empId}
     * @param empId 员工工号
     * @return 员工对象或 404 Not Found
     */
    @GetMapping("/byEmpId/{empId}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('EMPLOYEE') and #empId == authentication.principal.username)")
    // 权限校验已在此处完成：管理员可查任意，员工只能查与自身用户名（通常是empId）匹配的记录
    public ResponseEntity<Employee> getEmployeeByEmpId(@PathVariable String empId) {
        // 由于 @PreAuthorize 已经确保了权限，Service 层只需要简单地获取数据即可
        Employee employee = employeeService.getEmployeeByEmpId(empId);

        if (employee != null) {
            return ResponseEntity.ok(employee);
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    /**
     * 添加新员工 (管理员权限)
     * POST /api/employees
     * @param createDto 员工信息 DTO（JSON 格式）
     * @return 成功状态和添加的员工信息（如果需要，可以返回完整实体）
     */
    @PostMapping // 处理 POST /api/employees 请求
    @PreAuthorize("hasRole('ADMIN')") // 只有管理员才能添加员工
    public ResponseEntity<Employee> addEmployee(@RequestBody /*@Valid*/ EmployeeCreateDTO createDto) {
        employeeService.addEmployee(createDto); // 调用 service 添加员工，传入 DTO
        // 通常添加成功后会返回完整的实体，这里根据您的业务需求返回。
        // 假设您希望返回刚刚创建的员工的完整信息，可以再查询一次。
        Employee createdEmployee = employeeService.getEmployeeByEmpId(createDto.getEmpId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEmployee); // 返回 201 Created 和添加的员工信息
    }

    /**
     * 更新员工信息（通过 empId 作为路径变量）。 (管理员权限)
     * @param empId 从 URL 路径中获取的员工工号。
     * @param updateDto 包含要更新字段的员工 DTO（来自请求体）。
     * @return 更新结果，200 OK 表示成功，403 Forbidden 表示权限不足，404 Not Found 表示员工不存在。
     */
    @PutMapping("/{empId}") // 处理 PUT /api/employees/{empId} 请求
    @PreAuthorize("hasRole('ADMIN')") // 只有管理员才能更新员工信息
    public ResponseEntity<Void> updateEmployeeByEmpId(@PathVariable String empId, @RequestBody /*@Valid*/ EmployeeUpdateDTO updateDto) {
        // 对于更新操作，请求体中的 empId 通常不会传递或被忽略，因为路径变量已经指定了目标。
        // 因此不再需要 employee.getEmpId() 的安全校验。

        try {
            boolean updated = employeeService.updateEmployeeByEmpId(empId, updateDto); // 传入 DTO
            if (updated) {
                return new ResponseEntity<>(HttpStatus.OK); // 更新成功返回 200 OK
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 未找到要更新的员工 (Service 层会抛 IllegalArgumentException)
            }
        } catch (IllegalArgumentException e) {
            // 捕获服务层抛出的非法参数异常（例如：尝试修改不存在的员工，或Service内部的业务规则验证失败）
            return ResponseEntity.badRequest().body(null); // 返回 400 Bad Request
        }
        catch (Exception e) {
            // 捕获其他未知异常
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // 返回 500 Internal Server Error
        }
    }

    /**
     * 删除状态为"离职"的员工。 (管理员权限)
     * DELETE /api/employees/{empId}/resigned
     * 只有状态为"离职"的员工才能被删除。
     * @param empId 员工工号
     * @return 204 No Content 表示成功删除，404 Not Found 表示员工不存在，
     * 400 Bad Request 表示员工状态不是"离职"，500 Internal Server Error 表示其他错误。
     */
    @DeleteMapping("/{empId}/resigned") // 处理 DELETE /api/employees/{empId}/resigned 请求
    @PreAuthorize("hasRole('ADMIN')") // 只有管理员才能删除员工
    public ResponseEntity<Void> deleteResignedEmployee(@PathVariable String empId) {
        try {
            boolean deleted = employeeService.deleteResignedEmployee(empId);
            if (deleted) {
                return ResponseEntity.noContent().build(); // 返回 204 No Content
            } else {
                // 如果deleteResignedEmployee返回false，理论上是员工不存在，但Service层会抛异常
                // 所以这里通常不会被调用，除非Service层逻辑有变
                return ResponseEntity.notFound().build(); // 员工不存在
            }
        } catch (IllegalArgumentException e) {
            // 捕获服务层抛出的非法参数异常（状态不为"离职"或员工不存在）
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // 返回 400 Bad Request
        } catch (Exception e) {
            // 捕获其他未知异常
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // 返回 500 Internal Server Error
        }
    }

    /**
     * 搜索员工 (管理员权限)
     * GET /api/employees/search?name=xxx&dept=yyy
     * @param name 姓名关键词 (可选)
     * @param dept 部门名称 (可选)
     * @return 符合条件的员工列表
     */
    @GetMapping("/search") // 处理 GET /api/employees/search 请求
    @PreAuthorize("hasRole('ADMIN')") // 只有管理员才能搜索员工
    public ResponseEntity<List<Employee>> searchEmployees(
            @RequestParam(required = false) String name, // 从请求参数获取 name，非必需
            @RequestParam(required = false) String dept) { // 从请求参数获取 dept，非必需
        List<Employee> employees = employeeService.searchEmployees(name, dept); // 调用 service 搜索员工
        return ResponseEntity.ok(employees); // 返回 200 OK 和搜索结果
    }

    /**
     * 员工：查看自己的薪资信息 (员工本人权限)
     * GET /api/employees/my-salary/{empId}
     * 这是一个方便员工访问的接口，内部调用 SalaryService
     * @param empId 员工工号 (String)
     * @return 该员工的所有薪资记录列表
     */
    @GetMapping("/my-salary/{empId}")
    @PreAuthorize("hasRole('EMPLOYEE') and #empId == authentication.principal.username")
    // 只有员工本人（其 username 匹配 empId）才能查看自己的薪资
    public ResponseEntity<List<Salary>> getMySalaryInfo(@PathVariable String empId) {
        // 这里的 empId 已经通过 @PreAuthorize 确保是当前登录的员工
        List<Salary> salaries = salaryService.getSalariesByEmpId(empId); // 假设 SalaryService 有此方法
        return ResponseEntity.ok(salaries); // 返回 200 OK 和薪资列表
    }

    /**
     * 员工：更新自己的基本信息 (员工本人权限)
     * PUT /api/employees/my-profile/{empId}
     * 员工只能修改自己的基本信息，如姓名等，不能修改部门、职位、状态等敏感信息
     * @param empId 员工工号
     * @param updateDto 要更新的基本信息
     * @return 更新结果
     */
    @PutMapping("/my-profile/{empId}")
    @PreAuthorize("hasRole('EMPLOYEE') and #empId == authentication.principal.username")
    public ResponseEntity<Void> updateMyProfile(@PathVariable String empId, @RequestBody EmployeeUpdateDTO updateDto) {
        try {
            // 创建一个限制版本的DTO，只允许修改基本信息
            EmployeeUpdateDTO restrictedDto = new EmployeeUpdateDTO();
            restrictedDto.setName(updateDto.getName()); // 允许修改姓名
            // restrictedDto.setPwd(updateDto.getPwd()); // 密码修改应该通过专门的接口
            
            // 不允许员工修改以下字段：
            // - dept (部门)
            // - pos (职位) 
            // - role (角色)
            // - status (状态)
            // - entryTime (入职时间)
            
            boolean updated = employeeService.updateEmployeeByEmpId(empId, restrictedDto);
            if (updated) {
                return ResponseEntity.ok().build(); // 更新成功返回 200 OK
            } else {
                return ResponseEntity.notFound().build(); // 员工不存在
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build(); // 返回 400 Bad Request
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // 返回 500 Internal Server Error
        }
    }

    /**
     * 员工：修改自己的密码 (员工本人权限)
     * PUT /api/employees/change-password/{empId}
     * 员工只能修改自己的密码
     * @param empId 员工工号
     * @param updateDto 包含新密码的DTO
     * @return 更新结果
     */
    @PutMapping("/change-password/{empId}")
    @PreAuthorize("hasRole('EMPLOYEE') and #empId == authentication.principal.username")
    public ResponseEntity<Void> changePassword(@PathVariable String empId, @RequestBody EmployeeUpdateDTO updateDto) {
        try {
            // 创建一个只包含密码的DTO
            EmployeeUpdateDTO passwordDto = new EmployeeUpdateDTO();
            passwordDto.setPwd(updateDto.getPwd()); // 只允许修改密码
            
            boolean updated = employeeService.updateEmployeeByEmpId(empId, passwordDto);
            if (updated) {
                return ResponseEntity.ok().build(); // 更新成功返回 200 OK
            } else {
                return ResponseEntity.notFound().build(); // 员工不存在
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build(); // 返回 400 Bad Request
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // 返回 500 Internal Server Error
        }
    }
}