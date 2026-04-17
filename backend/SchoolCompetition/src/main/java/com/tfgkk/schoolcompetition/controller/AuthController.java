package com.tfgkk.schoolcompetition.controller;

import com.tfgkk.schoolcompetition.common.Result;
import com.tfgkk.schoolcompetition.dto.UserDTO;
import com.tfgkk.schoolcompetition.dto.UserLoginDTO;
import com.tfgkk.schoolcompetition.entity.User;
import com.tfgkk.schoolcompetition.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {

    @Autowired
    private UserService userService;

    /**
     * 登录接口
     */
    @PostMapping("/login")
    public Result<UserDTO> login(@RequestBody UserLoginDTO loginRequest) {
        UserDTO userDTO = userService.login(loginRequest);
        return Result.success(userDTO);
    }

    /**
     * 注册接口
     */
    @PostMapping("/register")
    public Result<UserDTO> register(@RequestBody User user) {
        return Result.success(userService.register(user));
    }
}
