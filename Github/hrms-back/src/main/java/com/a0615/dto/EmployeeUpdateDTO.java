package com.a0615.dto;

import lombok.Data;
import java.time.LocalDate;
// import jakarta.validation.constraints.Size;

@Data
public class EmployeeUpdateDTO {
    private String name;
    private String dept;
    private String pos;
    // 密码更新：如果包含，则Service层需要处理加密
    // @Size(min = 6, message = "密码至少需要6位") // 如果密码可选更新，则不加 @NotBlank
    private String pwd;

    // 角色和状态通常只有管理员能修改，但在 DTO 中可以包含，Service 层进行权限判断
    private String role;
    private String status;

    private LocalDate entryTime; // 入职时间是否可更新，根据业务需求
}