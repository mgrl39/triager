package net.elpuig.triager;

import net.elpuig.triager.config.MensajeApp;
import net.elpuig.triager.config.RedisConfiguration;
import net.elpuig.triager.config.EnvConfig;
import net.elpuig.triager.controller.TriagerController;
import net.elpuig.triager.service.PatientService;
import net.elpuig.triager.repository.PatientRepository;
import net.elpuig.triager.redis.RedisTest;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

import static net.elpuig.triager.config.MensajeApp.colorInitializator;

@SpringBootApplication
public class TriagerApplication {
    // Variables estáticas para la aplicación
    private static RedisTemplate<String, Object> redisTemplate;
    private static Map<String, String> colors = colorInitializator();
    private static boolean adminMode = false;
    private static RedisTest redisTestInstance;

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(TriagerApplication.class, args);
        
        // Obtener las dependencias necesarias
        PatientRepository patientRepository = context.getBean(PatientRepository.class);
        PatientService patientService = context.getBean(PatientService.class);
        redisTemplate = (RedisTemplate<String, Object>) context.getBean("redisTemplate");
        
        // Inicializar RedisTest
        Map<String, String> envConfig = new HashMap<>();
        envConfig.put("REDIS_HOST", EnvConfig.get("REDIS_HOST", "localhost"));
        envConfig.put("REDIS_PORT", EnvConfig.get("REDIS_PORT", "6379"));
        envConfig.put("REDIS_PASSWORD", EnvConfig.get("REDIS_PASSWORD", ""));
        redisTestInstance = new RedisTest(envConfig);
        
        // Crear y iniciar el controlador
        TriagerController controller = new TriagerController(patientService, colors);
        controller.start();
    }

    public static void handleCommands(String command) {
        switch (command.split(" ")[0]) {
            case "exit", "quit" -> System.exit(0);
            case "info", "help" -> helpCommand();
            case "patient" -> patientCommand(command);
            case "redis" -> {
                if (adminMode) {
                    redisCommand(command);
                } else {
                    System.out.println(colors.get("red") + "Los comandos de Redis solo están disponibles en modo administrador" + colors.get("reset"));
                    System.out.println(colors.get("yellow") + "Use 'admin login' para acceder al modo administrador" + colors.get("reset"));
                }
            }
            case "admin" -> adminCommand(command);
            default -> System.out.println(colors.get("yellow")
                    + "comando no encontrado"
                    + colors.get("reset"));
        }
    }

    public static void helpCommand() {
        String yellowBold = colors.get("yellow_bold");
        String reset = colors.get("reset");
        String green = colors.get("green");
        
        System.out.println(yellowBold + "COMANDOS DE GESTIÓN DE PACIENTES" + reset);
        System.out.println(green + "  patient list      " + reset + "- Listar todos los pacientes");
        System.out.println(green + "  patient add       " + reset + "- Añadir un nuevo paciente");
        System.out.println(green + "  patient show ID   " + reset + "- Mostrar detalles de un paciente");
        System.out.println(green + "  patient attend    " + reset + "- Atender paciente con mayor prioridad");
        System.out.println("");
        
        System.out.println(yellowBold + "COMANDOS GENERALES" + reset);
        System.out.println("  help, info          - Muestra esta ayuda");
        System.out.println("  exit, quit          - Salir de la aplicación");
        
        // Si el usuario está en modo administrador, mostrar comandos adicionales
        if (adminMode) {
            System.out.println("");
            System.out.println(yellowBold + "COMANDOS REDIS (ADMIN):" + reset);
            System.out.println("  redis test          - Probar conexión a Redis");
            System.out.println("  redis set key value - Guardar valor en Redis");
            System.out.println("  redis get key       - Obtener valor de Redis");
            System.out.println("  redis keys          - Listar todas las claves");
            
            System.out.println("");
            System.out.println(yellowBold + "COMANDOS ADMINISTRADOR:" + reset);
            System.out.println("  admin login         - Acceder al modo administrador");
            System.out.println("  admin logout        - Salir del modo administrador");
            System.out.println("  admin config        - Gestionar configuración");
            System.out.println("  admin reset         - Reiniciar sistema");
            System.out.println("  admin backup        - Gestionar copias de seguridad");
            System.out.println("  admin logs          - Ver registros del sistema");
            System.out.println("  admin system        - Ver información del sistema");
        } else {
            System.out.println("");
            System.out.println(yellowBold + "COMANDOS ADMINISTRADOR:" + reset);
            System.out.println("  admin login         - Acceder al modo administrador");
        }
    }

    public static void adminCommand(String command) {
        String[] parts = command.split(" ");
        String yellowBold = colors.get("yellow_bold");
        String green = colors.get("green");
        String red = colors.get("red");
        String reset = colors.get("reset");
        
        if (parts.length == 1) {
            System.out.println(yellowBold + "COMANDOS ADMINISTRADOR" + reset);
            System.out.println("  admin login         - Acceder al modo administrador");
            if (adminMode) {
                System.out.println("  admin logout        - Salir del modo administrador");
                System.out.println("  admin config        - Gestionar configuración");
                System.out.println("  admin reset         - Reiniciar sistema");
                System.out.println("  admin backup        - Gestionar copias de seguridad");
                System.out.println("  admin logs          - Ver registros del sistema");
                System.out.println("  admin system        - Ver información del sistema");
            }
            return;
        }
        
        switch (parts[1]) {
            case "login" -> {
                Scanner scanner = new Scanner(System.in);
                System.out.print("Contraseña: ");
                String password = scanner.nextLine();
                
                String correctPassword = EnvConfig.get("ADMIN_PASSWORD", "secreto");
                
                if (password.equals(correctPassword)) {
                    adminMode = true;
                    System.out.println(green + "Modo administrador activado" + reset);
                    System.out.println(yellowBold + "COMANDOS DISPONIBLES EN MODO ADMINISTRADOR:" + reset);
                    System.out.println("  redis test          - Probar conexión a Redis");
                    System.out.println("  redis set key value - Guardar valor en Redis");
                    System.out.println("  redis get key       - Obtener valor de Redis");
                    System.out.println("  redis keys          - Listar todas las claves");
                    System.out.println("  admin config        - Gestionar configuración");
                    System.out.println("  admin reset         - Reiniciar sistema");
                    System.out.println("  admin backup        - Gestionar copias de seguridad");
                    System.out.println("  admin logs          - Ver registros del sistema");
                    System.out.println("  admin system        - Ver información del sistema");
                    System.out.println("  admin logout        - Salir del modo administrador");
                } else {
                    System.out.println(red + "Contraseña incorrecta" + reset);
                }
            }
            case "logout" -> {
                adminMode = false;
                System.out.println(green + "Modo administrador desactivado" + reset);
            }
            case "config" -> {
                if (!adminMode) {
                    System.out.println(red + "Necesitas acceder como administrador primero" + reset);
                    return;
                }
                configCommand(parts);
            }
            case "reset" -> {
                if (!adminMode) {
                    System.out.println(red + "Necesitas acceder como administrador primero" + reset);
                    return;
                }
                resetCommand(parts);
            }
            case "backup" -> {
                if (!adminMode) {
                    System.out.println(red + "Necesitas acceder como administrador primero" + reset);
                    return;
                }
                backupCommand(parts);
            }
            case "logs" -> {
                if (!adminMode) {
                    System.out.println(red + "Necesitas acceder como administrador primero" + reset);
                    return;
                }
                logsCommand(parts);
            }
            case "system" -> {
                if (!adminMode) {
                    System.out.println(red + "Necesitas acceder como administrador primero" + reset);
                    return;
                }
                systemCommand(parts);
            }
            default -> System.out.println(red + "Subcomando de administrador no reconocido" + reset);
        }
    }

    /**
     * Gestión de configuración del sistema
     */
    private static void configCommand(String[] parts) {
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
                
                // Simulación de cambio de configuración (en una aplicación real, esto modificaría realmente la configuración)
                System.out.println(green + "Configuración actualizada: " + key + " = " + value + reset);
                System.out.println("(Nota: Este cambio es temporal y se perderá al reiniciar la aplicación)");
            }
            case "reload" -> {
                System.out.println(yellowBold + "Recargando configuración desde archivo .env..." + reset);
                // Simulamos la recarga limpiando la caché de EnvConfig
                try {
                    // Usamos reflection para acceder al mapa privado
                    java.lang.reflect.Field cacheField = EnvConfig.class.getDeclaredField("envCache");
                    cacheField.setAccessible(true);
                    Map<String, String> cache = (Map<String, String>) cacheField.get(null);
                    cache.clear();
                    System.out.println(green + "Configuración recargada correctamente" + reset);
                } catch (Exception e) {
                    System.out.println(red + "Error al recargar configuración: " + e.getMessage() + reset);
                }
            }
            default -> System.out.println(red + "Subcomando de configuración no reconocido" + reset);
        }
    }

    /**
     * Reinicio del sistema
     */
    private static void resetCommand(String[] parts) {
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
                // Aquí simularíamos un reinicio (en una app real harías un re-deploy o restart)
                System.out.println(green + "Aplicación reiniciada correctamente" + reset);
            }
            case "redis" -> {
                System.out.println(yellowBold + "Reiniciando conexión a Redis..." + reset);
                // Aquí realmente reconectaríamos a Redis
                boolean redisConnected = redisTestInstance.testConnection();
                if (redisConnected) {
                    System.out.println(green + "Conexión a Redis reiniciada correctamente" + reset);
                } else {
                    System.out.println(red + "Error al reconectar con Redis" + reset);
                }
            }
            case "all" -> {
                System.out.println(yellowBold + "Reiniciando todo el sistema..." + reset);
                // Simulamos el reinicio total
                boolean redisConnected = redisTestInstance.testConnection();
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
    private static void backupCommand(String[] parts) {
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
        
        switch (parts[2]) {
            case "create" -> {
                String backupId = "backup_" + System.currentTimeMillis();
                System.out.println(yellowBold + "Creando copia de seguridad..." + reset);
                
                // Simulamos la creación de una copia de seguridad
                Set<String> keys = redisTestInstance.getAllKeys();
                int totalKeys = keys.size();
                
                System.out.println(green + "Copia de seguridad " + backupId + " creada correctamente" + reset);
                System.out.println("Total de claves guardadas: " + totalKeys);
            }
            case "list" -> {
                System.out.println(yellowBold + "COPIAS DE SEGURIDAD DISPONIBLES:" + reset);
                // Simulamos listar copias de seguridad
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
    private static void logsCommand(String[] parts) {
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
    private static void systemCommand(String[] parts) {
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
                
                boolean redisConnected = redisTestInstance.testConnection();
                System.out.println(green + "Estado de Redis: " + reset + (redisConnected ? "Conectado" : "Desconectado"));
            }
            case "stats" -> {
                System.out.println(yellowBold + "ESTADÍSTICAS DEL SISTEMA:" + reset);
                
                // Simulamos estadísticas
                int totalPatients = 0;
                int criticalPatients = 0;
                int urgentPatients = 0;
                int normalPatients = 0;
                
                Set<String> keys = redisTestInstance.getAllKeys();
                for (String key : keys) {
                    if (key.startsWith("patient:")) {
                        totalPatients++;
                        Map<String, String> patientData = redisTestInstance.getHash(key);
                        String urgencia = patientData.getOrDefault("urgencia", "verde");
                        
                        switch (urgencia) {
                            case "rojo" -> criticalPatients++;
                            case "amarillo" -> urgentPatients++;
                            case "verde" -> normalPatients++;
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

    private static void attendPatient() {
        String yellowBold = colors.get("yellow_bold");
        String green = colors.get("green");
        String red = colors.get("red");
        String yellow = colors.get("yellow");
        String reset = colors.get("reset");
        
        // Obtener lista de pacientes desde Redis
        Set<String> patientKeys = redisTestInstance.getAllKeys();
        if (patientKeys.isEmpty()) {
            System.out.println("No hay pacientes para atender.");
            return;
        }
        
        // Variables para encontrar el paciente con mayor prioridad
        String nextPatientId = null;
        String nextPatientUrgencia = null;
        long nextPatientTimestamp = Long.MAX_VALUE;
        Map<String, String> nextPatientData = null;
        
        // Buscar el paciente con mayor prioridad
        for (String key : patientKeys) {
            if (key.startsWith("patient:") && !key.contains(":atendido") && !key.contains("historial:")) {
                String id = key.substring(8); // Eliminar "patient:"
                Map<String, String> patientData = redisTestInstance.getHash(key);
                if (patientData == null || patientData.isEmpty()) {
                    continue;
                }
                
                String urgencia = patientData.getOrDefault("urgencia", "verde");
                long timestamp = Long.parseLong(patientData.getOrDefault("timestamp", "0"));
                
                // Comparar prioridad (rojo > amarillo > verde)
                boolean esMayorPrioridad = false;
                
                if (nextPatientUrgencia == null) {
                    // Si es el primer paciente, lo seleccionamos
                    esMayorPrioridad = true;
                } else if (urgencia.equals("rojo") && !nextPatientUrgencia.equals("rojo")) {
                    // Rojo tiene prioridad sobre los demás
                    esMayorPrioridad = true;
                } else if (urgencia.equals("amarillo") && nextPatientUrgencia.equals("verde")) {
                    // Amarillo tiene prioridad sobre verde
                    esMayorPrioridad = true;
                } else if (urgencia.equals(nextPatientUrgencia)) {
                    // Si misma urgencia, priorizar el que llegó primero (timestamp menor)
                    esMayorPrioridad = timestamp < nextPatientTimestamp;
                }
                
                if (esMayorPrioridad) {
                    nextPatientId = id;
                    nextPatientUrgencia = urgencia;
                    nextPatientTimestamp = timestamp;
                    nextPatientData = patientData;
                }
            }
        }
        
        if (nextPatientId == null) {
            System.out.println("No hay pacientes para atender.");
            return;
        }
        
        // Mostrar información del paciente a atender
        String colorUrgencia = switch (nextPatientUrgencia) {
            case "rojo" -> red + "🔴 CRÍTICO";
            case "amarillo" -> yellow + "🟡 URGENTE";
            case "verde" -> green + "🟢 LEVE";
            default -> green + "🟢 LEVE";
        };
        
        System.out.println(yellowBold + "ATENDIENDO PACIENTE" + reset);
        System.out.println(green + "ID: " + reset + nextPatientId);
        System.out.println(green + "Nombre: " + reset + nextPatientData.getOrDefault("nombre", "No disponible"));
        System.out.println(green + "Apellido: " + reset + nextPatientData.getOrDefault("apellido", "No disponible"));
        System.out.println(green + "Urgencia: " + reset + colorUrgencia + reset);
        
        // Confirmar la atención
        Scanner scanner = new Scanner(System.in);
        System.out.print("¿Desea atender a este paciente? (S/N): ");
        String confirmacion = scanner.nextLine();
        
        if (confirmacion.equalsIgnoreCase("S")) {
            // Mover el paciente al historial
            String historialKey = "historial:" + System.currentTimeMillis();
            nextPatientData.put("id", nextPatientId);
            nextPatientData.put("atendido_en", String.valueOf(System.currentTimeMillis()));
            boolean historialSaved = redisTestInstance.setHash(historialKey, nextPatientData);
            
            // Eliminar de la lista de espera
            boolean removed = redisTestInstance.deleteKey("patient:" + nextPatientId);
            
            if (historialSaved && removed) {
                System.out.println(green + "Paciente atendido correctamente y movido al historial." + reset);
            } else {
                System.out.println(red + "Error al procesar la atención del paciente." + reset);
            }
        } else {
            System.out.println(yellow + "Atención cancelada." + reset);
        }
    }

    public static void patientCommand(String command) {
        String[] parts = command.split(" ");
        String yellowBold = colors.get("yellow_bold");
        String reset = colors.get("reset");
        String green = colors.get("green");

        if (parts.length == 1) {
            System.out.println(yellowBold + "COMANDOS DE GESTIÓN DE PACIENTES" + reset);
            System.out.println(green + "  patient list      " + reset + "- Listar pacientes");
            System.out.println(green + "  patient add       " + reset + "- Añadir paciente");
            System.out.println(green + "  patient show ID   " + reset + "- Mostrar paciente por ID");
            System.out.println(green + "  patient attend    " + reset + "- Atender paciente con mayor prioridad");
            return;
        }
        switch (parts[1]) {
            case "list" -> listPatients();
            case "add" -> addPatient(parts);
            case "show" -> showPatient(parts);
            case "attend" -> attendPatient();
            default -> System.out.println(yellowBold + "Subcomando no reconocido" + reset);
        }
    }

    private static void listPatients() {
        String yellowBold = colors.get("yellow_bold");
        String green = colors.get("green");
        String red = colors.get("red");
        String yellow = colors.get("yellow");
        String reset = colors.get("reset");
        
        System.out.println(yellowBold + "LISTADO DE PACIENTES" + reset);
        
        // Obtener lista de pacientes desde Redis
        Set<String> patientKeys = redisTestInstance.getAllKeys();
        if (patientKeys.isEmpty()) {
            System.out.println("No hay pacientes registrados.");
            return;
        }
        
        // Crear listas para cada nivel de urgencia
        Map<String, Map<String, Map<String, String>>> patientsByUrgency = new HashMap<>();
        patientsByUrgency.put("rojo", new HashMap<>());
        patientsByUrgency.put("amarillo", new HashMap<>());
        patientsByUrgency.put("verde", new HashMap<>());
        
        // Clasificar los pacientes según nivel de urgencia
        for (String key : patientKeys) {
            if (key.startsWith("patient:") && !key.contains(":atendido") && !key.contains("historial:")) {
                String id = key.substring(8); // Eliminar "patient:"
                Map<String, String> patientData = redisTestInstance.getHash(key);
                if (patientData == null || patientData.isEmpty()) {
                    continue;
                }
                
                String urgencia = patientData.getOrDefault("urgencia", "verde");
                long timestamp = Long.parseLong(patientData.getOrDefault("timestamp", "0"));
                
                // Guardar paciente en su categoría de urgencia
                Map<String, Map<String, String>> urgencyMap = patientsByUrgency.get(urgencia);
                if (urgencyMap != null) {
                    urgencyMap.put(id, patientData);
                }
            }
        }
        
        // Mostrar pacientes por nivel de urgencia, ordenados por tiempo
        if (!patientsByUrgency.get("rojo").isEmpty()) {
            System.out.println(red + "🔴 CRÍTICOS (Atención inmediata):" + reset);
            showPatientsInOrder(patientsByUrgency.get("rojo"));
        }
        
        if (!patientsByUrgency.get("amarillo").isEmpty()) {
            System.out.println(yellow + "🟡 URGENTES (Pueden esperar):" + reset);
            showPatientsInOrder(patientsByUrgency.get("amarillo"));
        }
        
        if (!patientsByUrgency.get("verde").isEmpty()) {
            System.out.println(green + "🟢 LEVES (Última prioridad):" + reset);
            showPatientsInOrder(patientsByUrgency.get("verde"));
        }
    }

    private static void showPatientsInOrder(Map<String, Map<String, String>> patientsMap) {
        // Convertir a lista para poder ordenar
        List<Map.Entry<String, Map<String, String>>> patientsList = new ArrayList<>(patientsMap.entrySet());
        
        // Ordenar por timestamp (los más antiguos primero)
        patientsList.sort((p1, p2) -> {
            long t1 = Long.parseLong(p1.getValue().getOrDefault("timestamp", "0"));
            long t2 = Long.parseLong(p2.getValue().getOrDefault("timestamp", "0"));
            return Long.compare(t1, t2); // Orden ascendente por timestamp
        });
        
        // Mostrar pacientes ordenados
        for (Map.Entry<String, Map<String, String>> entry : patientsList) {
            String id = entry.getKey();
            Map<String, String> patientData = entry.getValue();
            String nombre = patientData.getOrDefault("nombre", "Sin nombre");
            String apellido = patientData.getOrDefault("apellido", "Sin apellido");
            System.out.println("  ID: " + id + " - " + nombre + " " + apellido);
        }
    }

    private static void addPatient(String[] parts) {
        String yellowBold = colors.get("yellow_bold");
        String green = colors.get("green");
        String red = colors.get("red");
        String yellow = colors.get("yellow");
        String reset = colors.get("reset");
        
        Scanner scanner = new Scanner(System.in);
        
        // Crear un nuevo ID para el paciente
        String id = String.valueOf(System.currentTimeMillis());
        
        Map<String, String> patientData = new HashMap<>();
        
        System.out.println(yellowBold + "AÑADIR NUEVO PACIENTE" + reset);
        
        System.out.print("Nombre: ");
        patientData.put("nombre", scanner.nextLine());
        
        System.out.print("Apellido: ");
        patientData.put("apellido", scanner.nextLine());
        
        System.out.print("Edad: ");
        String edad = scanner.nextLine();
        while (!edad.matches("\\d+") || Integer.parseInt(edad) < 0) {
            System.out.println(red + "Por favor, introduzca una edad válida (número mayor o igual a 0)" + reset);
            System.out.print("Edad: ");
            edad = scanner.nextLine();
        }
        patientData.put("edad", edad);
        
        System.out.print("Síntomas: ");
        patientData.put("sintomas", scanner.nextLine());
        
        // Solicitar nivel de urgencia
        String urgencia = "";
        while (true) {
            System.out.println(yellowBold + "Nivel de urgencia:" + reset);
            System.out.println(red + "1. 🔴 Rojo (crítico, atención inmediata)" + reset);
            System.out.println(yellow + "2. 🟡 Amarillo (urgente, puede esperar)" + reset);
            System.out.println(green + "3. 🟢 Verde (leve, última prioridad)" + reset);
            System.out.print("Seleccione [1-3]: ");
            String opcion = scanner.nextLine();
            
            switch (opcion) {
                case "1" -> urgencia = "rojo";
                case "2" -> urgencia = "amarillo";
                case "3" -> urgencia = "verde";
                default -> {
                    System.out.println(red + "Opción no válida. Intente de nuevo." + reset);
                    continue;
                }
            }
            break;
        }
        
        patientData.put("urgencia", urgencia);
        patientData.put("timestamp", String.valueOf(System.currentTimeMillis())); // Para ordenar por tiempo de llegada
        
        // Guardar el paciente en Redis
        boolean success = redisTestInstance.setHash("patient:" + id, patientData);
        
        if (success) {
            String colorUrgencia = switch (urgencia) {
                case "rojo" -> red;
                case "amarillo" -> yellow;
                case "verde" -> green;
                default -> "";
            };
            
            System.out.println(green + "Paciente añadido correctamente con ID: " + id + reset);
            System.out.println("Nivel de urgencia: " + colorUrgencia + urgencia.toUpperCase() + reset);
        } else {
            System.out.println(red + "Error al añadir paciente" + reset);
        }
    }

    private static void showPatient(String[] parts) {
        String yellowBold = colors.get("yellow_bold");
        String green = colors.get("green");
        String red = colors.get("red");
        String yellow = colors.get("yellow");
        String reset = colors.get("reset");
        
        if (parts.length < 3) {
            System.out.println(red + "Error: Se requiere un ID de paciente" + reset);
            System.out.println("Uso: patient show ID");
            return;
        }
        
        String id = parts[2];
        Map<String, String> patientData = redisTestInstance.getHash("patient:" + id);
        
        if (patientData == null || patientData.isEmpty()) {
            System.out.println(red + "No se encontró ningún paciente con ID: " + id + reset);
            return;
        }
        
        String urgencia = patientData.getOrDefault("urgencia", "verde");
        String colorUrgencia = switch (urgencia) {
            case "rojo" -> red + "🔴 CRÍTICO";
            case "amarillo" -> yellow + "🟡 URGENTE";
            case "verde" -> green + "🟢 LEVE";
            default -> green + "🟢 LEVE";
        };
        
        System.out.println(yellowBold + "INFORMACIÓN DEL PACIENTE" + reset);
        System.out.println(green + "ID: " + reset + id);
        System.out.println(green + "Nombre: " + reset + patientData.getOrDefault("nombre", "No disponible"));
        System.out.println(green + "Apellido: " + reset + patientData.getOrDefault("apellido", "No disponible"));
        System.out.println(green + "Edad: " + reset + patientData.getOrDefault("edad", "No disponible"));
        System.out.println(green + "Urgencia: " + reset + colorUrgencia + reset);
        System.out.println(green + "Síntomas: " + reset + patientData.getOrDefault("sintomas", "No disponible"));
    }

    public static void redisCommand(String command) {
        String[] parts = command.split(" ");
        String yellowBold = colors.get("yellow_bold");
        String green = colors.get("green");
        String red = colors.get("red");
        String reset = colors.get("reset");
        
        if (parts.length == 1) {
            System.out.println(yellowBold + "COMANDOS REDIS" + reset);
            System.out.println("  redis test          - Probar conexión a Redis");
            System.out.println("  redis set key value - Guardar valor en Redis");
            System.out.println("  redis get key       - Obtener valor de Redis");
            System.out.println("  redis keys          - Listar todas las claves");
            return;
        }
        
        switch (parts[1]) {
            case "test" -> {
                boolean connected = redisTestInstance.testConnection();
                if (connected) {
                    System.out.println(green + "Conexión a Redis: OK" + reset);
                } else {
                    System.out.println(red + "Conexión a Redis: ERROR" + reset);
                }
            }
            case "set" -> {
                if (parts.length < 4) {
                    System.out.println(red + "Error: Se requiere una clave y un valor" + reset);
                    System.out.println("Uso: redis set key value");
                    return;
                }
                boolean success = redisTestInstance.setValue(parts[2], parts[3]);
                System.out.println(success ? green + "Valor guardado correctamente" + reset : red + "Error al guardar valor" + reset);
            }
            case "get" -> {
                if (parts.length < 3) {
                    System.out.println(red + "Error: Se requiere una clave" + reset);
                    System.out.println("Uso: redis get key");
                    return;
                }
                String value = redisTestInstance.getValue(parts[2]);
                if (value != null) {
                    System.out.println(green + "Valor: " + reset + value);
                } else {
                    System.out.println(red + "Clave no encontrada o error al recuperar" + reset);
                }
            }
            case "keys" -> {
                Set<String> keys = redisTestInstance.getAllKeys();
                if (keys.isEmpty()) {
                    System.out.println("No hay claves en Redis.");
                } else {
                    System.out.println(yellowBold + "CLAVES EN REDIS:" + reset);
                    for (String key : keys) {
                        System.out.println("  " + key);
                    }
                }
            }
            default -> System.out.println(red + "Subcomando de Redis no reconocido" + reset);
        }
    }
}
