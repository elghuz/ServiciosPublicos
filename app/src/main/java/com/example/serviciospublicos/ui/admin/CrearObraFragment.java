package com.example.serviciospublicos.ui.admin;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.serviciospublicos.R;
import com.example.serviciospublicos.firebase.FirestoreService;

public class CrearObraFragment extends Fragment {

    private EditText inputNombre, inputDescripcion, inputSupervisorUid,
            inputLat, inputLng, inputRadio;

    private final FirestoreService firestore = FirestoreService.getInstance();

    public CrearObraFragment() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_crear_obra, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        inputNombre = view.findViewById(R.id.inputNombreObra);
        inputDescripcion = view.findViewById(R.id.inputDescripcionObra);
        inputSupervisorUid = view.findViewById(R.id.inputSupervisorUid);
        inputLat = view.findViewById(R.id.inputLat);
        inputLng = view.findViewById(R.id.inputLng);
        inputRadio = view.findViewById(R.id.inputRadio);

        Button btnGuardar = view.findViewById(R.id.btnGuardarObra);
        btnGuardar.setOnClickListener(v -> guardarObra(v));
    }

    private void guardarObra(View v) {
        String nombre = inputNombre.getText().toString().trim();
        String descripcion = inputDescripcion.getText().toString().trim();
        String supervisorUid = inputSupervisorUid.getText().toString().trim();
        String latStr = inputLat.getText().toString().trim();
        String lngStr = inputLng.getText().toString().trim();
        String radioStr = inputRadio.getText().toString().trim();

        if (TextUtils.isEmpty(nombre) || TextUtils.isEmpty(supervisorUid)) {
            Toast.makeText(getContext(), "Nombre y supervisor son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        Double lat = TextUtils.isEmpty(latStr) ? null : Double.parseDouble(latStr);
        Double lng = TextUtils.isEmpty(lngStr) ? null : Double.parseDouble(lngStr);
        Double radio = TextUtils.isEmpty(radioStr) ? null : Double.parseDouble(radioStr);

        firestore.createObra(
                nombre,
                descripcion,
                "INICIANDO",
                supervisorUid,
                "radio",
                lat,
                lng,
                radio
        ).addOnSuccessListener(aVoid -> {
            Toast.makeText(getContext(), "Obra creada", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(v).popBackStack();
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Error al crear obra", Toast.LENGTH_SHORT).show();
        });
    }
}
