package com.a0615.dto;

import lombok.Data;
import java.time.LocalDate;
// import jakarta.validation.constraints.NotBlank;
// import jakarta.validation.constraints.NotNull;
// import jakarta.validation.constraints.Pattern;

@Data
public class EmployeeCreateDTO {
    // @NotBlank(message = "员工工号不能为空")
    // @Pattern(regexp = "^EMP\\d{4}$", message = "员工工号格式不正确，应为EMPxxxx") // 示例：自定义工号格式
    private String empId;

    // @NotBlank(message = "姓名不能为空")
    private String name;

    // @NotBlank(message = "部门不能为空")
    private String dept;

    // @NotBlank(message = "职位不能为空")
    private String pos;

    // @NotBlank(message = "密码不能为空")
    // @Size(min = 6, message = "密码至少需要6位")
    private String pwd; // 客户端发送明文密码，Service 层负责加密

    // 角色通常由后端指定或管理员控制，如果允许创建时指定，则保留
    // @NotBlank(message = "角色不能为空")
    private String role; // 例如 "EMPLOYEE"

    // @NotNull(message = "入职时间不能为空") // 如果前端必须提供，则添加
    private LocalDate entryTime; // 可以让后端默认为当前日期

    // @NotBlank(message = "状态不能为空") // 例如 "在职"，可以后端默认
    private String status;
}