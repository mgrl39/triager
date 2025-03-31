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

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(TriagerApplication.class, args);

        Map<String, String> colors = colorInitializator();
        Scanner scanner = new Scanner(System.in);
        System.out.println(MensajeApp.BIENVENIDA.get());
        while (true) {
            System.out.print(colors.get("blue_bold") + MensajeApp.PROGRAMA.get() + colors.get("reset") + "$ ");
            handleCommands(scanner.nextLine());
        }
    }

    public static void handleCommands(String command)
    {
        if (command.equals("exit") || command.equals("quit")) System.exit(0);
        else if (command.equals("info") || command.equals("help")) helpCommand();
        else if (command.split(" ")[0].equals("patient")) {
            patientCommand(command);
        }
    }

    public static void helpCommand() {
        System.out.println(colorInitializator().get("yellow_bold") + "COMANDOS TRIAGER" + colorInitializator().get("reset") + "");
    }

    public static void patientCommand(String command){
        if (command.split(" ").length == 1) {
            System.out.println(colorInitializator().get("yellow_bold") + "COMANDOS DE GESTIÓN DE PACIENTES" + colorInitializator().get("reset") + "");
            return;
        }
        if (command.split(" ")[1].equals("list")){
            System.out.println(colorInitializator().get("yellow_bold") + "LISTADO" + colorInitializator().get("reset") + "");
        }
        else if (command.split(" ")[1].equals("add")) {
            System.out.println(colorInitializator().get("yellow_bold") + "AÑADIDO" + colorInitializator().get("reset") + "");
        }
        else if (command.split(" ")[1].equals("show")) {
            System.out.println(colorInitializator().get("yellow_bold") + "MOSTRADO" + colorInitializator().get("reset") + "");
        }
    }

}
