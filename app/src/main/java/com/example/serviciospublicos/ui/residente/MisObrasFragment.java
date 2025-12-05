package com.example.serviciospublicos.ui.residente;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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

public class MisObrasFragment extends Fragment {

    private RecyclerView recyclerObras;
    private ObrasAdapter adapter;

    private final FirestoreService firestore = FirestoreService.getInstance();
    private final AuthService auth = AuthService.getInstance();

    public MisObrasFragment() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mis_obras, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerObras = view.findViewById(R.id.recyclerMisObras);
        recyclerObras.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new ObrasAdapter(obra -> {
            // Aquí luego puedes navegar al DetallesObraFragment / CapturarEvidencia
        });
        recyclerObras.setAdapter(adapter);

        cargarObrasOperador();
    }

    private void cargarObrasOperador() {
        String uid = auth.getCurrentUid();
        if (uid == null) return;

        firestore.getUsuario(uid).addOnSuccessListener(userDoc -> {
            List<String> obrasAsignadas = (List<String>) userDoc.get("obrasAsignadas");
            if (obrasAsignadas == null || obrasAsignadas.isEmpty()) {
                adapter.setObras(new ArrayList<>());
                return;
            }

            // Firestore whereIn máximo 10 elementos. Para pruebas está perfecto.
            firestore.getObrasCollection()
                    .whereIn("id", obrasAsignadas)
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
                    });
        });
    }
}
