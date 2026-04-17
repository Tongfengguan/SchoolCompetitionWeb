package com.tfgkk.schoolcompetition.controller;

import com.alibaba.excel.EasyExcel;
import com.tfgkk.schoolcompetition.common.Result;
import com.tfgkk.schoolcompetition.dto.UserDTO;
import com.tfgkk.schoolcompetition.entity.User;
import com.tfgkk.schoolcompetition.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 批量导入学生账号
     */
    @PostMapping("/import")
    public Result<String> importStudents(@RequestParam("file") MultipartFile file) throws IOException {
        userService.importStudents(file);
        return Result.success("导入完成，已自动跳过重复账号");
    }

    /**
     * 删除某个用户
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success("用户已删除");
    }

    /**
     * 获取所有用户信息
     */
    @GetMapping
    public Result<List<UserDTO>> getAllUsers() {
        return Result.success(userService.getAllUsers());
    }

    /**
     * 修改个人信息
     */
    @PutMapping("/profile")
    public Result<String> updateProfile(@RequestBody UserDTO userDTO) {
        userService.updateProfile(userDTO);
        return Result.success("个人信息更新成功");
    }

    /**
     * 修改个人密码
     */
    @PutMapping("/password")
    public Result<String> updatePassword(@RequestBody Map<String, Object> request) {
        Long userId = Long.valueOf(request.get("id").toString());
        String oldPassword = request.get("oldPassword").toString();
        String newPassword = request.get("newPassword").toString();

        userService.updatePassword(userId, oldPassword, newPassword);
        return Result.success("密码修改成功");
    }

    /**
     * 学生端修改个人账号和密码
     */
    @PutMapping("/student/update")
    public Result<String> updateStudentAccount(@RequestBody Map<String, Object> request) {
        Long userId = Long.valueOf(request.get("id").toString());
        String newUsername = request.get("username") != null ? request.get("username").toString() : null;
        String newPassword = request.get("password") != null ? request.get("password").toString() : null;

        userService.updateStudentAccount(userId, newUsername, newPassword);
        return Result.success("账号信息更新成功，请重新登录");
    }

    /**
     * 下载学生导入模板
     */
    @GetMapping("/template")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");

        String fileName = URLEncoder.encode("学生账号导入模板", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        EasyExcel.write(response.getOutputStream(), User.class)
                .autoCloseStream(Boolean.FALSE)
                .sheet("学生信息")
                .doWrite(new ArrayList<>());
    }
}
