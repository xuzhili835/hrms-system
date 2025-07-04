package com.a0615.config;

import com.a0615.config.JwtRequestFilter;
import com.a0615.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        // 使用BCrypt密码编码器（推荐的安全方式）
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        // 设置密码编码器 - 这一行很重要！
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173", "http://localhost:5174", "http://127.0.0.1:5173", "http://127.0.0.1:5174", "http://192.168.22.54:5173", "http://192.168.22.54:5174"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .formLogin(form -> form.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .anonymous(anonymous -> anonymous.disable())
                .authorizeHttpRequests(authz -> authz
                        // 1. 公开访问路径 (无需认证或权限)
                        .requestMatchers("/api/auth/login", "/api/auth/register", "/error").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        // 移除公告的公开访问，所有公告接口都需要认证
                        // .requestMatchers(HttpMethod.GET, "/api/announcements", "/api/announcements/**").permitAll()

                        // 2. 详细API权限规则 (更具体的路径在前)

                        // 员工信息模块权限 (/api/employees)
                        // 允许管理员和员工查询自己的信息
                        .requestMatchers(HttpMethod.GET, "/api/employees/byEmpId/{empId}").hasAnyRole("ADMIN", "EMPLOYEE")
                        // 【新增规则】允许员工查看自己的薪资信息。这条规则必须在 /api/employees/**.hasRole("ADMIN") 之前
                        .requestMatchers(HttpMethod.GET, "/api/employees/my-salary/{empId}").hasRole("EMPLOYEE")
                        // 【新增规则】允许员工修改自己的密码
                        .requestMatchers(HttpMethod.PUT, "/api/employees/change-password/{empId}").hasRole("EMPLOYEE")
                        // 【新增规则】允许员工更新自己的基本信息
                        .requestMatchers(HttpMethod.PUT, "/api/employees/my-profile/{empId}").hasRole("EMPLOYEE")

                        // 删除状态为离职的员工，需要ADMIN权限
                        .requestMatchers(HttpMethod.DELETE, "/api/employees/{empId}/resigned").hasRole("ADMIN")
                        // 管理员查询所有员工，添加/更新/删除员工
                        .requestMatchers("/api/employees/**").hasRole("ADMIN")

                        // 公告模块权限 (/api/announcements) - 所有操作都需要认证
                        .requestMatchers(HttpMethod.POST, "/api/announcements").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/announcements/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/announcements/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/announcements/**").hasAnyRole("ADMIN", "EMPLOYEE")

                        // 请假申请模块权限 (/api/leave-applications)
                        .requestMatchers(HttpMethod.POST, "/api/leave-applications").hasRole("EMPLOYEE")
                        .requestMatchers(HttpMethod.GET, "/api/leave-applications/my-applications/**").hasRole("EMPLOYEE")
                        .requestMatchers(HttpMethod.DELETE, "/api/leave-applications/my-applications/**").hasRole("EMPLOYEE")
                        .requestMatchers("/api/leave-applications/**").hasRole("ADMIN")

                        // 管理员模块权限 (/api/admins)
                        .requestMatchers("/api/admins/**").hasRole("ADMIN")

                        // 薪资模块权限 (/api/salaries) - 请注意，这个 /api/salaries 是一个独立的路径，不影响 /api/employees
                        .requestMatchers(HttpMethod.GET, "/api/salaries/my-salaries/**").hasRole("EMPLOYEE")
                        .requestMatchers(HttpMethod.GET, "/api/salaries/my-salary-detail/**").hasRole("EMPLOYEE")
                        .requestMatchers("/api/salaries/**").hasRole("ADMIN")

                        // 3. 任何其他未匹配的请求都需要认证
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
        http.authenticationProvider(daoAuthenticationProvider());

        http.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()));

        return http.build();
    }
}
