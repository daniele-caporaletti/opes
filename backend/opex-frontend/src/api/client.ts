import type { DashboardSummaryDTO } from '../types/api';

const API_BASE = import.meta.env.VITE_API_BASE_URL as string;

async function http<T = any>(input: RequestInfo, init?: RequestInit): Promise<T> {
  const res = await fetch(input, {
    headers: { Accept: 'application/hal+json', ...(init?.headers ?? {}) },
    ...init
  });
  if (!res.ok) {
    const text = await res.text().catch(() => '');
    throw new Error(`${res.status} ${res.statusText} ${text}`.trim());
  }
  // 204 no content
  if (res.status === 204) return undefined as unknown as T;
  return res.json() as Promise<T>;
}

/**
 * Aggiorna parzialmente un AppUser con JSON Merge Patch.
 * PATCH /appUsers/{id}  Content-Type: application/merge-patch+json
 */
async function mergePatchAppUser(
  id: string,
  payload: Record<string, unknown>
) {
  return http(`${API_BASE}/appUsers/${encodeURIComponent(id)}`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/merge-patch+json' },
    body: JSON.stringify(payload)
  });
}

/**
 * Crea un AppUser (se non esiste).
 * POST /appUsers  body: { id, ... }
 */
async function createAppUser(
  id: string,
  payload: Record<string, unknown>
) {
  return http(`${API_BASE}/appUsers`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ id, ...payload })
  });
}

/**
 * Upsert robusto:
 *   1) PATCH /appUsers/{id}
 *   2) se 404 => POST /appUsers
 */
export async function upsertAppUser(
  id: string,
  data: Partial<{
    email: string;
    firstName: string;
    lastName: string;
    birthDate: string; // yyyy-MM-dd
    sparkSelfRecognition:
      'OK_SAVE_MORE'|'STRESSED_P2P'|'ENOUGH_BUT_DISAPPEARS'|'GOAL_NO_PLAN'|null;
    emotionalGoal:
      'TRAVEL'|'APARTMENT'|'PAY_OFF_DEBT'|'SAVE_BUFFER'|'OTHER'|null;
    emotionalGoalOther: string|null;
    currentSituation: 'STUDENT'|'FULL_TIME'|'STUDY_AND_WORK'|null;
    monthlyIncome: 'RANGE_0_300'|'RANGE_300_700'|'RANGE_700_1500'|'OVER_1500'|null;
  }>
) {
  try {
    return await mergePatchAppUser(id, data as Record<string, unknown>);
  } catch (e: any) {
    // se non esiste, crealo
    if (String(e.message).startsWith('404')) {
      return await createAppUser(id, data as Record<string, unknown>);
    }
    throw e;
  }
}

/** Home summary (dev, senza JWT) */
export async function fetchHomeSummaryDev(userId: string): Promise<DashboardSummaryDTO> {
  return http(`${API_BASE}/home/summary/dev?userId=${encodeURIComponent(userId)}`);
}
