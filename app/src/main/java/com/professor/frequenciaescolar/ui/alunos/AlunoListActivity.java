package com.professor.frequenciaescolar.ui.alunos;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.professor.frequenciaescolar.R;
import com.professor.frequenciaescolar.data.entities.Aluno;
import com.professor.frequenciaescolar.data.entities.Matricula;
import com.professor.frequenciaescolar.data.repository.FrequenciaRepository;
import com.professor.frequenciaescolar.ui.graficos.GraficosFrequenciaActivity;
import com.professor.frequenciaescolar.ui.importar.ImportarAlunosActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlunoListActivity extends AppCompatActivity {

    private RecyclerView rvAlunos;
    private TextView tvEmpty;
    private AlunoAdapter adapter;
    private FrequenciaRepository repository;

    private long turmaId;
    private String turmaNome;
    private Map<Long, Aluno> alunosMap = new HashMap<>();
    private SwipeRefreshLayout swipeRefresh;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aluno_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Configurar Swipe to Refresh
        swipeRefresh = findViewById(R.id.swipeRefresh);
        if (swipeRefresh != null) {
            swipeRefresh.setOnRefreshListener(() -> {
                carregarAlunos();  // Corrigido: carregarAlunos, não carregarTurmas
                swipeRefresh.setRefreshing(false);
            });
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Receber dados da turma
        turmaId = getIntent().getLongExtra("turma_id", -1);
        turmaNome = getIntent().getStringExtra("turma_nome");

        if (getSupportActionBar() != null && turmaNome != null) {
            getSupportActionBar().setTitle("Alunos - " + turmaNome);
        }

        rvAlunos = findViewById(R.id.rvAlunos);
        tvEmpty = findViewById(R.id.tvEmpty);

        adapter = new AlunoAdapter();
        rvAlunos.setLayoutManager(new LinearLayoutManager(this));
        rvAlunos.setAdapter(adapter);

        repository = FrequenciaRepository.getInstance(this);

        // Configurar clique no aluno - vai para tela de detalhes
        adapter.setOnItemClickListener(aluno -> {
            Intent intent = new Intent(AlunoListActivity.this, AlunoDetalheActivity.class);
            intent.putExtra("aluno_id", aluno.getId());
            startActivity(intent);
        });

        if (turmaId != -1) {
            carregarAlunos();
        } else {
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText("Turma não encontrada");
        }
    }

    private void carregarAlunos() {
        repository.getAlunosMatriculadosNaTurma(turmaId, matriculas -> {
            runOnUiThread(() -> {
                if (matriculas == null || matriculas.isEmpty()) {
                    rvAlunos.setVisibility(View.GONE);
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText("Nenhum aluno matriculado nesta turma.\nClique no + para adicionar");
                    adapter.setAlunos(new ArrayList<>());
                    return;
                }

                // Limpar mapa antes de carregar
                alunosMap.clear();

                for (Matricula m : matriculas) {
                    repository.getAlunoById(m.getAlunoId(), aluno -> {
                        runOnUiThread(() -> {
                            if (aluno != null && "ativo".equals(aluno.getStatus())) {
                                // Usar HashMap para evitar duplicação
                                alunosMap.put(aluno.getId(), aluno);
                                // Converter mapa para lista
                                List<Aluno> alunosList = new ArrayList<>(alunosMap.values());
                                adapter.setAlunos(alunosList);

                                if (alunosList.isEmpty()) {
                                    rvAlunos.setVisibility(View.GONE);
                                    tvEmpty.setVisibility(View.VISIBLE);
                                    tvEmpty.setText("Nenhum aluno ativo nesta turma");
                                } else {
                                    rvAlunos.setVisibility(View.VISIBLE);
                                    tvEmpty.setVisibility(View.GONE);
                                }
                            }
                        });
                    });
                }
            });
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_aluno_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_add) {
            Intent intent = new Intent(this, AlunoFormActivity.class);
            intent.putExtra("turma_id", turmaId);
            intent.putExtra("turma_nome", turmaNome);
            startActivity(intent);
            return true;
        } else if (itemId == android.R.id.home) {
            finish();
            return true;
        } else if (itemId == R.id.action_graficos) {
            Intent intent = new Intent(this, GraficosFrequenciaActivity.class);
            intent.putExtra("turma_id", turmaId);
            intent.putExtra("aluno_id", -1L);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.action_importar) {
            Intent intent = new Intent(this, ImportarAlunosActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (turmaId != -1) {
            // Limpar adapter antes de recarregar
            adapter.setAlunos(new ArrayList<>());
            alunosMap.clear();
            carregarAlunos();
        }
    }
}