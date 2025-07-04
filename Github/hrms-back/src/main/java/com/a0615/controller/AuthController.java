package com.a0615.controller;

import com.a0615.entity.Admin;
import com.a0615.entity.Employee;
import com.a0615.dto.LoginRequestDTO;    // 使用 LoginRequestDTO
import com.a0615.dto.AuthResponseDTO;     // 使用 AuthResponseDTO
import com.a0615.service.UserDetailsServiceImpl;
import com.a0615.service.EmployeeService;
import com.a0615.service.AdminService;
import com.a0615.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping; // 添加 RequestMapping
import org.springframework.web.bind.annotation.RestController;

// 不需要 Objects 了
// import java.util.Objects;

@RestController // 标记这是一个 RESTful 控制器
@RequestMapping("/api/auth") // 添加基础请求路径，与 SecurityConfig 保持一致
public class AuthController {

    @Autowired // 自动注入 AuthenticationManager
    private AuthenticationManager authenticationManager;

    @Autowired // 自动注入 UserDetailsServiceImpl
    private UserDetailsServiceImpl userDetailsService;

    @Autowired // 自动注入 JwtUtil
    private JwtUtil jwtUtil;
    
    @Autowired // 注入密码编码器用于调试
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private EmployeeService employeeService;
    
    @Autowired
    private AdminService adminService;

    /**
     * 用户登录接口
     * POST /api/auth/login
     * @param loginRequest 包含用户名和密码的 DTO
     * @return 包含 JWT Token、用户角色和完整用户信息的响应
     */
    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody LoginRequestDTO loginRequest) {
        try {
            // 添加详细的调试信息
            System.out.println("=== 登录调试信息 ===");
            System.out.println("接收到的用户名: '" + loginRequest.getUsername() + "'");
            System.out.println("接收到的密码: '" + loginRequest.getPassword() + "'");
            System.out.println("密码长度: " + loginRequest.getPassword().length());
            System.out.println("密码字节数组: " + java.util.Arrays.toString(loginRequest.getPassword().getBytes()));
            
            // 手动验证密码哈希
            try {
                UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getUsername());
                System.out.println("从数据库获取的用户: " + userDetails.getUsername());
                System.out.println("从数据库获取的密码哈希: " + userDetails.getPassword());
                
                // 手动验证密码
                boolean manualCheck = passwordEncoder.matches(loginRequest.getPassword(), userDetails.getPassword());
                System.out.println("手动密码验证结果: " + manualCheck);
                
                // 测试不同的密码变体
                System.out.println("=== 密码变体测试 ===");
                String[] passwordVariants = {
                    loginRequest.getPassword(),
                    loginRequest.getPassword().trim(),
                    "123456"
                };
                
                for (String variant : passwordVariants) {
                    boolean matches = passwordEncoder.matches(variant, userDetails.getPassword());
                    System.out.println("密码变体 '" + variant + "' (长度:" + variant.length() + ") -> " + matches);
                }
                
            } catch (Exception e) {
                System.out.println("手动验证过程出错: " + e.getMessage());
            }
            
            // 尝试认证用户
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );
            
            // 如果认证成功，获取用户详情
            final UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // 从 authorities 中获取角色信息
            String role = userDetails.getAuthorities().stream()
                    .findFirst()
                    .map(grantedAuthority -> grantedAuthority.getAuthority().replace("ROLE_", ""))
                    .orElse("");

            // 生成 JWT Token
            final String jwt = jwtUtil.generateToken(userDetails, role);

            // 根据角色获取完整的用户信息
            Object userInfo = null;
            if ("EMPLOYEE".equals(role)) {
                Employee employee = employeeService.getEmployeeByEmpId(userDetails.getUsername());
                if (employee != null) {
                    userInfo = employee;
                    System.out.println("员工登录成功: " + employee.getName() + " (ID: " + employee.getEmpId() + ")");
                }
            } else if ("ADMIN".equals(role)) {
                Admin admin = adminService.getAdminByUsername(userDetails.getUsername());
                if (admin != null) {
                    userInfo = admin;
                    System.out.println("管理员登录成功: " + admin.getName() + " (用户名: " + admin.getUsername() + ")");
                }
            }

            // 创建响应DTO，包含完整用户信息
            AuthResponseDTO response = new AuthResponseDTO();
            response.setToken(jwt);
            response.setRole(role.toLowerCase()); // 转为小写以保持前端一致性
            response.setUsername(userDetails.getUsername());
            response.setUser(userInfo); // 设置完整的用户对象

            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            System.out.println("认证失败: " + e.getMessage());
            // 认证失败（用户名或密码错误），GlobalExceptionHandler 会捕获并处理
            throw new BadCredentialsException("用户名或密码不正确！", e);
        }
    }
}