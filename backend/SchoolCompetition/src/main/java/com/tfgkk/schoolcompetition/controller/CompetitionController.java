package com.tfgkk.schoolcompetition.controller;

import com.tfgkk.schoolcompetition.common.Result;
import com.tfgkk.schoolcompetition.entity.Competition;
import com.tfgkk.schoolcompetition.service.CompetitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/competitions")
@CrossOrigin
public class CompetitionController {

    @Autowired
    private CompetitionService competitionService;

    /**
     * 获取竞赛列表 (支持分页)
     * @PageableDefault 设置默认页码和大小
     */
    @GetMapping
    public Result<Page<Competition>> getAllCompetitions(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10) Pageable pageable) {
        return Result.success(competitionService.getAllCompetitions(keyword, pageable));
    }

    /**
     * 发布竞赛
     */
    @PostMapping
    public Result<Competition> addCompetition(@RequestBody Competition competition) {
        return Result.success(competitionService.createCompetition(competition));
    }

    /**
     * 删除竞赛
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteCompetition(@PathVariable Long id) {
        competitionService.deleteCompetition(id);
        return Result.success("竞赛及相关报名信息已删除");
    }
}
