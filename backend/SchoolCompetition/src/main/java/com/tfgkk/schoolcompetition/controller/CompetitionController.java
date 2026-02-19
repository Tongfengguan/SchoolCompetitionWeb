package com.tfgkk.schoolcompetition.controller;

import com.tfgkk.schoolcompetition.entity.Competition;
import com.tfgkk.schoolcompetition.repository.CompetitionRepository;
import com.tfgkk.schoolcompetition.repository.RegistrationRepository; // 导入报名库
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional; // 导入事务
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/competitions")
@CrossOrigin
public class CompetitionController {

    @Autowired
    private CompetitionRepository competitionRepository;

    @Autowired
    private RegistrationRepository registrationRepository;

    /**
     * 获取竞赛列表 (升级版：支持关键词模糊搜索)
     */
    @GetMapping
    public List<Competition> getAllCompetitions(@RequestParam(required = false) String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            // 如果有关键词，调用自定义的搜索方法
            return competitionRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword);
        }
        // 否则返回全部，并按 ID 倒序排列（让新发布的排在前面）
        return competitionRepository.findAllByOrderByIdDesc();
    }

    @PostMapping
    public Competition addCompetition(@RequestBody Competition competition) {
        return competitionRepository.save(competition);
    }

    /**
     * 删除竞赛 (升级版：级联删除报名记录)
     */
    @DeleteMapping("/{id}")
    @Transactional // 🚨 关键：保证删除“竞赛”和“报名信息”是一个原子操作
    public void deleteCompetition(@PathVariable Long id) {
        // 1. 先根据竞赛 ID 清理掉所有的报名学生记录
        registrationRepository.deleteByCompetitionId(id);

        // 2. 再删除竞赛本体
        competitionRepository.deleteById(id);
    }
}