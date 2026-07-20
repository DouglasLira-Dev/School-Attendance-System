package com.professor.frequenciaescolar.ui.importar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.professor.frequenciaescolar.R;

import java.util.List;

public class PreviewAdapter extends RecyclerView.Adapter<PreviewAdapter.PreviewViewHolder> {

    private List<ImportarAlunosActivity.AlunoPreview> previewList;
    private OnRemoveClickListener removeClickListener;

    public PreviewAdapter(List<ImportarAlunosActivity.AlunoPreview> previewList) {
        this.previewList = previewList;
    }

    public void setOnRemoveClickListener(OnRemoveClickListener listener) {
        this.removeClickListener = listener;
    }

    public interface OnRemoveClickListener {
        void onRemoveClick(int position);
    }

    @NonNull
    @Override
    public PreviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_preview_aluno, parent, false);
        return new PreviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PreviewViewHolder holder, int position) {
        ImportarAlunosActivity.AlunoPreview preview = previewList.get(position);

        holder.tvNumero.setText(String.valueOf(position + 1));
        holder.tvNome.setText(preview.nome);
        holder.tvInfo.setText(String.format("Matrícula: %s | Turma: %s",
                preview.matricula, preview.turmaNome));
        holder.ivStatus.setImageResource(android.R.drawable.checkbox_on_background);

        holder.btnRemover.setOnClickListener(v -> {
            int adapterPosition = holder.getBindingAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION && removeClickListener != null) {
                removeClickListener.onRemoveClick(adapterPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return previewList.size();
    }

    static class PreviewViewHolder extends RecyclerView.ViewHolder {
        TextView tvNumero;
        TextView tvNome;
        TextView tvInfo;
        ImageView ivStatus;
        ImageView btnRemover;

        PreviewViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNumero = itemView.findViewById(R.id.tvNumero);
            tvNome = itemView.findViewById(R.id.tvNome);
            tvInfo = itemView.findViewById(R.id.tvInfo);
            ivStatus = itemView.findViewById(R.id.ivStatus);
            btnRemover = itemView.findViewById(R.id.btnRemover);
        }
    }
}