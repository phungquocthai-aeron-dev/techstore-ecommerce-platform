export type PeriodType = 'TODAY' | 'MONTHLY' | 'QUARTERLY' | 'YEARLY' | 'CUSTOM';

export interface CostStatPoint {
  period: string;
  totalCost: number;
  totalQuantity: number;
  transactionCount: number;
}

export interface InboundCostStatResponse {
  data: CostStatPoint[];
  grandTotalCost: number;
  grandTotalQuantity: number;
  grandTransactionCount: number;
  periodType: PeriodType;
  fromDate: string;
  toDate: string;
}