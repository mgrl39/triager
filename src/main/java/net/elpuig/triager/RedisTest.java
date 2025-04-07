package net.elpuig.triager;

import net.elpuig.triager.config.EnvConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPooled;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RedisTest {
    private static String getRedisHost() {
        return EnvConfig.get("REDIS_HOST", "localhost");
    }
    
    private static int getRedisPort() {
        return Integer.parseInt(EnvConfig.get("REDIS_PORT", "6379"));
    }
    
    private static Jedis createJedisClient() {
        String host = getRedisHost();
        int port = getRedisPort();
        Jedis jedis = new Jedis(host, port);
        
        // Si hay credenciales configuradas, las usamos
        String username = EnvConfig.get("REDIS_USERNAME");
        String password = EnvConfig.get("REDIS_PASSWORD");
        
        if (password != null && !password.isEmpty()) {
            if (username != null && !username.isEmpty()) {
                jedis.auth(username, password);
            } else {
                jedis.auth(password);
            }
        }
        
        return jedis;
    }

    /**
     * Prueba simple de conexión a Redis
     * @return true si la conexión fue exitosa, false en caso contrario
     */
    public static boolean testConnection() {
        try (Jedis jedis = createJedisClient()) {
            String response = jedis.ping();
            System.out.println("Respuesta de Redis: " + response);
            return "PONG".equalsIgnoreCase(response);
        } catch (Exception e) {
            System.err.println("Error al conectar con Redis: " + e.getMessage());
            return false;
        }
    }

    /**
     * Guarda un valor simple en Redis
     * @param key Clave
     * @param value Valor
     * @return true si se guardó correctamente, false en caso contrario
     */
    public static boolean setValue(String key, String value) {
        try (Jedis jedis = createJedisClient()) {
            jedis.set(key, value);
            return true;
        } catch (Exception e) {
            System.err.println("Error al guardar en Redis: " + e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene un valor simple de Redis
     * @param key Clave
     * @return Valor asociado a la clave
     */
    public static String getValue(String key) {
        try (Jedis jedis = createJedisClient()) {
            return jedis.get(key);
        } catch (Exception e) {
            System.err.println("Error al obtener de Redis: " + e.getMessage());
            return null;
        }
    }

    /**
     * Guarda un objeto (Map) en Redis como un hash
     * @param key Clave del hash
     * @param map Mapa con los valores
     * @return true si se guardó correctamente, false en caso contrario
     */
    public static boolean setHash(String key, Map<String, String> map) {
        try (Jedis jedis = createJedisClient()) {
            jedis.hset(key, map);
            return true;
        } catch (Exception e) {
            System.err.println("Error al guardar hash en Redis: " + e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene un hash completo de Redis
     * @param key Clave del hash
     * @return Mapa con los valores del hash
     */
    public static Map<String, String> getHash(String key) {
        try (Jedis jedis = createJedisClient()) {
            String type = jedis.type(key);
            if (type == null || !type.equals("hash")) {
                // Si la clave no existe o no es un hash, la eliminamos
                if (type != null) {
                    jedis.del(key);
                }
                return new HashMap<>();
            }
            return jedis.hgetAll(key);
        } catch (Exception e) {
            System.err.println("Error al obtener hash de Redis: " + e.getMessage());
            return new HashMap<>();
        }
    }

    /**
     * Lista todas las claves en Redis
     * @return Conjunto de claves
     */
    public static Set<String> getAllKeys() {
        try (Jedis jedis = createJedisClient()) {
            return jedis.keys("*");
        } catch (Exception e) {
            System.err.println("Error al obtener claves de Redis: " + e.getMessage());
            return Set.of();
        }
    }

    /**
     * Elimina una clave de Redis
     * @param key Clave a eliminar
     * @return true si se eliminó correctamente, false en caso contrario
     */
    public static boolean deleteKey(String key) {
        try (Jedis jedis = createJedisClient()) {
            return jedis.del(key) > 0;
        } catch (Exception e) {
            System.err.println("Error al eliminar clave de Redis: " + e.getMessage());
            return false;
        }
    }

    public static void main(String[] args) {
        // Ejemplo de uso de los métodos
        System.out.println("Probando conexión a Redis...");
        if (testConnection()) {
            System.out.println("Conexión exitosa!");
            
            // Aquí puedes probar otros métodos
            setValue("test-key", "test-value");
            String value = getValue("test-key");
            System.out.println("Valor recuperado: " + value);
        }
    }
}
