package com.issuetracker.service;

import com.issuetracker.dto.IssueRequest;
import com.issuetracker.dto.IssueResponse;
import com.issuetracker.model.Issue;
import com.issuetracker.model.enums.IssuePriority;
import com.issuetracker.model.enums.IssueStatus;
import com.issuetracker.model.enums.UserRole;
import com.issuetracker.repository.IssueRepository;
import com.issuetracker.repository.ProjectRepository;
import com.issuetracker.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IssueService {
    private final IssueRepository issueRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectService projectService;
    private final SimpMessagingTemplate messagingTemplate;

    public IssueService(IssueRepository issueRepository, ProjectRepository projectRepository, UserRepository userRepository, ProjectService projectService, SimpMessagingTemplate messagingTemplate) {
        this.issueRepository = issueRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.projectService = projectService;
        this.messagingTemplate = messagingTemplate;
    }

    
    
    @Transactional
    public IssueResponse createIssue(IssueRequest request, Long userId) {
        // Verify project exists and user has access
        projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found"));
        
        UserRole role = projectService.getUserRole(request.getProjectId(), userId);
        if (role == null) {
            throw new RuntimeException("Access denied");
        }
        
        Issue issue = new Issue();
        issue.setProjectId(request.getProjectId());
        issue.setTitle(request.getTitle());
        issue.setDescription(request.getDescription());
        issue.setStatus(request.getStatus() != null ? request.getStatus() : IssueStatus.OPEN);
        issue.setPriority(request.getPriority() != null ? request.getPriority() : IssuePriority.MEDIUM);
        issue.setAssigneeId(request.getAssigneeId());
        issue.setTags(request.getTags());
        
        issue = issueRepository.save(issue);
        
        IssueResponse response = mapToResponse(issue);
        
        // Send real-time update
        messagingTemplate.convertAndSend("/topic/projects/" + issue.getProjectId() + "/issues", response);
        
        return response;
    }
    
    public Page<IssueResponse> getIssues(Long projectId, IssueStatus status, IssuePriority priority,
                                         Long assigneeId, String searchText, Pageable pageable, Long userId) {
        // Check access
        UserRole role = projectService.getUserRole(projectId, userId);
        if (role == null) {
            throw new RuntimeException("Access denied");
        }
        
        Page<Issue> issues = issueRepository.findByFilters(projectId, status, priority, assigneeId, searchText, pageable);
        
        return issues.map(this::mapToResponse);
    }
    
    public IssueResponse getIssue(Long issueId, Long userId) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue not found"));
        
        // Check access
        UserRole role = projectService.getUserRole(issue.getProjectId(), userId);
        if (role == null) {
            throw new RuntimeException("Access denied");
        }
        
        return mapToResponse(issue);
    }
    
    @Transactional
    public IssueResponse updateIssue(Long issueId, IssueRequest request, Long userId) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue not found"));
        
        // Check access - at least REPORTER role required
        UserRole role = projectService.getUserRole(issue.getProjectId(), userId);
        if (role == null) {
            throw new RuntimeException("Access denied");
        }
        
        issue.setTitle(request.getTitle());
        issue.setDescription(request.getDescription());
        
        if (request.getStatus() != null) {
            issue.setStatus(request.getStatus());
        }
        if (request.getPriority() != null) {
            issue.setPriority(request.getPriority());
        }
        if (request.getAssigneeId() != null) {
            issue.setAssigneeId(request.getAssigneeId());
        }
        if (request.getTags() != null) {
            issue.setTags(request.getTags());
        }
        
        issue = issueRepository.save(issue);
        
        IssueResponse response = mapToResponse(issue);
        
        // Send real-time update
        messagingTemplate.convertAndSend("/topic/projects/" + issue.getProjectId() + "/issues", response);
        
        return response;
    }
    
    @Transactional
    public void deleteIssue(Long issueId, Long userId) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue not found"));
        
        // Only OWNER or MAINTAINER can delete
        UserRole role = projectService.getUserRole(issue.getProjectId(), userId);
        if (role != UserRole.OWNER && role != UserRole.MAINTAINER) {
            throw new RuntimeException("Access denied");
        }
        
        issueRepository.deleteById(issueId);
        
        // Send real-time update
        messagingTemplate.convertAndSend("/topic/projects/" + issue.getProjectId() + "/issues/deleted", issueId);
    }
    
    private IssueResponse mapToResponse(Issue issue) {
        IssueResponse response = new IssueResponse();
        response.setId(issue.getId());
        response.setProjectId(issue.getProjectId());
        response.setTitle(issue.getTitle());
        response.setDescription(issue.getDescription());
        response.setStatus(issue.getStatus());
        response.setPriority(issue.getPriority());
        response.setAssigneeId(issue.getAssigneeId());
        response.setTags(issue.getTags());
        response.setCreatedAt(issue.getCreatedAt());
        response.setUpdatedAt(issue.getUpdatedAt());
        response.setVersion(issue.getVersion());
        
        projectRepository.findById(issue.getProjectId())
                .ifPresent(project -> response.setProjectName(project.getName()));
        
        if (issue.getAssigneeId() != null) {
            userRepository.findById(issue.getAssigneeId())
                    .ifPresent(assignee -> response.setAssigneeName(assignee.getName()));
        }
        
        return response;
    }
}
