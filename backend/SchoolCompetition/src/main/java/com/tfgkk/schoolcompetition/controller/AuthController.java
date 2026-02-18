package com.tfgkk.schoolcompetition.controller;

import com.tfgkk.schoolcompetition.entity.User;
import com.tfgkk.schoolcompetition.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity; // 用于返回状态码
import org.springframework.web.bind.annotation.*;

import java.util.Map; // 用于封装简单的 JSON 返回

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    /**
     * 登录接口
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginRequest) {
        // 1. 根据用户名去数据库查
        User user = userRepository.findByUsername(loginRequest.getUsername());
        // 2. 判断用户是否存在，以及密码是否正确
        if (user == null || !user.getPassword().equals(loginRequest.getPassword())) {
            return ResponseEntity.status(401).body("账号或密码错误");
        }

        // 3. 登录成功！返回用户信息 (不返回密码)
        user.setPassword(null);
        return ResponseEntity.ok(user);
    }

    /**
     * 注册接口 (方便我们可以先造几个账号)
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public User register(@RequestBody User user) {
        return userRepository.save(user);
    }
}