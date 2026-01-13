import PatientCard from './PatientCard'

const PatientsPanel = ({ doctor, patients }) => {
  return (
    <section className="admin-section">
      <div className="admin-panel">
        <div className="admin-section-header">
          <div>
            <h3 className="admin-section-title">Patients</h3>
            {doctor ? (
              <p className="admin-muted">
                Dr. {doctor.name} | {doctor.qualifications}
              </p>
            ) : (
              <p className="admin-muted">Select a doctor to view their patients.</p>
            )}
          </div>
        </div>
        {!doctor ? null : patients.length === 0 ? (
          <p className="admin-muted">No patients assigned to this doctor.</p>
        ) : (
          patients.map((patient) => (
            <PatientCard key={patient.id} patient={patient} />
          ))
        )}
      </div>
    </section>
  )
}

export default PatientsPanel
