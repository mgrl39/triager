package net.elpuig.triager;

import net.elpuig.triager.config.MensajeApp;
import net.elpuig.triager.config.RedisConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static net.elpuig.triager.config.MensajeApp.colorInitializator;
import net.elpuig.triager.config.EnvConfig;

@SpringBootApplication
public class TriagerApplication {

    private static RedisTemplate<String, Object> redisTemplate;
    static Map<String, String> colors = colorInitializator();
    private static boolean adminMode = false;

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(TriagerApplication.class, args);
        redisTemplate = (RedisTemplate<String, Object>) context.getBean("redisTemplate");

        // Imprimir información de conexión desde las variables de entorno
        String redisHost = EnvConfig.get("REDIS_HOST", "localhost");
        String redisPort = EnvConfig.get("REDIS_PORT", "6379");
        
        System.out.println(colors.get("cyan") + "Configuración de Redis desde .env:" + colors.get("reset"));
        System.out.println(colors.get("cyan") + "  Host: " + redisHost + colors.get("reset"));
        System.out.println(colors.get("cyan") + "  Puerto: " + redisPort + colors.get("reset"));

        // Probar la conexión a Redis al inicio
        boolean redisConnected = RedisTest.testConnection();
        if (redisConnected) {
            System.out.println(colors.get("green") + "Conexión a Redis: OK" + colors.get("reset"));
        } else {
            System.out.println(colors.get("red") + "Conexión a Redis: ERROR" + colors.get("reset"));
            System.out.println(colors.get("yellow") + "Asegúrate de que Redis esté funcionando en " + 
                EnvConfig.get("REDIS_HOST", "localhost") + ":" + 
                EnvConfig.get("REDIS_PORT", "6379") + colors.get("reset"));
            System.out.println(colors.get("yellow") + "Puedes usar el script tools/install_redis_direct.sh para instalar Redis" + colors.get("reset"));
            System.out.println(colors.get("yellow") + "O verifica la configuración en el archivo .env" + colors.get("reset"));
        }

        Scanner scanner = new Scanner(System.in);
        System.out.println(MensajeApp.BIENVENIDA.get());
        while (true) {
            String promptColor = adminMode ? colors.get("red_bold") : colors.get("blue_bold");
            System.out.print(promptColor + MensajeApp.PROGRAMA.get() + colors.get("reset") + "$ ");
            handleCommands(scanner.nextLine());
        }
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
        
        if (adminMode) {
            System.out.println("");
            System.out.println(yellowBold + "COMANDOS REDIS (ADMIN):" + reset);
            System.out.println("  redis test          - Probar conexión a Redis");
            System.out.println("  redis set key value - Guardar valor en Redis");
            System.out.println("  redis get key       - Obtener valor de Redis");
            System.out.println("  redis keys          - Listar todas las claves");
        }
        
        System.out.println("");
        if (adminMode) {
            System.out.println(yellowBold + "COMANDOS ADMINISTRADOR:" + reset);
            System.out.println("  admin login         - Acceder al modo administrador");
            System.out.println("  admin logout        - Salir del modo administrador");
        } else {
            System.out.println(yellowBold + "COMANDOS ADMINISTRADOR:" + reset);
            System.out.println("  admin login         - Acceder al modo administrador");
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
        Set<String> patientKeys = RedisTest.getAllKeys();
        if (patientKeys.isEmpty()) {
            System.out.println("No hay pacientes registrados.");
            return;
        }
        
        // Crear listas para cada nivel de urgencia
        Map<String, String> redPatients = new HashMap<>();
        Map<String, String> yellowPatients = new HashMap<>();
        Map<String, String> greenPatients = new HashMap<>();
        
        // Clasificar los pacientes según nivel de urgencia
        for (String key : patientKeys) {
            if (key.startsWith("patient:") && !key.contains(":atendido") && !key.contains("historial:")) {
                String id = key.substring(8); // Eliminar "patient:"
                Map<String, String> patientData = RedisTest.getHash(key);
                if (patientData.isEmpty()) {
                    continue; // Saltar si no es un hash válido
                }
                String urgencia = patientData.getOrDefault("urgencia", "verde");
                String nombreCompleto = patientData.getOrDefault("nombre", "Sin nombre") + " " +
                        patientData.getOrDefault("apellido", "Sin apellido");
                
                switch (urgencia.toLowerCase()) {
                    case "rojo" -> redPatients.put(id, nombreCompleto);
                    case "amarillo" -> yellowPatients.put(id, nombreCompleto);
                    case "verde" -> greenPatients.put(id, nombreCompleto);
                }
            }
        }
        
        // Mostrar pacientes ordenados por urgencia
        if (!redPatients.isEmpty()) {
            System.out.println(red + "🔴 CRÍTICOS (Atención inmediata):" + reset);
            for (Map.Entry<String, String> entry : redPatients.entrySet()) {
                System.out.println(red + "  ID: " + entry.getKey() + reset + " - " + entry.getValue());
            }
        }
        
        if (!yellowPatients.isEmpty()) {
            System.out.println(yellow + "🟡 URGENTES (Pueden esperar):" + reset);
            for (Map.Entry<String, String> entry : yellowPatients.entrySet()) {
                System.out.println(yellow + "  ID: " + entry.getKey() + reset + " - " + entry.getValue());
            }
        }
        
        if (!greenPatients.isEmpty()) {
            System.out.println(green + "🟢 LEVES (Última prioridad):" + reset);
            for (Map.Entry<String, String> entry : greenPatients.entrySet()) {
                System.out.println(green + "  ID: " + entry.getKey() + reset + " - " + entry.getValue());
            }
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
        patientData.put("edad", scanner.nextLine());
        
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
        patientData.put("timestamp", String.valueOf(System.currentTimeMillis())); // Para ordenar por tiempo de llegada en caso de misma urgencia
        
        // Guardar el paciente en Redis
        boolean success = RedisTest.setHash("patient:" + id, patientData);
        
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
        Map<String, String> patientData = RedisTest.getHash("patient:" + id);
        
        if (patientData.isEmpty()) {
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
                boolean connected = RedisTest.testConnection();
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
                boolean success = RedisTest.setValue(parts[2], parts[3]);
                System.out.println(success ? green + "Valor guardado correctamente" + reset : red + "Error al guardar valor" + reset);
            }
            case "get" -> {
                if (parts.length < 3) {
                    System.out.println(red + "Error: Se requiere una clave" + reset);
                    System.out.println("Uso: redis get key");
                    return;
                }
                String value = RedisTest.getValue(parts[2]);
                if (value != null) {
                    System.out.println(green + "Valor: " + reset + value);
                } else {
                    System.out.println(red + "Clave no encontrada o error al recuperar" + reset);
                }
            }
            case "keys" -> {
                Set<String> keys = RedisTest.getAllKeys();
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
                    System.out.println("  admin logout        - Salir del modo administrador");
                } else {
                    System.out.println(red + "Contraseña incorrecta" + reset);
                }
            }
            case "logout" -> {
                adminMode = false;
                System.out.println(green + "Modo administrador desactivado" + reset);
            }
            default -> System.out.println(red + "Subcomando de administrador no reconocido" + reset);
        }
    }

    private static String convertToMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : messageDigest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static void attendPatient() {
        String yellowBold = colors.get("yellow_bold");
        String green = colors.get("green");
        String red = colors.get("red");
        String yellow = colors.get("yellow");
        String reset = colors.get("reset");
        
        // Obtener lista de pacientes desde Redis
        Set<String> patientKeys = RedisTest.getAllKeys();
        if (patientKeys.isEmpty()) {
            System.out.println("No hay pacientes para atender.");
            return;
        }
        
        // Variables para encontrar el paciente con mayor prioridad
        String nextPatientId = null;
        String nextPatientUrgencia = "verde";
        long nextPatientTimestamp = Long.MAX_VALUE;
        Map<String, String> nextPatientData = null;
        
        // Buscar el paciente con mayor prioridad
        for (String key : patientKeys) {
            if (key.startsWith("patient:") && !key.contains(":atendido") && !key.contains("historial:")) {
                String id = key.substring(8); // Eliminar "patient:"
                Map<String, String> patientData = RedisTest.getHash(key);
                if (patientData.isEmpty()) {
                    continue; // Saltar si no es un hash válido
                }
                String urgencia = patientData.getOrDefault("urgencia", "verde");
                long timestamp = Long.parseLong(patientData.getOrDefault("timestamp", "0"));
                
                // Comparar prioridad (rojo > amarillo > verde)
                boolean esMayorPrioridad = false;
                
                if (nextPatientUrgencia.equals("verde") && !urgencia.equals("verde")) {
                    esMayorPrioridad = true;
                } else if (nextPatientUrgencia.equals("amarillo") && urgencia.equals("rojo")) {
                    esMayorPrioridad = true;
                } else if (nextPatientUrgencia.equals(urgencia) && timestamp < nextPatientTimestamp) {
                    esMayorPrioridad = true;
                }
                
                if (esMayorPrioridad || nextPatientId == null) {
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
            // Guardar en historial
            String historialKey = "historial:patient:" + nextPatientId;
            nextPatientData.put("id", nextPatientId);
            nextPatientData.put("atendido_en", String.valueOf(System.currentTimeMillis()));
            boolean historialSuccess = RedisTest.setHash(historialKey, nextPatientData);
            
            // Eliminar el paciente original
            boolean deleteSuccess = RedisTest.deleteKey("patient:" + nextPatientId);
            
            if (historialSuccess && deleteSuccess) {
                System.out.println(green + "Paciente atendido correctamente y movido al historial." + reset);
            } else {
                System.out.println(red + "Error al procesar la atención del paciente." + reset);
            }
        } else {
            System.out.println(yellow + "Atención cancelada." + reset);
        }
    }
}
