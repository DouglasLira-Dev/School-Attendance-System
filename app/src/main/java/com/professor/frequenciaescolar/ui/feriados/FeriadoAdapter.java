package com.professor.frequenciaescolar.ui.feriados;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.professor.frequenciaescolar.R;
import com.professor.frequenciaescolar.data.entities.Feriado;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class FeriadoAdapter extends RecyclerView.Adapter<FeriadoAdapter.FeriadoViewHolder> {

    private List<Feriado> feriados;
    private OnFeriadoDeleteListener listener;

    public interface OnFeriadoDeleteListener {
        void onDelete(Feriado feriado);
    }

    public FeriadoAdapter(List<Feriado> feriados, OnFeriadoDeleteListener listener) {
        this.feriados = feriados;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FeriadoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_feriado, parent, false);
        return new FeriadoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeriadoViewHolder holder, int position) {
        Feriado feriado = feriados.get(position);

        holder.tvNome.setText(feriado.getNome());

        // Formatar data
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat sdfOut = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String dataFormatada = sdfOut.format(sdf.parse(feriado.getData()));
            holder.tvData.setText(dataFormatada);
        } catch (Exception e) {
            holder.tvData.setText(feriado.getData());
        }

        if (feriado.isRecorrente()) {
            holder.tvRecorrente.setVisibility(View.VISIBLE);
        } else {
            holder.tvRecorrente.setVisibility(View.GONE);
        }

        holder.btnExcluir.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete(feriado);
            }
        });
    }

    @Override
    public int getItemCount() {
        return feriados.size();
    }

    static class FeriadoViewHolder extends RecyclerView.ViewHolder {
        TextView tvNome, tvData, tvRecorrente;
        ImageButton btnExcluir;

        FeriadoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNome = itemView.findViewById(R.id.tvNome);
            tvData = itemView.findViewById(R.id.tvData);
            tvRecorrente = itemView.findViewById(R.id.tvRecorrente);
            btnExcluir = itemView.findViewById(R.id.btnExcluir);
        }
    }
}