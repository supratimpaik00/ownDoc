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
