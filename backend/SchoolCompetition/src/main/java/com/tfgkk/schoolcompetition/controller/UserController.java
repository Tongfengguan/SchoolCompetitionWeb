package com.tfgkk.schoolcompetition.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.listener.PageReadListener;
import com.tfgkk.schoolcompetition.entity.User;
import com.tfgkk.schoolcompetition.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users") // ✨ 修正：增加 /api 前缀，匹配你前端的 baseURL
@CrossOrigin
public class UserController {

    @Autowired
    private UserRepository userRepository;

    /**
     * ✨ 新增：批量导入学生账号
     * 逻辑：以电话作为账号和初始密码，角色固定为 student
     */
    @PostMapping("/import")
    public ResponseEntity<?> importStudents(@RequestParam("file") MultipartFile file) throws IOException {
        EasyExcel.read(file.getInputStream(), User.class, new PageReadListener<User>(dataList -> {
            for (User item : dataList) {
                // 1. 数据校验：如果电话为空则跳过
                if (item.getPhone() == null || item.getPhone().isBlank()) continue;

                // 2. 查重：如果该手机号（账号）已存在则跳过
                if (userRepository.findByUsername(item.getPhone()).isPresent()) continue;

                // 3. 填充学生信息
                User student = new User();
                student.setName(item.getName());
                student.setPhone(item.getPhone());
                student.setUsername(item.getPhone()); // 账号 = 电话
                student.setPassword(item.getPhone()); // 密码 = 电话
                student.setRole("student");           // 固定角色为学生

                userRepository.save(student);
            }
        })).sheet().doRead();

        return ResponseEntity.ok().body(Map.of("message", "导入完成，已自动跳过重复账号"));
    }

    /**
     * 修改个人信息
     */
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody User userDetails) {
        Optional<User> userOptional = userRepository.findById(userDetails.getId());
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setName(userDetails.getName());
            user.setPhone(userDetails.getPhone());
            userRepository.save(user);
            return ResponseEntity.ok().body(Map.of("message", "个人信息更新成功"));
        }
        return ResponseEntity.status(404).body("用户不存在");
    }

    /**
     * 修改个人密码
     */
    @PutMapping("/password")
    public ResponseEntity<?> updatePassword(@RequestBody Map<String, String> request) {
        // 需注意前端传参是否包含 id
        if (!request.containsKey("id")) return ResponseEntity.badRequest().body("缺少用户ID");

        Long userId = Long.valueOf(request.get("id"));
        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");

        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (!user.getPassword().equals(oldPassword)) {
                return ResponseEntity.badRequest().body("原密码输入不正确");
            }
            user.setPassword(newPassword);
            userRepository.save(user);
            return ResponseEntity.ok().body(Map.of("message", "密码修改成功"));
        }
        return ResponseEntity.status(404).body("用户不存在");
    }

    /**
     * ✨ 学生端修改个人账号和密码
     */
    @PutMapping("/student/update")
    public ResponseEntity<?> updateStudentAccount(@RequestBody Map<String, String> request) {
        Long userId = Long.valueOf(request.get("id"));
        String newUsername = request.get("username");
        String newPassword = request.get("password");

        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();

            // 1. 修改账号名 (需确保唯一性)
            if (newUsername != null && !newUsername.isBlank()) {
                // 如果新账号名已被他人占用，返回错误
                Optional<User> existing = userRepository.findByUsername(newUsername);
                if (existing.isPresent() && !existing.get().getId().equals(userId)) {
                    return ResponseEntity.badRequest().body(Map.of("message", "该账号名已存在"));
                }
                user.setUsername(newUsername);
            }

            // 2. 修改密码
            if (newPassword != null && !newPassword.isBlank()) {
                user.setPassword(newPassword);
            }

            userRepository.save(user);
            return ResponseEntity.ok().body(Map.of("message", "账号信息更新成功，请重新登录"));
        }
        return ResponseEntity.status(404).body("用户不存在");
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