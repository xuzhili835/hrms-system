package com.a0615.config;

import com.a0615.util.JwtUtil;
import com.a0615.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain; // 对于 Spring Boot 3+ (Jakarta EE 9+)
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

// 导入 SLF4J 日志相关的类
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component // 标记这是一个 Spring 组件
public class JwtRequestFilter extends OncePerRequestFilter { // 确保每个请求只过滤一次

    // 初始化日志记录器
    private static final Logger logger = LoggerFactory.getLogger(JwtRequestFilter.class);

    @Autowired // 自动注入 UserDetailsServiceImpl
    private UserDetailsServiceImpl userDetailsService;

    @Autowired // 自动注入 JwtUtil
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        // ==========================================================
        // IMPORTANT: 新增的日志行 - 记录每个进入过滤器的请求 URL 和 HTTP 方法
        // 这对于调试 403 错误非常有用，因为它可以显示哪个请求被拒绝了
        logger.debug("Incoming Request: {} {}", request.getMethod(), request.getRequestURI());
        // ==========================================================

        final String authorizationHeader = request.getHeader("Authorization"); // 获取请求头中的 Authorization 字段

        String username = null;
        String jwt = null;

        // 检查 Authorization 头是否存在且以 "Bearer " 开头
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7); // 提取 JWT Token
            try {
                username = jwtUtil.extractUsername(jwt); // 从 Token 中提取用户名
                // 添加日志：显示从 JWT 中提取的用户名
                logger.debug("JwtRequestFilter: Extracted username from JWT: {}", username);
            } catch (Exception e) {
                // 捕获并记录解析 JWT 失败的异常
                logger.error("JwtRequestFilter: Error extracting username from JWT or JWT is invalid: {}", e.getMessage());
            }
        }

        // 如果提取到了用户名，并且当前没有用户认证信息
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = null;
            try {
                // 根据用户名加载用户详细信息
                userDetails = this.userDetailsService.loadUserByUsername(username);

                // 添加日志：显示从 UserDetailsService 加载的用户详情及其权限
                logger.debug("JwtRequestFilter: User details loaded for {}. Authorities: {}", userDetails.getUsername(), userDetails.getAuthorities());
            } catch (Exception e) {
                // 捕获并记录加载用户详情失败的异常
                logger.error("JwtRequestFilter: Error loading user details for username {}: {}", username, e.getMessage());
                // 如果用户详情加载失败，则无需继续验证Token，直接返回
                chain.doFilter(request, response);
                return;
            }


            // 验证 Token 是否有效
            if (jwtUtil.validateToken(jwt, userDetails)) {
                // 如果 Token 有效，创建认证对象
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                usernamePasswordAuthenticationToken
                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                // 将认证对象设置到 Spring Security 上下文中
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

                // 添加日志：显示成功认证的用户及其权限
                logger.debug("JwtRequestFilter: Successfully authenticated user: {} with authorities: {}", userDetails.getUsername(), userDetails.getAuthorities());
            } else {
                // Token 验证失败
                logger.warn("JwtRequestFilter: JWT validation failed for user: {}", username);
            }
        } else if (username == null) {
            logger.debug("JwtRequestFilter: No username found in JWT or Authorization header is missing/invalid.");
        } else if (SecurityContextHolder.getContext().getAuthentication() != null) {
            logger.debug("JwtRequestFilter: User already authenticated. Skipping JWT processing.");
        }

        chain.doFilter(request, response); // 继续执行过滤器链中的下一个过滤器
    }
}
