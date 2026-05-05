package com.resumescreening.service;

import com.resumescreening.dto.ShortlistedCandidateDTO;
import com.resumescreening.model.ShortlistedCandidate;
import com.resumescreening.repository.ShortlistedCandidateRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ShortlistedCandidateService {

    private final ShortlistedCandidateRepository shortlistedCandidateRepository;

    public ShortlistedCandidateService(ShortlistedCandidateRepository shortlistedCandidateRepository) {
        this.shortlistedCandidateRepository = shortlistedCandidateRepository;
    }

    public List<ShortlistedCandidateDTO> findAll() {
        return shortlistedCandidateRepository.findAll().stream().map(this::toDto).toList();
    }

    public String exportCsv() {
        StringBuilder builder = new StringBuilder("id,resumeId,candidateName,email,roleName,score,createdAt\n");
        for (ShortlistedCandidateDTO candidate : findAll()) {
            builder.append(candidate.id()).append(',')
                    .append(candidate.resumeId()).append(',')
                    .append(csv(candidate.candidateName())).append(',')
                    .append(csv(candidate.email())).append(',')
                    .append(csv(candidate.roleName())).append(',')
                    .append(candidate.score()).append(',')
                    .append(candidate.createdAt()).append('\n');
        }
        return builder.toString();
    }

    private ShortlistedCandidateDTO toDto(ShortlistedCandidate candidate) {
        return new ShortlistedCandidateDTO(
                candidate.getId(),
                candidate.getResumeId(),
                candidate.getReportId(),
                candidate.getCandidateName(),
                candidate.getEmail(),
                candidate.getRoleName(),
                candidate.getScore(),
                candidate.getCreatedAt()
        );
    }

    private String csv(String value) {
        if (value == null) {
            return "";
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
