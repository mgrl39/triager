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

import static net.elpuig.triager.config.MensajeApp.colorInitializator;

@SpringBootApplication
public class TriagerApplication {

    private static RedisTemplate<String, Object> redisTemplate;
    static Map<String, String> colors = colorInitializator();

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(TriagerApplication.class, args);
        redisTemplate = (RedisTemplate<String, Object>) context.getBean("redisTemplate");

        // Probar la conexión a Redis al inicio
        boolean redisConnected = RedisTest.testConnection();
        if (redisConnected) {
            System.out.println(colors.get("green") + "Conexión a Redis: OK" + colors.get("reset"));
        } else {
            System.out.println(colors.get("red") + "Conexión a Redis: ERROR" + colors.get("reset"));
            System.out.println(colors.get("yellow") + "Asegúrate de que Redis esté funcionando en 172.16.0.36:6379" + colors.get("reset"));
            System.out.println(colors.get("yellow") + "Puedes usar el script tools/install_redis_direct.sh para instalar Redis" + colors.get("reset"));
        }

        Scanner scanner = new Scanner(System.in);
        System.out.println(MensajeApp.BIENVENIDA.get());
        while (true) {
            System.out.print(colors.get("blue_bold") + MensajeApp.PROGRAMA.get() + colors.get("reset") + "$ ");
            handleCommands(scanner.nextLine());
        }
    }

    public static void handleCommands(String command) {
        switch (command.split(" ")[0]) {
            case "exit", "quit" -> System.exit(0);
            case "info", "help" -> helpCommand();
            case "patient" -> patientCommand(command);
            case "redis" -> redisCommand(command);
            default -> System.out.println(colors.get("yellow")
                    + "comando no encontrado"
                    + colors.get("reset"));
        }
    }

    public static void helpCommand() {
        String yellowBold = colors.get("yellow_bold");
        String reset = colors.get("reset");
        
        System.out.println(yellowBold + "COMANDOS TRIAGER" + reset);
        System.out.println("  help, info          - Muestra esta ayuda");
        System.out.println("  exit, quit          - Salir de la aplicación");
        System.out.println("  patient             - Gestión de pacientes");
        System.out.println("  redis               - Comandos para Redis");
        System.out.println("");
        System.out.println(yellowBold + "COMANDOS REDIS:" + reset);
        System.out.println("  redis test          - Probar conexión a Redis");
        System.out.println("  redis set key value - Guardar valor en Redis");
        System.out.println("  redis get key       - Obtener valor de Redis");
        System.out.println("  redis keys          - Listar todas las claves");
    }

    public static void patientCommand(String command) {
        String[] parts = command.split(" ");
        String yellowBold = colors.get("yellow_bold");
        String reset = colors.get("reset");

        if (parts.length == 1) {
            System.out.println(yellowBold + "COMANDOS DE GESTIÓN DE PACIENTES" + reset);
            System.out.println("  patient list      - Listar pacientes");
            System.out.println("  patient add       - Añadir paciente");
            System.out.println("  patient show ID   - Mostrar paciente por ID");
            return;
        }
        switch (parts[1]) {
            case "list" -> listPatients();
            case "add" -> addPatient(parts);
            case "show" -> showPatient(parts);
            default -> System.out.println(yellowBold + "Subcomando no reconocido" + reset);
        }
    }
    
    private static void listPatients() {
        String yellowBold = colors.get("yellow_bold");
        String green = colors.get("green");
        String reset = colors.get("reset");
        
        System.out.println(yellowBold + "LISTADO DE PACIENTES" + reset);
        
        // Obtener lista de pacientes desde Redis
        Set<String> patientKeys = RedisTest.getAllKeys();
        if (patientKeys.isEmpty()) {
            System.out.println("No hay pacientes registrados.");
            return;
        }
        
        for (String key : patientKeys) {
            if (key.startsWith("patient:")) {
                String id = key.substring(8); // Eliminar "patient:"
                Map<String, String> patientData = RedisTest.getHash(key);
                System.out.println(green + "ID: " + id + reset + " - " + 
                        patientData.getOrDefault("nombre", "Sin nombre") + " " +
                        patientData.getOrDefault("apellido", "Sin apellido"));
            }
        }
    }
    
    private static void addPatient(String[] parts) {
        String yellowBold = colors.get("yellow_bold");
        String green = colors.get("green");
        String red = colors.get("red");
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
        
        // Guardar el paciente en Redis
        boolean success = RedisTest.setHash("patient:" + id, patientData);
        
        if (success) {
            System.out.println(green + "Paciente añadido correctamente con ID: " + id + reset);
        } else {
            System.out.println(red + "Error al añadir paciente" + reset);
        }
    }
    
    private static void showPatient(String[] parts) {
        String yellowBold = colors.get("yellow_bold");
        String green = colors.get("green");
        String red = colors.get("red");
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
        
        System.out.println(yellowBold + "INFORMACIÓN DEL PACIENTE" + reset);
        System.out.println(green + "ID: " + reset + id);
        System.out.println(green + "Nombre: " + reset + patientData.getOrDefault("nombre", "No disponible"));
        System.out.println(green + "Apellido: " + reset + patientData.getOrDefault("apellido", "No disponible"));
        System.out.println(green + "Edad: " + reset + patientData.getOrDefault("edad", "No disponible"));
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
                if (success) {
                    System.out.println(green + "Valor guardado correctamente" + reset);
                } else {
                    System.out.println(red + "Error al guardar valor" + reset);
                }
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
}
