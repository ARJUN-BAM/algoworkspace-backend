package com.algoworkspace.algoworkspace_backend.repository;

import com.algoworkspace.algoworkspace_backend.entity.Step;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StepRepository extends JpaRepository<Step, Long> {
    List<Step> findByWorkspaceIdOrderByStepNumberAsc(Long workspaceId);
    Optional<Step> findByWorkspaceIdAndStepNumber(Long workspaceId, Integer stepNumber);
}
