package com.professor.frequenciaescolar.ui.alunos;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.professor.frequenciaescolar.R;
import com.professor.frequenciaescolar.data.entities.Aluno;

import java.util.ArrayList;
import java.util.List;

public class AlunoAdapter extends RecyclerView.Adapter<AlunoAdapter.AlunoViewHolder> {

    private List<Aluno> alunos = new ArrayList<>();
    private OnItemClickListener listener;

    @NonNull
    @Override
    public AlunoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_aluno, parent, false);
        return new AlunoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlunoViewHolder holder, int position) {
        Aluno aluno = alunos.get(position);
        holder.tvNome.setText(aluno.getNome());
        holder.tvMatricula.setText("Matrícula: " + aluno.getMatricula());
        holder.tvStatus.setText("Status: " + aluno.getStatus());
        holder.tvResponsavel.setText("Responsável: " + aluno.getResponsavel());

        // Mudar cor do status
        if ("ativo".equals(aluno.getStatus())) {
            holder.tvStatus.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_green_dark));
        } else {
            holder.tvStatus.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_red_dark));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(aluno);
            }
        });
    }

    @Override
    public int getItemCount() {
        return alunos.size();
    }

    public void setAlunos(List<Aluno> alunos) {
        this.alunos = alunos;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(Aluno aluno);
    }

    static class AlunoViewHolder extends RecyclerView.ViewHolder {
        TextView tvNome;
        TextView tvMatricula;
        TextView tvStatus;
        TextView tvResponsavel;

        AlunoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNome = itemView.findViewById(R.id.tvAlunoNome);
            tvMatricula = itemView.findViewById(R.id.tvMatricula);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvResponsavel = itemView.findViewById(R.id.tvResponsavel);
        }
    }
}