package com.tfgkk.schoolcompetition.controller;

import com.tfgkk.schoolcompetition.common.Result;
import com.tfgkk.schoolcompetition.dto.UserDTO;
import com.tfgkk.schoolcompetition.dto.UserLoginDTO;
import com.tfgkk.schoolcompetition.entity.User;
import com.tfgkk.schoolcompetition.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "身份鉴权", description = "处理用户登录、注册及初始化逻辑")
@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {

    @Autowired
    private UserService userService;

    /**
     * 登录接口
     */
    @Operation(summary = "用户登录", description = "验证用户名密码并返回 JWT Token")
    @PostMapping("/login")
    public Result<UserDTO> login(@Valid @RequestBody UserLoginDTO loginRequest) {
        UserDTO userDTO = userService.login(loginRequest);
        return Result.success(userDTO);
    }

    /**
     * 注册接口
     */
    @Operation(summary = "用户注册", description = "创建新学生账号")
    @PostMapping("/register")
    public Result<UserDTO> register(@Valid @RequestBody User user) {
        return Result.success(userService.register(user));
    }
}
