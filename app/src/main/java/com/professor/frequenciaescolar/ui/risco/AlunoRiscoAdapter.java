package com.professor.frequenciaescolar.ui.risco;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.professor.frequenciaescolar.R;

import java.util.ArrayList;
import java.util.List;

public class AlunoRiscoAdapter extends RecyclerView.Adapter<AlunoRiscoAdapter.RiscoViewHolder> {

    private List<AlunoRisco> alunos = new ArrayList<>();

    @NonNull
    @Override
    public RiscoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_aluno_risco, parent, false);
        return new RiscoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RiscoViewHolder holder, int position) {
        AlunoRisco aluno = alunos.get(position);

        holder.tvNome.setText(aluno.getNome());
        holder.tvAlunoInfo.setText(String.format("Matrícula: %s | Turma: %s",
                aluno.getMatricula(), aluno.getTurma()));

        double percentual = aluno.getFrequencia();
        holder.tvPercentual.setText(String.format("%.1f%%", percentual));

        // Cor do percentual baseado no risco usando cores personalizadas
        if (percentual < 50) {
            holder.tvPercentual.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.risk_critical));
            holder.viewStatus.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.risk_critical));
        } else if (percentual < 65) {
            holder.tvPercentual.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.risk_high));
            holder.viewStatus.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.risk_high));
        } else if (percentual < 75) {
            holder.tvPercentual.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.risk_moderate));
            holder.viewStatus.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.risk_moderate));
        } else if (percentual < 80) {
            holder.tvPercentual.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.risk_attention));
            holder.viewStatus.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.risk_attention));
        } else {
            holder.tvPercentual.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.risk_good));
            holder.viewStatus.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.risk_good));
        }

        holder.tvPresencas.setText(String.valueOf(aluno.getPresencas()));
        holder.tvFaltas.setText(String.valueOf(aluno.getFaltas()));
        holder.tvDiasLetivos.setText(String.valueOf(aluno.getTotalDias()));
    }

    @Override
    public int getItemCount() {
        return alunos.size();
    }

    public void setAlunos(List<AlunoRisco> alunos) {
        this.alunos = alunos;
        notifyDataSetChanged();
    }

    public static class AlunoRisco {
        private long id;
        private String nome;
        private String matricula;
        private String turma;
        private double frequencia;
        private int presencas;
        private int faltas;
        private int faltasJustificadas;
        private int totalDias;

        // Getters e Setters
        public long getId() { return id; }
        public void setId(long id) { this.id = id; }
        public String getNome() { return nome; }
        public void setNome(String nome) { this.nome = nome; }
        public String getMatricula() { return matricula; }
        public void setMatricula(String matricula) { this.matricula = matricula; }
        public String getTurma() { return turma; }
        public void setTurma(String turma) { this.turma = turma; }
        public double getFrequencia() { return frequencia; }
        public void setFrequencia(double frequencia) { this.frequencia = frequencia; }
        public int getPresencas() { return presencas; }
        public void setPresencas(int presencas) { this.presencas = presencas; }
        public int getFaltas() { return faltas; }
        public void setFaltas(int faltas) { this.faltas = faltas; }
        public int getFaltasJustificadas() { return faltasJustificadas; }
        public void setFaltasJustificadas(int faltasJustificadas) { this.faltasJustificadas = faltasJustificadas; }
        public int getTotalDias() { return totalDias; }
        public void setTotalDias(int totalDias) { this.totalDias = totalDias; }
    }

    static class RiscoViewHolder extends RecyclerView.ViewHolder {
        View viewStatus;
        TextView tvNome, tvAlunoInfo, tvPercentual, tvPresencas, tvFaltas, tvDiasLetivos;

        RiscoViewHolder(@NonNull View itemView) {
            super(itemView);
            viewStatus = itemView.findViewById(R.id.viewStatus);
            tvNome = itemView.findViewById(R.id.tvAlunoNome);
            tvAlunoInfo = itemView.findViewById(R.id.tvAlunoInfo);
            tvPercentual = itemView.findViewById(R.id.tvPercentual);
            tvPresencas = itemView.findViewById(R.id.tvPresencas);
            tvFaltas = itemView.findViewById(R.id.tvFaltas);
            tvDiasLetivos = itemView.findViewById(R.id.tvDiasLetivos);
        }
    }
}