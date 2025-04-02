package net.elpuig.triager.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EnvConfig {
    
    private static Dotenv dotenv;
    
    static {
        try {
            dotenv = Dotenv.configure().load();
            System.out.println("Variables de entorno cargadas correctamente desde .env");
        } catch (Exception e) {
            System.err.println("Error al cargar el archivo .env: " + e.getMessage());
            // Crear un dotenv vacío para evitar NullPointerException
            dotenv = Dotenv.configure().ignoreIfMissing().load();
        }
    }
    
    @Bean
    public Dotenv dotenv() {
        return dotenv;
    }
    
    public static String get(String key) {
        return dotenv.get(key);
    }
    
    public static String get(String key, String defaultValue) {
        return dotenv.get(key, defaultValue);
    }
} 