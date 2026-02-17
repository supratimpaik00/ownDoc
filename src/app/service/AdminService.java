package app.service;

import app.dto.AdminDashboardDto;
import app.dto.AdminSummaryDto;
import app.dto.DiagnosisDto;
import app.dto.DoctorSummaryDto;
import app.dto.PatientDto;
import app.model.DiagnosisSession;
import app.model.Doctor;
import app.model.Patient;
import app.repository.DiagnosisRepository;
import app.repository.DoctorRepository;
import app.repository.PatientRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class AdminService {
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final DiagnosisRepository diagnosisRepository;
    private final DeliveryService deliveryService;

    public AdminService(DoctorRepository doctorRepository, PatientRepository patientRepository,
                        DiagnosisRepository diagnosisRepository, DeliveryService deliveryService) {
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.diagnosisRepository = diagnosisRepository;
        this.deliveryService = deliveryService;
    }

    // Builds dashboard.
    public AdminDashboardDto buildDashboard(String selectedDoctor, String baseUrl) {
        List<Doctor> doctors = doctorRepository.findAll();
        List<Patient> allPatients = patientRepository.findAll();
        Map<String, Long> patientCounts = allPatients.stream()
                .collect(Collectors.groupingBy(Patient::doctorUsername, Collectors.counting()));
        long activeDoctors = patientCounts.values().stream().filter(count -> count != null && count > 0).count();
        AdminSummaryDto summary = new AdminSummaryDto(doctors.size(), activeDoctors, allPatients.size());

        List<DoctorSummaryDto> doctorDtos = new ArrayList<>();
        // Performs for.
        for (Doctor doctor : doctors) {
            long count = patientCounts.getOrDefault(doctor.username(), 0L);
            String status = count > 0 ? "Active" : "Idle";
            doctorDtos.add(new DoctorSummaryDto(doctor.username(), doctor.name(), doctor.qualifications(), count, status));
        }

        Optional<Doctor> selected = Optional.empty();
        // Performs if.
        if (selectedDoctor != null && !selectedDoctor.isBlank()) {
            selected = doctors.stream().filter(d -> d.username().equals(selectedDoctor)).findFirst();
        }
        // Performs if.
        if (selected.isEmpty() && !doctors.isEmpty()) {
            selected = Optional.of(doctors.get(0));
        }

        List<PatientDto> patientDtos = new ArrayList<>();
        // Performs if.
        if (selected.isPresent()) {
            List<Patient> patients = patientRepository.findByDoctor(selected.get().username());
            // Performs for.
            for (Patient patient : patients) {
                List<DiagnosisSession> history = diagnosisRepository.findByPatient(patient.id());
                List<DiagnosisDto> historyDtos = history.stream()
                        .map(session -> new DiagnosisDto(session.createdAt().toString(), session.diagnosis(), session.plan()))
                        .toList();
                String deliveryMessage = deliveryService.buildMessage(baseUrl, patient.id());
                patientDtos.add(new PatientDto(
                        patient.id().toString(),
                        patient.name(),
                        patient.email(),
                        patient.phone(),
                        patient.age(),
                        patient.gender(),
                        patient.address(),
                        patient.notes(),
                        patient.deliveryStatus(),
                        deliveryMessage,
                        historyDtos
                ));
            }
        }

        DoctorSummaryDto selectedDto = selected
                .map(d -> new DoctorSummaryDto(d.username(), d.name(), d.qualifications(),
                        patientCounts.getOrDefault(d.username(), 0L),
                        patientCounts.getOrDefault(d.username(), 0L) > 0 ? "Active" : "Idle"))
                .orElse(null);

        // Performs admin dashboard dto.
        return new AdminDashboardDto(summary, doctorDtos, selectedDto, patientDtos);
    }

    // Performs record delivery response.
    public Patient recordDeliveryResponse(UUID patientId, String choice) {
        return deliveryService.recordResponse(patientId, choice);
    }
}
