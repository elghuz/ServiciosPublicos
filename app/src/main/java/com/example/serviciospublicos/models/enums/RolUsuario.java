package com.example.serviciospublicos.models.enums;

public enum RolUsuario {
    ADMIN,
    SUPERVISOR,
    OPERADOR;

    public static RolUsuario fromString(String value) {
        if (value == null) return OPERADOR;
        switch (value.toUpperCase()) {
            case "ADMIN":
            case "ADMINISTRADOR":
                return ADMIN;
            case "SUPERVISOR":
                return SUPERVISOR;
            default:
                return OPERADOR;
        }
    }

    public String toFirestoreValue() {
        return name(); // "ADMIN", "SUPERVISOR", "OPERADOR"
    }
}
