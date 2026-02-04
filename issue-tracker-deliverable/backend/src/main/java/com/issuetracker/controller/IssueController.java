package com.issuetracker.controller;

import com.issuetracker.dto.IssueRequest;
import com.issuetracker.dto.IssueResponse;
import com.issuetracker.model.enums.IssuePriority;
import com.issuetracker.model.enums.IssueStatus;
import com.issuetracker.security.JwtUtil;
import com.issuetracker.service.IssueService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/issues")
public class IssueController {
    private final IssueService issueService;
    private final JwtUtil jwtUtil;

    public IssueController(IssueService issueService, JwtUtil jwtUtil) {
        this.issueService = issueService;
        this.jwtUtil = jwtUtil;
    }

    
    
    @PostMapping
    public ResponseEntity<IssueResponse> createIssue(
            @Valid @RequestBody IssueRequest request,
            @RequestHeader("Authorization") String token) {
        Long userId = extractUserId(token);
        return ResponseEntity.ok(issueService.createIssue(request, userId));
    }
    
    @GetMapping
    public ResponseEntity<Page<IssueResponse>> getIssues(
            @RequestParam Long projectId,
            @RequestParam(required = false) IssueStatus status,
            @RequestParam(required = false) IssuePriority priority,
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir,
            @RequestHeader("Authorization") String token) {
        
        Long userId = extractUserId(token);
        
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? 
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<IssueResponse> issues = issueService.getIssues(
                projectId, status, priority, assigneeId, search, pageable, userId);
        
        return ResponseEntity.ok(issues);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<IssueResponse> getIssue(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        Long userId = extractUserId(token);
        return ResponseEntity.ok(issueService.getIssue(id, userId));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<IssueResponse> updateIssue(
            @PathVariable Long id,
            @Valid @RequestBody IssueRequest request,
            @RequestHeader("Authorization") String token) {
        Long userId = extractUserId(token);
        return ResponseEntity.ok(issueService.updateIssue(id, request, userId));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIssue(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        Long userId = extractUserId(token);
        issueService.deleteIssue(id, userId);
        return ResponseEntity.noContent().build();
    }
    
    private Long extractUserId(String token) {
        String jwt = token.substring(7); // Remove "Bearer " prefix
        return jwtUtil.extractUserId(jwt);
    }
}
