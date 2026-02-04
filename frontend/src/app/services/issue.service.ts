import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Issue, PageResponse } from '../models/models';

@Injectable({
  providedIn: 'root'
})
export class IssueService {
  private apiUrl = 'http://localhost:8080/api/issues';

  constructor(private http: HttpClient) {}

  getIssues(
    projectId: number,
    page: number = 0,
    size: number = 20,
    status?: string,
    priority?: string,
    searchText?: string,
    sortBy?: string,
    sortOrder?: string
  ): Observable<PageResponse<Issue>> {
    let params = new HttpParams()
      .set('projectId', projectId.toString())
      .set('page', page.toString())
      .set('size', size.toString());

    if (status) params = params.set('status', status);
    if (priority) params = params.set('priority', priority);
    if (searchText) params = params.set('searchText', searchText);
    if (sortBy) params = params.set('sortBy', sortBy);
    if (sortOrder) params = params.set('sortOrder', sortOrder);

    return this.http.get<PageResponse<Issue>>(this.apiUrl, { params });
  }

  getIssue(id: number): Observable<Issue> {
    return this.http.get<Issue>(`${this.apiUrl}/${id}`);
  }

  createIssue(issue: Partial<Issue>): Observable<Issue> {
    return this.http.post<Issue>(this.apiUrl, issue);
  }

  updateIssue(id: number, issue: Partial<Issue>): Observable<Issue> {
    return this.http.put<Issue>(`${this.apiUrl}/${id}`, issue);
  }

  deleteIssue(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
