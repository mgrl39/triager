package net.elpuig.triager;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPooled;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RedisTest {
    private static final String REDIS_HOST = "172.16.0.36";
    private static final int REDIS_PORT = 6379;

    /**
     * Prueba simple de conexión a Redis
     * @return true si la conexión fue exitosa, false en caso contrario
     */
    public static boolean testConnection() {
        try (Jedis jedis = new Jedis(REDIS_HOST, REDIS_PORT)) {
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
        try (Jedis jedis = new Jedis(REDIS_HOST, REDIS_PORT)) {
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
        try (Jedis jedis = new Jedis(REDIS_HOST, REDIS_PORT)) {
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
        try (Jedis jedis = new Jedis(REDIS_HOST, REDIS_PORT)) {
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
        try (Jedis jedis = new Jedis(REDIS_HOST, REDIS_PORT)) {
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
        try (Jedis jedis = new Jedis(REDIS_HOST, REDIS_PORT)) {
            return jedis.keys("*");
        } catch (Exception e) {
            System.err.println("Error al obtener claves de Redis: " + e.getMessage());
            return Set.of();
        }
    }
}
