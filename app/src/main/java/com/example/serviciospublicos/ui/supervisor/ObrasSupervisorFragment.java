package com.example.serviciospublicos.ui.supervisor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.serviciospublicos.R;
import com.example.serviciospublicos.auth.AuthService;
import com.example.serviciospublicos.firebase.FirestoreService;
import com.example.serviciospublicos.models.Obra;
import com.example.serviciospublicos.models.enums.EstatusObra;
import com.example.serviciospublicos.ui.adapters.ObrasAdapter;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ObrasSupervisorFragment extends Fragment {

    private RecyclerView recyclerObras;
    private ObrasAdapter adapter;

    private final FirestoreService firestore = FirestoreService.getInstance();
    private final AuthService auth = AuthService.getInstance();

    public ObrasSupervisorFragment() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_obras_supervisor, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerObras = view.findViewById(R.id.recyclerObrasSupervisor);
        recyclerObras.setLayoutManager(new LinearLayoutManager(getContext()));

        // Al tocar una obra → ir a EvidenciasPendientesFragment (obraId + obraNombre)
        adapter = new ObrasAdapter(obra -> {
            Bundle args = new Bundle();
            args.putString("obraId", obra.getId());
            args.putString("obraNombre", obra.getNombre());
            Navigation.findNavController(view)
                    .navigate(R.id.action_obrasSupervisorFragment_to_evidenciasPendientesFragment, args);
        });
        recyclerObras.setAdapter(adapter);

        cargarObrasSupervisor();
    }

    private void cargarObrasSupervisor() {
        String uid = auth.getCurrentUid();
        if (uid == null) {
            Toast.makeText(getContext(), "No hay usuario autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        // Puedes usar este helper si ya lo agregaste en FirestoreService:
        // firestore.getObrasPorSupervisor(uid)...
        firestore.getObrasCollection()
                .whereEqualTo("supervisorAsignado", uid)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Obra> lista = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Obra obra = new Obra();
                        obra.setId(doc.getId());
                        obra.setNombre(doc.getString("nombre"));
                        obra.setDescripcion(doc.getString("descripcion"));
                        obra.setSupervisorAsignado(doc.getString("supervisorAsignado"));

                        String estatusStr = doc.getString("estatus");
                        obra.setEstatus(EstatusObra.fromString(estatusStr));

                        lista.add(obra);
                    }
                    adapter.setObras(lista);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error al cargar obras", Toast.LENGTH_SHORT).show()
                );
    }
}
