package com.tfgkk.schoolcompetition.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CompetitionDTO {
    private Long id;
    private String title;
    private String description;
    private String startTime;
    private String endTime;
    private Integer status; // 1: 进行中, 0: 已结束
}
