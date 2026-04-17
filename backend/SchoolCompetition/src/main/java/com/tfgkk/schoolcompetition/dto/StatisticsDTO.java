package com.tfgkk.schoolcompetition.dto;

import java.util.Map;

public class StatisticsDTO {
    private long totalUsers;
    private long totalCompetitions;
    private long totalRegistrations;
    private Map<String, Long> competitionDistribution; // 竞赛名 -> 报名人数
    private Map<String, Long> userRoleDistribution;    // 角色 -> 人数

    public StatisticsDTO() {}

    public long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }

    public long getTotalCompetitions() { return totalCompetitions; }
    public void setTotalCompetitions(long totalCompetitions) { this.totalCompetitions = totalCompetitions; }

    public long getTotalRegistrations() { return totalRegistrations; }
    public void setTotalRegistrations(long totalRegistrations) { this.totalRegistrations = totalRegistrations; }

    public Map<String, Long> getCompetitionDistribution() { return competitionDistribution; }
    public void setCompetitionDistribution(Map<String, Long> competitionDistribution) { this.competitionDistribution = competitionDistribution; }

    public Map<String, Long> getUserRoleDistribution() { return userRoleDistribution; }
    public void setUserRoleDistribution(Map<String, Long> userRoleDistribution) { this.userRoleDistribution = userRoleDistribution; }
}
