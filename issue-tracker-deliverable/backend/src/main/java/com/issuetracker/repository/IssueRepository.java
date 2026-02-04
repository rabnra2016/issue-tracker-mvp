package com.issuetracker.repository;

import com.issuetracker.model.Issue;
import com.issuetracker.model.enums.IssuePriority;
import com.issuetracker.model.enums.IssueStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface IssueRepository extends JpaRepository<Issue, Long> {
    
    @Query("SELECT i FROM Issue i WHERE " +
           "(:projectId IS NULL OR i.projectId = :projectId) AND " +
           "(:status IS NULL OR i.status = :status) AND " +
           "(:priority IS NULL OR i.priority = :priority) AND " +
           "(:assigneeId IS NULL OR i.assigneeId = :assigneeId) AND " +
           "(:searchText IS NULL OR LOWER(i.title) LIKE LOWER(CONCAT('%', :searchText, '%')))")
    Page<Issue> findByFilters(
        @Param("projectId") Long projectId,
        @Param("status") IssueStatus status,
        @Param("priority") IssuePriority priority,
        @Param("assigneeId") Long assigneeId,
        @Param("searchText") String searchText,
        Pageable pageable
    );
    
    Page<Issue> findByProjectId(Long projectId, Pageable pageable);
}
