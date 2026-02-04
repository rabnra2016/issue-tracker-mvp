package com.issuetracker.repository;

import com.issuetracker.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByOwnerId(Long ownerId);
    
    @Query("SELECT DISTINCT p FROM Project p LEFT JOIN FETCH p.owner WHERE p.ownerId = :ownerId")
    List<Project> findByOwnerIdWithOwner(Long ownerId);
}
