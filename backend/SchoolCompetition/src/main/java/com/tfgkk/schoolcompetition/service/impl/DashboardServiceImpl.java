package com.tfgkk.schoolcompetition.service.impl;

import com.tfgkk.schoolcompetition.dto.StatisticsDTO;
import com.tfgkk.schoolcompetition.entity.Competition;
import com.tfgkk.schoolcompetition.entity.Registration;
import com.tfgkk.schoolcompetition.entity.User;
import com.tfgkk.schoolcompetition.repository.CompetitionRepository;
import com.tfgkk.schoolcompetition.repository.RegistrationRepository;
import com.tfgkk.schoolcompetition.repository.UserRepository;
import com.tfgkk.schoolcompetition.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompetitionRepository competitionRepository;

    @Autowired
    private RegistrationRepository registrationRepository;

    @Override
    public StatisticsDTO getStatistics() {
        StatisticsDTO stats = new StatisticsDTO();

        try {
            // 1. 基础总数
            stats.setTotalUsers(userRepository.count());
            stats.setTotalCompetitions(competitionRepository.count());
            stats.setTotalRegistrations(registrationRepository.count());

            // 2. 角色分布
            List<User> users = userRepository.findAll();
            Map<String, Long> roleMap = users.stream()
                    .filter(u -> u.getRole() != null)
                    .collect(Collectors.groupingBy(User::getRole, Collectors.counting()));
            stats.setUserRoleDistribution(roleMap);

            // 3. 竞赛报名分布 (使用更稳健的循环方式，防止 toMap 报错)
            List<Competition> competitions = competitionRepository.findAll();
            List<Registration> registrations = registrationRepository.findAll();
            
            Map<Long, String> idToTitle = new HashMap<>();
            for (Competition c : competitions) {
                if (c.getId() != null) {
                    idToTitle.put(c.getId(), c.getTitle() != null ? c.getTitle() : "未命名竞赛");
                }
            }

            Map<String, Long> compMap = new HashMap<>();
            for (Registration r : registrations) {
                String title = idToTitle.getOrDefault(r.getCompetitionId(), "未知竞赛");
                compMap.put(title, compMap.getOrDefault(title, 0L) + 1);
            }
            
            stats.setCompetitionDistribution(compMap);
        } catch (Exception e) {
            e.printStackTrace();
            // 如果报错，至少返回基础计数
            if (stats.getCompetitionDistribution() == null) stats.setCompetitionDistribution(new HashMap<>());
            if (stats.getUserRoleDistribution() == null) stats.setUserRoleDistribution(new HashMap<>());
        }

        return stats;
    }
}
