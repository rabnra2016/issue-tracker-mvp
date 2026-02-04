package com.issuetracker.controller;

import com.issuetracker.dto.ProjectRequest;
import com.issuetracker.dto.ProjectResponse;
import com.issuetracker.security.JwtUtil;
import com.issuetracker.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {
    private final ProjectService projectService;
    private final JwtUtil jwtUtil;

    public ProjectController(ProjectService projectService, JwtUtil jwtUtil) {
        this.projectService = projectService;
        this.jwtUtil = jwtUtil;
    }

    
    
    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(
            @Valid @RequestBody ProjectRequest request,
            @RequestHeader("Authorization") String token) {
        Long userId = extractUserId(token);
        return ResponseEntity.ok(projectService.createProject(request, userId));
    }
    
    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getUserProjects(
            @RequestHeader("Authorization") String token) {
        Long userId = extractUserId(token);
        return ResponseEntity.ok(projectService.getUserProjects(userId));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getProject(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        Long userId = extractUserId(token);
        return ResponseEntity.ok(projectService.getProject(id, userId));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody ProjectRequest request,
            @RequestHeader("Authorization") String token) {
        Long userId = extractUserId(token);
        return ResponseEntity.ok(projectService.updateProject(id, request, userId));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        Long userId = extractUserId(token);
        projectService.deleteProject(id, userId);
        return ResponseEntity.noContent().build();
    }
    
    private Long extractUserId(String token) {
        String jwt = token.substring(7); // Remove "Bearer " prefix
        return jwtUtil.extractUserId(jwt);
    }
}
