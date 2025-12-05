// com.example.serviciospublicos.ui.adapters.EvidenciasAdapter

package com.example.serviciospublicos.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.serviciospublicos.R;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EvidenciasAdapter extends RecyclerView.Adapter<EvidenciasAdapter.EvidenciaViewHolder> {

    public interface OnEvidenciaClickListener {
        void onEvidenciaClick(String evidenciaId, Map<String, Object> data);
    }

    private final List<Map<String, Object>> evidencias = new ArrayList<>();
    private final List<String> evidenciasIds = new ArrayList<>();
    private final OnEvidenciaClickListener listener;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public EvidenciasAdapter(OnEvidenciaClickListener listener) {
        this.listener = listener;
    }

    public void setEvidencias(List<String> ids, List<Map<String, Object>> dataList) {
        evidenciasIds.clear();
        evidencias.clear();
        evidenciasIds.addAll(ids);
        evidencias.addAll(dataList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EvidenciaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_evidencia, parent, false);
        return new EvidenciaViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull EvidenciaViewHolder holder, int position) {
        Map<String, Object> data = evidencias.get(position);
        String evidenciaId = evidenciasIds.get(position);

        String tipo = (String) data.get("tipo");
        String usuarioNombre = (String) data.get("usuarioNombre");
        String estado = (String) data.get("estado");
        Timestamp fechaHora = (Timestamp) data.get("fechaHora");

        holder.txtTipo.setText(tipo != null ? tipo.toUpperCase() : "Tipo");
        holder.txtUsuario.setText(usuarioNombre != null ? usuarioNombre : "Usuario desconocido");
        holder.txtEstado.setText(estado != null ? estado : "pendiente");

        if (fechaHora != null) {
            holder.txtFecha.setText(sdf.format(fechaHora.toDate()));
        } else {
            holder.txtFecha.setText("Sin fecha");
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEvidenciaClick(evidenciaId, data);
            }
        });
    }

    @Override
    public int getItemCount() {
        return evidencias.size();
    }

    static class EvidenciaViewHolder extends RecyclerView.ViewHolder {

        TextView txtTipo, txtUsuario, txtFecha, txtEstado;

        EvidenciaViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTipo = itemView.findViewById(R.id.txtTipoEvidencia);
            txtUsuario = itemView.findViewById(R.id.txtUsuarioEvidencia);
            txtFecha = itemView.findViewById(R.id.txtFechaEvidencia);
            txtEstado = itemView.findViewById(R.id.txtEstadoEvidencia);
        }
    }
}
