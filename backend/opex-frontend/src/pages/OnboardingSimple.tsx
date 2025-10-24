import { useEffect, useState } from 'react';
import { upsertAppUser } from '../api/client';
import { Link, useNavigate } from 'react-router-dom';

type Spark = 'OK_SAVE_MORE'|'STRESSED_P2P'|'ENOUGH_BUT_DISAPPEARS'|'GOAL_NO_PLAN'|null;
type Goal  = 'TRAVEL'|'APARTMENT'|'PAY_OFF_DEBT'|'SAVE_BUFFER'|'OTHER'|null;
type Sit   = 'STUDENT'|'FULL_TIME'|'STUDY_AND_WORK'|null;
type Inc   = 'RANGE_0_300'|'RANGE_300_700'|'RANGE_700_1500'|'OVER_1500'|null;

export default function OnboardingSimple() {
  const nav = useNavigate();
  const [userId, setUserId] = useState('');

  const [spark, setSpark] = useState<Spark>(null);
  const [goal, setGoal]   = useState<Goal>(null);
  const [goalOther, setGoalOther] = useState('');
  const [sit, setSit]     = useState<Sit>(null);
  const [inc, setInc]     = useState<Inc>(null);

  const [saving, setSaving] = useState(false);
  const [error, setError]   = useState<string|null>(null);
  const [saved, setSaved]   = useState<any>(null);

  useEffect(() => {
    setUserId(localStorage.getItem('userId') ?? '');
  }, []);

  async function onSave(e: React.FormEvent) {
    e.preventDefault();
    if (!userId) { setError('UserId mancante: torna allo Step 1.'); return; }
    setError(null); setSaving(true); setSaved(null);
    try {
      const res = await upsertAppUser(userId, {
        sparkSelfRecognition: spark,
        emotionalGoal: goal,
        emotionalGoalOther: goal === 'OTHER' ? (goalOther || null) : null,
        currentSituation: sit,
        monthlyIncome: inc
      });
      setSaved(res);
      nav('/'); // o dove preferisci
    } catch (err:any) {
      setError(err.message ?? 'Errore');
    } finally { setSaving(false); }
  }

  return (
    <div style={{ padding:24, color:'#e5e7eb' }}>
      <h1>Step 3 — Onboarding (semplice)</h1>
      {!userId && <p style={{ color:'#fca5a5' }}>Manca l’User ID: <Link to="/">torna allo Step 1</Link></p>}

      <form onSubmit={onSave} style={{ display:'grid', gap:18, maxWidth:720 }}>
        <section>
          <h3>Spark Self-Recognition</h3>
          {[
            ['OK_SAVE_MORE',"I’m doing okay, but want to save more"],
            ['STRESSED_P2P',"I’m stressed — I live paycheck to paycheck"],
            ['ENOUGH_BUT_DISAPPEARS',"I make enough, but it disappears"],
            ['GOAL_NO_PLAN',"I want to reach a goal, but no idea how"],
          ].map(([v,l])=>(
            <label key={v} style={{ display:'flex', gap:10, marginTop:6 }}>
              <input type="radio" name="spark" checked={spark===v} onChange={()=>setSpark(v as Spark)} />
              <span>{l}</span>
            </label>
          ))}
          <button type="button" onClick={()=>setSpark(null)} style={{ marginTop:8 }}>Skip</button>
        </section>

        <section>
          <h3>Plant an Emotional Goal</h3>
          {[
            ['TRAVEL','Travel'],['APARTMENT','Apartment'],['PAY_OFF_DEBT','Pay off debt'],
            ['SAVE_BUFFER','Save buffer'],['OTHER','Other']
          ].map(([v,l])=>(
            <label key={v} style={{ display:'flex', gap:10, marginTop:6 }}>
              <input type="radio" name="goal" checked={goal===v} onChange={()=>setGoal(v as Goal)} />
              <span>{l}</span>
            </label>
          ))}
          {goal==='OTHER' && (
            <input
              placeholder="Specifica il tuo obiettivo…"
              value={goalOther}
              onChange={e=>setGoalOther(e.target.value)}
              style={{ marginTop:8, width:'100%', padding:'10px 12px',
                       border:'1px solid #334155', borderRadius:10, background:'#0b1220', color:'#e5e7eb' }}
            />
          )}
          <button type="button" onClick={()=>{ setGoal(null); setGoalOther(''); }} style={{ marginTop:8 }}>Skip</button>
        </section>

        <section>
          <h3>Current Situation</h3>
          {[
            ['STUDENT','🎓 Student'],
            ['FULL_TIME','💼 Full-time worker'],
            ['STUDY_AND_WORK','📚💼 Study & work']
          ].map(([v,l])=>(
            <label key={v} style={{ display:'flex', gap:10, marginTop:6 }}>
              <input type="radio" name="sit" checked={sit===v} onChange={()=>setSit(v as Sit)} />
              <span>{l}</span>
            </label>
          ))}
          <button type="button" onClick={()=>setSit(null)} style={{ marginTop:8 }}>Skip</button>
        </section>

        <section>
          <h3>Monthly Income</h3>
          {[
            ['RANGE_0_300','€0–€300'],
            ['RANGE_300_700','€300–€700'],
            ['RANGE_700_1500','€700–€1,500'],
            ['OVER_1500','Over €1,500']
          ].map(([v,l])=>(
            <label key={v} style={{ display:'flex', gap:10, marginTop:6 }}>
              <input type="radio" name="inc" checked={inc===v} onChange={()=>setInc(v as Inc)} />
              <span>{l}</span>
            </label>
          ))}
          <button type="button" onClick={()=>setInc(null)} style={{ marginTop:8 }}>Skip</button>
        </section>

        <div style={{ display:'flex', gap:10, marginTop:8 }}>
          <button disabled={saving || !userId}>
            {saving ? 'Salvo…' : 'Salva Onboarding'}
          </button>
          <Link to="/" style={{ marginLeft:'auto' }}>Home</Link>
        </div>

        {error && <pre style={{ color:'#fca5a5' }}>{error}</pre>}
        {saved && <pre>{JSON.stringify(saved,null,2)}</pre>}
      </form>
    </div>
  );
}
