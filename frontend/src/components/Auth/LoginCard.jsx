import { useState } from 'react'
import { loginAdmin } from '../../services/admin'

function LoginCard() {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const submit = async () => {
    setError('')
    setLoading(true)
    const result = await loginAdmin(username, password)
    setLoading(false)
    if (!result.ok) {
      setError(result.error || 'Login failed')
      return
    }
    window.location.assign('/admin')
  }

  return (
    <section className="login-card">
      {error ? <p className="error-text">{error}</p> : null}
      <label className="field">
        <span>User ID</span>
        <input
          type="text"
          placeholder="Enter user ID"
          value={username}
          onChange={(event) => setUsername(event.target.value)}
        />
      </label>
      <label className="field">
        <span>Password</span>
        <input
          type="password"
          placeholder="Enter password"
          value={password}
          onChange={(event) => setPassword(event.target.value)}
        />
      </label>
      <button type="button" className="primary-btn" onClick={submit} disabled={loading}>
        {loading ? 'Logging in...' : 'Login'}
      </button>
    </section>
  )
}

export default LoginCard
