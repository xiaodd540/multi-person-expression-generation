package com.example.springboot_part.utils;

import com.example.springboot_part.pojo.Users;
import com.example.springboot_part.service.UsersService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UsersService usersService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UsersService usersService) {
        this.jwtUtil = jwtUtil;
        this.usersService = usersService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        // 获取请求路径
        String requestURI = request.getRequestURI();

        // 允许不需要 Token 的接口（白名单）
        List<String> excludePaths = Arrays.asList(
                "/users/login",
                "/users/register",
                "/users/avatar",
                "/users/check-username",
                "/users/password",
                "/users/avatar"
        );

        // 如果请求在白名单中，直接放行
        if (excludePaths.contains(requestURI)) {
            chain.doFilter(request, response);
            return;
        }

        // 获取 Authorization 头
        String authHeader = request.getHeader("Authorization");

        // 检查 Token 格式是否正确
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("你们好呀");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: Missing or invalid token");
            return;
        }

        // 提取 Token
        String token = authHeader.substring(7);
        String username = jwtUtil.extractUsername(token);

        // 确保用户未被认证
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            Users user = usersService.findByName(username);
            // 如果用户未找到或 Token 无效，返回 401
            if (user == null || !jwtUtil.isTokenValid(token, user)) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
                return;
            }

            // 认证成功，构建 Authentication 对象
            UserDetails userDetails = User.withUsername(user.getUserName())
                    .password("") // 通常这里可以为空
                    .authorities("USER")
                    .build();

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // 设置认证信息到 SecurityContextHolder
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // 继续执行过滤链
        chain.doFilter(request, response);
        System.out.println("最终 SecurityContext: " + SecurityContextHolder.getContext().getAuthentication());
    }
}



