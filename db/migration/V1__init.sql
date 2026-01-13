create table if not exists doctors (
    username varchar(64) primary key,
    name varchar(255) not null,
    password_hash varchar(255) not null,
    qualifications varchar(255) not null
);

create table if not exists patients (
    id uuid primary key,
    name varchar(255) not null,
    email varchar(255) not null,
    phone varchar(64) not null,
    notes text
);

create table if not exists diagnosis_sessions (
    id uuid primary key,
    patient_id uuid not null references patients(id) on delete cascade,
    diagnosis text not null,
    plan text not null,
    created_at timestamp not null
);

create index if not exists idx_diagnosis_sessions_patient_created
    on diagnosis_sessions (patient_id, created_at desc);
