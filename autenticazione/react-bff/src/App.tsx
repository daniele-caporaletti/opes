import { useEffect, useState } from 'react'
import './App.css'

type Me = { authenticated: boolean; name?: string; email?: string; sub?: string; claims?: Record<string, unknown> }

async function api<T>(path: string): Promise<T> {
  const res = await fetch(path)
  if (res.status === 401) {
    // utente non loggato → vai al login OIDC del BFF
    window.location.href = '/oauth2/authorization/keycloak'
    throw new Error('Redirecting to login...')
  }
  return res.json()
}

export default function App() {
  const [me, setMe] = useState<Me>({ authenticated: false })
  const [loading, setLoading] = useState(true)

  const loadMe = async () => {
    try {
      setLoading(true)
      const data = await api<Me>('/api/session/me')
      setMe(data)
    } finally { setLoading(false) }
  }

  useEffect(() => { loadMe() }, [])

  return (
    <div style={{ fontFamily: 'system-ui', maxWidth: 720, margin: '3rem auto', lineHeight: 1.5 }}>
      <h1>App • React × Spring BFF × Keycloak</h1>

      {loading ? <p>Carico…</p> : (
        !me.authenticated ? (
          <>
            <p>Non sei autenticato.</p>
            <a href="/oauth2/authorization/keycloak">🔐 Login</a>
          </>
        ) : (
          <>
            <p>✅ Ciao <b>{me.name ?? me.email ?? me.sub}</b></p>
            <div style={{ display: 'flex', gap: 12, margin: '12px 0' }}>
              <button onClick={loadMe}>Aggiorna profilo</button>
              <a href="/logout">Logout</a>
            </div>
            <details>
              <summary>Dettagli claim</summary>
              <pre style={{ background: '#f6f8fa', padding: 12, borderRadius: 8 }}>{JSON.stringify(me, null, 2)}</pre>
            </details>
          </>
        )
      )}

      <hr />
      <p>Endpoint pubblico BFF: <a href="/api/session/hello">/api/session/hello</a></p>
    </div>
  )
}
