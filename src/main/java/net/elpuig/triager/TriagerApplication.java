package net.elpuig.triager;

import net.elpuig.triager.config.MensajeApp;
import net.elpuig.triager.config.RedisConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import static net.elpuig.triager.config.MensajeApp.colorInitializator;

@SpringBootApplication
public class TriagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TriagerApplication.class, args);

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
    }

    public static void helpCommand() {
        System.out.println(colorInitializator().get("yellow_bold") + "COMANDOS TRIAGER" + colorInitializator().get("reset") + "");
    }

}
