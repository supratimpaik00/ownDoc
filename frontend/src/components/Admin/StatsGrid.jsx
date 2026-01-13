const StatsGrid = ({ summary }) => {
  if (!summary) {
    return null
  }

  return (
    <section className="admin-stats">
      <div className="stat-card">
        <div className="stat-label">Total Doctors</div>
        <div className="stat-value">{summary.totalDoctors}</div>
      </div>
      <div className="stat-card">
        <div className="stat-label">Active Doctors</div>
        <div className="stat-value">{summary.activeDoctors}</div>
      </div>
      <div className="stat-card">
        <div className="stat-label">Total Patients</div>
        <div className="stat-value">{summary.totalPatients}</div>
      </div>
    </section>
  )
}

export default StatsGrid
