const AdminTopBar = ({ onLogout }) => {
  return (
    <header className="admin-topbar">
      <div className="admin-brand">
        chry<span>sa</span>lis
      </div>
      <button className="admin-ghost" type="button" onClick={onLogout}>
        Logout
      </button>
    </header>
  )
}

export default AdminTopBar
