package com.tfgkk.schoolcompetition.service;

import com.tfgkk.schoolcompetition.entity.Competition;
import com.tfgkk.schoolcompetition.entity.Registration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CompetitionService {
    Page<Competition> getAllCompetitions(String keyword, Pageable pageable);
    Competition createCompetition(Competition competition);
    void deleteCompetition(Long id);
    
    List<Registration> getRegistrations(Long competitionId);
    Registration register(Registration registration);
    void auditStudent(Long id, Integer status);
    void deleteRegistration(Long id);
}
