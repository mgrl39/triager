package net.elpuig.triager.controller;

import net.elpuig.triager.model.Patient;
import net.elpuig.triager.service.PatientService;
import net.elpuig.triager.config.MensajeApp;
import org.springframework.stereotype.Controller;

import java.util.Map;
import java.util.Scanner;

@Controller
public class TriagerController {
    private final PatientService patientService;
    private final Map<String, String> colors;
    private final Scanner scanner;
    private boolean adminMode;

    public TriagerController(PatientService patientService, Map<String, String> colors) {
        this.patientService = patientService;
        this.colors = colors;
        this.scanner = new Scanner(System.in);
        this.adminMode = false;
    }

    public void start() {
        System.out.println(MensajeApp.BIENVENIDA.get());
        while (true) {
            String promptColor = adminMode ? colors.get("red_bold") : colors.get("blue_bold");
            System.out.print(promptColor + MensajeApp.PROGRAMA.get() + colors.get("reset") + "$ ");
            handleCommand(scanner.nextLine());
        }
    }

    private void handleCommand(String command) {
        String[] parts = command.split(" ");
        switch (parts[0]) {
            case "exit", "quit" -> System.exit(0);
            case "help", "info" -> showHelp();
            case "patient" -> handlePatientCommand(parts);
            case "admin" -> handleAdminCommand(parts);
            default -> System.out.println(colors.get("yellow") + "Comando no reconocido" + colors.get("reset"));
        }
    }

    private void handlePatientCommand(String[] parts) {
        if (parts.length == 1) {
            showPatientHelp();
            return;
        }

        switch (parts[1]) {
            case "list" -> listPatients();
            case "add" -> addPatient();
            case "show" -> showPatient(parts);
            case "attend" -> attendPatient();
            default -> System.out.println(colors.get("yellow") + "Subcomando no reconocido" + colors.get("reset"));
        }
    }

    private void listPatients() {
        System.out.println(colors.get("yellow_bold") + "LISTADO DE PACIENTES" + colors.get("reset"));
        Map<String, java.util.List<Patient>> patientsByUrgency = patientService.getPatientsByUrgency();

        if (patientsByUrgency.isEmpty()) {
            System.out.println("No hay pacientes registrados.");
            return;
        }

        // Mostrar pacientes por nivel de urgencia
        for (Patient.UrgencyLevel level : Patient.UrgencyLevel.values()) {
            String urgencyKey = level.getValue();
            if (patientsByUrgency.containsKey(urgencyKey)) {
                System.out.println(colors.get(level == Patient.UrgencyLevel.ROJO ? "red" : 
                                           level == Patient.UrgencyLevel.AMARILLO ? "yellow" : "green") + 
                                 level.getDisplay() + colors.get("reset"));
                for (Patient patient : patientsByUrgency.get(urgencyKey)) {
                    System.out.println("  ID: " + patient.getId() + " - " + 
                                     patient.getNombre() + " " + patient.getApellido());
                }
            }
        }
    }

    private void addPatient() {
        System.out.println(colors.get("yellow_bold") + "AÑADIR NUEVO PACIENTE" + colors.get("reset"));
        
        System.out.print("Nombre: ");
        String nombre = scanner.nextLine();
        
        System.out.print("Apellido: ");
        String apellido = scanner.nextLine();
        
        System.out.print("Edad: ");
        int edad = Integer.parseInt(scanner.nextLine());
        
        System.out.print("Síntomas: ");
        String sintomas = scanner.nextLine();
        
        Patient.UrgencyLevel urgencia = selectUrgencyLevel();
        
        Patient patient = patientService.addPatient(nombre, apellido, edad, sintomas, urgencia);
        System.out.println(colors.get("green") + "Paciente añadido correctamente con ID: " + 
                         patient.getId() + colors.get("reset"));
    }

    private Patient.UrgencyLevel selectUrgencyLevel() {
        while (true) {
            System.out.println(colors.get("yellow_bold") + "Nivel de urgencia:" + colors.get("reset"));
            System.out.println(colors.get("red") + "1. 🔴 Rojo (crítico, atención inmediata)" + colors.get("reset"));
            System.out.println(colors.get("yellow") + "2. 🟡 Amarillo (urgente, puede esperar)" + colors.get("reset"));
            System.out.println(colors.get("green") + "3. 🟢 Verde (leve, última prioridad)" + colors.get("reset"));
            System.out.print("Seleccione [1-3]: ");
            
            String opcion = scanner.nextLine();
            return switch (opcion) {
                case "1" -> Patient.UrgencyLevel.ROJO;
                case "2" -> Patient.UrgencyLevel.AMARILLO;
                case "3" -> Patient.UrgencyLevel.VERDE;
                default -> {
                    System.out.println(colors.get("red") + "Opción no válida. Intente de nuevo." + colors.get("reset"));
                    yield selectUrgencyLevel();
                }
            };
        }
    }

    private void showPatient(String[] parts) {
        if (parts.length < 3) {
            System.out.println(colors.get("red") + "Error: Se requiere un ID de paciente" + colors.get("reset"));
            return;
        }

        String id = parts[2];
        patientService.getPatient(id).ifPresentOrElse(
            patient -> {
                System.out.println(colors.get("yellow_bold") + "INFORMACIÓN DEL PACIENTE" + colors.get("reset"));
                System.out.println(colors.get("green") + "ID: " + colors.get("reset") + patient.getId());
                System.out.println(colors.get("green") + "Nombre: " + colors.get("reset") + patient.getNombre());
                System.out.println(colors.get("green") + "Apellido: " + colors.get("reset") + patient.getApellido());
                System.out.println(colors.get("green") + "Edad: " + colors.get("reset") + patient.getEdad());
                System.out.println(colors.get("green") + "Urgencia: " + colors.get("reset") + patient.getUrgencia().getDisplay());
                System.out.println(colors.get("green") + "Síntomas: " + colors.get("reset") + patient.getSintomas());
            },
            () -> System.out.println(colors.get("red") + "No se encontró ningún paciente con ID: " + id + colors.get("reset"))
        );
    }

    private void attendPatient() {
        patientService.attendNextPatient().ifPresentOrElse(
            patient -> {
                System.out.println(colors.get("yellow_bold") + "ATENDIENDO PACIENTE" + colors.get("reset"));
                System.out.println(colors.get("green") + "ID: " + colors.get("reset") + patient.getId());
                System.out.println(colors.get("green") + "Nombre: " + colors.get("reset") + patient.getNombre());
                System.out.println(colors.get("green") + "Apellido: " + colors.get("reset") + patient.getApellido());
                System.out.println(colors.get("green") + "Urgencia: " + colors.get("reset") + patient.getUrgencia().getDisplay());

                System.out.print("¿Desea atender a este paciente? (S/N): ");
                if (scanner.nextLine().equalsIgnoreCase("S")) {
                    patientService.confirmAttendance(patient);
                    System.out.println(colors.get("green") + "Paciente atendido correctamente y movido al historial." + colors.get("reset"));
                } else {
                    System.out.println(colors.get("yellow") + "Atención cancelada. El paciente permanecerá en la lista." + colors.get("reset"));
                }
            },
            () -> System.out.println("No hay pacientes para atender.")
        );
    }

    private void handleAdminCommand(String[] parts) {
        if (parts.length == 1) {
            showAdminHelp();
            return;
        }

        switch (parts[1]) {
            case "login" -> loginAdmin();
            case "logout" -> logoutAdmin();
            default -> System.out.println(colors.get("red") + "Subcomando de administrador no reconocido" + colors.get("reset"));
        }
    }

    private void loginAdmin() {
        System.out.print("Contraseña: ");
        String password = scanner.nextLine();
        
        String correctPassword = System.getenv().getOrDefault("ADMIN_PASSWORD", "secreto");
        
        if (password.equals(correctPassword)) {
            adminMode = true;
            System.out.println(colors.get("green") + "Modo administrador activado" + colors.get("reset"));
        } else {
            System.out.println(colors.get("red") + "Contraseña incorrecta" + colors.get("reset"));
        }
    }

    private void logoutAdmin() {
        adminMode = false;
        System.out.println(colors.get("green") + "Modo administrador desactivado" + colors.get("reset"));
    }

    private void showHelp() {
        System.out.println(colors.get("yellow_bold") + "COMANDOS DE GESTIÓN DE PACIENTES" + colors.get("reset"));
        System.out.println(colors.get("green") + "  patient list      " + colors.get("reset") + "- Listar todos los pacientes");
        System.out.println(colors.get("green") + "  patient add       " + colors.get("reset") + "- Añadir un nuevo paciente");
        System.out.println(colors.get("green") + "  patient show ID   " + colors.get("reset") + "- Mostrar detalles de un paciente");
        System.out.println(colors.get("green") + "  patient attend    " + colors.get("reset") + "- Atender paciente con mayor prioridad");
        System.out.println("");
        System.out.println(colors.get("yellow_bold") + "COMANDOS GENERALES" + colors.get("reset"));
        System.out.println("  help, info          - Muestra esta ayuda");
        System.out.println("  exit, quit          - Salir de la aplicación");
        System.out.println("");
        System.out.println(colors.get("yellow_bold") + "COMANDOS ADMINISTRADOR:" + colors.get("reset"));
        System.out.println("  admin login         - Acceder al modo administrador");
        if (adminMode) {
            System.out.println("  admin logout        - Salir del modo administrador");
        }
    }

    private void showPatientHelp() {
        System.out.println(colors.get("yellow_bold") + "COMANDOS DE GESTIÓN DE PACIENTES" + colors.get("reset"));
        System.out.println(colors.get("green") + "  patient list      " + colors.get("reset") + "- Listar pacientes");
        System.out.println(colors.get("green") + "  patient add       " + colors.get("reset") + "- Añadir paciente");
        System.out.println(colors.get("green") + "  patient show ID   " + colors.get("reset") + "- Mostrar paciente por ID");
        System.out.println(colors.get("green") + "  patient attend    " + colors.get("reset") + "- Atender paciente con mayor prioridad");
    }

    private void showAdminHelp() {
        System.out.println(colors.get("yellow_bold") + "COMANDOS ADMINISTRADOR" + colors.get("reset"));
        System.out.println("  admin login         - Acceder al modo administrador");
        if (adminMode) {
            System.out.println("  admin logout        - Salir del modo administrador");
        }
    }
} 