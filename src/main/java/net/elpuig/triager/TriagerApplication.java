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

import static net.elpuig.triager.config.MensajeApp.colorInitializator;

@SpringBootApplication
public class TriagerApplication {

    private static RedisTemplate<String, Object> redisTemplate;
    static Map<String, String> colors = colorInitializator();

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(TriagerApplication.class, args);

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
            default -> System.out.println(colorInitializator().get("yellow")
                    + "comando no encontrado"
                    + colorInitializator().get("reset"));
        }
    }

    public static void helpCommand() {
        System.out.println(colorInitializator().get("yellow_bold") + "COMANDOS TRIAGER" + colorInitializator().get("reset"));
    }

    public static void patientCommand(String command) {
        String[] parts = command.split(" ");
        String yellowBold = colors.get("yellow_bold");
        String reset = colors.get("reset");

        if (parts.length == 1) {
            System.out.println(yellowBold + "COMANDOS DE GESTIÓN DE PACIENTES" + reset);
            return;
        }
        switch (parts[1]) {
            case "list" -> System.out.println(yellowBold + "LISTADO" + reset);
            case "add" -> System.out.println(yellowBold + "AÑADIDO" + reset);
            case "show" -> System.out.println(yellowBold + "MOSTRADO" + reset);
            default -> System.out.println(yellowBold + "Subcomando no reconocido" + reset);
        }
    }

}
