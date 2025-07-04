package com.a0615.dto;

import lombok.Data;
// 可以根据需要添加验证注解，例如 @NotBlank
// import jakarta.validation.constraints.NotBlank;

@Data // Lombok 注解，自动生成 getter/setter/equals/hashCode/toString
public class LoginRequestDTO {
    // @NotBlank(message = "用户名不能为空") // 示例验证：确保字段不为空且不为纯空白字符
    private String username;
    // @NotBlank(message = "密码不能为空")
    private String password;
}