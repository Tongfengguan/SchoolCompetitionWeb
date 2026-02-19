package com.tfgkk.schoolcompetition.repository;

import com.tfgkk.schoolcompetition.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * 根据用户名查询用户
     * 返回 Optional<User> 才能支持 .isPresent() 调用
     */
    Optional<User> findByUsername(String username);
}