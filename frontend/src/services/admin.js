export async function loginAdmin(username, password) {
  const body = new URLSearchParams()
  body.set('username', username)
  body.set('password', password)
  try {
    const response = await fetch('/api/admin/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body,
      credentials: 'include',
    })
    if (!response.ok) {
      return { ok: false, error: 'Invalid credentials' }
    }
    return await response.json()
  } catch (error) {
    return { ok: false, error: 'Network error' }
  }
}

export async function fetchAdminDashboard(doctor) {
  const params = new URLSearchParams()
  if (doctor) {
    params.set('doctor', doctor)
  }
  const url = params.toString()
    ? `/api/admin/dashboard?${params.toString()}`
    : '/api/admin/dashboard'
  try {
    const response = await fetch(url, { credentials: 'include' })
    if (response.status === 401) {
      return { ok: false, unauthorized: true }
    }
    if (!response.ok) {
      return { ok: false, error: 'Failed to load dashboard.' }
    }
    return await response.json()
  } catch (error) {
    return { ok: false, error: 'Network error' }
  }
}

export async function logoutAdmin() {
  try {
    await fetch('/api/admin/logout', {
      method: 'POST',
      credentials: 'include',
    })
  } catch (error) {
    return false
  }
  return true
}

export async function requestAdminOtp(phone) {
  const body = new URLSearchParams()
  body.set('phone', phone)
  try {
    const response = await fetch('/api/admin/password/request', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body,
    })
    if (!response.ok) {
      const data = await response.json().catch(() => ({}))
      return { ok: false, error: data.error || 'Unable to send OTP.' }
    }
    return { ok: true }
  } catch (error) {
    return { ok: false, error: 'Network error' }
  }
}

export async function verifyAdminOtp(phone, otp) {
  const body = new URLSearchParams()
  body.set('phone', phone)
  body.set('otp', otp)
  try {
    const response = await fetch('/api/admin/password/verify', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body,
    })
    const data = await response.json().catch(() => ({}))
    if (!response.ok) {
      return { ok: false, error: data.error || 'OTP verification failed.' }
    }
    return { ok: true, token: data.token }
  } catch (error) {
    return { ok: false, error: 'Network error' }
  }
}

export async function resetAdminPassword(token, password) {
  const body = new URLSearchParams()
  body.set('token', token)
  body.set('password', password)
  try {
    const response = await fetch('/api/admin/password/reset', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body,
    })
    const data = await response.json().catch(() => ({}))
    if (!response.ok) {
      return { ok: false, error: data.error || 'Password reset failed.' }
    }
    return { ok: true }
  } catch (error) {
    return { ok: false, error: 'Network error' }
  }
}
