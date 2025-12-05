package com.example.serviciospublicos.models.enums;

public enum EstatusObra {
    INICIANDO,
    PROCESO,
    TERMINANDO;

    public static EstatusObra fromString(String value) {
        if (value == null) return INICIANDO;
        switch (value.toUpperCase()) {
            case "PROCESO":
                return PROCESO;
            case "TERMINANDO":
                return TERMINANDO;
            default:
                return INICIANDO;
        }
    }

    public String toFirestoreValue() {
        return name(); // "INICIANDO", "PROCESO", "TERMINANDO"
    }
}
