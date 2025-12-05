package com.example.serviciospublicos.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.serviciospublicos.R;
import com.example.serviciospublicos.models.Obra;
import com.example.serviciospublicos.models.enums.EstatusObra;

import java.util.ArrayList;
import java.util.List;

public class ObrasAdapter extends RecyclerView.Adapter<ObrasAdapter.ObraViewHolder> {

    public interface OnObraClickListener {
        void onObraClick(Obra obra);
    }

    private final List<Obra> obras = new ArrayList<>();
    private final OnObraClickListener listener;

    public ObrasAdapter(OnObraClickListener listener) {
        this.listener = listener;
    }

    public void setObras(List<Obra> nuevasObras) {
        obras.clear();
        if (nuevasObras != null) {
            obras.addAll(nuevasObras);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ObraViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_obra, parent, false);
        return new ObraViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ObraViewHolder holder, int position) {
        Obra obra = obras.get(position);
        holder.bind(obra, listener);
    }

    @Override
    public int getItemCount() {
        return obras.size();
    }

    static class ObraViewHolder extends RecyclerView.ViewHolder {

        TextView txtNombre, txtEstatus, txtSupervisor;

        public ObraViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNombre = itemView.findViewById(R.id.txtNombreObra);
            txtEstatus = itemView.findViewById(R.id.txtEstatusObra);
            txtSupervisor = itemView.findViewById(R.id.txtSupervisorObra);
        }

        public void bind(Obra obra, OnObraClickListener listener) {
            txtNombre.setText(obra.getNombre());
            EstatusObra estatus = obra.getEstatus();
            txtEstatus.setText(estatus != null ? estatus.name() : "SIN ESTATUS");
            txtSupervisor.setText("Supervisor: " + obra.getSupervisorAsignado());

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onObraClick(obra);
                }
            });
        }
    }
}
