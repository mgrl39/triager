package net.elpuig.triager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class ColorConfig {
    
    @Bean
    public Map<String, String> colors() {
        Map<String, String> colors = new HashMap<>();
        
        // Colores básicos
        colors.put("reset", "\u001B[0m");
        colors.put("black", "\u001B[30m");
        colors.put("red", "\u001B[31m");
        colors.put("green", "\u001B[32m");
        colors.put("yellow", "\u001B[33m");
        colors.put("blue", "\u001B[34m");
        colors.put("purple", "\u001B[35m");
        colors.put("cyan", "\u001B[36m");
        colors.put("white", "\u001B[37m");
        
        // Colores en negrita
        colors.put("black_bold", "\u001B[1;30m");
        colors.put("red_bold", "\u001B[1;31m");
        colors.put("green_bold", "\u001B[1;32m");
        colors.put("yellow_bold", "\u001B[1;33m");
        colors.put("blue_bold", "\u001B[1;34m");
        colors.put("purple_bold", "\u001B[1;35m");
        colors.put("cyan_bold", "\u001B[1;36m");
        colors.put("white_bold", "\u001B[1;37m");
        
        return colors;
    }
} 