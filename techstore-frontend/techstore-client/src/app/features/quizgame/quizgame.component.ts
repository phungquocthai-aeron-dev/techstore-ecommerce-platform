import {
  Component, OnInit, OnDestroy, ChangeDetectorRef, NgZone
} from '@angular/core';
import { Subject, forkJoin, interval } from 'rxjs';
import { takeUntil, take } from 'rxjs/operators';

import { GameService } from './game.service';
import { CouponService } from './couponGame.service';
import { AnswerOptionDTO, AnswerResultDTO, CouponConfigResponseDTO, DailyPlayInfo, GameResultResponseDTO, GameScreen, QuestionResponseDTO, UserAnswerDTO, UserCoupon } from './models/game.model';
import { CustomerService } from '../user/customer.service';
import { DatePipe, NgClass, NgFor, NgIf } from '@angular/common';


interface Toast {
  id: number;
  message: string;
  type: 'success' | 'error' | 'info';
}

@Component({
  selector: 'app-quiz-game',
  imports: [NgFor, NgIf, NgClass, DatePipe],
  standalone: true,
  templateUrl: './quizgame.component.html',
  styleUrls: ['./quizgame.component.css'],
})
export class QuizGameComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();

  // ─── Auth ───────────────────────────────────────────────────────────
  userId = 0; // set từ CustomerService

  // ─── Screens ────────────────────────────────────────────────────────
  screen: GameScreen = 'intro';

  // ─── Daily / Points ──────────────────────────────────────────────────
  dailyInfo: DailyPlayInfo | null = null;
  userPoints = 0;

  // ─── Game state ──────────────────────────────────────────────────────
  questions: QuestionResponseDTO[] = [];
  currentQIndex = 0;
  userAnswers: UserAnswerDTO[] = [];
  selectedAnswerId: number | null = null;
  answerLocked = false;
  correctAnswerId: number | null = null;  // revealed after answer
  isLoadingGame = false;
  isSubmitting = false;
  gameResult: GameResultResponseDTO | null = null;

  // ─── Countdown timer ─────────────────────────────────────────────────
  timeLeft = 20; // seconds per question
  private timerSub?: ReturnType<typeof setInterval>;

  // ─── Result / shop ────────────────────────────────────────────────────
  availableCoupons: CouponConfigResponseDTO[] = [];
  couponHistory: UserCoupon[] = [];
  isLoadingCoupons = false;
  isRedeeming = false;
  redeemingId: number | null = null;

  // ─── Countdown to midnight ───────────────────────────────────────────
  countdownToMidnight = '';

  // ─── Toasts ──────────────────────────────────────────────────────────
  toasts: Toast[] = [];
  private toastCounter = 0;

  // ─── Planet display ──────────────────────────────────────────────────
  readonly PLANET_CONFIGS = [
    { name: 'Aquoris', icon: '🔵', color: '#38bdf8' },
    { name: 'Rosara',  icon: '🌸', color: '#f472b6' },
    { name: 'Violex',  icon: '🟣', color: '#a78bfa' },
    { name: 'Verdis',  icon: '🟢', color: '#34d399' },
    { name: 'Auris',   icon: '🌟', color: '#fbbf24' },
    { name: 'Pyron',   icon: '🔴', color: '#fb923c' },
    { name: 'Neonx',   icon: '💜', color: '#e879f9' },
    { name: 'Cyros',   icon: '🔷', color: '#22d3ee' },
    { name: 'Kraton',  icon: '❤️', color: '#f87171' },
    { name: 'Lumis',   icon: '💚', color: '#a3e635' },
  ];

  constructor(
    private gameService: GameService,
    private couponService: CouponService,
    private customerService: CustomerService,
    private cdr: ChangeDetectorRef,
    private zone: NgZone,
  ) {}

  ngOnInit(): void {
    // Lấy userId từ current user
    const user = this.customerService.currentUser;
    if (user) this.userId = user.id;

    this.customerService.currentUser$
      .pipe(takeUntil(this.destroy$))
      .subscribe(u => { if (u) this.userId = u.id; });

    // Load daily info + points
    this.loadMeta();

    // Countdown to midnight
    this.startMidnightCountdown();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.clearTimer();
  }

  // ─── Meta ─────────────────────────────────────────────────────────────
  private loadMeta(): void {
    forkJoin([
      this.gameService.loadDailyInfo(this.userId),
      this.gameService.loadUserPoints(this.userId),
    ]).pipe(takeUntil(this.destroy$))
      .subscribe({
        next: ([dailyRes, pointsRes]) => {
          this.dailyInfo = dailyRes.result ?? null;
          this.userPoints = pointsRes.result?.totalPoints ?? 0;
        },
        error: () => this.showToast('Không thể tải thông tin game', 'error'),
      });
  }

  // ─── Navigation ───────────────────────────────────────────────────────
  goTo(screen: GameScreen): void {
    this.screen = screen;
    if (screen === 'shop') this.loadShopData();
    if (screen === 'history') this.loadHistory();
  }

  // ─── Start game ───────────────────────────────────────────────────────
  startGame(): void {
    if (!this.dailyInfo?.canPlay) {
      this.showToast('Bạn đã hết lượt chơi hôm nay!', 'error');
      return;
    }
    this.isLoadingGame = true;
    this.gameService.startGame(this.userId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: res => {
          this.isLoadingGame = false;
          if (res.result) {
            this.questions = res.result.questions;
            this.userAnswers = [];
            this.currentQIndex = 0;
            this.selectedAnswerId = null;
            this.answerLocked = false;
            this.correctAnswerId = null;
            this.dailyInfo = {
              ...this.dailyInfo!,
              remainingPlays: res.result.remainingPlays,
              playCount: res.result.totalPlaysToday,
              canPlay: res.result.remainingPlays > 0,
            };
            this.screen = 'playing';
            this.startTimer();
          }
        },
        error: () => {
          this.isLoadingGame = false;
          this.showToast('Không thể bắt đầu game, thử lại!', 'error');
        },
      });
  }

  // ─── Answer ───────────────────────────────────────────────────────────
  selectAnswer(answerId: number): void {
    if (this.answerLocked) return;
    this.selectedAnswerId = answerId;
    this.answerLocked = true;
    this.clearTimer();

    this.userAnswers.push({
      questionId: this.currentQuestion.id,
      answerId,
    });

    // Gọi API hoặc local check — ở đây ta chờ submit cuối
    // Tự động next sau 1.5s
    setTimeout(() => this.nextQuestion(), 1500);
  }

  private nextQuestion(): void {
    if (this.currentQIndex < this.questions.length - 1) {
      this.currentQIndex++;
      this.selectedAnswerId = null;
      this.answerLocked = false;
      this.correctAnswerId = null;
      this.startTimer();
    } else {
      this.submitGame();
    }
  }

  // Auto-submit khi hết giờ
  private onTimerExpired(): void {
    if (!this.answerLocked) {
      // Không chọn → skip câu này (không push vào answers)
      this.answerLocked = true;
      setTimeout(() => this.nextQuestion(), 800);
    }
  }

  // ─── Submit ───────────────────────────────────────────────────────────
  private submitGame(): void {
    this.isSubmitting = true;
    this.screen = 'intro'; // tạm show loading
    this.gameService.submitGame({ userId: this.userId, answers: this.userAnswers })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: res => {
          this.isSubmitting = false;
          this.gameResult = res.result ?? null;
          this.userPoints = res.result?.totalPoints ?? this.userPoints;
          this.screen = 'result';
          this.loadMeta();
        },
        error: () => {
          this.isSubmitting = false;
          this.showToast('Lỗi khi nộp bài, thử lại!', 'error');
          this.screen = 'intro';
        },
      });
  }

  // ─── Timer ───────────────────────────────────────────────────────────
  private startTimer(): void {
    this.timeLeft = 20;
    this.clearTimer();
    this.timerSub = setInterval(() => {
      this.zone.run(() => {
        this.timeLeft--;
        if (this.timeLeft <= 0) {
          this.clearTimer();
          this.onTimerExpired();
        }
        this.cdr.markForCheck();
      });
    }, 1000);
  }

  private clearTimer(): void {
    if (this.timerSub) { clearInterval(this.timerSub); this.timerSub = undefined; }
  }

  // ─── Shop ─────────────────────────────────────────────────────────────
  private loadShopData(): void {
    this.isLoadingCoupons = true;
    this.couponService.loadAvailableCoupons(this.userId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: res => {
          this.isLoadingCoupons = false;
          this.availableCoupons = res.result ?? [];
        },
        error: () => {
          this.isLoadingCoupons = false;
          this.showToast('Không tải được cửa hàng', 'error');
        },
      });
  }

  redeemCoupon(coupon: CouponConfigResponseDTO): void {
    if (!coupon.canRedeem || this.isRedeeming) return;
    this.isRedeeming = true;
    this.redeemingId = coupon.id;
    this.couponService.redeemCoupon(this.userId, coupon.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: res => {
          this.isRedeeming = false;
          this.redeemingId = null;
          const r = res.result;
          if (r) {
            this.userPoints = r.remainingPoints;
            this.showToast(`✅ Đổi thành công: ${r.couponName}! Còn ${r.remainingPoints} điểm`, 'success');
            this.loadShopData();
          }
        },
        error: () => {
          this.isRedeeming = false;
          this.redeemingId = null;
          this.showToast('Đổi coupon thất bại!', 'error');
        },
      });
  }

  // ─── History ──────────────────────────────────────────────────────────
  private loadHistory(): void {
    this.couponService.loadCouponHistory(this.userId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: res => { this.couponHistory = res.result ?? []; },
        error: () => this.showToast('Không tải được lịch sử', 'error'),
      });
  }

  // ─── Midnight countdown ───────────────────────────────────────────────
  private startMidnightCountdown(): void {
    const tick = () => {
      const now = new Date();
      const midnight = new Date();
      midnight.setHours(24, 0, 0, 0);
      const diff = midnight.getTime() - now.getTime();
      const h = String(Math.floor(diff / 3600000)).padStart(2, '0');
      const m = String(Math.floor((diff % 3600000) / 60000)).padStart(2, '0');
      const s = String(Math.floor((diff % 60000) / 1000)).padStart(2, '0');
      this.countdownToMidnight = `${h}:${m}:${s}`;
    };
    tick();
    interval(1000).pipe(takeUntil(this.destroy$)).subscribe(() => {
      this.zone.run(() => { tick(); this.cdr.markForCheck(); });
    });
  }

  // ─── Toasts ───────────────────────────────────────────────────────────
  showToast(message: string, type: Toast['type'] = 'info'): void {
    const id = ++this.toastCounter;
    this.toasts.push({ id, message, type });
    setTimeout(() => this.removeToast(id), 3500);
  }

  removeToast(id: number): void {
    this.toasts = this.toasts.filter(t => t.id !== id);
  }

  // ─── Helpers / Getters ────────────────────────────────────────────────
  get currentQuestion(): QuestionResponseDTO {
    return this.questions[this.currentQIndex];
  }

  get progressPercent(): number {
    return ((this.currentQIndex) / this.questions.length) * 100;
  }

  get timerPercent(): number {
    return (this.timeLeft / 20) * 100;
  }

  get timerDanger(): boolean {
    return this.timeLeft <= 5;
  }

  get resultLevel(): { label: string; desc: string; icon: string; color: string } {
    const sc = this.gameResult?.correctCount ?? 0;
    if (sc === 10) return { label: 'TECH MASTER 🏆',      desc: 'Hoàn hảo! Bạn là bậc thầy thiên hà!',          icon: '🏆', color: '#f59e0b' };
    if (sc >= 8)  return { label: 'GEAR EXPERT ⚡',       desc: 'Xuất sắc! Tiếp tục chinh phục vũ trụ!',          icon: '⚡', color: '#818cf8' };
    if (sc >= 5)  return { label: 'TECH LOVER 💡',        desc: 'Khá tốt! Còn nhiều hành tinh chờ đón!',          icon: '🌟', color: '#38bdf8' };
    return            { label: 'TECH NEWBIE 🔰',          desc: 'Mới bắt đầu – luyện tập thêm nhé!',              icon: '🛸', color: '#64748b' };
  }

trackByAnswer(_: number, ans: AnswerOptionDTO): number {
  return ans.id;
}

getAnswerClass(answerId: number): string {
  if (!this.answerLocked) {
    return this.selectedAnswerId === answerId ? 'selected' : '';
  }

  if (answerId === this.correctAnswerId) return 'correct';
  if (answerId === this.selectedAnswerId) return 'wrong';
  return 'dimmed';
}

  getAnswerClass2(answerId: number, result: AnswerResultDTO): string {
    if (answerId === result.correctAnswerId) return 'correct';
    if (answerId === result.selectedAnswerId && !result.isCorrect) return 'wrong';
    return '';
  }

  letterOf(i: number): string {
    return ['A', 'B', 'C', 'D'][i];
  }

  discountLabel(c: CouponConfigResponseDTO): string {
    return c.discountType === 'PERCENTAGE'
      ? `Giảm ${c.discountValue}%`
      : `Giảm ${c.discountValue?.toLocaleString('vi-VN')}₫`;
  }

  get scoreArc(): string {
    const sc = this.gameResult?.correctCount ?? 0;
    const total = this.gameResult?.totalQuestions ?? 10;
    const r = 52;
    const circ = 2 * Math.PI * r;
    const offset = circ * (1 - sc / total);
    return offset.toFixed(2);
  }

  get scoreArcCirc(): string {
    return (2 * Math.PI * 52).toFixed(2);
  }

  trackByToast(_: number, t: Toast): number { return t.id; }
  trackByQ(_: number, q: QuestionResponseDTO): number { return q.id; }
  trackByCoupon(_: number, c: CouponConfigResponseDTO): number { return c.id; }
  trackByHistory(_: number, h: UserCoupon): number { return h.id; }
}