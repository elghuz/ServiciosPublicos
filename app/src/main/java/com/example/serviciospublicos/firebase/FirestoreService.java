package com.example.serviciospublicos.firebase;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.GeoPoint;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirestoreService {private static FirestoreService instance;

    private final FirebaseFirestore db;

    private static final String USERS_COLLECTION = "Usuarios";
    private static final String OBRAS_COLLECTION = "Obras";
    private static final String EVIDENCIAS_SUBCOLLECTION = "Evidencias";

    private FirestoreService() {
        db = FirebaseFirestore.getInstance();
    }

    public static synchronized FirestoreService getInstance() {
        if (instance == null) {
            instance = new FirestoreService();
        }
        return instance;
    }

    // ---------- USUARIOS ----------

    public Task<Void> createOrUpdateUser(
            @NonNull String uid,
            @NonNull String nombre,
            @NonNull String correo,
            @NonNull String rol,
            List<String> obrasAsignadas
    ) {
        Map<String, Object> data = new HashMap<>();
        data.put("nombre", nombre);
        data.put("correo", correo);
        data.put("rol", rol);
        if (obrasAsignadas != null) {
            data.put("obrasAsignadas", obrasAsignadas);
        }
        data.put("fechaRegistro", Timestamp.now());

        return db.collection(USERS_COLLECTION)
                .document(uid)
                .set(data, SetOptions.merge());
    }

    public Task<DocumentSnapshot> getUsuario(@NonNull String uid) {
        return db.collection(USERS_COLLECTION)
                .document(uid)
                .get();
    }

    public Task<QuerySnapshot> getSupervisores() {
        return db.collection(USERS_COLLECTION)
                .whereEqualTo("rol", "SUPERVISOR")
                .get();
    }

    // ---------- OBRAS ----------

    /**
     * Crea una nueva obra pudiendo ser:
     * - punto
     * - radio (punto + radioMetros)
     * - polígono (lista de GeoPoint)
     */
    public Task<Void> createObra(
            @NonNull String nombre,
            String descripcion,
            @NonNull String estatus,
            @NonNull String supervisorUid,
            @NonNull String ubicacionTipo,
            Double lat,
            Double lng,
            Double radioMetros,
            List<GeoPoint> poligono
    ) {
        CollectionReference obrasRef = db.collection(OBRAS_COLLECTION);
        String obraId = obrasRef.document().getId();

        Map<String, Object> data = new HashMap<>();
        data.put("id", obraId);
        data.put("nombre", nombre);
        data.put("descripcion", descripcion);
        data.put("estatus", estatus);
        data.put("supervisorAsignado", supervisorUid);
        data.put("ubicacionTipo", ubicacionTipo);

        if (lat != null) data.put("lat", lat);
        if (lng != null) data.put("lng", lng);
        if (radioMetros != null) data.put("radioMetros", radioMetros);
        if (poligono != null && !poligono.isEmpty()) {
            data.put("poligono", poligono);
        }

        data.put("fechaInicio", Timestamp.now());
        data.put("fechaCreacion", Timestamp.now());

        return obrasRef.document(obraId).set(data);
    }

    public CollectionReference getObrasCollection() {
        return db.collection(OBRAS_COLLECTION);
    }

    // ---------- EVIDENCIAS ----------

    public Task<Void> addEvidencia(
            @NonNull String obraId,
            @NonNull String usuarioId,
            @NonNull String usuarioNombre,
            @NonNull String rolUsuario,
            @NonNull String tipo,
            @NonNull String urlArchivo,
            String descripcion,
            String etapaConstruccion,
            double lat,
            double lng
    ) {
        CollectionReference evidenciasRef = db.collection(OBRAS_COLLECTION)
                .document(obraId)
                .collection(EVIDENCIAS_SUBCOLLECTION);

        String evidenciaId = evidenciasRef.document().getId();

        Map<String, Object> data = new HashMap<>();
        data.put("id", evidenciaId);
        data.put("obraId", obraId);
        data.put("usuarioId", usuarioId);
        data.put("usuarioNombre", usuarioNombre);
        data.put("rolUsuario", rolUsuario);
        data.put("tipo", tipo);
        data.put("urlArchivo", urlArchivo);
        data.put("descripcion", descripcion);
        data.put("etapaConstruccion", etapaConstruccion);
        data.put("lat", lat);
        data.put("lng", lng);
        data.put("fechaHora", Timestamp.now());
        data.put("estado", "pendiente");

        return evidenciasRef.document(evidenciaId).set(data);
    }
}
