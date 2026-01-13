import AdminDashboard from './pages/AdminDashboard'
import AdminLogin from './pages/AdminLogin'

function App() {
  const path = window.location.pathname
  if (path.startsWith('/admin') && !path.startsWith('/admin/login')) {
    return <AdminDashboard />
  }
  return <AdminLogin />
}

export default App
