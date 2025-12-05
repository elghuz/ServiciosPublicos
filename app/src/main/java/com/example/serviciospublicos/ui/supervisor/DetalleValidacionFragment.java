package com.example.serviciospublicos.ui.supervisor;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.serviciospublicos.R;
import com.example.serviciospublicos.auth.AuthService;
import com.example.serviciospublicos.firebase.FirestoreService;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class DetalleValidacionFragment extends Fragment {

    private TextView txtTipo, txtUsuario, txtFecha, txtUbicacion,
            txtDescripcion, txtEtapa, txtEstado, txtUrlArchivo;
    private EditText inputComentario;
    private Button btnAprobar, btnRechazar;
    private ProgressBar progressBar;

    private final FirestoreService firestore = FirestoreService.getInstance();
    private final AuthService auth = AuthService.getInstance();

    private String obraId;
    private String obraNombre;
    private String evidenciaId;

    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public DetalleValidacionFragment() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detalle_validacion, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        txtTipo = view.findViewById(R.id.txtTipoDetalle);
        txtUsuario = view.findViewById(R.id.txtUsuarioDetalle);
        txtFecha = view.findViewById(R.id.txtFechaDetalle);
        txtUbicacion = view.findViewById(R.id.txtUbicacionDetalle);
        txtDescripcion = view.findViewById(R.id.txtDescripcionDetalle);
        txtEtapa = view.findViewById(R.id.txtEtapaDetalle);
        txtEstado = view.findViewById(R.id.txtEstadoDetalle);
        txtUrlArchivo = view.findViewById(R.id.txtUrlArchivo);
        inputComentario = view.findViewById(R.id.inputComentarioSupervisor);
        btnAprobar = view.findViewById(R.id.btnAprobar);
        btnRechazar = view.findViewById(R.id.btnRechazar);
        progressBar = view.findViewById(R.id.progressDetalle);

        if (getArguments() != null) {
            obraId = getArguments().getString("obraId");
            obraNombre = getArguments().getString("obraNombre");
            evidenciaId = getArguments().getString("evidenciaId");
        }

        if (obraId == null || evidenciaId == null) {
            Toast.makeText(getContext(), "Faltan datos de evidencia", Toast.LENGTH_SHORT).show();
            return;
        }

        cargarDetalleEvidencia();

        btnAprobar.setOnClickListener(v -> cambiarEstado("aprobada"));
        btnRechazar.setOnClickListener(v -> cambiarEstado("rechazada"));
    }

    private void cargarDetalleEvidencia() {
        progressBar.setVisibility(View.VISIBLE);

        firestore.getObrasCollection()
                .document(obraId)
                .collection("Evidencias")
                .document(evidenciaId)
                .get()
                .addOnSuccessListener(this::mostrarDetalle)
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Error al cargar evidencia", Toast.LENGTH_SHORT).show();
                });
    }

    private void mostrarDetalle(DocumentSnapshot doc) {
        progressBar.setVisibility(View.GONE);

        if (!doc.exists()) {
            Toast.makeText(getContext(), "La evidencia no existe", Toast.LENGTH_SHORT).show();
            return;
        }

        String tipo = doc.getString("tipo");
        String usuarioNombre = doc.getString("usuarioNombre");
        String descripcion = doc.getString("descripcion");
        String etapa = doc.getString("etapaConstruccion");
        String estado = doc.getString("estado");
        String urlArchivo = doc.getString("urlArchivo");

        Double lat = doc.getDouble("lat");
        Double lng = doc.getDouble("lng");
        Timestamp fechaHora = doc.getTimestamp("fechaHora");

        txtTipo.setText("Tipo: " + (tipo != null ? tipo.toUpperCase() : "N/D"));
        txtUsuario.setText("Usuario: " + (usuarioNombre != null ? usuarioNombre : "Desconocido"));

        if (fechaHora != null) {
            txtFecha.setText("Fecha: " + sdf.format(fechaHora.toDate()));
        } else {
            txtFecha.setText("Fecha: N/D");
        }

        if (lat != null && lng != null) {
            txtUbicacion.setText("Ubicación: " + lat + ", " + lng);
        } else {
            txtUbicacion.setText("Ubicación: N/D");
        }

        txtDescripcion.setText("Descripción: " + (descripcion != null ? descripcion : ""));
        txtEtapa.setText("Etapa: " + (etapa != null ? etapa : ""));
        txtEstado.setText("Estado: " + (estado != null ? estado : "pendiente"));
        txtUrlArchivo.setText("Archivo: " + (urlArchivo != null ? urlArchivo : ""));
    }

    private void cambiarEstado(String nuevoEstado) {
        String comentario = inputComentario.getText().toString().trim();
        if (TextUtils.isEmpty(comentario)) {
            comentario = null;
        }

        String supervisorUid = auth.getCurrentUid();
        String supervisorNombre = null; // opcional: podrías leerlo desde Usuarios

        progressBar.setVisibility(View.VISIBLE);
        btnAprobar.setEnabled(false);
        btnRechazar.setEnabled(false);

        firestore.actualizarEstadoEvidencia(
                obraId,
                evidenciaId,
                nuevoEstado,
                comentario,
                supervisorUid,
                supervisorNombre
        ).addOnSuccessListener(aVoid -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(getContext(), "Evidencia " + nuevoEstado, Toast.LENGTH_SHORT).show();
            Navigation.findNavController(requireView()).popBackStack();
        }).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            btnAprobar.setEnabled(true);
            btnRechazar.setEnabled(true);
            Toast.makeText(getContext(), "Error al actualizar evidencia", Toast.LENGTH_SHORT).show();
        });
    }
}
