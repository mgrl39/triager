package net.elpuig.triager;

import net.elpuig.triager.config.EnvConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
public class RedisConnectionTest {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    public void testRedisConnection() {
        // Imprimir información de la conexión
        System.out.println("Conectando a Redis en: " + 
            EnvConfig.get("REDIS_HOST", "localhost") + ":" + 
            EnvConfig.get("REDIS_PORT", "6379"));
        
        final String key = "clave";
        final String value = "valor";

        redisTemplate.opsForValue().set(key, value);

        // Recuperamos el valor de Redis
        String retrievedValue = (String) redisTemplate.opsForValue().get(key);
        System.out.println("Valor recuperado: " + retrievedValue);
        redisTemplate.delete(key);
    }

}
