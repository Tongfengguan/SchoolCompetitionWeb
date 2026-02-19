package com.tfgkk.schoolcompetition.entity;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ExcelIgnore
    private Long id;

    @Column(unique = true, nullable = false)
    @ExcelIgnore
    private String username;

    @Column(nullable = false)
    @ExcelIgnore
    private String password;

    @ExcelIgnore
    private String role;// admin 或 student

    @ExcelProperty("姓名")
    private String name;

    // ✨ 新增 phone 属性
    @ExcelProperty("电话")
    private String phone;

    public User() {}
    // --- Getter / Setter ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    // ✨ 新增 phone 的 Getter / Setter
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}