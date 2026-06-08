package com.professor.frequenciaescolar.ui.turmas;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.professor.frequenciaescolar.R;
import com.professor.frequenciaescolar.data.entities.Turma;
import com.professor.frequenciaescolar.data.repository.FrequenciaRepository;
import com.professor.frequenciaescolar.ui.chamada.ChamadaActivity;
import com.professor.frequenciaescolar.ui.relatorios.RelatorioDashboardActivity;

import java.util.List;

public class TurmaListActivity extends AppCompatActivity {

    private RecyclerView rvTurmas;
    private TextView tvEmpty;
    private TurmaAdapter adapter;
    private FrequenciaRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_turma_list);

        // Configurar toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Inicializar views
        rvTurmas = findViewById(R.id.rvTurmas);
        tvEmpty = findViewById(R.id.tvEmpty);

        // Configurar RecyclerView
        adapter = new TurmaAdapter();
        rvTurmas.setLayoutManager(new LinearLayoutManager(this));
        rvTurmas.setAdapter(adapter);

        // Configurar clique no item
        adapter.setOnItemClickListener(turma -> {
            Intent intent = new Intent(TurmaListActivity.this, TurmaFormActivity.class);
            intent.putExtra("turma_id", turma.getId());
            startActivity(intent);
        });

        // Inicializar repository
        repository = FrequenciaRepository.getInstance(this);

        // Carregar turmas
        carregarTurmas();
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarTurmas();
    }

    private void carregarTurmas() {
        repository.getAllTurmasAtivas(turmas -> {
            runOnUiThread(() -> {
                if (turmas == null || turmas.isEmpty()) {
                    rvTurmas.setVisibility(android.view.View.GONE);
                    tvEmpty.setVisibility(android.view.View.VISIBLE);
                } else {
                    rvTurmas.setVisibility(android.view.View.VISIBLE);
                    tvEmpty.setVisibility(android.view.View.GONE);
                    adapter.setTurmas(turmas);
                }
            });
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_turma_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_add) {
            Intent intent = new Intent(this, TurmaFormActivity.class);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.action_relatorios) {
            Intent intent = new Intent(this, RelatorioDashboardActivity.class);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.action_chamada) {
            Intent intent = new Intent(this, ChamadaActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}