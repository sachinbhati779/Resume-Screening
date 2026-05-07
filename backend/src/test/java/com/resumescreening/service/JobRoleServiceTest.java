package com.resumescreening.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.resumescreening.dto.JobRoleRequestDTO;
import com.resumescreening.model.JobRole;
import com.resumescreening.repository.JobRoleRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class JobRoleServiceTest {

    @Test
    void removesGenericRecruitingWordsFromKeywordsBeforeSavingRole() {
        JobRoleRepository repository = mock(JobRoleRepository.class);
        when(repository.save(any(JobRole.class))).thenAnswer(invocation -> {
            JobRole role = invocation.getArgument(0);
            role.setId(1L);
            return role;
        });
        JobRoleService service = new JobRoleService(repository);

        service.create(new JobRoleRequestDTO(
                "AI Software Engineer",
                List.of("Java", "SQL", "Python"),
                0,
                "Computer Science",
                List.of("looking", "passionate", "skilled", "join", "java", "firebase", "rest api", "ai"),
                40,
                25,
                15,
                10,
                10
        ));

        ArgumentCaptor<JobRole> captor = ArgumentCaptor.forClass(JobRole.class);
        verify(repository).save(captor.capture());
        JobRole savedRole = captor.getValue();
        assertThat(savedRole.getKeywords())
                .containsExactly("java", "firebase", "rest api", "ai")
                .doesNotContain("looking", "passionate", "skilled", "join");
    }
}
