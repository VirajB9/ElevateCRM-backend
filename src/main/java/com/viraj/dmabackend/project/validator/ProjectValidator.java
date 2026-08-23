package com.viraj.dmabackend.project.validator;

import com.viraj.dmabackend.client.exception.ClientNotFoundException;
import com.viraj.dmabackend.client.repository.ClientRepository;
import com.viraj.dmabackend.project.exception.DuplicateProjectException;
import com.viraj.dmabackend.project.exception.InvalidProjectDateException;
import com.viraj.dmabackend.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class ProjectValidator {

    private final ProjectRepository projectRepository;
    private final ClientRepository clientRepository;

    public void validateClientExists(String clientId) {
        if (!clientRepository.existsById(clientId)) {
            throw new ClientNotFoundException(clientId);
        }
    }

    public void validateDuplicateProject(String projectName, String clientId) {
        if (projectRepository.existsByProjectNameAndClientId(projectName, clientId)) {
            throw new DuplicateProjectException(projectName, clientId);
        }
    }

    public void validateProjectDates(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new InvalidProjectDateException();
        }
    }

    public void validateDuplicateProjectForUpdate(String projectName, String clientId, String projectId) {
        if (projectRepository.existsByProjectNameAndClientIdAndIdNot(projectName, clientId, projectId)) {
            throw new DuplicateProjectException(projectName, clientId);
        }
    }
}
