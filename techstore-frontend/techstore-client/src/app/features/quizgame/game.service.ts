import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../shared/models/api-response.model';
import { DailyPlayInfo, GameResultResponseDTO, StartGameRequestDTO, StartGameResponseDTO, SubmitAnswerRequestDTO, UserPointsInfo } from './models/game.model';


@Injectable({ providedIn: 'root' })
export class GameService {
  private baseUrl = `${environment.quizGameUrl}/game`;

  // ─── Reactive state ───────────────────────────────────────────────
  private dailyInfoSubject = new BehaviorSubject<DailyPlayInfo | null>(null);
  dailyInfo$ = this.dailyInfoSubject.asObservable();

  private userPointsSubject = new BehaviorSubject<number>(0);
  userPoints$ = this.userPointsSubject.asObservable();

  private lastResultSubject = new BehaviorSubject<GameResultResponseDTO | null>(null);
  lastResult$ = this.lastResultSubject.asObservable();

  constructor(private http: HttpClient) {}

  // ─── Start game ───────────────────────────────────────────────────
  startGame(userId: number): Observable<ApiResponse<StartGameResponseDTO>> {
    const body: StartGameRequestDTO = { userId };
    return this.http.post<ApiResponse<StartGameResponseDTO>>(
      `${this.baseUrl}/start`,
      body
    );
  }

  // ─── Submit answers ───────────────────────────────────────────────
  submitGame(req: SubmitAnswerRequestDTO): Observable<ApiResponse<GameResultResponseDTO>> {
    return this.http.post<ApiResponse<GameResultResponseDTO>>(
      `${this.baseUrl}/submit`,
      req
    ).pipe(
      tap(res => {
        if (res.result) {
          this.lastResultSubject.next(res.result);
          this.userPointsSubject.next(res.result.totalPoints);
        }
      })
    );
  }

  // ─── Daily info ───────────────────────────────────────────────────
  loadDailyInfo(userId: number): Observable<ApiResponse<DailyPlayInfo>> {
    const params = new HttpParams().set('userId', userId);
    return this.http.get<ApiResponse<DailyPlayInfo>>(
      `${this.baseUrl}/daily-info`,
      { params }
    ).pipe(
      tap(res => this.dailyInfoSubject.next(res.result ?? null))
    );
  }

  // ─── User points ──────────────────────────────────────────────────
  loadUserPoints(userId: number): Observable<ApiResponse<UserPointsInfo>> {
    const params = new HttpParams().set('userId', userId);
    return this.http.get<ApiResponse<UserPointsInfo>>(
      `${this.baseUrl}/points`,
      { params }
    ).pipe(
      tap(res => {
        if (res.result) this.userPointsSubject.next(res.result.totalPoints);
      })
    );
  }

  get dailyInfo(): DailyPlayInfo | null {
    return this.dailyInfoSubject.getValue();
  }

  get userPoints(): number {
    return this.userPointsSubject.getValue();
  }

  get lastResult(): GameResultResponseDTO | null {
    return this.lastResultSubject.getValue();
  }
}