import { useEffect, useState } from 'react'
import AdminTopBar from '../components/Admin/AdminTopBar'
import DoctorTable from '../components/Admin/DoctorTable'
import PatientsPanel from '../components/Admin/PatientsPanel'
import StatsGrid from '../components/Admin/StatsGrid'
import { fetchAdminDashboard, logoutAdmin } from '../services/admin'

const getDoctorParam = () => {
  const params = new URLSearchParams(window.location.search)
  return params.get('doctor') || ''
}

const updateDoctorParam = (doctor) => {
  const url = new URL(window.location.href)
  if (doctor) {
    url.searchParams.set('doctor', doctor)
  } else {
    url.searchParams.delete('doctor')
  }
  window.history.replaceState({}, '', url)
}

const AdminDashboard = () => {
  const [dashboard, setDashboard] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [selectedDoctor, setSelectedDoctor] = useState(getDoctorParam)

  useEffect(() => {
    let isMounted = true
    const load = async () => {
      setLoading(true)
      setError('')
      const result = await fetchAdminDashboard(selectedDoctor)
      if (!isMounted) {
        return
      }
      if (result.unauthorized) {
        window.location.assign('/admin/login')
        return
      }
      if (!result.ok) {
        setError(result.error || 'Unable to load admin dashboard.')
        setLoading(false)
        return
      }
      setDashboard(result)
      setLoading(false)
    }
    load()
    return () => {
      isMounted = false
    }
  }, [selectedDoctor])

  const handleDoctorSelect = (username) => {
    const next = selectedDoctor === username ? '' : username
    updateDoctorParam(next)
    setSelectedDoctor(next)
  }

  const handleLogout = async () => {
    await logoutAdmin()
    window.location.assign('/admin/login')
  }

  if (loading) {
    return (
      <div className="admin-dashboard">
        <AdminTopBar onLogout={handleLogout} />
        <div className="admin-feedback">Loading admin dashboard...</div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="admin-dashboard">
        <AdminTopBar onLogout={handleLogout} />
        <div className="admin-feedback">{error}</div>
      </div>
    )
  }

  const hasSelection = Boolean(selectedDoctor)
  const selectedUsername = hasSelection ? dashboard?.selectedDoctor?.username || '' : ''
  const displayDoctor = hasSelection ? dashboard?.selectedDoctor : null
  const displayPatients = hasSelection ? dashboard?.patients || [] : []

  return (
    <div className="admin-dashboard">
      <AdminTopBar onLogout={handleLogout} />
      <StatsGrid summary={dashboard?.summary} />
      <DoctorTable
        doctors={dashboard?.doctors || []}
        selectedDoctor={selectedUsername}
        onSelect={handleDoctorSelect}
      />
      <PatientsPanel
        doctor={displayDoctor}
        patients={displayPatients}
      />
    </div>
  )
}

export default AdminDashboard
