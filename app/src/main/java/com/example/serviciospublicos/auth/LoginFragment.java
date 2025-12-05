package com.example.serviciospublicos.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.serviciospublicos.R;
import com.example.serviciospublicos.firebase.FirestoreService;
import com.example.serviciospublicos.models.enums.RolUsuario;
import com.google.firebase.firestore.DocumentSnapshot;

public class LoginFragment extends Fragment {

    private EditText inputEmail, inputPassword;
    private Button btnLogin, btnGoToRegister;
    private ProgressBar progressBar;

    private final AuthService auth = AuthService.getInstance();
    private final FirestoreService firestore = FirestoreService.getInstance();

    public LoginFragment() { }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {

        inputEmail = view.findViewById(R.id.inputEmail);
        inputPassword = view.findViewById(R.id.inputPassword);
        btnLogin = view.findViewById(R.id.btnLogin);
        btnGoToRegister = view.findViewById(R.id.btnGoToRegister);
        progressBar = view.findViewById(R.id.progressBar);

        btnLogin.setOnClickListener(v -> doLogin());

        btnGoToRegister.setOnClickListener(v ->
                Navigation.findNavController(view)
                        .navigate(R.id.action_loginFragment_to_registerFragment)
        );
    }

    private void doLogin() {
        String email = inputEmail.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(getContext(), "Ingresa correo y contraseña", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);
        btnGoToRegister.setEnabled(false);

        auth.login(email, password).addOnSuccessListener(authResult -> {

            String uid = auth.getCurrentUid();
            if (uid == null) {
                showError("Error obteniendo usuario actual");
                return;
            }

            firestore.getUsuario(uid).addOnSuccessListener(userDoc -> {

                if (!userDoc.exists()) {
                    // Usuario nuevo → Ponemos rol OPERADOR por defecto
                    firestore.createOrUpdateUser(
                            uid,
                            "Operador",
                            email,
                            "OPERADOR",
                            null
                    );
                }

                String rolStr = userDoc.getString("rol");
                RolUsuario rol = RolUsuario.fromString(rolStr);

                openHomeForRole(rol);

            }).addOnFailureListener(e -> {
                showError("Error al obtener usuario");
            });

        }).addOnFailureListener(e -> {
            showError("Inicio de sesión incorrecto");
        });
    }

    private void openHomeForRole(RolUsuario rol) {
        progressBar.setVisibility(View.GONE);
        btnLogin.setEnabled(true);
        btnGoToRegister.setEnabled(true);

        switch (rol) {
            case ADMIN:
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_login_to_adminPanelFragment);
                break;

            case SUPERVISOR:
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_login_to_obrasSupervisorFragment);
                break;

            default:
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_login_to_misObrasFragment);
        }
    }

    private void showError(String message) {
        progressBar.setVisibility(View.GONE);
        btnLogin.setEnabled(true);
        btnGoToRegister.setEnabled(true);
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}
