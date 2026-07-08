package com.professor.frequenciaescolar.ui.onboarding;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.professor.frequenciaescolar.R;
import com.professor.frequenciaescolar.ui.auth.LoginActivity;

import java.util.Arrays;

public class OnboardingActivity extends AppCompatActivity {

    private RecyclerView rvOnboarding;
    private Button btnPular, btnProximo, btnComecar;
    private OnboardingAdapter adapter;
    private int posicaoAtual = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        rvOnboarding = findViewById(R.id.rvOnboarding);
        btnPular = findViewById(R.id.btnPular);
        btnProximo = findViewById(R.id.btnProximo);
        btnComecar = findViewById(R.id.btnComecar);

        // Dados do onboarding
        adapter = new OnboardingAdapter(Arrays.asList(
                new OnboardingAdapter.OnboardingItem(
                        android.R.drawable.ic_menu_edit,
                        "Crie Turmas",
                        "Cadastre as turmas que você leciona com nome, turno e ano letivo."
                ),
                new OnboardingAdapter.OnboardingItem(
                        android.R.drawable.ic_menu_add,
                        "Cadastre Alunos",
                        "Adicione seus alunos com nome, matrícula e informações de contato."
                ),
                new OnboardingAdapter.OnboardingItem(
                        android.R.drawable.ic_menu_today,
                        "Registre Chamadas",
                        "Marque presença e ausência com um clique, com geolocalização e justificativas."
                ),
                new OnboardingAdapter.OnboardingItem(
                        android.R.drawable.ic_menu_gallery,
                        "Veja Relatórios",
                        "Acompanhe a frequência com gráficos, relatórios PDF e exportação Excel."
                )
        ));

        rvOnboarding.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvOnboarding.setAdapter(adapter);

        // Listener para mudar botões conforme a página
        rvOnboarding.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                posicaoAtual = ((LinearLayoutManager) recyclerView.getLayoutManager())
                        .findFirstVisibleItemPosition();
                atualizarBotoes();
            }
        });

        btnPular.setOnClickListener(v -> finalizarOnboarding());
        btnProximo.setOnClickListener(v -> {
            if (posicaoAtual < adapter.getItemCount() - 1) {
                rvOnboarding.smoothScrollToPosition(posicaoAtual + 1);
            }
        });
        btnComecar.setOnClickListener(v -> finalizarOnboarding());

        atualizarBotoes();
    }

    private void atualizarBotoes() {
        boolean ultimo = posicaoAtual == adapter.getItemCount() - 1;
        btnPular.setVisibility(ultimo ? View.GONE : View.VISIBLE);
        btnProximo.setVisibility(ultimo ? View.GONE : View.VISIBLE);
        btnComecar.setVisibility(ultimo ? View.VISIBLE : View.GONE);
    }

    private void finalizarOnboarding() {
        // Salvar que já foi mostrado
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        prefs.edit().putBoolean("onboarding_mostrado", true).apply();

        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}