package com.professor.frequenciaescolar.ui.alunos;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.textfield.TextInputEditText;
import com.professor.frequenciaescolar.R;
import com.professor.frequenciaescolar.data.entities.Aluno;
import com.professor.frequenciaescolar.data.entities.Matricula;
import com.professor.frequenciaescolar.data.repository.FrequenciaRepository;
import com.professor.frequenciaescolar.ui.graficos.GraficosFrequenciaActivity;
import com.professor.frequenciaescolar.ui.importar.ImportarAlunosActivity;

import java.util.ArrayList;
import java.util.List;

public class AlunoListActivity extends AppCompatActivity {

    private RecyclerView rvAlunos;
    private TextView tvEmpty;
    private TextInputEditText etBusca;
    private AlunoAdapter adapter;
    private FrequenciaRepository repository;
    private SwipeRefreshLayout swipeRefresh;

    private long turmaId;
    private String turmaNome;
    private List<Aluno> alunos = new ArrayList<>();
    private List<Aluno> alunosFiltrados = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aluno_list);

        try {
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            // Inicializar SwipeRefreshLayout
            swipeRefresh = findViewById(R.id.swipeRefresh);
            if (swipeRefresh != null) {
                swipeRefresh.setOnRefreshListener(() -> {
                    carregarAlunos();
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
            etBusca = findViewById(R.id.etBusca);

            adapter = new AlunoAdapter();
            rvAlunos.setLayoutManager(new LinearLayoutManager(this));
            rvAlunos.setAdapter(adapter);

            repository = FrequenciaRepository.getInstance(this);

            // Configurar clique no aluno
            adapter.setOnItemClickListener(aluno -> {
                Intent intent = new Intent(AlunoListActivity.this, AlunoDetalheActivity.class);
                intent.putExtra("aluno_id", aluno.getId());
                startActivity(intent);
            });

            // ==================== CONFIGURAR BUSCA ====================
            if (etBusca != null) {
                etBusca.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        filtrarAlunos(s.toString());
                    }

                    @Override
                    public void afterTextChanged(Editable s) {}
                });
            }

            // ==================== CARREGAR ALUNOS ====================
            if (turmaId != -1) {
                carregarAlunos();
            } else {
                tvEmpty.setVisibility(View.VISIBLE);
                tvEmpty.setText("Turma não encontrada");
                rvAlunos.setVisibility(View.GONE);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao abrir lista de alunos: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    // ==================== CARREGAR ALUNOS ====================
    // ==================== CARREGAR ALUNOS ====================
    private void carregarAlunos() {
        repository.getAlunosMatriculadosNaTurma(turmaId, matriculas -> {
            runOnUiThread(() -> {
                try {
                    if (matriculas == null || matriculas.isEmpty()) {
                        rvAlunos.setVisibility(View.GONE);
                        tvEmpty.setVisibility(View.VISIBLE);
                        tvEmpty.setText("Nenhum aluno matriculado nesta turma.\nClique no + para adicionar");
                        adapter.setAlunos(new ArrayList<>());
                        alunos.clear();
                        return;
                    }

                    alunos.clear();

                    // Usar um contador para saber quando todos os alunos foram carregados
                    final int[] totalAlunos = {matriculas.size()};
                    final int[] carregados = {0};

                    for (Matricula m : matriculas) {
                        repository.getAlunoById(m.getAlunoId(), aluno -> {
                            runOnUiThread(() -> {
                                if (aluno != null && "ativo".equals(aluno.getStatus())) {
                                    alunos.add(aluno);
                                }
                                carregados[0]++;

                                // Só atualizar quando todos os alunos forem carregados
                                if (carregados[0] == totalAlunos[0]) {
                                    String textoBusca = etBusca != null ? etBusca.getText().toString() : "";
                                    filtrarAlunos(textoBusca);
                                }
                            });
                        });
                    }

                    // Se não houver alunos para carregar (fallback)
                    if (totalAlunos[0] == 0) {
                        adapter.setAlunos(new ArrayList<>());
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Erro ao carregar alunos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    // ==================== FILTRAR ALUNOS ====================
    private void filtrarAlunos(String texto) {
        alunosFiltrados.clear();

        if (texto.isEmpty()) {
            alunosFiltrados.addAll(alunos);
        } else {
            String busca = texto.toLowerCase().trim();
            for (Aluno aluno : alunos) {
                if (aluno != null) {
                    String nome = aluno.getNome() != null ? aluno.getNome().toLowerCase() : "";
                    String matricula = aluno.getMatricula() != null ? aluno.getMatricula().toLowerCase() : "";
                    if (nome.contains(busca) || matricula.contains(busca)) {
                        alunosFiltrados.add(aluno);
                    }
                }
            }
        }

        adapter.setAlunos(new ArrayList<>(alunosFiltrados));

        if (alunosFiltrados.isEmpty()) {
            rvAlunos.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
            String mensagem = texto.isEmpty() ? "Nenhum aluno matriculado nesta turma.\nClique no + para adicionar" : "Nenhum aluno encontrado para a busca";
            tvEmpty.setText(mensagem);
        } else {
            rvAlunos.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
        }
    }

    // ==================== MENU ====================
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
        } else if (itemId == R.id.action_graficos) {
            Intent intent = new Intent(this, GraficosFrequenciaActivity.class);
            intent.putExtra("turma_id", turmaId);
            intent.putExtra("aluno_id", -1L);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.action_importar) {
            Intent intent = new Intent(this, ImportarAlunosActivity.class);
            intent.putExtra("turma_id", turmaId);
            intent.putExtra("turma_nome", turmaNome);
            startActivity(intent);
            return true;
        } else if (itemId == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (turmaId != -1) {
            carregarAlunos();
        }
    }
}