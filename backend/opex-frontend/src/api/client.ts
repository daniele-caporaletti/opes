// src/api/client.ts
import type { DashboardSummaryDTO } from '../types/api';

/* =========================================================
 * Base
 * =======================================================*/
const API_BASE = import.meta.env.VITE_API_BASE_URL as string;

/** fetch helper tollerante a body vuoti / content-type sbagliato */
async function http<T = any>(input: RequestInfo, init?: RequestInit): Promise<T> {
  const res = await fetch(input, {
    headers: { Accept: 'application/json', ...(init?.headers ?? {}) },
    ...init
  });
  if (!res.ok) {
    const text = await res.text().catch(() => '');
    throw new Error(`${res.status} ${res.statusText} ${text}`.trim());
  }
  const raw = await res.text().catch(() => '');
  if (!raw) return undefined as unknown as T;

  const ct = (res.headers.get('content-type') || '').toLowerCase();
  if (ct.includes('application/json')) {
    try { return JSON.parse(raw) as T; } catch { /* fallthrough */ }
  }
  try { return JSON.parse(raw) as T; } catch { /* not json */ }
  return raw as unknown as T;
}

/* =========================================================
 * Types comuni
 * =======================================================*/
export type TotalBalanceAccountItem = {
  accountId: string;
  name: string;
  balance: string;
  asOf: string;
};

export type TotalBalanceResponse = {
  currency: string;        // "EUR"
  total: string;           // "1234.56"
  accounts?: TotalBalanceAccountItem[] | null;
  meta?: { accounts: number; withSnapshot: number; latestAt?: string | null };
};

/** Weekly Summary (endpoint dedicato) */
export type WeeklySummaryResponse = {
  period?: { year: number; week: number; from: string; to: string } | null;
  totals?: { income: string; expenses: string; net: string } | null;
  highlights?: Array<{
    type: 'merchant' | 'category' | 'tag' | 'account';
    key: string;
    amount: string;   // negativo per uscite
    count: number;
    oneOff: boolean;
  }>;
};

/** Spending Snapshot (endpoint generico) */
export type SpendingSnapshotItem = {
  type: 'transaction' | 'bucket';
  groupBy: 'merchant' | 'category' | 'tag' | 'account';
  key: string;
  label: string;
  amount: string;                // negativo per uscite
  date?: string | null;
  count?: number | null;
  oneOff?: boolean | null;
  sharePct?: number | null;
  trend?: { delta: string; pct: number | null } | null;
};

export type SpendingSnapshotResponse = {
  mode: 'last' | 'top';
  period: { type: string; from: string; to: string };
  totals?: { income: string; expenses: string; net: string } | null;
  items: SpendingSnapshotItem[];
  meta?: { txCount30d: number };
};

/** Totals (Income/Expenses) */
export type TotalBreakdownRow = {
  key: string;     // bucket (categoria/merchant/tag/account/provider)
  amount: string;  // somma nel periodo (pos per income, neg per expenses)
  count: number;   // numero di transazioni nel bucket
};

export type TotalBreakdownResponse = {
  currency: 'EUR';
  total: string;  // somma complessiva
  groupBy: 'category' | 'merchant' | 'tag' | 'account' | 'provider';
  breakdown: TotalBreakdownRow[];
};

export type TxRow = { id: string; date: string; amount: string; description: string };
export type TransactionsPageResponse = {
  data: TxRow[];
  page: number;
  pageSize: number;
  nextCursor?: string | null;
};

/* =========================================================
 * AppUsers (Onboarding)
 * =======================================================*/

/** CREA utente: PUT /appUsers?userId={id}  body: { email } */
export async function createAppUser(id: string, email: string) {
  const url = `${API_BASE}/appUsers?userId=${encodeURIComponent(id)}`;
  return http(url, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email })
  });
}

/** PATCH profilo: PATCH /appUsers/profile?userId={id}  body: { firstName?, lastName?, birthDate? } */
export async function patchAppUserProfile(id: string, payload: {
  firstName?: string; lastName?: string; birthDate?: string;
}) {
  const url = `${API_BASE}/appUsers/profile?userId=${encodeURIComponent(id)}`;
  return http(url, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/merge-patch+json' },
    body: JSON.stringify(payload ?? {})
  });
}

/** PATCH onboarding: PATCH /appUsers/onboarding?userId={id} */
export async function patchAppUserOnboarding(id: string, payload: {
  sparkSelfRecognition?: 'OK_SAVE_MORE'|'STRESSED_P2P'|'ENOUGH_BUT_DISAPPEARS'|'GOAL_NO_PLAN'|null;
  emotionalGoal?: 'TRAVEL'|'APARTMENT'|'PAY_OFF_DEBT'|'SAVE_BUFFER'|'OTHER'|null;
  emotionalGoalOther?: string|null;
  currentSituation?: 'STUDENT'|'FULL_TIME'|'STUDY_AND_WORK'|null;
  monthlyIncome?: 'RANGE_0_300'|'RANGE_300_700'|'RANGE_700_1500'|'OVER_1500'|null;
}) {
  const url = `${API_BASE}/appUsers/onboarding?userId=${encodeURIComponent(id)}`;
  return http(url, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/merge-patch+json' },
    body: JSON.stringify(payload ?? {})
  });
}

/** (opzionale) Home summary dev */
export async function fetchHomeSummaryDev(userId: string): Promise<DashboardSummaryDTO> {
  return http(`${API_BASE}/home/summary/dev?userId=${encodeURIComponent(userId)}`);
}

/* =========================================================
 * Analytics — Total Balance
 * =======================================================*/

/** GET /analytics/total-balance?userId=...&expand=accounts */
export async function fetchTotalBalance(userId: string, expandAccounts = false) {
  const qs = new URLSearchParams({
    userId,
    ...(expandAccounts ? { expand: 'accounts' } : {})
  }).toString();
  return http<TotalBalanceResponse>(`${API_BASE}/analytics/total-balance?${qs}`);
}

/* =========================================================
 * Analytics — Weekly Summary (endpoint dedicato)
 * =======================================================*/

/** GET /analytics/weekly */
export async function fetchWeeklySummary(
  userId: string,
  opts?: {
    when?: 'last'|'current';
    year?: number;            // con week
    week?: number;            // 1..53
    groupBy?: 'merchant'|'category'|'tag'|'account';
    limit?: number;           // 1..10 (default 3 lato BE)
    expandHighlights?: boolean; // -> expand=highlights
  }
) {
  const p = new URLSearchParams({ userId });
  if (opts?.year && opts?.week) {
    p.set('year', String(opts.year));
    p.set('week', String(opts.week));
  } else if (opts?.when) {
    p.set('when', opts.when);
  }
  if (opts?.groupBy) p.set('groupBy', opts.groupBy);
  if (opts?.limit)   p.set('limit', String(Math.min(10, Math.max(1, opts.limit))));
  if (opts?.expandHighlights) p.set('expand', 'highlights');

  return http<WeeklySummaryResponse>(`${API_BASE}/analytics/weekly?${p.toString()}`);
}

/* =========================================================
 * Analytics — Spending Snapshot (endpoint generico)
 * =======================================================*/

/** GET /analytics/spending-snapshot */
export async function fetchSpendingSnapshot(
  userId: string,
  opts?: {
    period?: 'auto'|'week'|'month'|'year'|'custom';
    from?: string;  // se period=custom
    to?: string;    // se period=custom
    mode?: 'auto'|'last'|'top';
    groupBy?: 'merchant'|'category'|'tag'|'account';
    limit?: number;            // 1..10, default 3
    includeTotals?: boolean;   // -> expand=totals
  }
) {
  const p = new URLSearchParams({ userId });

  const period = opts?.period ?? 'auto';
  p.set('period', period);
  if (period === 'custom') {
    if (opts?.from) p.set('from', opts.from);
    if (opts?.to)   p.set('to',   opts.to);
  }
  if (opts?.mode)     p.set('mode', opts.mode);
  if (opts?.groupBy)  p.set('groupBy', opts.groupBy);
  if (opts?.limit)    p.set('limit', String(Math.min(10, Math.max(1, opts.limit))));
  if (opts?.includeTotals) p.set('expand', 'totals');

  return http<SpendingSnapshotResponse>(`${API_BASE}/analytics/spending-snapshot?${p.toString()}`);
}

/** Helper client-side: range ISO week (lun→dom) – NO UTC drift */
const pad2 = (n: number) => String(n).padStart(2, '0');
const toLocalISO = (d: Date) =>
  `${d.getFullYear()}-${pad2(d.getMonth() + 1)}-${pad2(d.getDate())}`;

/** Ritorna { from, to } per settimana 'current' o 'last' (lun→dom) */
export function weekRange(when: 'last'|'current') {
  const now = new Date();
  const d = new Date(now.getFullYear(), now.getMonth(), now.getDate()); // strip time
  const wd = d.getDay(); // 0..6 (0=dom)
  const diffToMon = wd === 0 ? -6 : 1 - wd;
  const monThis = new Date(d); monThis.setDate(d.getDate() + diffToMon);
  const mon = when === 'current' ? monThis : new Date(monThis.getTime() - 7 * 86400000);
  const sun = new Date(mon); sun.setDate(mon.getDate() + 6);
  return { from: toLocalISO(mon), to: toLocalISO(sun) };
}

/** Shortcut: Spending Snapshot per settimana (top merchant, 3 righe) */
export async function fetchSpendingSnapshotWeek(
  userId: string,
  opts?: { when?: 'last'|'current'; groupBy?: 'merchant'|'category'|'tag'|'account'; limit?: number; includeTotals?: boolean }
) {
  const { from, to } = weekRange(opts?.when ?? 'last');
  return fetchSpendingSnapshot(userId, {
    period: 'custom',
    from, to,
    mode: 'top',
    groupBy: opts?.groupBy ?? 'merchant',
    limit: Math.min(10, Math.max(1, opts?.limit ?? 3)),
    includeTotals: !!opts?.includeTotals
  });
}

/* =========================================================
 * Analytics — Totals (Income / Expenses)
 * =======================================================*/

/** GET /analytics/total-income */
export async function fetchTotalIncome(
  userId: string,
  opts?: {
    groupBy?: 'category' | 'merchant' | 'tag' | 'account' | 'provider';
    from?: string; // yyyy-MM-dd
    to?: string;   // yyyy-MM-dd
  }
) {
  const p = new URLSearchParams({ userId });
  if (opts?.groupBy) p.set('groupBy', opts.groupBy);
  if (opts?.from)    p.set('from', opts.from);
  if (opts?.to)      p.set('to', opts.to);
  return http<TotalBreakdownResponse>(`${API_BASE}/analytics/total-income?${p.toString()}`);
}

/** GET /analytics/total-expenses */
export async function fetchTotalExpenses(
  userId: string,
  opts?: {
    groupBy?: 'category' | 'merchant' | 'tag' | 'account' | 'provider';
    from?: string;
    to?: string;
  }
) {
  const p = new URLSearchParams({ userId });
  if (opts?.groupBy) p.set('groupBy', opts.groupBy);
  if (opts?.from)    p.set('from', opts.from);
  if (opts?.to)      p.set('to', opts.to);
  return http<TotalBreakdownResponse>(`${API_BASE}/analytics/total-expenses?${p.toString()}`);
}

/** GET /analytics/total-income/details */
export async function fetchIncomeDetails(
  userId: string,
  params: {
    groupBy: 'category' | 'merchant' | 'tag' | 'account' | 'provider';
    key: string;
    from?: string;
    to?: string;
    period?: 'day'|'week'|'month'|'year';
    anchorDate?: string; // yyyy-MM-dd
    page?: number;
    pageSize?: number;
  }
) {
  const p = new URLSearchParams({ userId, groupBy: params.groupBy, key: params.key });
  if (params.from)       p.set('from', params.from);
  if (params.to)         p.set('to', params.to);
  if (params.period)     p.set('period', params.period);
  if (params.anchorDate) p.set('anchorDate', params.anchorDate);
  if (params.page)       p.set('page', String(params.page));
  if (params.pageSize)   p.set('pageSize', String(params.pageSize));
  return http<TransactionsPageResponse>(`${API_BASE}/analytics/total-income/details?${p.toString()}`);
}

/** GET /analytics/total-expenses/details */
export async function fetchExpensesDetails(
  userId: string,
  params: {
    groupBy: 'category' | 'merchant' | 'tag' | 'account' | 'provider';
    key: string;
    from?: string;
    to?: string;
    period?: 'day'|'week'|'month'|'year';
    anchorDate?: string;
    page?: number;
    pageSize?: number;
  }
) {
  const p = new URLSearchParams({ userId, groupBy: params.groupBy, key: params.key });
  if (params.from)       p.set('from', params.from);
  if (params.to)         p.set('to', params.to);
  if (params.period)     p.set('period', params.period);
  if (params.anchorDate) p.set('anchorDate', params.anchorDate);
  if (params.page)       p.set('page', String(params.page));
  if (params.pageSize)   p.set('pageSize', String(params.pageSize));
  return http<TransactionsPageResponse>(`${API_BASE}/analytics/total-expenses/details?${p.toString()}`);
}
