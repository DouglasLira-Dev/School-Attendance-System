package com.professor.frequenciaescolar.ui.relatorios;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.professor.frequenciaescolar.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoricoAdapter extends RecyclerView.Adapter<HistoricoAdapter.HistoricoViewHolder> {

    private List<ItemHistorico> itens = new ArrayList<>();

    @NonNull
    @Override
    public HistoricoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_historico_chamada, parent, false);
        return new HistoricoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoricoViewHolder holder, int position) {
        ItemHistorico item = itens.get(position);

        holder.tvData.setText(item.getData());
        holder.tvHorario.setText(item.getHorario());

        if (item.isPresente()) {
            holder.tvStatusTexto.setText("Presente");
            holder.tvStatusTexto.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_green_dark));
            holder.viewStatus.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_green_dark));
            holder.tvJustificativa.setVisibility(View.GONE);
        } else if (item.isJustificada()) {
            holder.tvStatusTexto.setText("Falta Justificada");
            holder.tvStatusTexto.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_orange_dark));
            holder.viewStatus.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_orange_dark));
            holder.tvJustificativa.setText(item.getJustificativa());
            holder.tvJustificativa.setVisibility(View.VISIBLE);
        } else {
            holder.tvStatusTexto.setText("Falta Não Justificada");
            holder.tvStatusTexto.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_red_dark));
            holder.viewStatus.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_red_dark));
            holder.tvJustificativa.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return itens.size();
    }

    public void setItens(List<ItemHistorico> itens) {
        this.itens = itens;
        notifyDataSetChanged();
    }

    static class HistoricoViewHolder extends RecyclerView.ViewHolder {
        View viewStatus;
        TextView tvData;
        TextView tvHorario;
        TextView tvStatusTexto;
        TextView tvJustificativa;

        HistoricoViewHolder(@NonNull View itemView) {
            super(itemView);
            viewStatus = itemView.findViewById(R.id.viewStatus);
            tvData = itemView.findViewById(R.id.tvData);
            tvHorario = itemView.findViewById(R.id.tvHorario);
            tvStatusTexto = itemView.findViewById(R.id.tvStatusTexto);
            tvJustificativa = itemView.findViewById(R.id.tvJustificativa);
        }
    }

    public static class ItemHistorico {
        private String data;
        private String horario;
        private boolean presente;
        private boolean justificada;
        private String justificativa;

        public ItemHistorico(String data, String horario, boolean presente, boolean justificada, String justificativa) {
            this.data = data;
            this.horario = horario;
            this.presente = presente;
            this.justificada = justificada;
            this.justificativa = justificativa;
        }

        public String getData() { return data; }
        public String getHorario() { return horario; }
        public boolean isPresente() { return presente; }
        public boolean isJustificada() { return justificada; }
        public String getJustificativa() { return justificativa; }
    }
}