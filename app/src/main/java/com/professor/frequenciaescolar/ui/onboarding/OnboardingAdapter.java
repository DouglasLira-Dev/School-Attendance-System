package com.professor.frequenciaescolar.ui.onboarding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.professor.frequenciaescolar.R;

import java.util.List;

public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder> {

    private List<OnboardingItem> items;

    public OnboardingAdapter(List<OnboardingItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public OnboardingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_onboarding, parent, false);
        return new OnboardingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OnboardingViewHolder holder, int position) {
        OnboardingItem item = items.get(position);
        holder.ivIcon.setImageResource(item.getIcon());
        holder.tvTitulo.setText(item.getTitulo());
        holder.tvDescricao.setText(item.getDescricao());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class OnboardingViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvTitulo;
        TextView tvDescricao;

        OnboardingViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            tvTitulo = itemView.findViewById(R.id.tvTitulo);
            tvDescricao = itemView.findViewById(R.id.tvDescricao);
        }
    }

    public static class OnboardingItem {
        private int icon;
        private String titulo;
        private String descricao;

        public OnboardingItem(int icon, String titulo, String descricao) {
            this.icon = icon;
            this.titulo = titulo;
            this.descricao = descricao;
        }

        public int getIcon() { return icon; }
        public String getTitulo() { return titulo; }
        public String getDescricao() { return descricao; }
    }
}