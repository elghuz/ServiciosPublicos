package com.example.serviciospublicos.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.serviciospublicos.R;
import com.example.serviciospublicos.firebase.FirestoreService;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class AsignarOperadorFragment extends Fragment {

    private TextView txtObraAsignacion;
    private Spinner spinnerOperadores;
    private Button btnAsignar;

    private final FirestoreService firestore = FirestoreService.getInstance();

    private String obraId;
    private String obraNombre;

    private final List<String> operadoresUids = new ArrayList<>();
    private final List<String> operadoresNombres = new ArrayList<>();

    public AsignarOperadorFragment() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_asignar_operador, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        txtObraAsignacion = view.findViewById(R.id.txtObraAsignacion);
        spinnerOperadores = view.findViewById(R.id.spinnerOperadores);
        btnAsignar = view.findViewById(R.id.btnAsignarOperador);

        if (getArguments() != null) {
            obraId = getArguments().getString("obraId");
            obraNombre = getArguments().getString("obraNombre");
        }

        if (obraId == null) {
            Toast.makeText(getContext(), "No se recibió la obra", Toast.LENGTH_SHORT).show();
            return;
        }

        txtObraAsignacion.setText("Obra: " + (obraNombre != null ? obraNombre : obraId));

        cargarOperadores();

        btnAsignar.setOnClickListener(v -> asignarOperador(v));
    }

    private void cargarOperadores() {
        firestore.getOperadores()
                .addOnSuccessListener(this::llenarSpinnerOperadores)
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error al cargar operadores", Toast.LENGTH_SHORT).show()
                );
    }

    private void llenarSpinnerOperadores(QuerySnapshot snapshot) {
        operadoresUids.clear();
        operadoresNombres.clear();

        for (DocumentSnapshot doc : snapshot.getDocuments()) {
            String uid = doc.getId();
            String nombre = doc.getString("nombre");
            String correo = doc.getString("correo");

            operadoresUids.add(uid);
            operadoresNombres.add((nombre != null ? nombre : "Sin nombre") +
                    " (" + (correo != null ? correo : "") + ")");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                operadoresNombres
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOperadores.setAdapter(adapter);
    }

    private void asignarOperador(View view) {
        int pos = spinnerOperadores.getSelectedItemPosition();
        if (pos == -1 || operadoresUids.isEmpty()) {
            Toast.makeText(getContext(), "Selecciona un operador", Toast.LENGTH_SHORT).show();
            return;
        }

        String operadorUid = operadoresUids.get(pos);

        firestore.asignarObraAOperador(obraId, operadorUid)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Operador asignado a la obra", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(view).popBackStack();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error al asignar operador", Toast.LENGTH_SHORT).show()
                );
    }
}
