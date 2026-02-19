package com.tfgkk.schoolcompetition.repository;

import com.tfgkk.schoolcompetition.entity.Competition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 数据库访问接口
 * 继承 JpaRepository <实体类, 主键类型>
 * 这样我们就直接拥有了 findAll(), save(), findById() 等方法
 */
@Repository
public interface CompetitionRepository extends JpaRepository<Competition, Long> {
    // 忽略大小写的模糊搜索：匹配标题或描述
    List<Competition> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String title, String desc);

    // 默认列表按 ID 降序排列
    List<Competition> findAllByOrderByIdDesc();
}