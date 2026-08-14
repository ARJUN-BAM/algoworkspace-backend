package com.algoworkspace.algoworkspace_backend.service;

import com.algoworkspace.algoworkspace_backend.entity.Step;
import com.algoworkspace.algoworkspace_backend.repository.StepRepository;
import com.algoworkspace.algoworkspace_backend.validator.SnapshotValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StepService {
    
    private final StepRepository stepRepository;
    private final SnapshotValidator snapshotValidator;
    
    @Transactional
    public Step createStep(Long workspaceId, String snapshotJson) {
        // Validate snapshot JSON format
        snapshotValidator.validate(snapshotJson);
        
        // Get the next step number for this workspace
        List<Step> existingSteps = stepRepository.findByWorkspaceIdOrderByStepNumberAsc(workspaceId);
        int nextStepNumber = existingSteps.isEmpty() ? 1 : existingSteps.get(existingSteps.size() - 1).getStepNumber() + 1;
        
        Step step = new Step();
        step.setWorkspaceId(workspaceId);
        step.setStepNumber(nextStepNumber);
        step.setSnapshotJson(snapshotJson);
        
        return stepRepository.save(step);
    }
    
    public List<Step> getStepsByWorkspace(Long workspaceId) {
        return stepRepository.findByWorkspaceIdOrderByStepNumberAsc(workspaceId);
    }
    
    public Step getStepByNumber(Long workspaceId, Integer stepNumber) {
        return stepRepository.findByWorkspaceIdAndStepNumber(workspaceId, stepNumber)
                .orElseThrow(() -> new RuntimeException("Step not found with number: " + stepNumber + " for workspace: " + workspaceId));
    }
    
    /**
     * Get the previous step relative to the current step number.
     * Returns empty if currentStepNumber is 1 (no previous step).
     */
    public Optional<Step> getPreviousStep(Long workspaceId, Integer currentStepNumber) {
        if (currentStepNumber <= 1) {
            return Optional.empty();
        }
        return stepRepository.findByWorkspaceIdAndStepNumber(workspaceId, currentStepNumber - 1);
    }
    
    /**
     * Get the next step relative to the current step number.
     * Returns empty if currentStepNumber is the last step.
     */
    public Optional<Step> getNextStep(Long workspaceId, Integer currentStepNumber) {
        return stepRepository.findByWorkspaceIdAndStepNumber(workspaceId, currentStepNumber + 1);
    }
}
