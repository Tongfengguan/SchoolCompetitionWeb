package com.tfgkk.schoolcompetition.controller;

import com.tfgkk.schoolcompetition.entity.User;
import com.tfgkk.schoolcompetition.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    /**
     * 登录接口
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginRequest) {
        // 1. 根据用户名去数据库查
        Optional<User> userOptional = userRepository.findByUsername(loginRequest.getUsername());

        // 2. 判断用户是否存在
        if (userOptional.isPresent()) {
            User loggedUser = userOptional.get(); // ✨ 从 Optional 盒子里取出真正的 User 对象

            // 3. 校验密码是否正确 (目前是明文比对)
            if (loggedUser.getPassword().equals(loginRequest.getPassword())) {

                // 4. 登录成功！擦除密码，防止传到前端泄露
                loggedUser.setPassword(null);

                // 返回完整的用户信息（包含 id, username, role 等）
                return ResponseEntity.ok(loggedUser);
            }
        }

        // 5. 如果用户不存在或密码错误，统一返回 401
        return ResponseEntity.status(401).body("账号或密码错误");
    }

    /**
     * 注册接口
     */
    @PostMapping("/register")
    public User register(@RequestBody User user) {
        // 建议增加：如果注册时不传 role，默认为 student
        if (user.getRole() == null) {
            user.setRole("student");
        }
        return userRepository.save(user);
    }
}