package net.elpuig.triager.repository;

import net.elpuig.triager.model.Patient;
import net.elpuig.triager.config.RedisConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class PatientRepository {
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String PATIENT_KEY_PREFIX = "patient:";
    private static final String HISTORY_KEY_PREFIX = "historial:patient:";

    public PatientRepository(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Patient save(Patient patient) {
        String key = PATIENT_KEY_PREFIX + patient.getId();
        redisTemplate.opsForHash().putAll(key, patient.toMap());
        return patient;
    }

    public Optional<Patient> findById(String id) {
        String key = PATIENT_KEY_PREFIX + id;
        Map<Object, Object> data = redisTemplate.opsForHash().entries(key);
        if (data.isEmpty()) {
            return Optional.empty();
        }
        Map<String, String> stringData = data.entrySet().stream()
            .collect(Collectors.toMap(
                e -> e.getKey().toString(),
                e -> e.getValue().toString()
            ));
        return Optional.of(Patient.fromMap(id, stringData));
    }

    public List<Patient> findAll() {
        Set<String> keys = redisTemplate.keys(PATIENT_KEY_PREFIX + "*");
        if (keys == null) {
            return Collections.emptyList();
        }
        return keys.stream()
            .filter(key -> !key.contains(":atendido") && !key.contains("historial:"))
            .map(key -> {
                Map<Object, Object> data = redisTemplate.opsForHash().entries(key);
                if (data.isEmpty()) {
                    return null;
                }
                Map<String, String> stringData = data.entrySet().stream()
                    .collect(Collectors.toMap(
                        e -> e.getKey().toString(),
                        e -> e.getValue().toString()
                    ));
                return Patient.fromMap(key.substring(PATIENT_KEY_PREFIX.length()), stringData);
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    public void delete(String id) {
        String key = PATIENT_KEY_PREFIX + id;
        redisTemplate.delete(key);
    }

    public void moveToHistory(Patient patient) {
        String historyKey = HISTORY_KEY_PREFIX + patient.getId();
        Map<String, String> historyData = new HashMap<>(patient.toMap());
        historyData.put("atendido_en", String.valueOf(System.currentTimeMillis()));
        redisTemplate.opsForHash().putAll(historyKey, historyData);
        delete(patient.getId());
    }

    public Optional<Patient> findNextPatient() {
        List<Patient> patients = findAll();
        if (patients.isEmpty()) {
            return Optional.empty();
        }

        // Ordenar por urgencia (ROJO > AMARILLO > VERDE) y por timestamp (más antiguo primero)
        return patients.stream()
                .sorted(Comparator
                        .comparing(Patient::getUrgencia, (u1, u2) -> {
                            // Ordenar por prioridad de urgencia (ROJO > AMARILLO > VERDE)
                            int priority1 = switch (u1) {
                                case ROJO -> 3;
                                case AMARILLO -> 2;
                                case VERDE -> 1;
                            };
                            int priority2 = switch (u2) {
                                case ROJO -> 3;
                                case AMARILLO -> 2;
                                case VERDE -> 1;
                            };
                            return priority2 - priority1; // Orden descendente
                        })
                        .thenComparing(Patient::getTimestamp)) // Si misma urgencia, ordenar por timestamp
                .findFirst();
    }
} 