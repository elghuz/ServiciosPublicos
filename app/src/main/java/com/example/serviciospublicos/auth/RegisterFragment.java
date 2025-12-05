package com.example.serviciospublicos.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.serviciospublicos.R;
import com.example.serviciospublicos.firebase.FirestoreService;
import com.example.serviciospublicos.models.enums.RolUsuario;

public class RegisterFragment extends Fragment {

    private EditText inputNombre, inputEmail, inputPassword;
    private Spinner spinnerRol;
    private Button btnRegister;
    private ProgressBar progressBar;

    private final AuthService auth = AuthService.getInstance();
    private final FirestoreService firestore = FirestoreService.getInstance();

    public RegisterFragment() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        inputNombre = view.findViewById(R.id.inputNombre);
        inputEmail = view.findViewById(R.id.inputEmailReg);
        inputPassword = view.findViewById(R.id.inputPasswordReg);
        spinnerRol = view.findViewById(R.id.spinnerRol);
        btnRegister = view.findViewById(R.id.btnRegister);
        progressBar = view.findViewById(R.id.progressBarRegister);

        // Llenar roles
        String[] roles = {"ADMIN", "SUPERVISOR", "OPERADOR"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                roles
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRol.setAdapter(adapter);

        btnRegister.setOnClickListener(v -> doRegister());
    }

    private void doRegister() {
        String nombre = inputNombre.getText().toString().trim();
        String email = inputEmail.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();
        String rolStr = (String) spinnerRol.getSelectedItem();

        if (TextUtils.isEmpty(nombre) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(getContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnRegister.setEnabled(false);

        auth.register(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = auth.getCurrentUid();
                    if (uid == null) {
                        showError("Error al obtener UID");
                        return;
                    }

                    firestore.createOrUpdateUser(
                            uid,
                            nombre,
                            email,
                            rolStr,
                            null
                    ).addOnSuccessListener(aVoid -> {
                        progressBar.setVisibility(View.GONE);
                        btnRegister.setEnabled(true);

                        RolUsuario rol = RolUsuario.fromString(rolStr);

                        // Tras registro, lo mandamos directo a su home
                        openHomeForRole(rol);

                    }).addOnFailureListener(e -> {
                        showError("Error al guardar usuario en Firestore");
                    });

                })
                .addOnFailureListener(e -> {
                    showError("Error al crear cuenta: " + e.getMessage());
                });
    }

    private void openHomeForRole(RolUsuario rol) {
        switch (rol) {
            case ADMIN:
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_registerFragment_to_adminPanelFragment);
                break;
            case SUPERVISOR:
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_registerFragment_to_obrasSupervisorFragment);
                break;
            default:
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_registerFragment_to_misObrasFragment);
                break;
        }
    }

    private void showError(String msg) {
        progressBar.setVisibility(View.GONE);
        btnRegister.setEnabled(true);
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }
}
