package com.tfgkk.schoolcompetition.controller;

import com.tfgkk.schoolcompetition.entity.Registration;
import com.tfgkk.schoolcompetition.repository.RegistrationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity; // 👈 记得引入这个
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/registrations")
@CrossOrigin
public class RegistrationController {

    @Autowired
    private RegistrationRepository registrationRepository;

    // 获取名单接口 (保持不变)
    @GetMapping
    public List<Registration> getRegistrations(@RequestParam Long competitionId) {
        return registrationRepository.findByCompetitionId(competitionId);
    }

    // ✨ 修改后的报名接口
    @PostMapping
    public ResponseEntity<?> register(@RequestBody Registration registration) {
        // 1. 检查是否重复报名
        boolean exists = registrationRepository.existsByCompetitionIdAndStudentId(
                registration.getCompetitionId(),
                registration.getStudentId()
        );

        if (exists) {
            // 如果存在，返回 400 Bad Request 状态码和错误信息
            return ResponseEntity.badRequest().body("您已经报名过该比赛，请勿重复提交！");
        }

        // 2. 如果没报过，保存并返回 200 OK
        Registration saved = registrationRepository.save(registration);
        return ResponseEntity.ok(saved);
    }
}