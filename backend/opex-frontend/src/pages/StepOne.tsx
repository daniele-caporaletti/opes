import { useState } from 'react';
import { upsertAppUser, fetchHomeSummaryDev } from '../api/client';
import type { DashboardSummaryDTO } from '../types/api';
import { Link } from 'react-router-dom';

export default function StepOne() {
  const [userId, setUserId] = useState(localStorage.getItem('userId') ?? '');
  const [email, setEmail]   = useState(localStorage.getItem('email') ?? '');
  const [loading, setLoading] = useState(false);
  const [error, setError]   = useState<string|null>(null);
  const [userJson, setUserJson] = useState<any>(null);
  const [summary, setSummary]   = useState<DashboardSummaryDTO|null>(null);

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null); setUserJson(null); setSummary(null);

    const id = userId.trim();
    const mail = email.trim();
    if (!id || !mail) { setError('Inserisci sia userId che email.'); return; }

    try {
      setLoading(true);
      localStorage.setItem('userId', id);
      localStorage.setItem('email', mail);

      const u = await upsertAppUser(id, { email: mail });
      setUserJson(u);

      const dash = await fetchHomeSummaryDev(id);
      setSummary(dash);
    } catch (err:any) {
      setError(err.message ?? 'Errore');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ padding:24, color:'#e5e7eb' }}>
      <h1>Step 1 — Accesso mock</h1>
      <form onSubmit={onSubmit} style={{ display:'grid', gap:12, maxWidth:520 }}>
        <input
          placeholder="User ID (Keycloak sub)"
          value={userId}
          onChange={e=>setUserId(e.target.value)}
        />
        <input
          placeholder="Email (mock)"
          type="email"
          value={email}
          onChange={e=>setEmail(e.target.value)}
        />
        <button disabled={loading}>{loading ? 'Carico…' : 'Crea/Aggiorna + Summary'}</button>
      </form>

      {error && <pre style={{ color:'#fca5a5' }}>{error}</pre>}
      {userJson && (<><h3>Utente</h3><pre>{JSON.stringify(userJson,null,2)}</pre></>)}
      {summary && (<><h3>Dashboard</h3><pre>{JSON.stringify(summary,null,2)}</pre></>)}

      <hr />
      <div style={{ display:'flex', gap:12 }}>
        <Link to="/profile">Step 2 — Profilo</Link>
        <Link to="/onboarding-simple">Step 3 — Onboarding</Link>
      </div>
    </div>
  );
}
