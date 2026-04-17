package com.tfgkk.schoolcompetition.controller;

import com.tfgkk.schoolcompetition.common.Result;
import com.tfgkk.schoolcompetition.entity.Registration;
import com.tfgkk.schoolcompetition.service.CompetitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/registrations")
@CrossOrigin
public class RegistrationController {

    @Autowired
    private CompetitionService competitionService;

    /**
     * 获取报名名单
     */
    @GetMapping
    public Result<List<Registration>> getRegistrations(@RequestParam Long competitionId) {
        return Result.success(competitionService.getRegistrations(competitionId));
    }

    /**
     * 报名竞赛
     */
    @PostMapping
    public Result<Registration> register(@RequestBody Registration registration) {
        return Result.success(competitionService.register(registration));
    }

    /**
     * 审核报名
     */
    @PutMapping("/{id}/audit")
    public Result<String> auditStudent(@PathVariable Long id, @RequestParam Integer status) {
        competitionService.auditStudent(id, status);
        return Result.success("审核成功");
    }

    /**
     * 取消报名 (管理员移除或学生取消)
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteRegistration(@PathVariable Long id) {
        competitionService.deleteRegistration(id);
        return Result.success("已取消报名资格");
    }
}
