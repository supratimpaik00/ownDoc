package app.core;

import app.controller.AdminController;
import app.controller.AssetController;
import app.controller.DeliveryController;
import app.controller.DoctorApiController;
import app.controller.DoctorController;
import app.controller.NlpController;
import app.controller.PatientApiController;
import app.controller.SessionApiController;
import app.http.Router;
import app.mail.EmailService;
import app.nlp.MedicationNlp;
import app.repository.DatabaseDiagnosisRepository;
import app.repository.DatabaseDoctorRepository;
import app.repository.DatabasePatientRepository;
import app.repository.DatabaseAdminUserRepository;
import app.repository.AdminUserRepository;
import app.repository.DiagnosisRepository;
import app.repository.DoctorRepository;
import app.repository.PatientRepository;
import app.service.AdminAuthService;
import app.service.AdminService;
import app.service.AdminUserService;
import app.service.AdminPasswordResetService;
import app.service.ConsoleSmsSender;
import app.service.DeliveryService;
import app.service.DiagnosisService;
import app.service.DoctorService;
import app.service.NlpService;
import app.service.NoopSmsSender;
import app.service.PatientService;
import app.service.PrescriptionService;
import app.service.SessionManager;
import app.service.SmsSender;
import app.db.Database;

public class AppBootstrap {
    private final Router router;

    // Initializes app bootstrap.
    public AppBootstrap(Database database, EmailService emailService) {
        DoctorRepository doctorRepository = new DatabaseDoctorRepository(database);
        PatientRepository patientRepository = new DatabasePatientRepository(database);
        DiagnosisRepository diagnosisRepository = new DatabaseDiagnosisRepository(database);
        AdminUserRepository adminUserRepository = new DatabaseAdminUserRepository(database);

        SessionManager sessionManager = new SessionManager();
        DoctorService doctorService = new DoctorService(doctorRepository);
        PatientService patientService = new PatientService(patientRepository);
        DiagnosisService diagnosisService = new DiagnosisService(diagnosisRepository);
        PrescriptionService prescriptionService = new PrescriptionService(emailService, diagnosisService);
        DeliveryService deliveryService = new DeliveryService(patientRepository);
        AdminService adminService = new AdminService(doctorRepository, patientRepository, diagnosisRepository, deliveryService);
        AdminUserService adminUserService = new AdminUserService(adminUserRepository);
        AdminAuthService adminAuthService = new AdminAuthService(adminUserService);
        SmsSender smsSender = resolveSmsSender();
        AdminPasswordResetService adminPasswordResetService = new AdminPasswordResetService(adminUserService, smsSender);
        NlpService nlpService = new NlpService(new MedicationNlp());

        AdminController adminController = new AdminController(adminAuthService, adminService, adminPasswordResetService, sessionManager);
        DoctorController doctorController = new DoctorController(doctorService, patientService, diagnosisService, prescriptionService, sessionManager);
        DoctorApiController doctorApiController = new DoctorApiController(doctorService, sessionManager);
        PatientApiController patientApiController = new PatientApiController(patientService, sessionManager);
        SessionApiController sessionApiController = new SessionApiController(diagnosisService, prescriptionService, patientService, doctorService, sessionManager);
        NlpController nlpController = new NlpController(nlpService, doctorService, sessionManager);
        DeliveryController deliveryController = new DeliveryController(deliveryService);
        AssetController assetController = new AssetController();

        Router router = new Router();
        adminController.registerRoutes(router);
        doctorController.registerRoutes(router);
        doctorApiController.registerRoutes(router);
        patientApiController.registerRoutes(router);
        sessionApiController.registerRoutes(router);
        nlpController.registerRoutes(router);
        deliveryController.registerRoutes(router);
        assetController.registerRoutes(router);

        this.router = router;
    }

    // Performs router.
    public Router router() {
        return router;
    }

    // Resolves sms sender.
    private SmsSender resolveSmsSender() {
        String dev = System.getenv("OTP_DEV_MODE");
        // Performs if.
        if (dev != null && dev.equalsIgnoreCase("true")) {
            // Performs console sms sender.
            return new ConsoleSmsSender();
        }
        // Performs noop sms sender.
        return new NoopSmsSender();
    }
}
