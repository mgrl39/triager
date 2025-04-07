package net.elpuig.triager;

import net.elpuig.triager.config.MensajeApp;
import net.elpuig.triager.config.RedisConfiguration;
import net.elpuig.triager.controller.TriagerController;
import net.elpuig.triager.service.PatientService;
import net.elpuig.triager.repository.PatientRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import java.util.Map;

import static net.elpuig.triager.config.MensajeApp.colorInitializator;

@SpringBootApplication
public class TriagerApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(TriagerApplication.class, args);
        
        // Obtener las dependencias necesarias
        PatientRepository patientRepository = context.getBean(PatientRepository.class);
        PatientService patientService = context.getBean(PatientService.class);
        Map<String, String> colors = colorInitializator();
        
        // Crear y iniciar el controlador
        TriagerController controller = new TriagerController(patientService, colors);
        controller.start();
    }
}
