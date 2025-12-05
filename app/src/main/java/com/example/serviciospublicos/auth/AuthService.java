package com.example.serviciospublicos.auth;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.serviciospublicos.firebase.FirestoreService;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Collections;

public class AuthService {

    private static AuthService instance;
    private final FirebaseAuth auth;

    private AuthService() {
        auth = FirebaseAuth.getInstance();
    }

    public static synchronized AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }

    // LOGIN
    public Task<AuthResult> login(String email, String password) {
        return auth.signInWithEmailAndPassword(email, password);
    }

    public Task<AuthResult> register(String email, String password) {
        return auth.createUserWithEmailAndPassword(email, password);
    }

    // Obtener usuario actual
    public String getCurrentUid() {
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
    }

    public void logout() {
        auth.signOut();
    }
}
