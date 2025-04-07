package net.elpuig.triager.model;

import java.util.HashMap;
import java.util.Map;

public class Patient {
    private String id;
    private String nombre;
    private String apellido;
    private int edad;
    private String sintomas;
    private UrgencyLevel urgencia;
    private long timestamp;

    public enum UrgencyLevel {
        ROJO("rojo", "🔴 CRÍTICO"),
        AMARILLO("amarillo", "🟡 URGENTE"),
        VERDE("verde", "🟢 LEVE");

        private final String value;
        private final String display;

        UrgencyLevel(String value, String display) {
            this.value = value;
            this.display = display;
        }

        public String getValue() {
            return value;
        }

        public String getDisplay() {
            return display;
        }

        public static UrgencyLevel fromString(String text) {
            for (UrgencyLevel level : UrgencyLevel.values()) {
                if (level.value.equalsIgnoreCase(text)) {
                    return level;
                }
            }
            return VERDE; // Por defecto
        }
    }

    public Patient(String id, String nombre, String apellido, int edad, String sintomas, UrgencyLevel urgencia) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.edad = edad;
        this.sintomas = sintomas;
        this.urgencia = urgencia;
        this.timestamp = System.currentTimeMillis();
    }

    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<>();
        map.put("id", id);
        map.put("nombre", nombre);
        map.put("apellido", apellido);
        map.put("edad", String.valueOf(edad));
        map.put("sintomas", sintomas);
        map.put("urgencia", urgencia.getValue());
        map.put("timestamp", String.valueOf(timestamp));
        return map;
    }

    public static Patient fromMap(String id, Map<String, String> map) {
        return new Patient(
            id,
            map.getOrDefault("nombre", "Sin nombre"),
            map.getOrDefault("apellido", "Sin apellido"),
            Integer.parseInt(map.getOrDefault("edad", "0")),
            map.getOrDefault("sintomas", ""),
            UrgencyLevel.fromString(map.getOrDefault("urgencia", "verde"))
        );
    }

    // Getters
    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public int getEdad() { return edad; }
    public String getSintomas() { return sintomas; }
    public UrgencyLevel getUrgencia() { return urgencia; }
    public long getTimestamp() { return timestamp; }

    // Setters
    public void setUrgencia(UrgencyLevel urgencia) { this.urgencia = urgencia; }
} 