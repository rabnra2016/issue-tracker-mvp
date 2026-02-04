package com.issuetracker.service;

import com.issuetracker.dto.ProjectRequest;
import com.issuetracker.dto.ProjectResponse;
import com.issuetracker.model.Project;
import com.issuetracker.model.ProjectMember;
import com.issuetracker.model.enums.UserRole;
import com.issuetracker.repository.ProjectMemberRepository;
import com.issuetracker.repository.ProjectRepository;
import com.issuetracker.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;

    public ProjectService(ProjectRepository projectRepository, ProjectMemberRepository projectMemberRepository, UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
    }

    
    
    @Transactional
    public ProjectResponse createProject(ProjectRequest request, Long userId) {
        Project project = new Project();
        project.setName(request.getName());
        project.setOwnerId(userId);
        
        project = projectRepository.save(project);
        
        // Add creator as owner in project members
        ProjectMember member = new ProjectMember();
        member.setProjectId(project.getId());
        member.setUserId(userId);
        member.setRole(UserRole.OWNER);
        projectMemberRepository.save(member);
        
        return mapToResponse(project);
    }
    
    public List<ProjectResponse> getUserProjects(Long userId) {
        List<ProjectMember> memberships = projectMemberRepository.findByUserId(userId);
        List<Long> projectIds = memberships.stream()
                .map(ProjectMember::getProjectId)
                .collect(Collectors.toList());
        
        return projectRepository.findAllById(projectIds).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public ProjectResponse getProject(Long projectId, Long userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        
        // Check access
        if (!hasAccess(projectId, userId)) {
            throw new RuntimeException("Access denied");
        }
        
        return mapToResponse(project);
    }
    
    @Transactional
    public ProjectResponse updateProject(Long projectId, ProjectRequest request, Long userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        
        // Check if user is owner
        if (!isOwner(projectId, userId)) {
            throw new RuntimeException("Only owner can update project");
        }
        
        project.setName(request.getName());
        project = projectRepository.save(project);
        
        return mapToResponse(project);
    }
    
    @Transactional
    public void deleteProject(Long projectId, Long userId) {
        if (!isOwner(projectId, userId)) {
            throw new RuntimeException("Only owner can delete project");
        }
        
        projectRepository.deleteById(projectId);
    }
    
    public UserRole getUserRole(Long projectId, Long userId) {
        return projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .map(ProjectMember::getRole)
                .orElse(null);
    }
    
    private boolean hasAccess(Long projectId, Long userId) {
        return projectMemberRepository.existsByProjectIdAndUserId(projectId, userId);
    }
    
    private boolean isOwner(Long projectId, Long userId) {
        return projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .map(pm -> pm.getRole() == UserRole.OWNER)
                .orElse(false);
    }
    
    private ProjectResponse mapToResponse(Project project) {
        ProjectResponse response = new ProjectResponse();
        response.setId(project.getId());
        response.setName(project.getName());
        response.setOwnerId(project.getOwnerId());
        response.setCreatedAt(project.getCreatedAt());
        
        userRepository.findById(project.getOwnerId())
                .ifPresent(owner -> response.setOwnerName(owner.getName()));
        
        return response;
    }
}
