import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ProjectService } from '../../services/project.service';
import { AuthService } from '../../services/auth.service';
import { Project } from '../../models/models';

@Component({
  selector: 'app-project-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './project-list.component.html',
  styleUrl: './project-list.component.css'
})
export class ProjectListComponent implements OnInit {
  projects: Project[] = [];
  loading = false;
  showCreateForm = false;
  newProjectName = '';

  constructor(
    private projectService: ProjectService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadProjects();
  }

  loadProjects(): void {
    this.loading = true;
    this.projectService.getProjects().subscribe({
      next: (projects) => {
        this.projects = projects;
        this.loading = false;
      },
      error: (err) => {
        console.error('Failed to load projects:', err);
        this.loading = false;
      }
    });
  }

  createProject(): void {
    if (!this.newProjectName.trim()) return;

    // Optimistic UI: Add project immediately with temporary data
    const optimisticProject: Project = {
      id: Date.now(), // Temporary ID
      name: this.newProjectName,
      ownerId: 0,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    };

    this.projects.unshift(optimisticProject);
    const projectName = this.newProjectName;
    this.newProjectName = '';
    this.showCreateForm = false;

    // Make API call
    this.projectService.createProject(projectName).subscribe({
      next: (project) => {
        // Replace optimistic project with real one
        const index = this.projects.findIndex(p => p.id === optimisticProject.id);
        if (index !== -1) {
          this.projects[index] = project;
        }
      },
      error: (err) => {
        console.error('Failed to create project:', err);
        // Rollback on error
        this.projects = this.projects.filter(p => p.id !== optimisticProject.id);
        alert('Failed to create project. Please try again.');
      }
    });
  }

  viewProject(projectId: number): void {
    this.router.navigate(['/projects', projectId]);
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
