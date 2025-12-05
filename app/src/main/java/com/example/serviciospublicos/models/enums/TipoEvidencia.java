package com.example.serviciospublicos.models.enums;

public enum TipoEvidencia {
    FOTO,
    VIDEO;

    public static TipoEvidencia fromString(String value) {
        if (value == null) return FOTO;
        switch (value.toUpperCase()) {
            case "VIDEO":
                return VIDEO;
            default:
                return FOTO;
        }
    }

    public String toFirestoreValue() {
        return name(); // "FOTO" o "VIDEO"
    }
}
