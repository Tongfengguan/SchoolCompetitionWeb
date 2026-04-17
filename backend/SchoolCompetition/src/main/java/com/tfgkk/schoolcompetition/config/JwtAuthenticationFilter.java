package com.tfgkk.schoolcompetition.config;

import com.tfgkk.schoolcompetition.common.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 从 Header 提取 "Authorization" (前端传的)
        String authHeader = request.getHeader("Authorization");

        // 检查是不是 Bearer Token 格式
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7); // 去掉 "Bearer " 前缀

            // 校验 Token 的合法性
            if (JwtUtil.validateToken(token)) {
                // 如果合法，从里面取出用户名和角色
                Claims claims = JwtUtil.parseToken(token);
                String username = claims.getSubject();
                String role = claims.get("role", String.class);

                // 在 Spring Security 上下文中设置 "已登录" 状态
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        username, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase())));
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        // 放行请求给后续链路（或者是由于 Token 不合法而继续被后续的过滤器阻拦）
        filterChain.doFilter(request, response);
    }
}
