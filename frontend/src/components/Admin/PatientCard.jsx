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
  const metaParts = []
  if (patient.email) {
    metaParts.push(patient.email)
  }
  if (patient.phone) {
    metaParts.push(patient.phone)
  }
  if (ageGender) {
    metaParts.push(`Age: ${ageGender}`)
  }
  if (patient.address) {
    metaParts.push(`Address: ${patient.address}`)
  }
  if (patient.notes) {
    metaParts.push(`Notes: ${patient.notes}`)
  }
  const metaLine = metaParts.join(' | ')

  return (
    <article className="patient-card">
      <div className="patient-header">
        <div>
          <h5 className={nameClass}>{patient.name}</h5>
          {metaLine ? <span className="patient-meta-line">{metaLine}</span> : null}
        </div>
      </div>
      {history.length === 0 ? (
        <p className="admin-muted">No diagnosis saved yet.</p>
      ) : (
        <div className="patient-history-scroll">
          <p className="patient-history-label">Diagnoses</p>
          <div className="patient-history-list">
            {history.map((session, index) => (
              <div className="patient-history-item" key={`${patient.id}-all-${index}`}>
                <div className="patient-history-row">
                  <div>
                    <p className="patient-history-date">{session.createdAt}</p>
                    <div className="patient-history-columns">
                      <div>
                        <p className="patient-history-label">Diagnosis</p>
                        <p className="patient-history-text">{session.diagnosis}</p>
                      </div>
                      <div>
                        <p className="patient-history-label">Plan</p>
                        <p className="patient-history-text">{session.plan}</p>
                      </div>
                    </div>
                  </div>
                  {whatsappLink && (
                    <div className="patient-history-action">
                      <a
                        className="admin-action"
                        href={whatsappLink}
                        target="_blank"
                        rel="noopener noreferrer"
                      >
                        Send WhatsApp
                      </a>
                    </div>
                  )}
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </article>
  )
}

export default PatientCard
