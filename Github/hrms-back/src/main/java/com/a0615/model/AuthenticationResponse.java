package com.a0615.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // Lombok 注解
@NoArgsConstructor // Lombok 注解
@AllArgsConstructor // Lombok 注解
public class AuthenticationResponse {
    private String jwt;
    private String role; // 添加角色信息，方便前端判断
}