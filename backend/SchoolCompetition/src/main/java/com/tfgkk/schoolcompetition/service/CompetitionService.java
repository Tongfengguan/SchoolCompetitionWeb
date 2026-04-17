package com.tfgkk.schoolcompetition.service;

import com.tfgkk.schoolcompetition.dto.CompetitionDTO;
import com.tfgkk.schoolcompetition.entity.Competition;
import com.tfgkk.schoolcompetition.entity.Registration;

import java.util.List;

public interface CompetitionService {
    List<Competition> getAllCompetitions(String keyword);
    Competition createCompetition(Competition competition);
    void deleteCompetition(Long id);
    
    List<Registration> getRegistrations(Long competitionId);
    Registration register(Registration registration);
    void auditStudent(Long id, Integer status);
    void deleteRegistration(Long id);
}
