package com.professor.frequenciaescolar.ui.chamada;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;
import com.professor.frequenciaescolar.R;
import com.professor.frequenciaescolar.data.entities.Aluno;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChamadaAdapter extends RecyclerView.Adapter<ChamadaAdapter.ChamadaViewHolder> {

    private List<Aluno> alunos = new ArrayList<>();
    private Map<Long, Boolean> presencas = new HashMap<>();
    private Map<Long, String> justificativas = new HashMap<>();

    @NonNull
    @Override
    public ChamadaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chamada_aluno, parent, false);
        return new ChamadaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChamadaViewHolder holder, int position) {
        Aluno aluno = alunos.get(position);
        holder.tvNome.setText(aluno.getNome());
        holder.tvMatricula.setText("Matrícula: " + aluno.getMatricula());

        // Carregar valores salvos
        Boolean presente = presencas.get(aluno.getId());
        holder.switchPresente.setChecked(presente != null ? presente : true);

        String justificativa = justificativas.get(aluno.getId());
        if (justificativa != null && !justificativa.isEmpty()) {
            holder.etJustificativa.setText(justificativa);
        } else {
            holder.etJustificativa.setText("");
        }

        // Mostrar/esconder justificativa
        if (!holder.switchPresente.isChecked()) {
            holder.tilJustificativa.setVisibility(View.VISIBLE);
        } else {
            holder.tilJustificativa.setVisibility(View.GONE);
        }

        // Listener do switch
        holder.switchPresente.setOnCheckedChangeListener((buttonView, isChecked) -> {
            presencas.put(aluno.getId(), isChecked);
            if (!isChecked) {
                holder.tilJustificativa.setVisibility(View.VISIBLE);
            } else {
                holder.tilJustificativa.setVisibility(View.GONE);
                justificativas.put(aluno.getId(), "");
                holder.etJustificativa.setText("");
            }
        });

        // Listener da justificativa
        holder.etJustificativa.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String texto = holder.etJustificativa.getText().toString().trim();
                justificativas.put(aluno.getId(), texto);
            }
        });
    }

    @Override
    public int getItemCount() {
        return alunos.size();
    }

    public void setAlunos(List<Aluno> alunos) {
        this.alunos = alunos;
        // Inicializar todos como presentes
        for (Aluno a : alunos) {
            if (!presencas.containsKey(a.getId())) {
                presencas.put(a.getId(), true);
            }
        }
        notifyDataSetChanged();
    }

    public Map<Long, Boolean> getPresencas() {
        // Salvar justificativas antes de retornar
        return presencas;
    }

    public Map<Long, String> getJustificativas() {
        return justificativas;
    }

    public void carregarDadosExistentes(Map<Long, Boolean> presencasExistentes, Map<Long, String> justificativasExistentes) {
        this.presencas = new HashMap<>(presencasExistentes);
        this.justificativas = new HashMap<>(justificativasExistentes);
        notifyDataSetChanged();
    }

    static class ChamadaViewHolder extends RecyclerView.ViewHolder {
        TextView tvNome;
        TextView tvMatricula;
        SwitchCompat switchPresente;
        TextInputLayout tilJustificativa;
        com.google.android.material.textfield.TextInputEditText etJustificativa;
        CardView cardView;

        ChamadaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNome = itemView.findViewById(R.id.tvAlunoNome);
            tvMatricula = itemView.findViewById(R.id.tvMatricula);
            switchPresente = itemView.findViewById(R.id.switchPresente);
            tilJustificativa = itemView.findViewById(R.id.tilJustificativa);
            etJustificativa = itemView.findViewById(R.id.etJustificativa);
            cardView = (CardView) itemView;
        }
    }
}