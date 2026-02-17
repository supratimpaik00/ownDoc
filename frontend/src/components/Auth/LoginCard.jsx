import { useState } from 'react'
import {
  loginAdmin,
  requestAdminOtp,
  resetAdminPassword,
  verifyAdminOtp,
} from '../../services/admin'

function LoginCard() {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const [showReset, setShowReset] = useState(false)
  const [resetStep, setResetStep] = useState('phone')
  const [resetPhone, setResetPhone] = useState('')
  const [resetOtp, setResetOtp] = useState('')
  const [resetToken, setResetToken] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [resetError, setResetError] = useState('')
  const [resetMessage, setResetMessage] = useState('')

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

  const openReset = () => {
    setShowReset(true)
    setResetStep('phone')
    setResetPhone('')
    setResetOtp('')
    setResetToken('')
    setNewPassword('')
    setResetError('')
    setResetMessage('')
  }

  const closeReset = () => {
    setShowReset(false)
  }

  const handleRequestOtp = async () => {
    setResetError('')
    setResetMessage('')
    const result = await requestAdminOtp(resetPhone)
    if (!result.ok) {
      setResetError(result.error || 'Unable to send OTP.')
      return
    }
    setResetMessage('OTP sent to your phone.')
    setResetStep('otp')
  }

  const handleVerifyOtp = async () => {
    setResetError('')
    const result = await verifyAdminOtp(resetPhone, resetOtp)
    if (!result.ok) {
      setResetError(result.error || 'OTP verification failed.')
      return
    }
    setResetToken(result.token || '')
    setResetStep('password')
  }

  const handleResetPassword = async () => {
    setResetError('')
    const result = await resetAdminPassword(resetToken, newPassword)
    if (!result.ok) {
      setResetError(result.error || 'Password reset failed.')
      return
    }
    setResetMessage('Password updated. You can login now.')
    setResetStep('done')
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
      <button type="button" className="link-btn" onClick={openReset}>
        Forgot password?
      </button>
      {showReset ? (
        <div className="modal-backdrop">
          <div className="modal-card">
            <h4>Reset admin password</h4>
            {resetError ? <p className="error-text">{resetError}</p> : null}
            {resetMessage ? <p className="hint-text">{resetMessage}</p> : null}
            {resetStep === 'phone' ? (
              <label className="field">
                <span>Registered phone</span>
                <input
                  type="tel"
                  placeholder="Enter phone number"
                  value={resetPhone}
                  onChange={(event) => setResetPhone(event.target.value)}
                />
              </label>
            ) : null}
            {resetStep === 'otp' ? (
              <label className="field">
                <span>OTP</span>
                <input
                  type="text"
                  placeholder="Enter OTP"
                  value={resetOtp}
                  onChange={(event) => setResetOtp(event.target.value)}
                />
              </label>
            ) : null}
            {resetStep === 'password' ? (
              <label className="field">
                <span>New password</span>
                <input
                  type="password"
                  placeholder="Set new password"
                  value={newPassword}
                  onChange={(event) => setNewPassword(event.target.value)}
                />
              </label>
            ) : null}
            <div className="modal-actions">
              <button type="button" className="ghost-btn" onClick={closeReset}>
                Close
              </button>
              {resetStep === 'phone' ? (
                <button type="button" className="primary-btn" onClick={handleRequestOtp}>
                  Send OTP
                </button>
              ) : null}
              {resetStep === 'otp' ? (
                <button type="button" className="primary-btn" onClick={handleVerifyOtp}>
                  Verify OTP
                </button>
              ) : null}
              {resetStep === 'password' ? (
                <button type="button" className="primary-btn" onClick={handleResetPassword}>
                  Update password
                </button>
              ) : null}
              {resetStep === 'done' ? (
                <button type="button" className="primary-btn" onClick={closeReset}>
                  Done
                </button>
              ) : null}
            </div>
          </div>
        </div>
      ) : null}
    </section>
  )
}

export default LoginCard
