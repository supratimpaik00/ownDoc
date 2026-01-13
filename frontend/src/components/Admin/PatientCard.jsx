const buildWhatsAppLink = (phone, message) => {
  const digits = (phone || '').replace(/\D/g, '')
  if (!digits) {
    return ''
  }
  const encoded = encodeURIComponent(message || '')
  return `https://wa.me/${digits}?text=${encoded}`
}

const PatientCard = ({ patient }) => {
  const status = (patient.deliveryStatus || '').toLowerCase()
  const nameClass =
    status === 'yes' ? 'patient-title is-yes' : status === 'no' ? 'patient-title is-no' : 'patient-title'
  const whatsappLink = buildWhatsAppLink(patient.phone, patient.deliveryMessage)
  const history = Array.isArray(patient.history) ? patient.history : []
  const ageGender = [
    patient.age !== null && patient.age !== undefined ? patient.age : null,
    patient.gender || null,
  ]
    .filter(Boolean)
    .join(' / ')

  return (
    <article className="patient-card">
      <h5 className={nameClass}>{patient.name}</h5>
      <p className="patient-meta">
        {patient.email} | {patient.phone}
      </p>
      {ageGender && <p className="patient-meta">Age: {ageGender}</p>}
      {patient.address && (
        <p className="patient-info">
          <strong>Address:</strong> {patient.address}
        </p>
      )}
      {patient.notes && (
        <p className="patient-info">
          <strong>Notes:</strong> {patient.notes}
        </p>
      )}
      {whatsappLink && (
        <div className="patient-actions">
          <a
            className="admin-action"
            href={whatsappLink}
            target="_blank"
            rel="noopener noreferrer"
          >
            Send WhatsApp message
          </a>
        </div>
      )}
      {history.length === 0 ? (
        <p className="admin-muted">No diagnosis saved yet.</p>
      ) : (
        history.map((session, index) => (
          <div className="patient-history" key={`${patient.id}-${index}`}>
            <p className="patient-history-date">{session.createdAt}</p>
            <p className="patient-history-label">Diagnosis</p>
            <p className="patient-history-text">{session.diagnosis}</p>
            <p className="patient-history-label">Plan</p>
            <p className="patient-history-text">{session.plan}</p>
          </div>
        ))
      )}
    </article>
  )
}

export default PatientCard
