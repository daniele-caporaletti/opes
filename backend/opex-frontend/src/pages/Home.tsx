// src/pages/Home.tsx
import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';

import {
  fetchTotalBalance, type TotalBalanceResponse,
  fetchSpendingSnapshotWeek, type SpendingSnapshotResponse,
  fetchWeeklySummary, type WeeklySummaryResponse, // opzionale: non usato, ma lo teniamo se vuoi switchare
  fetchTotalIncome, fetchTotalExpenses,
  fetchIncomeDetails, fetchExpensesDetails,
  type TotalBreakdownResponse, type TransactionsPageResponse,
  fetchSpendingSnapshot, type SpendingSnapshotItem
} from '../api/client';

// --- helpers valuta ---
function fmtEUR(n: number) {
  return new Intl.NumberFormat('it-IT', { style:'currency', currency:'EUR' }).format(n);
}
function fmtEURAbs(amount: string) {
  const x = Number(amount);
  if (Number.isNaN(x)) return amount;
  return fmtEUR(Math.abs(x));
}
function fmtEURSigned(amount: string, absForExpenses = false) {
  const x = Number(amount);
  if (Number.isNaN(x)) return amount;
  return fmtEUR(absForExpenses ? Math.abs(x) : x);
}

export default function Home() {
  // ---- common ----
  const [userId, setUserId] = useState('');

  // ---- Total Balance ----
  const [expandAccounts, setExpandAccounts] = useState(true);
  const [tbLoading, setTbLoading] = useState(false);
  const [tbErr, setTbErr] = useState<string|null>(null);
  const [tb, setTb] = useState<TotalBalanceResponse|undefined>();

  // ---- Weekly card (Spending Snapshot Week) ----
  const [wsWhen, setWsWhen] = useState<'last'|'current'>('last');
  const [wsIncludeTotals, setWsIncludeTotals] = useState(true);
  const [wsLoading, setWsLoading] = useState(false);
  const [wsErr, setWsErr] = useState<string|null>(null);
  const [ws, setWs] = useState<SpendingSnapshotResponse|undefined>();

  // ---- Totals (Income/Expenses) ----
  const [totalsType, setTotalsType] = useState<'income'|'expenses'>('expenses');
  const [totalsGroupBy, setTotalsGroupBy] =
    useState<'category'|'merchant'|'tag'|'account'|'provider'>('category');
  const [dateFrom, setDateFrom] = useState('');
  const [dateTo, setDateTo] = useState('');
  const [totalsLoading, setTotalsLoading] = useState(false);
  const [totalsErr, setTotalsErr] = useState<string|null>(null);
  const [totals, setTotals] = useState<TotalBreakdownResponse|undefined>();

  // details
  const [detailsLoading, setDetailsLoading] = useState(false);
  const [detailsErr, setDetailsErr] = useState<string|null>(null);
  const [details, setDetails] = useState<TransactionsPageResponse|undefined>();
  const [detailsHeader, setDetailsHeader] = useState<{type:'income'|'expenses'; groupBy:string; key:string} | null>(null);

  // ---- Spending Snapshot (generico) ----
  const [snapPeriod, setSnapPeriod] = useState<'auto'|'week'|'month'|'year'|'custom'>('auto');
  const [snapFrom, setSnapFrom] = useState('');
  const [snapTo, setSnapTo] = useState('');
  const [snapMode, setSnapMode] = useState<'auto'|'last'|'top'>('auto');
  const [snapGroupBy, setSnapGroupBy] = useState<'merchant'|'category'|'tag'|'account'>('category');
  const [snapLimit, setSnapLimit] = useState<number>(3);
  const [snapIncludeTotals, setSnapIncludeTotals] = useState<boolean>(true);
  const [snapLoading, setSnapLoading] = useState(false);
  const [snapErr, setSnapErr] = useState<string|null>(null);
  const [snap, setSnap] = useState<SpendingSnapshotResponse|undefined>();

  useEffect(() => {
    const uid = localStorage.getItem('userId') ?? '';
    setUserId(uid);
    // opzionale: autoreload total balance se già presente
    if (uid) { void loadTotalBalance(uid, expandAccounts); }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // ---- loaders ----
  async function loadTotalBalance(uid: string, expand: boolean) {
    if (!uid) { setTbErr('Imposta un userId (crea prima l’utente)'); return; }
    setTbErr(null); setTb(undefined); setTbLoading(true);
    try {
      const res = await fetchTotalBalance(uid, expand);
      setTb(res);
    } catch (e:any) {
      setTbErr(e?.message ?? 'Errore');
    } finally {
      setTbLoading(false);
    }
  }

  async function loadWeeklyCard(uid: string) {
    if (!uid) { setWsErr('Imposta un userId (crea prima l’utente)'); return; }
    setWsErr(null); setWs(undefined); setWsLoading(true);
    try {
      const res = await fetchSpendingSnapshotWeek(uid, {
        when: wsWhen,
        groupBy: 'merchant',
        limit: 3,
        includeTotals: wsIncludeTotals
      });
      setWs(res && typeof res === 'object' ? res : undefined);
    } catch (e:any) {
      setWsErr(e?.message ?? 'Errore');
    } finally {
      setWsLoading(false);
    }
  }

  async function loadTotals() {
    const uid = userId.trim();
    if (!uid) { setTotalsErr('Imposta un userId'); return; }
    setTotalsErr(null); setTotals(undefined); setDetails(undefined); setDetailsHeader(null);
    setTotalsLoading(true);
    try {
      const opts = {
        groupBy: totalsGroupBy,
        from: dateFrom || undefined,
        to: dateTo || undefined
      };
      const res = totalsType === 'income'
        ? await fetchTotalIncome(uid, opts)
        : await fetchTotalExpenses(uid, opts);
      setTotals(res);
    } catch (e:any) {
      setTotalsErr(e?.message ?? 'Errore');
    } finally {
      setTotalsLoading(false);
    }
  }

  async function loadDetails(key: string) {
    const uid = userId.trim();
    if (!uid) return;
    setDetailsErr(null); setDetails(undefined);
    setDetailsHeader({ type: totalsType, groupBy: totalsGroupBy, key });
    setDetailsLoading(true);
    try {
      const base = {
        groupBy: totalsGroupBy,
        key,
        from: dateFrom || undefined,
        to: dateTo || undefined,
        page: 1,
        pageSize: 50
      } as const;
      const res = totalsType === 'income'
        ? await fetchIncomeDetails(uid, base)
        : await fetchExpensesDetails(uid, base);
      setDetails(res);
    } catch (e:any) {
      setDetailsErr(e?.message ?? 'Errore');
    } finally {
      setDetailsLoading(false);
    }
  }

  async function loadSnapshot() {
    const uid = userId.trim();
    if (!uid) { setSnapErr('Imposta un userId'); return; }
    setSnapErr(null); setSnap(undefined); setSnapLoading(true);
    try {
      const res = await fetchSpendingSnapshot(uid, {
        period: snapPeriod,
        from: snapPeriod === 'custom' ? (snapFrom || undefined) : undefined,
        to:   snapPeriod === 'custom' ? (snapTo   || undefined) : undefined,
        mode: snapMode,
        groupBy: snapGroupBy,
        limit: snapLimit,
        includeTotals: snapIncludeTotals
      });
      setSnap(res);
    } catch (e:any) {
      setSnapErr(e?.message ?? 'Errore');
    } finally {
      setSnapLoading(false);
    }
  }

  // ---- render ----
  return (
    <div style={{ padding: 16, color: '#e5e7eb', background:'#0b1220', minHeight:'100vh' }}>
      <h1>Home</h1>

      {/* User ID + Onboarding */}
      <div style={{ display:'flex', gap:8, alignItems:'center', marginBottom:12 }}>
        <input
          placeholder="User ID"
          value={userId}
          onChange={e=>setUserId(e.target.value)}
          style={{ minWidth: 320, padding:'8px 10px', border:'1px solid #334155', borderRadius:8, background:'#0b1220', color:'#e5e7eb' }}
        />
        <Link to="/onboarding" style={{ marginLeft:'auto', color:'#93c5fd' }}>Onboarding</Link>
      </div>

      {/* Total Balance */}
      <section style={{ marginTop: 12 }}>
        <h3>Total Balance</h3>
        <div style={{ display:'flex', gap:8, alignItems:'center', marginBottom:10 }}>
          <label style={{ display:'flex', gap:6 }}>
            <input type="checkbox" checked={expandAccounts} onChange={e=>setExpandAccounts(e.target.checked)} />
            Dettaglio per conto
          </label>
          <button disabled={tbLoading} onClick={()=>loadTotalBalance(userId.trim(), expandAccounts)}>
            {tbLoading ? 'Carico…' : 'Carica Total Balance'}
          </button>
        </div>

        {tbErr && <pre style={{ color:'#fca5a5', whiteSpace:'pre-wrap' }}>{tbErr}</pre>}

        {tb && (
          <div style={{
            display:'inline-block',
            padding:'8px 12px',
            border:'1px solid #334155',
            borderRadius:8,
            background:'#111827'
          }}>
            <div style={{ fontSize:14, opacity:0.8 }}>Currency</div>
            <div style={{ fontSize:20, fontWeight:600 }}>{tb.currency}</div>
            <div style={{ fontSize:14, opacity:0.8, marginTop:6 }}>Total</div>
            <div style={{ fontSize:28, fontWeight:700 }}>{tb.total}</div>

            {Array.isArray(tb.accounts) && tb.accounts.length > 0 && (
              <div style={{ marginTop: 16 }}>
                <h4>Conti</h4>
                <ul style={{ paddingLeft: 16 }}>
                  {tb.accounts.map(a => (
                    <li key={a.accountId}>
                      <span style={{ fontWeight:600 }}>{a.name}</span>
                      {' — '}
                      <span>{a.balance}</span>
                      {'  '}
                      <span style={{ opacity:0.7, fontSize:12 }}>({a.asOf})</span>
                    </li>
                  ))}
                </ul>
              </div>
            )}

            {tb.meta && (
              <div style={{ marginTop: 12, opacity:0.8, fontSize:12 }}>
                <div>Accounts: {tb.meta.accounts}</div>
                <div>With snapshot: {tb.meta.withSnapshot}</div>
                {tb.meta.latestAt && <div>Latest asOf: {tb.meta.latestAt}</div>}
              </div>
            )}

            <details style={{ marginTop: 16 }}>
              <summary>Raw JSON</summary>
              <pre>{JSON.stringify(tb, null, 2)}</pre>
            </details>
          </div>
        )}
      </section>

      {/* Weekly Summary (usando Spending Snapshot settimanale) */}
      <section style={{ marginTop: 24 }}>
        <h3>Weekly Summary</h3>

        <div style={{ display:'flex', gap:8, alignItems:'center', marginBottom:10 }}>
          <label style={{ display:'flex', gap:6, alignItems:'center' }}>
            <span>Periodo</span>
            <select value={wsWhen} onChange={e=>setWsWhen(e.target.value as 'last'|'current')}>
              <option value="last">Ultima settimana</option>
              <option value="current">Settimana corrente</option>
            </select>
          </label>

          <label style={{ display:'flex', gap:6, alignItems:'center' }}>
            <input type="checkbox" checked={wsIncludeTotals} onChange={e=>setWsIncludeTotals(e.target.checked)} />
            Include totals
          </label>

          <button disabled={wsLoading} onClick={()=>loadWeeklyCard(userId.trim())}>
            {wsLoading ? 'Carico…' : 'Carica Weekly'}
          </button>
        </div>

        {wsErr && <pre style={{ color:'#fca5a5', whiteSpace:'pre-wrap' }}>{wsErr}</pre>}

        {ws && (
          <div style={{
            border:'1px solid #334155', borderRadius:12, background:'#111827', padding:12, maxWidth:520
          }}>
            <div style={{ fontSize:12, opacity:0.8, marginBottom:8 }}>
              {ws.period?.from ?? '—'} → {ws.period?.to ?? '—'}
            </div>

            {ws.totals && (
              <div style={{ display:'flex', gap:24, marginBottom:8 }}>
                <div>Entrate: <b>{ws.totals.income}</b></div>
                <div>Uscite: <b>{ws.totals.expenses}</b></div>
                <div>Netto: <b>{ws.totals.net}</b></div>
              </div>
            )}

            <ul style={{ listStyle:'none', margin:0, padding:0 }}>
              {ws.items.map((it, idx) => (
                <li key={idx} style={{
                  display:'flex', alignItems:'center', justifyContent:'space-between',
                  padding:'10px 4px', borderTop: idx===0 ? 'none' : '1px solid #1f2937'
                }}>
                  <div style={{ display:'grid' }}>
                    <span style={{ fontWeight:600 }}>{it.label}</span>
                    <span style={{ fontSize:12, opacity:0.7 }}>
                      {it.oneOff || it.count === 1 ? 'One off' : `${it.count ?? 0}×`}
                    </span>
                  </div>
                  <div style={{ fontWeight:600 }}>{fmtEURAbs(it.amount)}</div>
                </li>
              ))}
            </ul>

            <details style={{ marginTop: 10 }}>
              <summary>Raw JSON</summary>
              <pre>{JSON.stringify(ws, null, 2)}</pre>
            </details>
          </div>
        )}
      </section>

      {/* Totals (Income/Expenses) */}
      <section style={{ marginTop: 28 }}>
        <h3>Totals — {totalsType === 'income' ? 'Income' : 'Expenses'}</h3>

        <div style={{ display:'grid', gap:8, marginBottom:10, maxWidth:720 }}>
          <div style={{ display:'flex', gap:10, alignItems:'center', flexWrap:'wrap' }}>
            <label style={{ display:'flex', gap:6, alignItems:'center' }}>
              <span>Tipo</span>
              <select value={totalsType} onChange={e=>setTotalsType(e.target.value as 'income'|'expenses')}>
                <option value="income">income</option>
                <option value="expenses">expenses</option>
              </select>
            </label>

            <label style={{ display:'flex', gap:6, alignItems:'center' }}>
              <span>Group by</span>
              <select value={totalsGroupBy} onChange={e=>setTotalsGroupBy(e.target.value as any)}>
                <option value="category">category</option>
                <option value="merchant">merchant</option>
                <option value="tag">tag</option>
                <option value="account">account</option>
                <option value="provider">provider</option>
              </select>
            </label>

            <label style={{ display:'flex', gap:6, alignItems:'center' }}>
              <span>From</span>
              <input type="date" value={dateFrom} onChange={e=>setDateFrom(e.target.value)} />
            </label>
            <label style={{ display:'flex', gap:6, alignItems:'center' }}>
              <span>To</span>
              <input type="date" value={dateTo} onChange={e=>setDateTo(e.target.value)} />
            </label>

            <button disabled={totalsLoading} onClick={loadTotals}>
              {totalsLoading ? 'Carico…' : 'Carica Totals'}
            </button>
          </div>
        </div>

        {totalsErr && <pre style={{ color:'#fca5a5', whiteSpace:'pre-wrap' }}>{totalsErr}</pre>}

        {totals && (
          <div style={{
            display:'inline-block', padding:'8px 12px',
            border:'1px solid #334155', borderRadius:8, background:'#111827', minWidth:320
          }}>
            <div style={{ fontSize:12, opacity:0.8, marginBottom:6 }}>
              Group by: <b>{totals.groupBy}</b> — Total:&nbsp;
              <b>{fmtEURSigned(totals.total, totalsType === 'expenses')}</b>
            </div>

            <ul style={{ listStyle:'none', margin:0, padding:0 }}>
              {totals.breakdown.map((r, i) => (
                <li key={i}
                    onClick={()=>loadDetails(r.key)}
                    style={{
                      display:'flex', justifyContent:'space-between', gap:12,
                      padding:'10px 4px', borderTop: i===0 ? 'none' : '1px solid #1f2937',
                      cursor:'pointer'
                    }}>
                  <div style={{ display:'grid' }}>
                    <span style={{ fontWeight:600 }}>{r.key || '—'}</span>
                    <span style={{ fontSize:12, opacity:0.7 }}>{r.count} tx</span>
                  </div>
                  <div style={{ fontWeight:600 }}>
                    {fmtEURSigned(r.amount, totalsType === 'expenses')}
                  </div>
                </li>
              ))}
            </ul>

            <details style={{ marginTop: 10 }}>
              <summary>Raw JSON</summary>
              <pre>{JSON.stringify(totals, null, 2)}</pre>
            </details>
          </div>
        )}

        {(detailsHeader || detailsErr) && (
          <div style={{ marginTop:12 }}>
            <h4>Details {detailsHeader ? `— ${detailsHeader.type} — ${detailsHeader.groupBy}=${detailsHeader.key}` : ''}</h4>
            {detailsErr && <pre style={{ color:'#fca5a5' }}>{detailsErr}</pre>}
            {detailsLoading && <div>Carico…</div>}
            {details && (
              <div style={{ border:'1px solid #334155', borderRadius:8, background:'#111827', padding:8 }}>
                <ul style={{ listStyle:'none', margin:0, padding:0 }}>
                  {details.data.map(tx => (
                    <li key={tx.id}
                        style={{
                          display:'flex', justifyContent:'space-between',
                          borderTop:'1px solid #1f2937', padding:'8px 4px'
                        }}>
                      <div style={{ display:'grid' }}>
                        <span style={{ fontSize:12, opacity:0.8 }}>{tx.date}</span>
                        <span style={{ fontSize:12, opacity:0.8 }}>{tx.description}</span>
                      </div>
                      <div style={{ fontWeight:600 }}>
                        {fmtEURSigned(tx.amount, totalsType === 'expenses')}
                      </div>
                    </li>
                  ))}
                </ul>

                <details style={{ marginTop: 8 }}>
                  <summary>Raw JSON (details)</summary>
                  <pre>{JSON.stringify(details, null, 2)}</pre>
                </details>
              </div>
            )}
          </div>
        )}
      </section>

      {/* Spending Snapshot (generico) */}
      <section style={{ marginTop: 28 }}>
        <h3>Spending Snapshot</h3>

        <div style={{ display:'grid', gap:8, marginBottom:10, maxWidth:820 }}>
          <div style={{ display:'flex', gap:10, alignItems:'center', flexWrap:'wrap' }}>
            <label style={{ display:'flex', gap:6, alignItems:'center' }}>
              <span>Period</span>
              <select value={snapPeriod} onChange={e=>setSnapPeriod(e.target.value as any)}>
                <option value="auto">auto</option>
                <option value="week">week</option>
                <option value="month">month</option>
                <option value="year">year</option>
                <option value="custom">custom</option>
              </select>
            </label>

            {snapPeriod === 'custom' && (
              <>
                <label style={{ display:'flex', gap:6, alignItems:'center' }}>
                  <span>From</span>
                  <input type="date" value={snapFrom} onChange={e=>setSnapFrom(e.target.value)} />
                </label>
                <label style={{ display:'flex', gap:6, alignItems:'center' }}>
                  <span>To</span>
                  <input type="date" value={snapTo} onChange={e=>setSnapTo(e.target.value)} />
                </label>
              </>
            )}

            <label style={{ display:'flex', gap:6, alignItems:'center' }}>
              <span>Mode</span>
              <select value={snapMode} onChange={e=>setSnapMode(e.target.value as any)}>
                <option value="auto">auto</option>
                <option value="last">last</option>
                <option value="top">top</option>
              </select>
            </label>

            <label style={{ display:'flex', gap:6, alignItems:'center' }}>
              <span>Group by</span>
              <select value={snapGroupBy} onChange={e=>setSnapGroupBy(e.target.value as any)}>
                <option value="category">category</option>
                <option value="merchant">merchant</option>
                <option value="tag">tag</option>
                <option value="account">account</option>
              </select>
            </label>

            <label style={{ display:'flex', gap:6, alignItems:'center' }}>
              <span>Limit</span>
              <input
                type="number" min={1} max={10} value={snapLimit}
                onChange={e=>setSnapLimit(Math.min(10, Math.max(1, parseInt(e.target.value || '3', 10))))}
                style={{ width:90 }}
              />
            </label>

            <label style={{ display:'flex', gap:6, alignItems:'center' }}>
              <input
                type="checkbox"
                checked={snapIncludeTotals}
                onChange={e=>setSnapIncludeTotals(e.target.checked)}
              />
              Include totals
            </label>

            <button disabled={snapLoading} onClick={loadSnapshot}>
              {snapLoading ? 'Carico…' : 'Carica Snapshot'}
            </button>
          </div>
        </div>

        {snapErr && <pre style={{ color:'#fca5a5', whiteSpace:'pre-wrap' }}>{snapErr}</pre>}

        {snap && (
          <div style={{
            display:'inline-block', padding:'8px 12px',
            border:'1px solid #334155', borderRadius:8, background:'#111827', minWidth:320
          }}>
            <div style={{ fontSize:12, opacity:0.8, marginBottom:6 }}>
              Periodo: <b>{snap.period.from} → {snap.period.to}</b> — Mode: <b>{snap.mode}</b> — GroupBy: <b>{snapGroupBy}</b>
            </div>

            {snap.totals && (
              <div style={{ display:'flex', gap:24, margin:'6px 0 10px' }}>
                <div>Entrate: <b>{snap.totals.income}</b></div>
                <div>Uscite: <b>{snap.totals.expenses}</b></div>
                <div>Netto: <b>{snap.totals.net}</b></div>
              </div>
            )}

            <ul style={{ listStyle:'none', margin:0, padding:0 }}>
              {snap.items.map((it, i) => (
                <li key={i}
                    style={{
                      display:'flex', justifyContent:'space-between',
                      padding:'10px 4px', borderTop: i===0 ? 'none' : '1px solid #1f2937'
                    }}>
                  <div style={{ display:'grid' }}>
                    <span style={{ fontWeight:600 }}>{it.label}</span>
                    {it.type === 'transaction' ? (
                      <span style={{ fontSize:12, opacity:0.7 }}>{it.date ?? ''}</span>
                    ) : (
                      <span style={{ fontSize:12, opacity:0.7 }}>
                        {it.oneOff || it.count === 1 ? 'One off' : `${it.count ?? 0}×`}
                        {typeof it.sharePct === 'number' ? ` • ${(it.sharePct*100).toFixed(0)}%` : ''}
                      </span>
                    )}
                  </div>
                  <div style={{ fontWeight:600 }}>
                    {fmtEURAbs(it.amount)}
                  </div>
                </li>
              ))}
            </ul>

            <details style={{ marginTop: 10 }}>
              <summary>Raw JSON</summary>
              <pre>{JSON.stringify(snap, null, 2)}</pre>
            </details>
          </div>
        )}
      </section>
    </div>
  );
}
