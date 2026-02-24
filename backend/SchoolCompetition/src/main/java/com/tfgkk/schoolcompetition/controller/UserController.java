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
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin
public class UserController {

    @Autowired
    private UserRepository userRepository;

    /**
     * 批量导入学生账号
     */
    @PostMapping("/import")
    public ResponseEntity<?> importStudents(@RequestParam("file") MultipartFile file) throws IOException {
        EasyExcel.read(file.getInputStream(), User.class, new PageReadListener<User>(dataList -> {
            for (User item : dataList) {
                if (item.getPhone() == null || item.getPhone().isBlank()) continue;
                if (userRepository.findByUsername(item.getPhone()).isPresent()) continue;

                User student = new User();
                student.setName(item.getName());
                student.setPhone(item.getPhone());
                student.setUsername(item.getPhone());
                student.setPassword(item.getPhone());
                student.setRole("student");

                userRepository.save(student);
            }
        })).sheet().doRead();

        return ResponseEntity.ok().body(Map.of("message", "导入完成，已自动跳过重复账号"));
    }

    /**
     * ✨ 优化：删除某个用户
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        // 增加健壮性校验：如果数据库没有这个用户，直接返回 404
        if (!userRepository.existsById(id)) {
            return ResponseEntity.status(404).body(Map.of("message", "用户不存在或已被删除"));
        }
        userRepository.deleteById(id);
        return ResponseEntity.ok().body(Map.of("message", "用户已删除"));
    }

    /**
     * 获取所有用户信息
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        users.forEach(u -> u.setPassword(null));
        return ResponseEntity.ok(users);
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
     * 学生端修改个人账号和密码
     */
    @PutMapping("/student/update")
    public ResponseEntity<?> updateStudentAccount(@RequestBody Map<String, String> request) {
        Long userId = Long.valueOf(request.get("id"));
        String newUsername = request.get("username");
        String newPassword = request.get("password");

        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();

            if (newUsername != null && !newUsername.isBlank()) {
                Optional<User> existing = userRepository.findByUsername(newUsername);
                if (existing.isPresent() && !existing.get().getId().equals(userId)) {
                    return ResponseEntity.badRequest().body(Map.of("message", "该账号名已存在"));
                }
                user.setUsername(newUsername);
            }

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