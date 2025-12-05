package com.example.serviciospublicos.firebase;

import android.graphics.Bitmap;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

public class StorageService {

    private static StorageService instance;
    private final FirebaseStorage storage;

    private StorageService() {
        storage = FirebaseStorage.getInstance();
    }

    public static synchronized StorageService getInstance() {
        if (instance == null) {
            instance = new StorageService();
        }
        return instance;
    }

    private StorageReference getEvidenciaRef(@NonNull String obraId,
                                             @NonNull String usuarioId,
                                             @NonNull String fileName) {
        return storage.getReference()
                .child("evidencias")
                .child(obraId)
                .child(usuarioId)
                .child(fileName);
    }

    // Subir FOTO desde Bitmap
    public Task<Uri> uploadImageEvidencia(@NonNull String obraId,
                                          @NonNull String usuarioId,
                                          @NonNull Bitmap bitmap) {

        long timestamp = System.currentTimeMillis();
        String fileName = "foto_" + timestamp + ".jpg";
        StorageReference ref = getEvidenciaRef(obraId, usuarioId, fileName);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = ref.putBytes(data);
        return uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            return ref.getDownloadUrl();
        });
    }

    // Subir VIDEO desde Uri
    public Task<Uri> uploadVideoEvidencia(@NonNull String obraId,
                                          @NonNull String usuarioId,
                                          @NonNull Uri videoUri) {

        long timestamp = System.currentTimeMillis();
        String fileName = "video_" + timestamp + ".mp4";
        StorageReference ref = getEvidenciaRef(obraId, usuarioId, fileName);

        UploadTask uploadTask = ref.putFile(videoUri);
        return uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            return ref.getDownloadUrl();
        });
    }
}
