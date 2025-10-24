const API_BASE = import.meta.env.VITE_API_BASE_URL as string;

export type HalLink = { href: string };
export type HalResource<T> = T & { _links: Record<string, HalLink> };
export type HalCollection<T> = { _embedded?: Record<string, HalResource<T>[]> };

export type Question = { id?: string; code: string; text: string; orderIndex?: number; };
export type Option   = { id?: string; label: string; value: string; orderIndex?: number; };

const extractIdFromSelf = (href: string) => href.split('/').pop()!;

export async function fetchQuestionByCode(code: string): Promise<HalResource<Question>> {
  const url = `${API_BASE}/onboardingQuestions/search/findByCode?code=${encodeURIComponent(code)}`;
  const res = await fetch(url, { headers: { Accept: 'application/json' } });
  if (!res.ok) throw new Error(`findByCode ${code} failed: ${res.status} ${await res.text()}`);
  const json = await res.json();
  return json as HalResource<Question>;
}

export async function fetchOptionsByQuestionId(questionId: string): Promise<HalResource<Option>[]> {
  const url = `${API_BASE}/onboardingOptions/search/findByQuestionId?questionId=${encodeURIComponent(questionId)}`;
  const res = await fetch(url, { headers: { Accept: 'application/json' } });
  if (!res.ok) throw new Error(`findByQuestionId failed: ${res.status} ${await res.text()}`);
  const json = (await res.json()) as HalCollection<Option>;
  const key = Object.keys(json._embedded ?? {})[0];
  return (key ? json._embedded![key] : []) as HalResource<Option>[];
}

export async function postOnboardingResponse(params: {
  userId: string;
  questionId: string;
  optionId?: string;          // se skip o other senza scelta, ometti
  freeText?: string | null;   // per "Other"
  skipped?: boolean;
}) {
  const body: any = {
    user: `${API_BASE}/appUsers/${encodeURIComponent(params.userId)}`,
    question: `${API_BASE}/onboardingQuestions/${encodeURIComponent(params.questionId)}`,
    skipped: !!params.skipped,
    answeredAt: new Date().toISOString()
  };
  if (params.optionId) body.option = `${API_BASE}/onboardingOptions/${encodeURIComponent(params.optionId)}`;
  if (params.freeText) body.freeText = params.freeText;

  const res = await fetch(`${API_BASE}/onboardingResponses`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
    body: JSON.stringify(body)
  });
  if (!res.ok) throw new Error(`POST response failed: ${res.status} ${await res.text()}`);
  return res.json();
}

// Utility per leggere l'id da una risorsa HAL
export function getIdFromResource(r: HalResource<any>): string {
  return extractIdFromSelf(r._links.self.href);
}
