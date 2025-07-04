package com.a0615.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys; // 导入 Keys 类
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey; // 导入 SecretKey
import jakarta.annotation.PostConstruct; // 导入 PostConstruct

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component // 标记这是一个 Spring 组件
public class JwtUtil {

    // 从 application.properties 中读取 JWT 秘钥
    @Value("${jwt.secret}") // 从配置文件中读取 jwt.secret
    private String secret;

    // 从 application.properties 中读取 JWT 过期时间（毫秒）
    @Value("${jwt.expiration}") // 从配置文件中读取 jwt.expiration
    private Long expiration;

    // 用于生成和解析 JWT 的秘钥对象
    private SecretKey key;

    // 初始化方法，在所有字段注入完成后执行
    @PostConstruct
    public void init() {
        // 使用 Keys.hmacShaKeyFor 方法从字符串秘钥生成 SecretKey 对象
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    // 从 JWT Token 中获取用户名
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // 从 JWT Token 中获取过期日期
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // 从 JWT Token 中获取特定声明
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // 从 JWT Token 中获取所有声明
    private Claims extractAllClaims(String token) {
        // 使用 Jwts.parserBuilder() 构建解析器，设置签名秘钥
        return Jwts.parser()
                .verifyWith(key) // key 是 SecretKey 类型
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // 检查 Token 是否过期
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // 为用户生成 Token
    public String generateToken(UserDetails userDetails, String userRole) {
        Map<String, Object> claims = new HashMap<>();
        // 将用户角色添加到 JWT 的 claims 中
        claims.put("role", userRole);
        return createToken(claims, userDetails.getUsername());
    }

    // 创建 Token
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims) // 设置声明
                .setSubject(subject) // 设置主题（通常是用户名）
                .setIssuedAt(new Date(System.currentTimeMillis())) // 设置签发时间
                .setExpiration(new Date(System.currentTimeMillis() + expiration)) // 设置过期时间
                .signWith(key, SignatureAlgorithm.HS256) // 使用秘钥和 HMAC SHA256 算法签名
                .compact(); // 压缩生成 Token
    }

    // 验证 Token
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}