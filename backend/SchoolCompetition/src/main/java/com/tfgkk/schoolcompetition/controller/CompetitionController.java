package com.tfgkk.schoolcompetition.controller; // 👈 修正了这里

// 👇 注意这里的 import 必须指向你的 entity 和 repository 包
import com.tfgkk.schoolcompetition.entity.Competition;
import com.tfgkk.schoolcompetition.repository.CompetitionRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/competitions")
@CrossOrigin
public class CompetitionController {

    @Autowired
    private CompetitionRepository competitionRepository;

    @GetMapping
    public List<Competition> getAllCompetitions() {
        return competitionRepository.findAll();
    }

    @PostMapping
    public Competition addCompetition(@RequestBody Competition competition) {
        return competitionRepository.save(competition);
    }

    @DeleteMapping("/{id}")
    public void deleteCompetition(@PathVariable Long id) {
        competitionRepository.deleteById(id);
    }
}