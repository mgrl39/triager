package net.elpuig.triager;

import net.elpuig.triager.config.MensajeApp;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Map;
import java.util.Scanner;

import static net.elpuig.triager.config.MensajeApp.colorInitializator;

@SpringBootApplication
public class TriagerApplication {

  static Scanner scanner = new Scanner(System.in);
  static Map<String, String> colors;
  static ConfigurableApplicationContext context;

  public static void main(String[] args) {
    context = SpringApplication.run(TriagerApplication.class, args);
    colors = colorInitializator();
    System.out.println(MensajeApp.BIENVENIDA.get());
    while (true) {
      System.out.print(colors.get("blue_bold") + MensajeApp.PROGRAMA.get() + colors.get("reset") + "$ ");
      handleCommands(scanner.nextLine());
    }
  }

  public static void handleCommands(String command) {
    if (command.equals("exit") || command.equals("quit")) {
      scanner.close();
      System.exit(0);
    } else if (command.equals("info") || command.equals("help"))
      helpCommand();
    else
      System.out.print(MensajeApp.COMMAND_NOT_FOUND.get());
  }

  public static void helpCommand() {
    System.out.println(colorInitializator()
        .get("yellow_bold") + "COMANDOS TRIAGER" + colorInitializator().get("reset") + "");
  }

}
