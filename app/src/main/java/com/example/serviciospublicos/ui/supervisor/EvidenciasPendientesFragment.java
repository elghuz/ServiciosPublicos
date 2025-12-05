package com.example.serviciospublicos.ui.supervisor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.serviciospublicos.R;
import com.example.serviciospublicos.firebase.FirestoreService;
import com.example.serviciospublicos.ui.adapters.EvidenciasAdapter;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EvidenciasPendientesFragment extends Fragment {

    private TextView txtTituloObra;
    private RecyclerView recyclerEvidencias;
    private EvidenciasAdapter adapter;

    private final FirestoreService firestore = FirestoreService.getInstance();

    private String obraId;
    private String obraNombre;

    public EvidenciasPendientesFragment() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_evidencias_pendientes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        txtTituloObra = view.findViewById(R.id.txtTituloObraEvidencias);
        recyclerEvidencias = view.findViewById(R.id.recyclerEvidenciasPendientes);
        recyclerEvidencias.setLayoutManager(new LinearLayoutManager(getContext()));

        if (getArguments() != null) {
            obraId = getArguments().getString("obraId");
            obraNombre = getArguments().getString("obraNombre");
        }

        if (obraId == null) {
            Toast.makeText(getContext(), "No se recibió la obra", Toast.LENGTH_SHORT).show();
            return;
        }

        txtTituloObra.setText("Evidencias pendientes - " + (obraNombre != null ? obraNombre : obraId));

        adapter = new EvidenciasAdapter((evidenciaId, data) -> {
            Bundle args = new Bundle();
            args.putString("obraId", obraId);
            args.putString("obraNombre", obraNombre);
            args.putString("evidenciaId", evidenciaId);

            Navigation.findNavController(view)
                    .navigate(R.id.action_evidenciasPendientesFragment_to_detalleValidacionFragment, args);
        });

        recyclerEvidencias.setAdapter(adapter);

        cargarEvidenciasPendientes();
    }

    private void cargarEvidenciasPendientes() {
        firestore.getEvidenciasPendientes(obraId)
                .addOnSuccessListener(querySnapshot -> {
                    List<String> ids = new ArrayList<>();
                    List<Map<String, Object>> dataList = new ArrayList<>();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        ids.add(doc.getId());
                        dataList.add(new HashMap<>(doc.getData()));
                    }

                    adapter.setEvidencias(ids, dataList);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error al cargar evidencias", Toast.LENGTH_SHORT).show()
                );
    }
}
