package net.elpuig.triager;

import net.elpuig.triager.config.EnvConfig;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class RedisTest {
    private static RedisTemplate<String, Object> redisTemplate;
    private static EnvConfig envConfig;

    public RedisTest(RedisTemplate<String, Object> redisTemplate, EnvConfig envConfig) {
        RedisTest.redisTemplate = redisTemplate;
        RedisTest.envConfig = envConfig;
    }

    public static boolean testConnection() {
        try {
            redisTemplate.getConnectionFactory().getConnection().ping();
            return true;
        } catch (Exception e) {
            System.err.println("Error al conectar con Redis: " + e.getMessage());
            return false;
        }
    }

    public static boolean setValue(String key, String value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            System.err.println("Error al guardar valor en Redis: " + e.getMessage());
            return false;
        }
    }

    public static String getValue(String key) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            return value != null ? value.toString() : null;
        } catch (Exception e) {
            System.err.println("Error al obtener valor de Redis: " + e.getMessage());
            return null;
        }
    }

    public static boolean setHash(String key, Map<String, String> hash) {
        try {
            redisTemplate.opsForHash().putAll(key, hash);
            return true;
        } catch (Exception e) {
            System.err.println("Error al guardar hash en Redis: " + e.getMessage());
            return false;
        }
    }

    public static Map<String, String> getHash(String key) {
        try {
            Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
            if (entries.isEmpty()) {
                return null;
            }
            
            // Convertir los valores a String
            Map<String, String> result = new java.util.HashMap<>();
            for (Map.Entry<Object, Object> entry : entries.entrySet()) {
                result.put(entry.getKey().toString(), entry.getValue().toString());
            }
            return result;
        } catch (Exception e) {
            System.err.println("Error al obtener hash de Redis: " + e.getMessage());
            return null;
        }
    }

    public static Set<String> getAllKeys() {
        try {
            return redisTemplate.keys("*");
        } catch (Exception e) {
            System.err.println("Error al obtener claves de Redis: " + e.getMessage());
            return java.util.Collections.emptySet();
        }
    }

    public static boolean deleteKey(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.delete(key));
        } catch (Exception e) {
            System.err.println("Error al eliminar clave de Redis: " + e.getMessage());
            return false;
        }
    }
}
