package net.elpuig.triager;

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
        final String key = "clave";
        final String value = "valor";

        redisTemplate.opsForValue().set(key, value);

        // Recuperamos el valor de Redis
        String retrievedValue = (String) redisTemplate.opsForValue().get(key);
        System.out.println(retrievedValue);
        redisTemplate.delete(key);
    }

}
