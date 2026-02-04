import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { IssueService } from '../../services/issue.service';
import { WebsocketService } from '../../services/websocket.service';
import { Issue } from '../../models/models';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-issue-detail',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './issue-detail.component.html',
  styleUrl: './issue-detail.component.css'
})
export class IssueDetailComponent implements OnInit, OnDestroy {
  issue: Issue | null = null;
  projectId!: number;
  issueId!: number;
  loading = false;
  editing = false;
  
  editForm = {
    title: '',
    description: '',
    status: '' as 'OPEN' | 'IN_PROGRESS' | 'CLOSED',
    priority: '' as 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'
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
      this.issueId = +params['issueId'];
      this.loadIssue();
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
        // Reload issue when update received
        if (message.issueId === this.issueId) {
          this.loadIssue();
        }
      }
    });
  }

  loadIssue(): void {
    this.loading = true;
    this.issueService.getIssue(this.issueId).subscribe({
      next: (issue) => {
        this.issue = issue;
        this.editForm = {
          title: issue.title,
          description: issue.description,
          status: issue.status,
          priority: issue.priority
        };
        this.loading = false;
      },
      error: (err) => {
        console.error('Failed to load issue:', err);
        this.loading = false;
      }
    });
  }

  startEdit(): void {
    this.editing = true;
  }

  cancelEdit(): void {
    this.editing = false;
    if (this.issue) {
      this.editForm = {
        title: this.issue.title,
        description: this.issue.description,
        status: this.issue.status,
        priority: this.issue.priority
      };
    }
  }

  saveEdit(): void {
    this.issueService.updateIssue(this.issueId, this.editForm).subscribe({
      next: (issue) => {
        this.issue = issue;
        this.editing = false;
      },
      error: (err) => {
        console.error('Failed to update issue:', err);
      }
    });
  }

  deleteIssue(): void {
    if (confirm('Are you sure you want to delete this issue?')) {
      this.issueService.deleteIssue(this.issueId).subscribe({
        next: () => {
          this.router.navigate(['/projects', this.projectId, 'issues']);
        },
        error: (err) => {
          console.error('Failed to delete issue:', err);
        }
      });
    }
  }

  goBack(): void {
    this.router.navigate(['/projects', this.projectId, 'issues']);
  }

  getPriorityClass(priority: string): string {
    return `priority-${priority.toLowerCase()}`;
  }

  getStatusClass(status: string): string {
    return `status-${status.toLowerCase().replace('_', '-')}`;
  }
}
