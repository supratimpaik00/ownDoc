import Brand from '../components/Auth/Brand'
import LoginCard from '../components/Auth/LoginCard'

function AdminLogin() {
  return (
    <main className="auth-shell">
      <div className="auth-center">
        <Brand />
        <LoginCard />
      </div>
    </main>
  )
}

export default AdminLogin
