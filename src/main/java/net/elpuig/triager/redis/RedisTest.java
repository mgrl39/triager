package net.elpuig.triager.redis;

import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Clase para probar la conexión y operaciones con Redis
 */
public class RedisTest {
    private final Map<String, String> envConfig;
    private StringRedisTemplate redisTemplate;

    public RedisTest(Map<String, String> envConfig) {
        this.envConfig = envConfig;
    }

    /**
     * Inicializa la conexión a Redis con los parámetros de configuración
     */
    private void initRedisConnection() {
        if (redisTemplate != null) {
            return;
        }

        try {
            String redisHost = envConfig.getOrDefault("REDIS_HOST", "localhost");
            int redisPort = Integer.parseInt(envConfig.getOrDefault("REDIS_PORT", "6379"));
            
            RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(redisHost, redisPort);
            
            // Si hay credenciales de Redis, establecerlas
            String redisPassword = envConfig.get("REDIS_PASSWORD");
            if (redisPassword != null && !redisPassword.isEmpty()) {
                config.setPassword(redisPassword);
            }
            
            JedisConnectionFactory connectionFactory = new JedisConnectionFactory(config);
            connectionFactory.afterPropertiesSet();
            
            StringRedisTemplate template = new StringRedisTemplate();
            template.setConnectionFactory(connectionFactory);
            template.setKeySerializer(new StringRedisSerializer());
            template.setValueSerializer(new StringRedisSerializer());
            template.afterPropertiesSet();
            
            this.redisTemplate = template;
        } catch (Exception e) {
            // No se pudo establecer la conexión
            this.redisTemplate = null;
        }
    }

    /**
     * Prueba la conexión a Redis
     * @return true si la conexión fue exitosa, false en caso contrario
     */
    public boolean testConnection() {
        try {
            initRedisConnection();
            if (redisTemplate == null) {
                return false;
            }
            // Intentar una operación simple para verificar la conexión
            redisTemplate.getConnectionFactory().getConnection().ping();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Establece un valor en Redis
     * @param key clave
     * @param value valor
     * @return true si se estableció correctamente, false en caso contrario
     */
    public boolean setValue(String key, String value) {
        try {
            initRedisConnection();
            if (redisTemplate == null) {
                return false;
            }
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Obtiene un valor de Redis
     * @param key clave
     * @return el valor asociado a la clave o null si no existe
     */
    public String getValue(String key) {
        try {
            initRedisConnection();
            if (redisTemplate == null) {
                return null;
            }
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Obtiene todas las claves almacenadas en Redis
     * @return conjunto de claves
     */
    public Set<String> getAllKeys() {
        try {
            initRedisConnection();
            if (redisTemplate == null) {
                return new HashSet<>();
            }
            Set<String> keys = redisTemplate.keys("*");
            return keys != null ? keys : new HashSet<>();
        } catch (Exception e) {
            return new HashSet<>();
        }
    }

    /**
     * Almacena un hash en Redis
     * @param key clave del hash
     * @param hash mapa con los campos y valores del hash
     * @return true si se estableció correctamente, false en caso contrario
     */
    public boolean setHash(String key, Map<String, String> hash) {
        try {
            initRedisConnection();
            if (redisTemplate == null) {
                return false;
            }
            redisTemplate.opsForHash().putAll(key, hash);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Obtiene un hash completo de Redis
     * @param key clave del hash
     * @return mapa con los campos y valores del hash, o null si no existe
     */
    public Map<String, String> getHash(String key) {
        try {
            initRedisConnection();
            if (redisTemplate == null) {
                return null;
            }
            Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
            if (entries.isEmpty()) {
                return null;
            }
            
            // Convertir los valores a String
            Map<String, String> result = new HashMap<>();
            for (Map.Entry<Object, Object> entry : entries.entrySet()) {
                result.put(entry.getKey().toString(), entry.getValue().toString());
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Elimina una clave de Redis
     * @param key clave a eliminar
     * @return true si se eliminó correctamente, false en caso contrario
     */
    public boolean deleteKey(String key) {
        try {
            initRedisConnection();
            if (redisTemplate == null) {
                return false;
            }
            return Boolean.TRUE.equals(redisTemplate.delete(key));
        } catch (Exception e) {
            return false;
        }
    }
} 