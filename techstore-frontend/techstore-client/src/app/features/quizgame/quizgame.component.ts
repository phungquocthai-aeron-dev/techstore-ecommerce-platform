import {
  Component, OnInit, OnDestroy, ChangeDetectorRef, NgZone,
  ElementRef, ViewChild, AfterViewInit
} from '@angular/core';
import { Subject, forkJoin, interval } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import { GameService } from './game.service';
import { CouponService } from './couponGame.service';
import {
  AnswerOptionDTO, AnswerResultDTO, CouponConfigResponseDTO,
  DailyPlayInfo, GameResultResponseDTO, GameScreen,
  QuestionResponseDTO, UserAnswerDTO, UserCoupon
} from './models/game.model';
import { CustomerService } from '../user/customer.service';
import { DatePipe, NgClass, NgFor, NgIf } from '@angular/common';

interface Toast {
  id: number;
  message: string;
  type: 'success' | 'error' | 'info';
}

interface Planet {
  x: number; y: number;
  cfg: PlanetCfg;
  idx: number;
  unlocked: boolean;
  hovered: boolean;
  scale: number;
  glowAmt: number;
  rotAngle: number;
  rotSpeed: number;
  clickable: boolean;
  pulseT: number;
}

interface PlanetCfg {
  col: string; glow: string; ring: boolean; size: number; name: string; icon: string;
}

interface Ship {
  x: number; y: number; tx: number; ty: number;
  moving: boolean; angle: number; bobT: number;
}

@Component({
  selector: 'app-quiz-game',
  imports: [NgFor, NgIf, NgClass, DatePipe],
  standalone: true,
  templateUrl: './quizgame.component.html',
  styleUrls: ['./quizgame.component.css'],
})
export class QuizGameComponent implements OnInit, OnDestroy, AfterViewInit {
  @ViewChild('galaxyCanvas') galaxyCanvasRef!: ElementRef<HTMLCanvasElement>;
  @ViewChild('qpanel') qpanelRef!: ElementRef<HTMLDivElement>;

  private destroy$ = new Subject<void>();

  // ─── Auth ────────────────────────────────────────────────────────────
  userId = 0;

  // ─── Screens ─────────────────────────────────────────────────────────
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
  isLoadingGame = false;
  isSubmitting = false;
  gameResult: GameResultResponseDTO | null = null;

  // ─── Countdown timer ─────────────────────────────────────────────────
  timeLeft = 90;
  private timerSub?: ReturnType<typeof setInterval>;

  // ─── Galaxy canvas state ──────────────────────────────────────────────
  planets: Planet[] = [];
  ship: Ship = { x: 0, y: 0, tx: 0, ty: 0, moving: false, angle: 0, bobT: 0 };
  private animId?: number;
  private gc!: HTMLCanvasElement;
  private gctx!: CanvasRenderingContext2D;
  qPanelOpen = false;
  currentGalaxyQ: QuestionResponseDTO | null = null;
  galaxyAnswerLocked = false;
  galaxySelectedId: number | null = null;
  galaxyCorrectId: number | null = null;
  showHint = true;
  showSkipMsg = false;

  // ─── Result / shop ────────────────────────────────────────────────────
  availableCoupons: CouponConfigResponseDTO[] = [];
  couponHistory: UserCoupon[] = [];
  isLoadingCoupons = false;
  isRedeeming = false;
  redeemingId: number | null = null;

  // ─── Countdown to midnight ────────────────────────────────────────────
  countdownToMidnight = '';

  // ─── Toasts ───────────────────────────────────────────────────────────
  toasts: Toast[] = [];
  private toastCounter = 0;

  readonly PLANET_CONFIGS: PlanetCfg[] = [
    { col: '#38bdf8', glow: 'rgba(56,189,248,',  ring: true,  size: 34, name: 'Aquoris', icon: '🔵' },
    { col: '#f472b6', glow: 'rgba(244,114,182,', ring: false, size: 28, name: 'Rosara',  icon: '🌸' },
    { col: '#a78bfa', glow: 'rgba(167,139,250,', ring: true,  size: 32, name: 'Violex',  icon: '🟣' },
    { col: '#34d399', glow: 'rgba(52,211,153,',  ring: false, size: 30, name: 'Verdis',  icon: '🟢' },
    { col: '#fbbf24', glow: 'rgba(251,191,36,',  ring: true,  size: 36, name: 'Auris',   icon: '🌟' },
    { col: '#fb923c', glow: 'rgba(251,146,60,',  ring: false, size: 27, name: 'Pyron',   icon: '🔴' },
    { col: '#e879f9', glow: 'rgba(232,121,249,', ring: true,  size: 31, name: 'Neonx',   icon: '💜' },
    { col: '#22d3ee', glow: 'rgba(34,211,238,',  ring: false, size: 29, name: 'Cyros',   icon: '🔷' },
    { col: '#f87171', glow: 'rgba(248,113,113,', ring: true,  size: 33, name: 'Kraton',  icon: '❤️' },
    { col: '#a3e635', glow: 'rgba(163,230,53,',  ring: false, size: 28, name: 'Lumis',   icon: '💚' },
  ];

  constructor(
    private gameService: GameService,
    private couponService: CouponService,
    private customerService: CustomerService,
    private cdr: ChangeDetectorRef,
    private zone: NgZone,
  ) {}

  ngOnInit(): void {
    const user = this.customerService.currentUser;
    if (user) this.userId = user.id;
    this.customerService.currentUser$
      .pipe(takeUntil(this.destroy$))
      .subscribe(u => { if (u) this.userId = u.id; });
    this.loadMeta();
    this.startMidnightCountdown();
  }

  ngAfterViewInit(): void {}

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.clearTimer();
    if (this.animId) cancelAnimationFrame(this.animId);
    window.removeEventListener('resize', this.onResize);
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
  goTo(s: GameScreen): void {
    if (this.animId) { cancelAnimationFrame(this.animId); this.animId = undefined; }
    this.screen = s;
    if (s === 'shop') this.loadShopData();
    if (s === 'history') this.loadHistory();
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
            this.dailyInfo = {
              ...this.dailyInfo!,
              remainingPlays: res.result.remainingPlays,
              playCount: res.result.totalPlaysToday,
              canPlay: res.result.remainingPlays > 0,
            };
            this.screen = 'playing';
            this.cdr.detectChanges();
            // init canvas after view updates
            setTimeout(() => this.initGalaxyCanvas(), 50);
          }
        },
        error: () => {
          this.isLoadingGame = false;
          this.showToast('Không thể bắt đầu game, thử lại!', 'error');
        },
      });
  }

  // ─── Galaxy Canvas ────────────────────────────────────────────────────
  private initGalaxyCanvas(): void {
    this.gc = this.galaxyCanvasRef?.nativeElement;
    if (!this.gc) return;
    this.gctx = this.gc.getContext('2d')!;
    this.resizeGC();
    this.onResize = this.resizeGC.bind(this);
    window.addEventListener('resize', this.onResize);
    this.gc.addEventListener('click', this.onGalaxyClick.bind(this));
    this.gc.addEventListener('mousemove', this.onGalaxyHover.bind(this));
    this.showHint = true;
    this.qPanelOpen = false;
    this.currentGalaxyQ = null;
    if (this.animId) cancelAnimationFrame(this.animId);
    this.drawLoop();
  }

  private onResize: () => void = () => {};

  private resizeGC(): void {
    if (!this.gc) return;
    this.gc.width = window.innerWidth;
    this.gc.height = window.innerHeight;
    this.buildPlanets();
  }

  private buildPlanets(): void {
    const W = this.gc.width, H = this.gc.height;
    const n = this.questions.length;
    this.planets = this.PLANET_CONFIGS.map((cfg, i) => {
      const t = i / (n - 1);
      const x = W * 0.1 + t * (W * 0.8);
      const amp = H * 0.22;
      const y = H * 0.5 + Math.sin(t * Math.PI * 2.2 + 0.4) * amp + (i % 2 === 0 ? -30 : 30);
      return {
        x, y, cfg, idx: i,
        unlocked: false, hovered: false,
        scale: 1, glowAmt: 0,
        rotAngle: Math.random() * Math.PI * 2,
        rotSpeed: 0.003 + Math.random() * 0.005 * (Math.random() < 0.5 ? 1 : -1),
        clickable: i === 0,
        pulseT: Math.random() * Math.PI * 2,
      };
    });
    const fp = this.planets[0];
    this.ship = { x: fp.x - 50, y: fp.y - 40, tx: fp.x, ty: fp.y, moving: false, angle: 0, bobT: 0 };
  }

  private drawLoop(): void {
    if (!this.gctx) return;
    this.gctx.clearRect(0, 0, this.gc.width, this.gc.height);
    this.drawOrbitalPath();
    this.drawPlanets();
    this.drawShip();
    this.animateShip();
    this.animId = requestAnimationFrame(() => this.drawLoop());
  }

  private drawOrbitalPath(): void {
    const cx = this.gctx, pts = this.planets;
    if (!pts.length) return;
    cx.beginPath();
    cx.moveTo(pts[0].x, pts[0].y);
    for (let i = 1; i < pts.length; i++) {
      const prev = pts[i - 1], cur = pts[i];
      const mx = (prev.x + cur.x) / 2, my = (prev.y + cur.y) / 2;
      cx.quadraticCurveTo(prev.x, prev.y, mx, my);
    }
    cx.lineTo(pts[pts.length - 1].x, pts[pts.length - 1].y);
    cx.strokeStyle = 'rgba(56,189,248,.14)';
    cx.lineWidth = 1.5;
    cx.setLineDash([6, 10]);
    cx.stroke();
    cx.setLineDash([]);
  }

  private drawPlanets(): void {
    this.planets.forEach(p => {
      p.rotAngle += p.rotSpeed;
      p.pulseT += 0.025;
      const tg = p.unlocked ? 1 : p.hovered ? 0.6 : 0;
      p.glowAmt += (tg - p.glowAmt) * 0.08;
      const ts = p.hovered && p.clickable ? 1.18 : p.unlocked ? 1.08 : 1;
      p.scale += (ts - p.scale) * 0.1;
      this.drawPlanet(p);
    });
  }

  private drawPlanet(p: Planet): void {
    const cx = this.gctx;
    const { x, y, cfg, scale, glowAmt, rotAngle, unlocked, idx } = p;
    const r = cfg.size * scale;
    cx.save();
    cx.translate(x, y);
    if (glowAmt > 0.01) {
      const glow = cx.createRadialGradient(0, 0, r * 0.5, 0, 0, r * 2.8);
      glow.addColorStop(0, cfg.glow + glowAmt * 0.45 + ')');
      glow.addColorStop(1, cfg.glow + '0)');
      cx.beginPath(); cx.arc(0, 0, r * 2.8, 0, Math.PI * 2);
      cx.fillStyle = glow; cx.fill();
    }
    if (cfg.ring) {
      cx.save(); cx.rotate(rotAngle * 0.4); cx.scale(1, 0.3);
      cx.beginPath(); cx.arc(0, 0, r * 1.55, 0, Math.PI * 2);
      cx.strokeStyle = cfg.glow + (unlocked ? 0.5 : 0.18) + ')';
      cx.lineWidth = 4 * scale; cx.stroke(); cx.restore();
    }
    const bg = cx.createRadialGradient(-r * 0.3, -r * 0.3, r * 0.1, 0, 0, r);
    bg.addColorStop(0, cfg.col + 'ee'); bg.addColorStop(0.5, cfg.col + 'cc'); bg.addColorStop(1, '#020817');
    cx.beginPath(); cx.arc(0, 0, r, 0, Math.PI * 2); cx.fillStyle = bg; cx.fill();
    cx.save(); cx.clip(); cx.rotate(rotAngle);
    for (let i = -3; i <= 3; i++) {
      cx.beginPath(); cx.ellipse(0, i * r * 0.28, r * 0.9, r * 0.08, 0, 0, Math.PI * 2);
      cx.fillStyle = `rgba(255,255,255,${unlocked ? 0.06 : 0.03})`; cx.fill();
    }
    cx.restore();
    if (!unlocked) {
      cx.font = `${r * 0.55}px serif`; cx.textAlign = 'center'; cx.textBaseline = 'middle';
      cx.globalAlpha = 0.45; cx.fillStyle = '#fff'; cx.fillText('🔒', 0, 0); cx.globalAlpha = 1;
    } else {
      cx.font = `${r * 0.5}px serif`; cx.textAlign = 'center'; cx.textBaseline = 'middle';
      cx.globalAlpha = 0.7 + Math.sin(p.pulseT) * 0.2; cx.fillStyle = '#fff'; cx.fillText('⭐', 0, 0); cx.globalAlpha = 1;
    }
    cx.font = `600 ${Math.max(10, r * 0.35)}px 'Exo 2',sans-serif`;
    cx.textAlign = 'center';
    cx.fillStyle = `rgba(255,255,255,${unlocked ? 0.85 : 0.35})`;
    cx.fillText(cfg.name, 0, r + 14);
    if (p.clickable && !unlocked) {
      const pf = 0.4 + Math.sin(p.pulseT) * 0.35;
      cx.beginPath(); cx.arc(0, 0, r * 1.32 + Math.sin(p.pulseT) * 4, 0, Math.PI * 2);
      cx.strokeStyle = cfg.glow + pf + ')'; cx.lineWidth = 2; cx.stroke();
    }
    cx.beginPath(); cx.arc(-r * 0.65, -r * 0.65, r * 0.28, 0, Math.PI * 2);
    cx.fillStyle = 'rgba(2,8,23,.85)'; cx.fill();
    cx.font = `700 ${Math.max(9, r * 0.26)}px 'Orbitron',sans-serif`;
    cx.textAlign = 'center'; cx.textBaseline = 'middle';
    cx.fillStyle = cfg.col; cx.fillText(String(idx + 1), -r * 0.65, -r * 0.65);
    cx.restore();
  }

  private drawShip(): void {
    const s = this.ship;
    s.bobT += 0.04;
    const bob = Math.sin(s.bobT) * 3;
    this.gctx.save();
    this.gctx.translate(s.x, s.y + bob);
    this.gctx.rotate(s.angle);
    this.gctx.font = '28px serif';
    this.gctx.textAlign = 'center';
    this.gctx.textBaseline = 'middle';
    this.gctx.fillText('🚀', 0, 0);
    if (s.moving) {
      const trail = this.gctx.createRadialGradient(-18, 0, 0, -18, 0, 22);
      trail.addColorStop(0, 'rgba(56,189,248,.35)'); trail.addColorStop(1, 'rgba(56,189,248,0)');
      this.gctx.beginPath(); this.gctx.arc(-18, 0, 22, 0, Math.PI * 2);
      this.gctx.fillStyle = trail; this.gctx.fill();
    }
    this.gctx.restore();
  }

  private animateShip(): void {
    const s = this.ship;
    const dx = s.tx - s.x, dy = s.ty - s.y;
    const dist = Math.sqrt(dx * dx + dy * dy);
    if (dist > 2) { s.moving = true; s.angle = Math.atan2(dy, dx); s.x += dx * 0.065; s.y += dy * 0.065; }
    else { s.moving = false; s.x = s.tx; s.y = s.ty; }
  }

  private onGalaxyHover(e: MouseEvent): void {
    const rect = this.gc.getBoundingClientRect();
    const mx = e.clientX - rect.left, my = e.clientY - rect.top;
    let any = false;
    this.planets.forEach(p => {
      if (!p.clickable) { p.hovered = false; return; }
      const dx = mx - p.x, dy = my - p.y;
      p.hovered = Math.sqrt(dx * dx + dy * dy) < p.cfg.size * 1.4;
      if (p.hovered) any = true;
    });
    this.gc.style.cursor = any ? 'pointer' : 'default';
  }

  private onGalaxyClick(e: MouseEvent): void {
    if (this.qPanelOpen) return;
    const rect = this.gc.getBoundingClientRect();
    const mx = e.clientX - rect.left, my = e.clientY - rect.top;
    this.planets.forEach(p => {
      if (!p.clickable || p.unlocked) return;
      const dx = mx - p.x, dy = my - p.y;
      if (Math.sqrt(dx * dx + dy * dy) < p.cfg.size * 1.5) {
        this.ship.tx = p.x - 20; this.ship.ty = p.y - 35;
        this.zone.run(() => this.openGalaxyQuestion(p.idx));
      }
    });
  }

  openGalaxyQuestion(idx: number): void {
    this.currentQIndex = idx;
    this.currentGalaxyQ = this.questions[idx];
    this.galaxyAnswerLocked = false;
    this.galaxySelectedId = null;
    this.galaxyCorrectId = null;
    this.showSkipMsg = false;
    this.qPanelOpen = true;
    this.showHint = false;
    this.startTimer();
    this.cdr.markForCheck();
  }

  selectGalaxyAnswer(answerId: number): void {
    if (this.galaxyAnswerLocked) return;
    this.galaxySelectedId = answerId;
    this.galaxyAnswerLocked = true;
    this.clearTimer();
    this.userAnswers.push({ questionId: this.currentGalaxyQ!.id, answerId });

    // unlock planet + advance
    const idx = this.currentQIndex;
    this.planets[idx].unlocked = true;
    this.planets[idx].clickable = false;
    if (idx + 1 < this.planets.length) {
      this.planets[idx + 1].clickable = true;
      const np = this.planets[idx + 1];
      setTimeout(() => { this.ship.tx = np.x - 20; this.ship.ty = np.y - 35; }, 700);
    }

    setTimeout(() => {
      this.zone.run(() => {
        this.qPanelOpen = false;
        this.showHint = true;
        this.cdr.markForCheck();
        const isLast = idx === this.questions.length - 1;
        if (isLast) setTimeout(() => this.submitGame(), 600);
      });
    }, 1200);
  }

  private onTimerExpiredGalaxy(): void {
    if (!this.galaxyAnswerLocked) {
      this.galaxyAnswerLocked = true;
      this.showSkipMsg = true;
      const idx = this.currentQIndex;
      this.planets[idx].clickable = false;
      if (idx + 1 < this.planets.length) {
        this.planets[idx + 1].clickable = true;
        const np = this.planets[idx + 1];
        setTimeout(() => { this.ship.tx = np.x - 20; this.ship.ty = np.y - 35; }, 700);
      }
      setTimeout(() => {
        this.zone.run(() => {
          this.qPanelOpen = false;
          this.showHint = true;
          this.cdr.markForCheck();
          const isLast = idx === this.questions.length - 1;
          if (isLast) setTimeout(() => this.submitGame(), 600);
        });
      }, 900);
    }
  }

  getGalaxyAnswerClass(answerId: number): string {
    if (!this.galaxyAnswerLocked) return this.galaxySelectedId === answerId ? 'selected' : '';
    if (answerId === this.galaxySelectedId) return 'selected-locked';
    return '';
  }

  // ─── Submit ───────────────────────────────────────────────────────────
  private submitGame(): void {
    if (this.animId) { cancelAnimationFrame(this.animId); this.animId = undefined; }
    this.isSubmitting = true;
    this.screen = 'submitting' as any;
    this.cdr.markForCheck();
    this.gameService.submitGame({ userId: this.userId, answers: this.userAnswers })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: res => {
          this.isSubmitting = false;
          this.gameResult = res.result ?? null;
          this.userPoints = res.result?.totalPoints ?? this.userPoints;
          this.screen = 'result';
          this.loadMeta();
          this.cdr.markForCheck();
        },
        error: () => {
          this.isSubmitting = false;
          this.showToast('Lỗi khi nộp bài, thử lại!', 'error');
          this.screen = 'intro';
          this.cdr.markForCheck();
        },
      });
  }

  // ─── Timer ────────────────────────────────────────────────────────────
  private startTimer(): void {
    this.timeLeft = 90;
    this.clearTimer();
    this.timerSub = setInterval(() => {
      this.zone.run(() => {
        this.timeLeft--;
        if (this.timeLeft <= 0) { this.clearTimer(); this.onTimerExpiredGalaxy(); }
        this.cdr.markForCheck();
      });
    }, 1000);
  }

  private clearTimer(): void {
    if (this.timerSub) { clearInterval(this.timerSub); this.timerSub = undefined; }
  }

  get timerPercent(): number { return (this.timeLeft / 90) * 100; }
  get timerDanger(): boolean { return this.timeLeft <= 10; }

  // ─── Shop ─────────────────────────────────────────────────────────────
  private loadShopData(): void {
    this.isLoadingCoupons = true;
    this.couponService.loadAvailableCoupons(this.userId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: res => { this.isLoadingCoupons = false; this.availableCoupons = res.result ?? []; },
        error: () => { this.isLoadingCoupons = false; this.showToast('Không tải được cửa hàng', 'error'); },
      });
  }

  redeemCoupon(coupon: CouponConfigResponseDTO): void {
    if (!coupon.canRedeem || this.isRedeeming) return;
    this.isRedeeming = true; this.redeemingId = coupon.id;
    this.couponService.redeemCoupon(this.userId, coupon.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: res => {
          this.isRedeeming = false; this.redeemingId = null;
          if (res.result) {
            this.userPoints = res.result.remainingPoints;
            this.showToast(`✅ Đổi thành công: ${res.result.couponName}! Còn ${res.result.remainingPoints} điểm`, 'success');
            this.loadShopData();
          }
        },
        error: () => { this.isRedeeming = false; this.redeemingId = null; this.showToast('Đổi coupon thất bại!', 'error'); },
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
      const now = new Date(), midnight = new Date();
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
  removeToast(id: number): void { this.toasts = this.toasts.filter(t => t.id !== id); }

  // ─── Helpers ─────────────────────────────────────────────────────────
  get resultLevel(): { label: string; desc: string; icon: string; color: string } {
    const sc = this.gameResult?.correctCount ?? 0;
    if (sc === 10) return { label: 'TECH MASTER 🏆', desc: 'Hoàn hảo! Bạn là bậc thầy thiên hà!', icon: '🏆', color: '#f59e0b' };
    if (sc >= 8)   return { label: 'GEAR EXPERT ⚡', desc: 'Xuất sắc! Tiếp tục chinh phục vũ trụ!', icon: '⚡', color: '#818cf8' };
    if (sc >= 5)   return { label: 'TECH LOVER 💡', desc: 'Khá tốt! Còn nhiều hành tinh chờ đón!', icon: '🌟', color: '#38bdf8' };
    return              { label: 'TECH NEWBIE 🔰', desc: 'Mới bắt đầu – luyện tập thêm nhé!', icon: '🛸', color: '#64748b' };
  }

  get scoreArc(): string {
    const sc = this.gameResult?.correctCount ?? 0;
    const total = this.gameResult?.totalQuestions ?? 10;
    const r = 52, circ = 2 * Math.PI * r;
    return (circ * (1 - sc / total)).toFixed(2);
  }
  get scoreArcCirc(): string { return (2 * Math.PI * 52).toFixed(2); }

  letterOf(i: number): string { return ['A', 'B', 'C', 'D'][i]; }
  discountLabel(c: CouponConfigResponseDTO): string {
    return c.discountType === 'PERCENT' ? `Giảm ${c.discountValue}%` : `Giảm ${c.discountValue?.toLocaleString('vi-VN')}₫`;
  }
  trackByToast(_: number, t: Toast): number { return t.id; }
  trackByQ(_: number, q: QuestionResponseDTO): number { return q.id; }
  trackByAnswer(_: number, a: AnswerOptionDTO): number { return a.id; }
  trackByCoupon(_: number, c: CouponConfigResponseDTO): number { return c.id; }
  trackByHistory(_: number, h: UserCoupon): number { return h.id; }
}