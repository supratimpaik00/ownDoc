# Doctor Prescription Webapp (Core Java)

Minimal Java 17 HTTP server (no Spring) that lets doctors sign up, log in, register patients, and send prescriptions to the patient email address (logged to console by default).

## Prerequisites
- Java 17+ on PATH (`java -version`).
- Maven 3.9+ (`mvn -version`).

## Database (Postgres)
For free local use, run Postgres with Docker:

```bash
docker run --name own-doc-db -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=own_doc -p 5432:5432 -d postgres:16
```

Or use Docker Compose:

```bash
docker compose up -d
```

Set environment variables before starting the app:

PowerShell:
```powershell
$env:DB_URL = "jdbc:postgresql://localhost:5432/own_doc"
$env:DB_USER = "postgres"
$env:DB_PASS = "postgres"
$env:DB_TIMEZONE = "Asia/Kolkata"
```

bash:
```bash
export DB_URL="jdbc:postgresql://localhost:5432/own_doc"
export DB_USER="postgres"
export DB_PASS="postgres"
export DB_TIMEZONE="Asia/Kolkata"
```

Migrations auto-run on startup from `db/migration`. If `DB_URL` is not set, the app falls back to in-memory storage.
Set `DB_TIMEZONE` if your OS timezone is not recognized by Postgres (default is UTC).
For AWS RDS later, just point `DB_URL`, `DB_USER`, and `DB_PASS` to the RDS instance.

Admin credentials (optional):
```bash
export ADMIN_USER="admin"
export ADMIN_PASS="adminpass"
```

Delivery response links (WhatsApp):
```bash
export PUBLIC_BASE_URL="http://localhost:8080"
export DELIVERY_TOKEN_SECRET="change-this-secret"
```
`PUBLIC_BASE_URL` must be reachable by patients so their Yes/No response links work.

## NLP models
Download OpenNLP models into `models/`:

PowerShell:
```powershell
Invoke-WebRequest -Uri https://opennlp.sourceforge.net/models-1.5/en-token.bin -OutFile models\en-token.bin
Invoke-WebRequest -Uri https://opennlp.sourceforge.net/models-1.5/en-pos-maxent.bin -OutFile models\en-pos-maxent.bin
```

bash:
```bash
curl -L -o models/en-token.bin https://opennlp.sourceforge.net/models-1.5/en-token.bin
curl -L -o models/en-pos-maxent.bin https://opennlp.sourceforge.net/models-1.5/en-pos-maxent.bin
```
If the models are missing, the app will still run but voice parsing is less accurate.

## Run locally
```bash
mvn -q -DskipTests package
# optional: set a different port if 8080 is in use
# $env:PORT=8081  # PowerShell
# export PORT=8081 # bash
mvn -q exec:java
```
Then open http://localhost:8080.

## React frontend (optional, separate)
React UI lives in `frontend/`. The admin login can be served by the Java backend when a build is available.

```bash
cd frontend
npm install
npm run dev
```

This starts the React dev server at http://localhost:5173. Backend HTML remains available at http://localhost:8080.

To serve the admin login from the Java backend, build the React app:
```bash
cd frontend
npm run build
```
Then start the Java server and visit `http://localhost:8080/admin/login`.

## Usage
- Sign up with a username/password, then log in.
- Add patients with name/email (stored in Postgres when `DB_URL` is set, otherwise in-memory).
- For each patient, enter medication + optional instructions and hit Send; an email is emitted via the `EmailService` (console by default).
- Logout with the button in the header.
- Admin console: visit `/admin`. Defaults to `admin/admin` unless `ADMIN_USER` and `ADMIN_PASS` are set.

## Notes
- Storage is in-memory only when `DB_URL` is not set; otherwise Postgres persists data.
- `ConsoleEmailService` just prints the email. To hook a real SMTP provider, implement `EmailService#sendEmail` and swap the instance in `Main`.
- Sessions use a simple cookie (`SESSION`); enable HTTPS/reverse proxy for production.
