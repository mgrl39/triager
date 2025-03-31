package net.elpuig.triager.config;

import java.util.HashMap;
import java.util.Map;

public enum MensajeApp {
    BIENVENIDA("Bienvenido a Triager. Escribe 'help' para ver los comandos disponibles o 'exit' para terminar"),
    PROGRAMA("triager");
    private final String texto;
    MensajeApp(String msg) {
        this.texto = msg;
    }

    public String get() {
        return texto;
    }

    public static Map<String, String> colorInitializator() {
        Map<String, String> colors = new HashMap<String, String>();
        colors.put("red", "\u001B[31m");
        colors.put("green", "\u001B[32m");
        colors.put("yellow", "\u001B[33m");
        colors.put("blue", "\u001B[34m");
        colors.put("purple", "\u001B[35m");
        colors.put("cyan", "\u001B[36m");
        colors.put("white", "\u001B[37m");
        colors.put("black", "\u001B[30m");
        colors.put("reset", "\u001B[0m");
        colors.put("red_bold", "\033[1;31m");
        colors.put("green_bold", "\033[1;32m");
        colors.put("yellow_bold", "\033[1;33m");
        colors.put("blue_bold", "\033[1;34m");
        colors.put("purple_bold", "\033[1;35m");
        colors.put("cyan_bold", "\033[1;36m");
        colors.put("white_bold", "\033[1;37m");
        colors.put("black_bold", "\033[1;30m");
        colors.put("reset_bold", "\033[0;31m");
        return (colors);
    }
}
