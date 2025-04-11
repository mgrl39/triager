package net.elpuig.triager;

import net.elpuig.triager.config.EnvConfig;
import net.elpuig.triager.controller.TriagerController;
import net.elpuig.triager.service.PatientService;
import net.elpuig.triager.redis.RedisTest;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import java.util.Map;
import java.util.HashMap;

import static net.elpuig.triager.config.MensajeApp.colorInitializator;

@SpringBootApplication
public class TriagerApplication {
    // Variables estáticas para la aplicación
    private static Map<String, String> colors = colorInitializator();

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(TriagerApplication.class, args);
        
        // Obtener las dependencias necesarias
        PatientService patientService = context.getBean(PatientService.class);
        
        // Inicializar RedisTest
        Map<String, String> envConfig = new HashMap<>();
        envConfig.put("REDIS_HOST", EnvConfig.get("REDIS_HOST", "localhost"));
        envConfig.put("REDIS_PORT", EnvConfig.get("REDIS_PORT", "6379"));
        envConfig.put("REDIS_PASSWORD", EnvConfig.get("REDIS_PASSWORD", ""));
        
        // Crear y iniciar el controlador
        TriagerController controller = new TriagerController(patientService, colors);
        controller.start();
    }
}
