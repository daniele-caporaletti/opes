export interface AmountPairDTO { income: string; expenses: string; }

export interface WeeklySummaryDTO {
  weekStart: string;
  weekEnd: string;
  income: string;
  expenses: string;
}

export interface SpendingItemDTO {
  label: string;
  amount: string;
  date?: string | null;
  categoryId?: string | null;
  merchantId?: string | null;
}

export interface SpendingSnapshotDTO {
  mode: 'LAST_EXPENSES' | 'TOP_CATEGORIES';
  items: SpendingItemDTO[];
}

export interface GoalRecapItemDTO {
  goalId: string;
  title: string;
  targetAmount: string;
  savedAmount: string;
  progressPercent: number;
}

export interface DashboardSummaryDTO {
  totalBalance: string;
  headlineMessage: string;
  weeklySummary: WeeklySummaryDTO;
  totals: AmountPairDTO;
  spendingSnapshot: SpendingSnapshotDTO;
  goalRecap: GoalRecapItemDTO[];
}
