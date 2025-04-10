package net.elpuig.triager.controller;

import net.elpuig.triager.model.Patient;
import net.elpuig.triager.service.PatientService;
import net.elpuig.triager.config.MensajeApp;
import net.elpuig.triager.config.EnvConfig;
import net.elpuig.triager.redis.RedisTest;
import org.springframework.stereotype.Controller;

import java.util.Map;
import java.util.Scanner;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.Set;

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
            case "redis" -> {
                if (adminMode) {
                    handleRedisCommand(parts);
                } else {
                    System.out.println(colors.get("red") + "Los comandos de Redis solo están disponibles en modo administrador" + colors.get("reset"));
                    System.out.println(colors.get("yellow") + "Use 'admin login' para acceder al modo administrador" + colors.get("reset"));
                }
            }
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

        // Si no es modo admin y no es login, mostrar mensaje
        if (!adminMode && !parts[1].equals("login")) {
            System.out.println(colors.get("red") + "Necesitas acceder como administrador primero" + colors.get("reset"));
            System.out.println(colors.get("yellow") + "Use 'admin login' para acceder al modo administrador" + colors.get("reset"));
            return;
        }

        switch (parts[1]) {
            case "login" -> loginAdmin();
            case "logout" -> logoutAdmin();
            case "config" -> configCommand(parts);
            case "reset" -> resetCommand(parts);
            case "backup" -> backupCommand(parts);
            case "logs" -> logsCommand(parts);
            case "system" -> systemCommand(parts);
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
            
            // Mostrar los comandos disponibles al iniciar sesión
            System.out.println(colors.get("yellow_bold") + "COMANDOS DISPONIBLES EN MODO ADMINISTRADOR:" + colors.get("reset"));
            System.out.println("  admin logout        - Salir del modo administrador");
            System.out.println("  admin config        - Gestionar configuración");
            System.out.println("  admin reset         - Reiniciar sistema");
            System.out.println("  admin backup        - Gestionar copias de seguridad");
            System.out.println("  admin logs          - Ver registros del sistema");
            System.out.println("  admin system        - Ver información del sistema");
            
            System.out.println("");
            System.out.println(colors.get("yellow_bold") + "COMANDOS REDIS (ADMIN):" + colors.get("reset"));
            System.out.println("  redis test          - Probar conexión a Redis");
            System.out.println("  redis set key value - Guardar valor en Redis");
            System.out.println("  redis get key       - Obtener valor de Redis");
            System.out.println("  redis keys          - Listar todas las claves");
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
            System.out.println("  admin config        - Gestionar configuración");
            System.out.println("  admin reset         - Reiniciar sistema");
            System.out.println("  admin backup        - Gestionar copias de seguridad");
            System.out.println("  admin logs          - Ver registros del sistema");
            System.out.println("  admin system        - Ver información del sistema");
            
            System.out.println("");
            System.out.println(colors.get("yellow_bold") + "COMANDOS REDIS (ADMIN):" + colors.get("reset"));
            System.out.println("  redis test          - Probar conexión a Redis");
            System.out.println("  redis set key value - Guardar valor en Redis");
            System.out.println("  redis get key       - Obtener valor de Redis");
            System.out.println("  redis keys          - Listar todas las claves");
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
            System.out.println("  admin config        - Gestionar configuración");
            System.out.println("  admin reset         - Reiniciar sistema");
            System.out.println("  admin backup        - Gestionar copias de seguridad");
            System.out.println("  admin logs          - Ver registros del sistema");
            System.out.println("  admin system        - Ver información del sistema");
        }
    }

    private void handleRedisCommand(String[] parts) {
        String red = colors.get("red");
        String green = colors.get("green");
        String yellow = colors.get("yellow");
        String reset = colors.get("reset");
        
        // Si solo se escribe "redis", mostrar ayuda
        if (parts.length == 1) {
            System.out.println(yellow + "COMANDOS REDIS DISPONIBLES:" + reset);
            System.out.println("  redis test          - Probar conexión a Redis");
            System.out.println("  redis set key valor - Guardar valor en Redis");
            System.out.println("  redis get key       - Obtener valor de Redis");
            System.out.println("  redis keys          - Listar todas las claves");
            return;
        }
        
        // Procesar subcomandos
        // Creamos Map con la configuración del entorno
        Map<String, String> envConfig = new HashMap<>();
        envConfig.put("REDIS_HOST", EnvConfig.get("REDIS_HOST", "localhost"));
        envConfig.put("REDIS_PORT", EnvConfig.get("REDIS_PORT", "6379"));
        envConfig.put("REDIS_PASSWORD", EnvConfig.get("REDIS_PASSWORD", ""));
        
        RedisTest redisTest = new RedisTest(envConfig);
        
        switch (parts[1]) {
            case "test" -> {
                boolean connected = redisTest.testConnection();
                if (connected) {
                    System.out.println(green + "✓ Conexión a Redis exitosa" + reset);
                } else {
                    System.out.println(red + "✗ No se pudo conectar a Redis" + reset);
                }
            }
            case "set" -> {
                if (parts.length < 4) {
                    System.out.println(red + "Error: Formato correcto es 'redis set clave valor'" + reset);
                    return;
                }
                String key = parts[2];
                String value = String.join(" ", Arrays.copyOfRange(parts, 3, parts.length));
                redisTest.setValue(key, value);
                System.out.println(green + "Valor guardado correctamente" + reset);
            }
            case "get" -> {
                if (parts.length < 3) {
                    System.out.println(red + "Error: Formato correcto es 'redis get clave'" + reset);
                    return;
                }
                String key = parts[2];
                String value = redisTest.getValue(key);
                if (value != null) {
                    System.out.println(key + " = " + value);
                } else {
                    System.out.println(yellow + "La clave no existe en Redis" + reset);
                }
            }
            case "keys" -> {
                Set<String> keys = redisTest.getAllKeys();
                if (keys.isEmpty()) {
                    System.out.println(yellow + "No hay claves en Redis" + reset);
                } else {
                    System.out.println(yellow + "CLAVES EN REDIS:" + reset);
                    for (String key : keys) {
                        System.out.println("  " + key);
                    }
                }
            }
            default -> System.out.println(red + "Subcomando de Redis no reconocido: " + parts[1] + reset);
        }
    }

    /**
     * Gestión de configuración del sistema
     */
    private void configCommand(String[] parts) {
        String yellowBold = colors.get("yellow_bold");
        String green = colors.get("green");
        String red = colors.get("red");
        String reset = colors.get("reset");
        
        if (parts.length == 2) {
            System.out.println(yellowBold + "GESTIÓN DE CONFIGURACIÓN" + reset);
            System.out.println("  admin config show    - Mostrar configuración actual");
            System.out.println("  admin config set     - Modificar parámetro de configuración");
            System.out.println("  admin config reload  - Recargar configuración desde .env");
            return;
        }
        
        switch (parts[2]) {
            case "show" -> {
                System.out.println(yellowBold + "CONFIGURACIÓN ACTUAL:" + reset);
                System.out.println(green + "Redis Host: " + reset + EnvConfig.get("REDIS_HOST", "localhost"));
                System.out.println(green + "Redis Port: " + reset + EnvConfig.get("REDIS_PORT", "6379"));
                System.out.println(green + "Admin Password: " + reset + "********");
                
                // Mostrar también variables de entorno de Spring
                System.out.println("\n" + yellowBold + "CONFIGURACIÓN DE SPRING:" + reset);
                System.out.println(green + "spring.redis.host: " + reset + System.getProperty("spring.redis.host", EnvConfig.get("REDIS_HOST", "localhost")));
                System.out.println(green + "spring.redis.port: " + reset + System.getProperty("spring.redis.port", EnvConfig.get("REDIS_PORT", "6379")));
                System.out.println(green + "spring.application.name: " + reset + System.getProperty("spring.application.name", "triager"));
            }
            case "set" -> {
                if (parts.length < 5) {
                    System.out.println(red + "Error: Se requiere clave y valor" + reset);
                    System.out.println("Uso: admin config set CLAVE VALOR");
                    return;
                }
                String key = parts[3];
                String value = parts[4];
                
                System.out.println(yellowBold + "ADVERTENCIA: " + reset + "Esta operación modifica solo la configuración en memoria.");
                System.out.println("Para cambios permanentes, modifica el archivo .env o las variables de entorno.");
                
                // Simulación de cambio de configuración
                System.out.println(green + "Configuración actualizada: " + key + " = " + value + reset);
                System.out.println("(Nota: Este cambio es temporal y se perderá al reiniciar la aplicación)");
            }
            case "reload" -> {
                System.out.println(yellowBold + "Recargando configuración desde archivo .env..." + reset);
                // Simulamos la recarga
                System.out.println(green + "Configuración recargada correctamente" + reset);
            }
            default -> System.out.println(red + "Subcomando de configuración no reconocido" + reset);
        }
    }

    /**
     * Reinicio del sistema
     */
    private void resetCommand(String[] parts) {
        String yellowBold = colors.get("yellow_bold");
        String green = colors.get("green");
        String red = colors.get("red");
        String reset = colors.get("reset");
        
        if (parts.length == 2) {
            System.out.println(yellowBold + "REINICIO DEL SISTEMA" + reset);
            System.out.println("  admin reset app       - Reiniciar aplicación");
            System.out.println("  admin reset redis     - Reiniciar conexión a Redis");
            System.out.println("  admin reset all       - Reiniciar todo");
            return;
        }
        
        Scanner scanner = new Scanner(System.in);
        System.out.print(red + "Esta operación puede interrumpir el servicio. ¿Estás seguro? (S/N): " + reset);
        String confirmacion = scanner.nextLine();
        
        if (!confirmacion.equalsIgnoreCase("S")) {
            System.out.println(yellowBold + "Operación cancelada." + reset);
            return;
        }
        
        switch (parts[2]) {
            case "app" -> {
                System.out.println(yellowBold + "Reiniciando aplicación..." + reset);
                // Simulamos un reinicio
                System.out.println(green + "Aplicación reiniciada correctamente" + reset);
            }
            case "redis" -> {
                System.out.println(yellowBold + "Reiniciando conexión a Redis..." + reset);
                // Creamos la configuración para Redis
                Map<String, String> envConfig = new HashMap<>();
                envConfig.put("REDIS_HOST", EnvConfig.get("REDIS_HOST", "localhost"));
                envConfig.put("REDIS_PORT", EnvConfig.get("REDIS_PORT", "6379"));
                envConfig.put("REDIS_PASSWORD", EnvConfig.get("REDIS_PASSWORD", ""));
                
                RedisTest redisTest = new RedisTest(envConfig);
                boolean redisConnected = redisTest.testConnection();
                if (redisConnected) {
                    System.out.println(green + "Conexión a Redis reiniciada correctamente" + reset);
                } else {
                    System.out.println(red + "Error al reconectar con Redis" + reset);
                }
            }
            case "all" -> {
                System.out.println(yellowBold + "Reiniciando todo el sistema..." + reset);
                Map<String, String> envConfig = new HashMap<>();
                envConfig.put("REDIS_HOST", EnvConfig.get("REDIS_HOST", "localhost"));
                envConfig.put("REDIS_PORT", EnvConfig.get("REDIS_PORT", "6379"));
                envConfig.put("REDIS_PASSWORD", EnvConfig.get("REDIS_PASSWORD", ""));
                
                RedisTest redisTest = new RedisTest(envConfig);
                boolean redisConnected = redisTest.testConnection();
                if (redisConnected) {
                    System.out.println(green + "Sistema reiniciado correctamente" + reset);
                } else {
                    System.out.println(red + "Error al reiniciar el sistema" + reset);
                }
            }
            default -> System.out.println(red + "Subcomando de reinicio no reconocido" + reset);
        }
    }

    /**
     * Gestión de copias de seguridad
     */
    private void backupCommand(String[] parts) {
        String yellowBold = colors.get("yellow_bold");
        String green = colors.get("green");
        String red = colors.get("red");
        String reset = colors.get("reset");
        
        if (parts.length == 2) {
            System.out.println(yellowBold + "GESTIÓN DE COPIAS DE SEGURIDAD" + reset);
            System.out.println("  admin backup create   - Crear copia de seguridad");
            System.out.println("  admin backup list     - Listar copias de seguridad");
            System.out.println("  admin backup restore  - Restaurar copia de seguridad");
            return;
        }
        
        // Creamos la configuración para Redis
        Map<String, String> envConfig = new HashMap<>();
        envConfig.put("REDIS_HOST", EnvConfig.get("REDIS_HOST", "localhost"));
        envConfig.put("REDIS_PORT", EnvConfig.get("REDIS_PORT", "6379"));
        envConfig.put("REDIS_PASSWORD", EnvConfig.get("REDIS_PASSWORD", ""));
        RedisTest redisTest = new RedisTest(envConfig);
        
        switch (parts[2]) {
            case "create" -> {
                String backupId = "backup_" + System.currentTimeMillis();
                System.out.println(yellowBold + "Creando copia de seguridad..." + reset);
                
                // Simulamos la creación de una copia de seguridad
                Set<String> keys = redisTest.getAllKeys();
                int totalKeys = keys.size();
                
                System.out.println(green + "Copia de seguridad " + backupId + " creada correctamente" + reset);
                System.out.println("Total de claves guardadas: " + totalKeys);
            }
            case "list" -> {
                System.out.println(yellowBold + "COPIAS DE SEGURIDAD DISPONIBLES:" + reset);
                System.out.println(green + "No hay copias de seguridad disponibles" + reset);
                System.out.println("(En una implementación real, aquí se listarían los backups)");
            }
            case "restore" -> {
                if (parts.length < 4) {
                    System.out.println(red + "Error: Se requiere ID de la copia de seguridad" + reset);
                    System.out.println("Uso: admin backup restore BACKUP_ID");
                    return;
                }
                
                String backupId = parts[3];
                Scanner scanner = new Scanner(System.in);
                System.out.print(red + "¿Estás seguro de querer restaurar esta copia? Los datos actuales se perderán (S/N): " + reset);
                String confirmacion = scanner.nextLine();
                
                if (!confirmacion.equalsIgnoreCase("S")) {
                    System.out.println(yellowBold + "Operación cancelada." + reset);
                    return;
                }
                
                System.out.println(yellowBold + "Restaurando copia de seguridad " + backupId + "..." + reset);
                System.out.println(green + "Copia de seguridad restaurada correctamente" + reset);
            }
            default -> System.out.println(red + "Subcomando de copia de seguridad no reconocido" + reset);
        }
    }

    /**
     * Visualización de logs
     */
    private void logsCommand(String[] parts) {
        String yellowBold = colors.get("yellow_bold");
        String green = colors.get("green");
        String red = colors.get("red");
        String cyan = colors.get("cyan");
        String reset = colors.get("reset");
        
        if (parts.length == 2) {
            System.out.println(yellowBold + "VISUALIZACIÓN DE LOGS" + reset);
            System.out.println("  admin logs show      - Mostrar logs recientes");
            System.out.println("  admin logs search    - Buscar en los logs");
            System.out.println("  admin logs clear     - Limpiar logs");
            return;
        }
        
        switch (parts[2]) {
            case "show" -> {
                System.out.println(yellowBold + "LOGS DEL SISTEMA (últimas 10 entradas):" + reset);
                
                // Simulamos mostrar logs
                System.out.println(cyan + "[INFO] " + reset + "2023-10-15 10:00:23 - Aplicación iniciada correctamente");
                System.out.println(cyan + "[INFO] " + reset + "2023-10-15 10:01:45 - Conexión a Redis establecida");
                System.out.println(green + "[DEBUG] " + reset + "2023-10-15 10:02:12 - Cargando configuración desde .env");
                System.out.println(cyan + "[INFO] " + reset + "2023-10-15 10:05:31 - Usuario administrador ha iniciado sesión");
                System.out.println(yellowBold + "[WARN] " + reset + "2023-10-15 10:10:45 - Intento de acceso con contraseña incorrecta");
            }
            case "search" -> {
                if (parts.length < 4) {
                    System.out.println(red + "Error: Se requiere un término de búsqueda" + reset);
                    System.out.println("Uso: admin logs search TÉRMINO");
                    return;
                }
                
                String searchTerm = parts[3];
                System.out.println(yellowBold + "RESULTADOS DE BÚSQUEDA PARA '" + searchTerm + "':" + reset);
                
                // Simulamos una búsqueda en los logs
                System.out.println("No se encontraron coincidencias para '" + searchTerm + "'");
            }
            case "clear" -> {
                Scanner scanner = new Scanner(System.in);
                System.out.print(red + "¿Estás seguro de querer eliminar todos los logs? (S/N): " + reset);
                String confirmacion = scanner.nextLine();
                
                if (!confirmacion.equalsIgnoreCase("S")) {
                    System.out.println(yellowBold + "Operación cancelada." + reset);
                    return;
                }
                
                System.out.println(yellowBold + "Limpiando logs del sistema..." + reset);
                System.out.println(green + "Logs eliminados correctamente" + reset);
            }
            default -> System.out.println(red + "Subcomando de logs no reconocido" + reset);
        }
    }

    /**
     * Información del sistema
     */
    private void systemCommand(String[] parts) {
        String yellowBold = colors.get("yellow_bold");
        String green = colors.get("green");
        String red = colors.get("red");
        String reset = colors.get("reset");
        
        if (parts.length == 2) {
            System.out.println(yellowBold + "INFORMACIÓN DEL SISTEMA" + reset);
            System.out.println("  admin system info     - Mostrar información general");
            System.out.println("  admin system stats    - Mostrar estadísticas");
            System.out.println("  admin system users    - Gestionar usuarios");
            return;
        }
        
        // Creamos la configuración para Redis
        Map<String, String> envConfig = new HashMap<>();
        envConfig.put("REDIS_HOST", EnvConfig.get("REDIS_HOST", "localhost"));
        envConfig.put("REDIS_PORT", EnvConfig.get("REDIS_PORT", "6379"));
        envConfig.put("REDIS_PASSWORD", EnvConfig.get("REDIS_PASSWORD", ""));
        RedisTest redisTest = new RedisTest(envConfig);
        
        switch (parts[2]) {
            case "info" -> {
                System.out.println(yellowBold + "INFORMACIÓN GENERAL DEL SISTEMA:" + reset);
                
                Runtime runtime = Runtime.getRuntime();
                long memTotal = runtime.totalMemory() / (1024 * 1024);
                long memFree = runtime.freeMemory() / (1024 * 1024);
                long memUsed = memTotal - memFree;
                
                System.out.println(green + "Sistema operativo: " + reset + System.getProperty("os.name") + " " + System.getProperty("os.version"));
                System.out.println(green + "Versión de Java: " + reset + System.getProperty("java.version"));
                System.out.println(green + "Memoria total: " + reset + memTotal + " MB");
                System.out.println(green + "Memoria usada: " + reset + memUsed + " MB");
                System.out.println(green + "Memoria libre: " + reset + memFree + " MB");
                System.out.println(green + "Procesadores disponibles: " + reset + runtime.availableProcessors());
                
                // Información de Redis
                String redisHost = EnvConfig.get("REDIS_HOST", "localhost");
                String redisPort = EnvConfig.get("REDIS_PORT", "6379");
                System.out.println(green + "Redis: " + reset + redisHost + ":" + redisPort);
                
                boolean redisConnected = redisTest.testConnection();
                System.out.println(green + "Estado de Redis: " + reset + (redisConnected ? "Conectado" : "Desconectado"));
            }
            case "stats" -> {
                System.out.println(yellowBold + "ESTADÍSTICAS DEL SISTEMA:" + reset);
                
                // Simulamos estadísticas
                int totalPatients = 0;
                int criticalPatients = 0;
                int urgentPatients = 0;
                int normalPatients = 0;
                
                Set<String> keys = redisTest.getAllKeys();
                for (String key : keys) {
                    if (key.startsWith("patient:")) {
                        totalPatients++;
                        Map<String, String> patientData = redisTest.getHash(key);
                        if (patientData != null) {
                            String urgencia = patientData.getOrDefault("urgencia", "verde");
                            
                            switch (urgencia) {
                                case "rojo" -> criticalPatients++;
                                case "amarillo" -> urgentPatients++;
                                case "verde" -> normalPatients++;
                            }
                        }
                    }
                }
                
                System.out.println(green + "Total de pacientes: " + reset + totalPatients);
                System.out.println(red + "Pacientes críticos: " + reset + criticalPatients);
                System.out.println(yellowBold + "Pacientes urgentes: " + reset + urgentPatients);
                System.out.println(green + "Pacientes normales: " + reset + normalPatients);
                
                // Tiempo de servicio (simulado)
                System.out.println(green + "Tiempo medio de atención: " + reset + "15 minutos");
                System.out.println(green + "Tiempo de espera estimado: " + reset + ((criticalPatients * 5) + (urgentPatients * 15) + (normalPatients * 30)) + " minutos");
            }
            case "users" -> {
                if (parts.length < 4) {
                    System.out.println(yellowBold + "GESTIÓN DE USUARIOS:" + reset);
                    System.out.println("  admin system users list     - Listar usuarios");
                    System.out.println("  admin system users add      - Añadir usuario");
                    System.out.println("  admin system users remove   - Eliminar usuario");
                    return;
                }
                
                switch (parts[3]) {
                    case "list" -> {
                        System.out.println(yellowBold + "USUARIOS DEL SISTEMA:" + reset);
                        System.out.println(green + "- admin" + reset + " (administrador)");
                        System.out.println(green + "- enfermero1" + reset + " (personal médico)");
                        System.out.println(green + "- recepcion" + reset + " (recepción)");
                    }
                    case "add" -> {
                        System.out.println(red + "Función no implementada" + reset);
                        System.out.println("En una implementación real, aquí se añadirían nuevos usuarios");
                    }
                    case "remove" -> {
                        System.out.println(red + "Función no implementada" + reset);
                        System.out.println("En una implementación real, aquí se eliminarían usuarios");
                    }
                    default -> System.out.println(red + "Subcomando de usuarios no reconocido" + reset);
                }
            }
            default -> System.out.println(red + "Subcomando de sistema no reconocido" + reset);
        }
    }
} 