package com.professor.frequenciaescolar.ui.backup;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.professor.frequenciaescolar.R;
import com.professor.frequenciaescolar.utils.BackupManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BackupAdapter extends RecyclerView.Adapter<BackupAdapter.BackupViewHolder> {

    private List<File> backups = new ArrayList<>();
    private OnBackupActionListener listener;

    public interface OnBackupActionListener {
        void onRestore(File backupFile);
    }

    public BackupAdapter(List<File> backups, OnBackupActionListener listener) {
        this.backups = backups;
        this.listener = listener;
    }

    public void updateList(List<File> newList) {
        this.backups = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BackupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_backup, parent, false);
        return new BackupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BackupViewHolder holder, int position) {
        File backup = backups.get(position);

        holder.tvNomeArquivo.setText(backup.getName());
        holder.tvData.setText(BackupManager.formatFileDate(backup));
        holder.tvTamanho.setText(BackupManager.formatFileSize(backup.length()));

        holder.btnRestaurar.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRestore(backup);
            }
        });

        // Compartilhar (opcional)
        holder.btnCompartilhar.setOnClickListener(v -> {
            // Implementar compartilhamento
        });

        // Excluir (opcional)
        holder.btnExcluir.setOnClickListener(v -> {
            if (backup.delete()) {
                updateList(new ArrayList<>(new ArrayList<>()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return backups.size();
    }

    static class BackupViewHolder extends RecyclerView.ViewHolder {
        TextView tvNomeArquivo, tvData, tvTamanho;
        ImageButton btnRestaurar, btnCompartilhar, btnExcluir;

        BackupViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNomeArquivo = itemView.findViewById(R.id.tvNomeArquivo);
            tvData = itemView.findViewById(R.id.tvData);
            tvTamanho = itemView.findViewById(R.id.tvTamanho);
            btnRestaurar = itemView.findViewById(R.id.btnRestaurar);
            btnCompartilhar = itemView.findViewById(R.id.btnCompartilhar);
            btnExcluir = itemView.findViewById(R.id.btnExcluir);
        }
    }
}