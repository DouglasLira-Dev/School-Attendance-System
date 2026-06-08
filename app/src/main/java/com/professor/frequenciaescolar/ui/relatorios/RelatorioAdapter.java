package com.professor.frequenciaescolar.ui.relatorios;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.professor.frequenciaescolar.R;

import java.util.ArrayList;
import java.util.List;

public class RelatorioAdapter extends RecyclerView.Adapter<RelatorioAdapter.RelatorioViewHolder> {

    private List<AlunoFrequencia> alunos = new ArrayList<>();

    @NonNull
    @Override
    public RelatorioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_relatorio_aluno, parent, false);
        return new RelatorioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RelatorioViewHolder holder, int position) {
        AlunoFrequencia aluno = alunos.get(position);
        holder.tvNome.setText(aluno.getNome());
        holder.tvMatricula.setText("Matrícula: " + aluno.getMatricula());
        holder.tvFaltas.setText("Faltas: " + aluno.getFaltas());
        holder.tvPercentual.setText(String.format("%.1f%%", aluno.getPercentual()));

        // Mudar cor baseado no percentual
        if (aluno.getPercentual() >= 80) {
            holder.tvPercentual.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_green_dark));
        } else if (aluno.getPercentual() >= 60) {
            holder.tvPercentual.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_orange_dark));
        } else {
            holder.tvPercentual.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_red_dark));
        }
    }

    @Override
    public int getItemCount() {
        return alunos.size();
    }

    public void setAlunos(List<AlunoFrequencia> alunos) {
        this.alunos = alunos;
        notifyDataSetChanged();
    }

    public static class AlunoFrequencia {
        private long id;
        private String nome;
        private String matricula;
        private int faltas;
        private int totalAulas;
        private double percentual;

        public AlunoFrequencia(long id, String nome, String matricula, int faltas, int totalAulas) {
            this.id = id;
            this.nome = nome;
            this.matricula = matricula;
            this.faltas = faltas;
            this.totalAulas = totalAulas;
            this.percentual = totalAulas > 0 ? ((totalAulas - faltas) * 100.0 / totalAulas) : 100;
        }

        public long getId() { return id; }
        public String getNome() { return nome; }
        public String getMatricula() { return matricula; }
        public int getFaltas() { return faltas; }
        public int getTotalAulas() { return totalAulas; }
        public double getPercentual() { return percentual; }
    }

    static class RelatorioViewHolder extends RecyclerView.ViewHolder {
        TextView tvNome;
        TextView tvMatricula;
        TextView tvFaltas;
        TextView tvPercentual;

        RelatorioViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNome = itemView.findViewById(R.id.tvAlunoNome);
            tvMatricula = itemView.findViewById(R.id.tvMatricula);
            tvFaltas = itemView.findViewById(R.id.tvFaltas);
            tvPercentual = itemView.findViewById(R.id.tvPercentual);
        }
    }
}