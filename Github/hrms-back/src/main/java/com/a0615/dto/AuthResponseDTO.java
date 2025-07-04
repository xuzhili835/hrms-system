package com.a0615.dto;

import lombok.AllArgsConstructor; // 自动生成全参数构造函数
import lombok.Data;
import lombok.NoArgsConstructor;   // 自动生成无参构造函数

@Data
@NoArgsConstructor // 需要无参构造函数以便 JSON 反序列化
@AllArgsConstructor // 方便在 Service/Controller 中创建实例
public class AuthResponseDTO {
    private String token;
    private String role;
    private String username;
    private Object user; // 完整的用户对象（Employee或Admin）
    
    // 保留原有的三参数构造函数以保持向后兼容
    public AuthResponseDTO(String token, String role, String username) {
        this.token = token;
        this.role = role;
        this.username = username;
    }
}