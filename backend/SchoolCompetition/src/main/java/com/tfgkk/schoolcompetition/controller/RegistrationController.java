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

    /**
     * 审核接口
     * 路径示例：PUT /api/registrations/5/audit?status=1
     */
    @PutMapping("/{id}/audit")
    public ResponseEntity<?> auditStudent(@PathVariable Long id, @RequestParam Integer status) {
        return registrationRepository.findById(id).map(registration -> {
            registration.setStatus(status);
            registrationRepository.save(registration);
            return ResponseEntity.ok().body("审核成功");
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * 删除单条报名记录 (取消资格)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRegistration(@PathVariable Long id) {
        registrationRepository.deleteById(id);
        return ResponseEntity.ok().body("删除成功");
    }
}