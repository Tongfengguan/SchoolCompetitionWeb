package com.tfgkk.schoolcompetition.repository;

import com.tfgkk.schoolcompetition.entity.Registration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {

    // 1. 查询某比赛的所有名单 (之前的)
    List<Registration> findByCompetitionId(Long competitionId);

    // 2. ✨ 新增：检查某人是否报过名
    // existsBy + 字段1 + And + 字段2 -> 返回 true 或 false
    boolean existsByCompetitionIdAndStudentId(Long competitionId, String studentId);

    void deleteByCompetitionId(Long competitionId);
}