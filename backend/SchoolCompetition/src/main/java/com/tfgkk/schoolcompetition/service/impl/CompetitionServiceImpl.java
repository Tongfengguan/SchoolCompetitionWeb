package com.tfgkk.schoolcompetition.service.impl;

import com.tfgkk.schoolcompetition.common.BusinessException;
import com.tfgkk.schoolcompetition.entity.Competition;
import com.tfgkk.schoolcompetition.entity.Registration;
import com.tfgkk.schoolcompetition.repository.CompetitionRepository;
import com.tfgkk.schoolcompetition.repository.RegistrationRepository;
import com.tfgkk.schoolcompetition.service.CompetitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CompetitionServiceImpl implements CompetitionService {

    @Autowired
    private CompetitionRepository competitionRepository;

    @Autowired
    private RegistrationRepository registrationRepository;

    @Override
    public List<Competition> getAllCompetitions(String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return competitionRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword);
        }
        return competitionRepository.findAllByOrderByIdDesc();
    }

    @Override
    public Competition createCompetition(Competition competition) {
        return competitionRepository.save(competition);
    }

    @Override
    @Transactional
    public void deleteCompetition(Long id) {
        registrationRepository.deleteByCompetitionId(id);
        competitionRepository.deleteById(id);
    }

    @Override
    public List<Registration> getRegistrations(Long competitionId) {
        return registrationRepository.findByCompetitionId(competitionId);
    }

    @Override
    public Registration register(Registration registration) {
        boolean exists = registrationRepository.existsByCompetitionIdAndStudentId(
                registration.getCompetitionId(),
                registration.getStudentId()
        );

        if (exists) {
            throw new BusinessException("您已经报名过该比赛，请勿重复提交！");
        }

        return registrationRepository.save(registration);
    }

    @Override
    public void auditStudent(Long id, Integer status) {
        Registration registration = registrationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("报名记录不存在"));
        registration.setStatus(status);
        registrationRepository.save(registration);
    }

    @Override
    public void deleteRegistration(Long id) {
        registrationRepository.deleteById(id);
    }
}
