// ─── Quiz Game Models ────────────────────────────────────────────────────────

export interface QuestionResponseDTO {
  id: number;
  content: string;
  answers: AnswerOptionDTO[];
}

export interface AnswerOptionDTO {
  id: number;
  content: string;
}

export interface UserAnswerDTO {
  questionId: number;
  answerId: number;
}

export interface StartGameRequestDTO {
  userId: number;
}

export interface StartGameResponseDTO {
  userId: number;
  remainingPlays: number;
  totalPlaysToday: number;
  questions: QuestionResponseDTO[];
}

export interface SubmitAnswerRequestDTO {
  userId: number;
  answers: UserAnswerDTO[];
}

export interface AnswerResultDTO {
  questionId: number;
  questionContent: string;
  selectedAnswerId: number;
  correctAnswerId: number;
  isCorrect: boolean;
}

export interface GameResultResponseDTO {
  sessionId: number;
  userId: number;
  score: number;
  totalPoints: number;
  correctCount: number;
  totalQuestions: number;
  answerResults: AnswerResultDTO[];
}

export interface DailyPlayInfo {
  userId: number;
  playDate: string;
  playCount: number;
  maxPlays: number;
  remainingPlays: number;
  canPlay: boolean;
}

export interface UserPointsInfo {
  userId: number;
  totalPoints: number;
}

// ─── Coupon Models ───────────────────────────────────────────────────────────

export interface CouponConfigResponseDTO {
  id: number;
  couponId: number;
  couponName: string;
  description: string;
  pointsRequired: number;
  quantity: number;
  status: string;
  canRedeem: boolean;
  discountType: 'PERCENTAGE' | 'FIXED';
  discountValue: number;
  endDate: string;
}

export interface RedeemCouponRequestDTO {
  userId: number;
  couponConfigId: number;
}

export interface RedeemCouponResponseDTO {
  userCouponId: number;
  userId: number;
  couponId: number;
  couponName: string;
  pointsSpent: number;
  remainingPoints: number;
  redeemedAt: string;
}

export interface UserCoupon {
  id: number;
  userId: number;
  couponId: number;
  couponConfigId: number;
  pointsSpent: number;
  redeemedAt: string;
}

// ─── UI State Models ─────────────────────────────────────────────────────────

export type GameScreen = 'intro' | 'playing' | 'result' | 'shop' | 'history';

export interface AnswerState {
  selected: number | null;
  correct: number | null;
  locked: boolean;
}

export interface PlanetConfig {
  name: string;
  color: string;
  icon: string;
}