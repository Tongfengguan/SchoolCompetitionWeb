package com.tfgkk.schoolcompetition.repository;

import com.tfgkk.schoolcompetition.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // 登录查询：根据用户名找用户
    User findByUsername(String username);
}