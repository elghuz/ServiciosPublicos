package com.example.serviciospublicos.firebase;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class StorageService {

    private static StorageService instance;
    private final FirebaseStorage storage;
    private final StorageReference rootRef;

    private static final String EVIDENCIAS_FOLDER = "evidencias";

    private StorageService() {
        storage = FirebaseStorage.getInstance();
        rootRef = storage.getReference();
    }

    public static synchronized StorageService getInstance() {
        if (instance == null) {
            instance = new StorageService();
        }
        return instance;
    }

    /**
     * Sube una evidencia (foto o video) a:
     *  evidencias/{obraId}/{userId}/{timestamp}.{ext}
     *
     * Regresa un Task<Uri> con la URL de descarga lista
     * para guardar en Firestore.
     */
    public Task<Uri> uploadEvidenceFile(
            @NonNull Uri fileUri,
            @NonNull String obraId,
            @NonNull String userId,
            @NonNull String extension // "jpg", "mp4", etc.
    ) {
        long timestamp = System.currentTimeMillis();
        String fileName = timestamp + "." + extension;

        StorageReference fileRef = rootRef
                .child(EVIDENCIAS_FOLDER)
                .child(obraId)
                .child(userId)
                .child(fileName);

        UploadTask uploadTask = fileRef.putFile(fileUri);

        // Primero sube el archivo, luego obtiene la URL de descarga
        return uploadTask
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return fileRef.getDownloadUrl();
                });
    }
}
