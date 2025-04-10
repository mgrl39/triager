package net.elpuig.triager.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Configuration
public class EnvConfig {

    private static Map<String, String> envCache = new HashMap<>();
    
    @Value("${spring.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.redis.port:6379}")
    private int redisPort;

    @Value("${admin.password:secreto}")
    private String adminPassword;

    public String getRedisHost() {
        return redisHost;
    }

    public int getRedisPort() {
        return redisPort;
    }

    public String getAdminPassword() {
        return adminPassword;
    }
    
    /**
     * Método estático para obtener valores de configuración desde variables de entorno o archivo .env
     * @param key La clave de configuración
     * @param defaultValue El valor por defecto si no se encuentra la clave
     * @return El valor de configuración o el valor por defecto
     */
    public static String get(String key, String defaultValue) {
        // Si ya lo tenemos en caché, lo devolvemos
        if (envCache.containsKey(key)) {
            return envCache.get(key);
        }
        
        // Primero intentamos obtener el valor desde las variables de entorno del sistema
        String value = System.getenv(key);
        
        // Si no está en las variables de entorno, buscamos en el archivo .env
        if (value == null || value.isEmpty()) {
            try {
                if (Files.exists(Paths.get(".env"))) {
                    Properties props = new Properties();
                    props.load(Files.newBufferedReader(Paths.get(".env")));
                    value = props.getProperty(key);
                }
            } catch (IOException e) {
                // Si hay error leyendo el archivo, usamos el valor por defecto
                value = defaultValue;
            }
        }
        
        // Si no encontramos el valor, usamos el valor por defecto
        if (value == null || value.isEmpty()) {
            value = defaultValue;
        }
        
        // Guardamos en caché para futuras consultas
        envCache.put(key, value);
        
        return value;
    }
} 