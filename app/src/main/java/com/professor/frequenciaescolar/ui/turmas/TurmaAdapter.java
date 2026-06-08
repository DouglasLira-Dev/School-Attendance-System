package com.professor.frequenciaescolar.ui.turmas;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.professor.frequenciaescolar.R;
import com.professor.frequenciaescolar.data.entities.Turma;

import java.util.ArrayList;
import java.util.List;

public class TurmaAdapter extends RecyclerView.Adapter<TurmaAdapter.TurmaViewHolder> {

    private List<Turma> turmas = new ArrayList<>();
    private OnItemClickListener listener;

    @NonNull
    @Override
    public TurmaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_turma, parent, false);
        return new TurmaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TurmaViewHolder holder, int position) {
        Turma turma = turmas.get(position);
        holder.tvNome.setText(turma.getNome());
        holder.tvTurno.setText("Turno: " + turma.getTurno());
        holder.tvAnoLetivo.setText("Ano: " + turma.getAnoLetivo());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(turma);
            }
        });
    }

    @Override
    public int getItemCount() {
        return turmas.size();
    }

    public void setTurmas(List<Turma> turmas) {
        this.turmas = turmas;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(Turma turma);
    }

    static class TurmaViewHolder extends RecyclerView.ViewHolder {
        TextView tvNome;
        TextView tvTurno;
        TextView tvAnoLetivo;

        TurmaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNome = itemView.findViewById(R.id.tvTurmaNome);
            tvTurno = itemView.findViewById(R.id.tvTurno);
            tvAnoLetivo = itemView.findViewById(R.id.tvAnoLetivo);
        }
    }
}