const DoctorTable = ({ doctors, selectedDoctor, onSelect }) => {
  return (
    <section className="admin-section">
      <div className="admin-section-header">
        <div>
          <h3 className="admin-section-title">Doctor List</h3>
          <p className="admin-muted">All registered doctors</p>
        </div>
      </div>
      <div className="admin-table-card">
        <table className="admin-table">
          <thead>
            <tr>
              <th>Doctor Name</th>
              <th>User ID</th>
              <th>Qualifications</th>
              <th>Patients</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            {doctors.length === 0 ? (
              <tr>
                <td colSpan="5" className="admin-muted">
                  No doctors registered yet.
                </td>
              </tr>
            ) : (
              doctors.map((doctor) => {
                const isSelected = doctor.username === selectedDoctor
                const rowClass = isSelected ? 'is-selected' : ''
                const statusClass =
                  doctor.status === 'Active' ? 'status-active' : 'status-idle'
                return (
                  <tr key={doctor.username} className={rowClass}>
                    <td>
                      <button
                        type="button"
                        className="admin-link"
                        onClick={() => onSelect(doctor.username)}
                      >
                        {doctor.name}
                      </button>
                    </td>
                    <td>{doctor.username}</td>
                    <td>{doctor.qualifications}</td>
                    <td>{doctor.patients}</td>
                    <td>
                      <span className={`status-pill ${statusClass}`}>
                        {doctor.status}
                      </span>
                    </td>
                  </tr>
                )
              })
            )}
          </tbody>
        </table>
      </div>
    </section>
  )
}

export default DoctorTable
