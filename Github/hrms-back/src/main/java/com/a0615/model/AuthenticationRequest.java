package com.a0615.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // Lombok 注解
@NoArgsConstructor // Lombok 注解，生成无参构造函数
@AllArgsConstructor // Lombok 注解，生成全参构造函数
public class AuthenticationRequest {
    private String username;
    private String password;
}