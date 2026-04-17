package com.tfgkk.schoolcompetition.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 1. 关闭 CSRF（因为我们采用的是 Token，不需要防范跨站请求伪造）
            .csrf(AbstractHttpConfigurer::disable)
            // 2. 开启跨域支持，会去应用我们之前的 CorsConfig
            .cors(Customizer.withDefaults())
            // 3. 将会话管理设为无状态（Stateless），完全依赖 Token
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 4. 配置权限拦截规则
            .authorizeHttpRequests(auth -> auth
                // 下面这些接口，所有人都可以无 Token 访问：登录、注册、导出模板
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/users/template").permitAll()
                
                // 放行 Knife4j 静态资源
                .requestMatchers("/doc.html", "/webjars/**", "/v3/api-docs/**", "/swagger-resources/**").permitAll()
                
                // 放行 uploads 文件夹下的附件
                .requestMatchers("/uploads/**").permitAll()
                
                // 剩下的所有请求，只要带着有效的 Token 就能访问
                .anyRequest().authenticated()
            );

        // 5. 将我们自定义的 JWT 过滤器，放到默认的 UsernamePasswordAuthenticationFilter 之前执行
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
