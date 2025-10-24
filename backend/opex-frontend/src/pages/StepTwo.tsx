import { useEffect, useState } from 'react';
import { upsertAppUser } from '../api/client';
import { Link } from 'react-router-dom';

export default function StepTwo() {
  const [userId, setUserId] = useState('');
  const [firstName, setFirstName] = useState('');
  const [lastName,  setLastName]  = useState('');
  const [birthDate, setBirthDate] = useState(''); // yyyy-MM-dd
  const [loading, setLoading] = useState(false);
  const [error,   setError]   = useState<string|null>(null);
  const [saved,   setSaved]   = useState<any>(null);

  useEffect(() => {
    setUserId(localStorage.getItem('userId') ?? '');
  }, []);

  const onSave = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!userId) { setError('UserId mancante: torna allo Step 1.'); return; }
    setError(null); setSaved(null);
    try {
      setLoading(true);
      const res = await upsertAppUser(userId, {
        ...(firstName ? { firstName } : {}),
        ...(lastName  ? { lastName  } : {}),
        ...(birthDate ? { birthDate } : {})
      });
      setSaved(res);
    } catch (err:any) {
      setError(err.message ?? 'Errore');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ padding:24, color:'#e5e7eb' }}>
      <h1>Step 2 — Profilo</h1>
      {!userId && <p style={{ color:'#fca5a5' }}>Manca l’User ID: <Link to="/">torna allo Step 1</Link></p>}
      <form onSubmit={onSave} style={{ display:'grid', gap:12, maxWidth:520 }}>
        <input placeholder="Nome" value={firstName} onChange={e=>setFirstName(e.target.value)} />
        <input placeholder="Cognome" value={lastName} onChange={e=>setLastName(e.target.value)} />
        <input placeholder="Data di nascita" type="date" value={birthDate} onChange={e=>setBirthDate(e.target.value)} />
        <button disabled={loading || !userId}>{loading?'Salvo…':'Salva'}</button>
      </form>
      {error && <pre style={{ color:'#fca5a5' }}>{error}</pre>}
      {saved && (<><h3>Salvato</h3><pre>{JSON.stringify(saved,null,2)}</pre></>)}
      <hr />
      <div style={{ display:'flex', gap:12 }}>
        <Link to="/">← Home</Link>
        <Link to="/onboarding-simple">Step 3 — Onboarding</Link>
      </div>
    </div>
  );
}
