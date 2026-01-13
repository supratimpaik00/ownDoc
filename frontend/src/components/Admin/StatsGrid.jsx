const StatsGrid = ({ summary }) => {
  if (!summary) {
    return null
  }

  return (
    <section className="admin-stats">
      <div className="stat-card">
        <div className="stat-label">Total Clients</div>
        <div className="stat-value">{summary.totalDoctors}</div>
      </div>
      <div className="stat-card">
        <div className="stat-label">Active Clients</div>
        <div className="stat-value">{summary.activeDoctors}</div>
      </div>
      <div className="stat-card">
        <div className="stat-label">Total Users</div>
        <div className="stat-value">{summary.totalPatients}</div>
      </div>
    </section>
  )
}

export default StatsGrid
