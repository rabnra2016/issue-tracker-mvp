import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { IssueService } from '../../services/issue.service';
import { WebsocketService } from '../../services/websocket.service';
import { Issue, PageResponse } from '../../models/models';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-issue-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './issue-list.component.html',
  styleUrl: './issue-list.component.css'
})
export class IssueListComponent implements OnInit, OnDestroy {
  projectId!: number;
  issues: Issue[] = [];
  totalPages = 0;
  currentPage = 0;
  loading = false;
  
  // Filters
  statusFilter = '';
  priorityFilter = '';
  searchText = '';
  sortBy = 'createdAt';
  sortOrder = 'desc';
  
  // Create form
  showCreateForm = false;
  newIssue = {
    title: '',
    description: '',
    priority: 'MEDIUM' as 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'
  };

  private wsSubscription?: Subscription;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private issueService: IssueService,
    private wsService: WebsocketService
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.projectId = +params['id'];
      this.loadIssues();
      this.connectWebSocket();
    });
  }

  ngOnDestroy(): void {
    this.wsSubscription?.unsubscribe();
    this.wsService.disconnect();
  }

  connectWebSocket(): void {
    this.wsService.connect(`ws://localhost:8080/ws`);
    this.wsSubscription = this.wsService.getMessages().subscribe({
      next: (message) => {
        console.log('WebSocket message:', message);
        // Reload issues when update received
        this.loadIssues();
      }
    });
  }

  loadIssues(): void {
    this.loading = true;
    this.issueService.getIssues(
      this.projectId,
      this.currentPage,
      20,
      this.statusFilter || undefined,
      this.priorityFilter || undefined,
      this.searchText || undefined,
      this.sortBy,
      this.sortOrder
    ).subscribe({
      next: (response: PageResponse<Issue>) => {
        this.issues = response.content;
        this.totalPages = response.totalPages;
        this.loading = false;
      },
      error: (err) => {
        console.error('Failed to load issues:', err);
        this.loading = false;
      }
    });
  }

  applyFilters(): void {
    this.currentPage = 0;
    this.loadIssues();
  }

  clearFilters(): void {
    this.statusFilter = '';
    this.priorityFilter = '';
    this.searchText = '';
    this.currentPage = 0;
    this.loadIssues();
  }

  changePage(page: number): void {
    this.currentPage = page;
    this.loadIssues();
  }

  createIssue(): void {
    if (!this.newIssue.title.trim()) return;

    this.issueService.createIssue({
      projectId: this.projectId,
      title: this.newIssue.title,
      description: this.newIssue.description,
      priority: this.newIssue.priority
    }).subscribe({
      next: () => {
        this.newIssue = { title: '', description: '', priority: 'MEDIUM' };
        this.showCreateForm = false;
        this.loadIssues();
      },
      error: (err) => {
        console.error('Failed to create issue:', err);
      }
    });
  }

  viewIssue(issueId: number): void {
    this.router.navigate(['/projects', this.projectId, 'issues', issueId]);
  }

  goBack(): void {
    this.router.navigate(['/projects', this.projectId]);
  }

  getPriorityClass(priority: string): string {
    return `priority-${priority.toLowerCase()}`;
  }

  getStatusClass(status: string): string {
    return `status-${status.toLowerCase().replace('_', '-')}`;
  }
}
