package com.algoworkspace.algoworkspace_backend.service;

import com.algoworkspace.algoworkspace_backend.entity.Workspace;
import com.algoworkspace.algoworkspace_backend.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorkspaceService {
    
    private final WorkspaceRepository workspaceRepository;
    
    @Transactional
    public Workspace createWorkspace(String name) {
        Workspace workspace = new Workspace();
        workspace.setName(name);
        return workspaceRepository.save(workspace);
    }
    
    public Workspace getWorkspace(Long id) {
        return workspaceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Workspace not found with id: " + id));
    }
}
