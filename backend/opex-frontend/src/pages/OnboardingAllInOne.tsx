import { useEffect, useState } from 'react';
import { createAppUser, patchAppUserProfile, patchAppUserOnboarding } from '../api/client';

type Spark = 'OK_SAVE_MORE'|'STRESSED_P2P'|'ENOUGH_BUT_DISAPPEARS'|'GOAL_NO_PLAN'|null;
type Goal  = 'TRAVEL'|'APARTMENT'|'PAY_OFF_DEBT'|'SAVE_BUFFER'|'OTHER'|null;
type Sit   = 'STUDENT'|'FULL_TIME'|'STUDY_AND_WORK'|null;
type Inc   = 'RANGE_0_300'|'RANGE_300_700'|'RANGE_700_1500'|'OVER_1500'|null;

export default function OnboardingAllInOne() {
  const [userId, setUserId] = useState('');
  const [email, setEmail] = useState('');

  const [firstName, setFirstName] = useState('');
  const [lastName,  setLastName]  = useState('');
  const [birthDate, setBirthDate] = useState('');

  const [spark, setSpark] = useState<Spark>(null);
  const [goal,  setGoal]  = useState<Goal>(null);
  const [goalOther, setGoalOther] = useState('');
  const [sit, setSit] = useState<Sit>(null);
  const [inc, setInc] = useState<Inc>(null);

  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string|null>(null);
  const [res, setRes] = useState<any>(null);

  useEffect(() => {
    setUserId(localStorage.getItem('userId') ?? '');
    setEmail(localStorage.getItem('email') ?? '');
  }, []);

  async function onCreateUser() {
    const id = userId.trim();
    const mail = email.trim();
    if (!id || !mail) { setError('Inserisci userId ed email'); return; }
    setError(null); setRes(null); setBusy(true);
    try {
      const out = await createAppUser(id, mail); // PUT /appUsers?userId=...  body:{email}
      localStorage.setItem('userId', id);
      localStorage.setItem('email', mail);
      setRes(out ?? { ok: true });
    } catch (e: any) { setError(e.message ?? 'Errore'); }
    finally { setBusy(false); }
  }

  async function onSaveProfile() {
    const id = userId.trim();
    if (!id) { setError('UserId mancante'); return; }
    const payload: any = {};
    if (firstName) payload.firstName = firstName;
    if (lastName)  payload.lastName  = lastName;
    if (birthDate) payload.birthDate = birthDate;
    if (!Object.keys(payload).length) { setError('Niente da salvare'); return; }

    setError(null); setRes(null); setBusy(true);
    try {
      const out = await patchAppUserProfile(id, payload); // PATCH /appUsers/profile
      setRes(out ?? { ok: true });
    } catch (e: any) { setError(e.message ?? 'Errore'); }
    finally { setBusy(false); }
  }

  async function onSaveOnboarding() {
    const id = userId.trim();
    if (!id) { setError('UserId mancante'); return; }
    setError(null); setRes(null); setBusy(true);
    try {
      const out = await patchAppUserOnboarding(id, {
        sparkSelfRecognition: spark,
        emotionalGoal: goal,
        emotionalGoalOther: goal === 'OTHER' ? (goalOther || null) : null,
        currentSituation: sit,
        monthlyIncome: inc
      }); // PATCH /appUsers/onboarding
      setRes(out ?? { ok: true });
    } catch (e: any) { setError(e.message ?? 'Errore'); }
    finally { setBusy(false); }
  }

  return (
    <div style={{ padding: 16, color: '#001846ff' }}>
      <h1>Onboarding — Pagina Unica</h1>

      <section style={{ marginTop: 12 }}>
        <h3>1) Crea utente (PUT con email)</h3>
        <input placeholder="User ID" value={userId} onChange={e=>setUserId(e.target.value)} />
        <input placeholder="Email" type="email" value={email} onChange={e=>setEmail(e.target.value)} />
        <button onClick={onCreateUser} disabled={busy}>{busy ? '...' : 'Crea utente'}</button>
      </section>

      <section style={{ marginTop: 12 }}>
        <h3>2) Profilo (PATCH /appUsers/profile)</h3>
        <input placeholder="Nome" value={firstName} onChange={e=>setFirstName(e.target.value)} />
        <input placeholder="Cognome" value={lastName} onChange={e=>setLastName(e.target.value)} />
        <input placeholder="Data di nascita" type="date" value={birthDate} onChange={e=>setBirthDate(e.target.value)} />
        <button onClick={onSaveProfile} disabled={busy}>{busy ? '...' : 'Salva Profilo'}</button>
      </section>

      <section style={{ marginTop: 12 }}>
        <h3>3) Onboarding (PATCH /appUsers/onboarding)</h3>
        <div style={{ display:'grid', gap:6, maxWidth:420 }}>
          <select value={spark ?? ''} onChange={e=>setSpark((e.target.value || null) as Spark)}>
            <option value="">Spark (skip)</option>
            <option value="OK_SAVE_MORE">OK_SAVE_MORE</option>
            <option value="STRESSED_P2P">STRESSED_P2P</option>
            <option value="ENOUGH_BUT_DISAPPEARS">ENOUGH_BUT_DISAPPEARS</option>
            <option value="GOAL_NO_PLAN">GOAL_NO_PLAN</option>
          </select>

          <select value={goal ?? ''} onChange={e=>setGoal((e.target.value || null) as Goal)}>
            <option value="">Goal (skip)</option>
            <option value="TRAVEL">TRAVEL</option>
            <option value="APARTMENT">APARTMENT</option>
            <option value="PAY_OFF_DEBT">PAY_OFF_DEBT</option>
            <option value="SAVE_BUFFER">SAVE_BUFFER</option>
            <option value="OTHER">OTHER</option>
          </select>
          {goal === 'OTHER' && (
            <input placeholder="Specifica il tuo obiettivo…" value={goalOther} onChange={e=>setGoalOther(e.target.value)} />
          )}

          <select value={sit ?? ''} onChange={e=>setSit((e.target.value || null) as Sit)}>
            <option value="">Situation (skip)</option>
            <option value="STUDENT">STUDENT</option>
            <option value="FULL_TIME">FULL_TIME</option>
            <option value="STUDY_AND_WORK">STUDY_AND_WORK</option>
          </select>

          <select value={inc ?? ''} onChange={e=>setInc((e.target.value || null) as Inc)}>
            <option value="">Monthly income (skip)</option>
            <option value="RANGE_0_300">RANGE_0_300</option>
            <option value="RANGE_300_700">RANGE_300_700</option>
            <option value="RANGE_700_1500">RANGE_700_1500</option>
            <option value="OVER_1500">OVER_1500</option>
          </select>
        </div>

        <button onClick={onSaveOnboarding} disabled={busy} style={{ marginTop: 6 }}>
          {busy ? '...' : 'Salva Onboarding'}
        </button>
      </section>

      {error && <pre style={{ color:'#fca5a5', whiteSpace:'pre-wrap' }}>{error}</pre>}
      {res && (<><h4>Response</h4><pre>{JSON.stringify(res, null, 2)}</pre></>)}
    </div>
  );
}
