package com.tfgkk.schoolcompetition.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "registrations")
public class Registration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "competition_id")
    private Long competitionId; // 记录是哪个比赛

    @Column(name = "student_name")
    private String studentName;

    @Column(name = "student_id")
    private String studentId;

    @Column(name = "class_name")
    private String className;

    private String phone;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(nullable = false)
    private Integer status = 0;

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    // 构造函数前，我们设一个默认时间
    @PrePersist
    public void prePersist() {
        if (createTime == null) {
            createTime = LocalDateTime.now();
        }
    }

    // ================= Getter / Setter =================
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCompetitionId() { return competitionId; }
    public void setCompetitionId(Long competitionId) { this.competitionId = competitionId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}