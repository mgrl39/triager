package net.elpuig.triager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class RedisConfiguration {

    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        
        // Obtenemos las configuraciones desde las variables de entorno
        String redisHost = EnvConfig.get("REDIS_HOST", "localhost");
        int redisPort = Integer.parseInt(EnvConfig.get("REDIS_PORT", "6379"));
        
        config.setHostName(redisHost);
        config.setPort(redisPort);
        
        // Si hay credenciales configuradas, las usamos
        String username = EnvConfig.get("REDIS_USERNAME");
        String password = EnvConfig.get("REDIS_PASSWORD");
        
        if (username != null && !username.isEmpty()) {
            config.setUsername(username);
        }
        
        if (password != null && !password.isEmpty()) {
            config.setPassword(password);
        }
        
        return new JedisConnectionFactory(config);
    }

    @Bean
    @Primary
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory());
        return template;
    }
}
