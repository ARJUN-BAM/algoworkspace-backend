package com.algoworkspace.algoworkspace_backend.controller;

import com.algoworkspace.algoworkspace_backend.dto.StepCreateRequest;
import com.algoworkspace.algoworkspace_backend.dto.WorkspaceCreateRequest;
import com.algoworkspace.algoworkspace_backend.entity.Step;
import com.algoworkspace.algoworkspace_backend.entity.Workspace;
import com.algoworkspace.algoworkspace_backend.service.StepService;
import com.algoworkspace.algoworkspace_backend.service.WorkspaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workspaces")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class WorkspaceController {
    
    private final WorkspaceService workspaceService;
    private final StepService stepService;
    
    // POST /api/workspaces
    @PostMapping
    public ResponseEntity<Workspace> createWorkspace(@RequestBody WorkspaceCreateRequest request) {
        Workspace workspace = workspaceService.createWorkspace(request.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(workspace);
    }
    
    // POST /api/workspaces/{id}/steps
    @PostMapping("/{id}/steps")
    public ResponseEntity<?> createStep(
            @PathVariable Long id, 
            @RequestBody StepCreateRequest request) {
        try {
            // Verify workspace exists
            workspaceService.getWorkspace(id);
            
            Step step = stepService.createStep(id, request.getSnapshotJson());
            return ResponseEntity.status(HttpStatus.CREATED).body(step);
        } catch (IllegalArgumentException e) {
            // Validation error
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }
    
    // Inner class for error responses
    private record ErrorResponse(String message) {}
    
    // GET /api/workspaces/{id}/steps
    @GetMapping("/{id}/steps")
    public ResponseEntity<List<Step>> getSteps(@PathVariable Long id) {
        // Verify workspace exists
        workspaceService.getWorkspace(id);
        
        List<Step> steps = stepService.getStepsByWorkspace(id);
        return ResponseEntity.ok(steps);
    }
    
    // GET /api/workspaces/{id}/steps/{stepNumber}
    @GetMapping("/{id}/steps/{stepNumber}")
    public ResponseEntity<Step> getStepByNumber(
            @PathVariable Long id, 
            @PathVariable Integer stepNumber) {
        // Verify workspace exists
        workspaceService.getWorkspace(id);
        
        Step step = stepService.getStepByNumber(id, stepNumber);
        return ResponseEntity.ok(step);
    }
}
