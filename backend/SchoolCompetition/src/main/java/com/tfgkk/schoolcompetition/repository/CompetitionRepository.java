package com.tfgkk.schoolcompetition.repository;

import com.tfgkk.schoolcompetition.entity.Competition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 数据库访问接口
 * 继承 JpaRepository <实体类, 主键类型>
 * 这样我们就直接拥有了 findAll(), save(), findById() 等方法
 */
@Repository
public interface CompetitionRepository extends JpaRepository<Competition, Long> {
    // 这里暂时什么都不用写，Spring Boot 会自动帮我们实现基础功能！
}