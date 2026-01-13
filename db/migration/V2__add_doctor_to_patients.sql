alter table patients add column doctor_username varchar(64);
alter table patients add constraint fk_patients_doctor
    foreign key (doctor_username) references doctors(username);
create index idx_patients_doctor on patients(doctor_username);
