package com.professor.frequenciaescolar.ui.alunos;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.professor.frequenciaescolar.R;
import com.professor.frequenciaescolar.data.entities.Aluno;
import com.professor.frequenciaescolar.data.entities.Matricula;
import com.professor.frequenciaescolar.data.entities.Turma;
import com.professor.frequenciaescolar.data.repository.FrequenciaRepository;

import java.util.ArrayList;
import java.util.List;

public class AlunoListActivity extends AppCompatActivity {

    private RecyclerView rvAlunos;
    private TextView tvEmpty;
    private Spinner spinnerTurma;
    private AlunoAdapter adapter;
    private FrequenciaRepository repository;
    private List<Turma> turmas = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aluno_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        rvAlunos = findViewById(R.id.rvAlunos);
        tvEmpty = findViewById(R.id.tvEmpty);
        spinnerTurma = findViewById(R.id.spinnerTurma);

        adapter = new AlunoAdapter();
        rvAlunos.setLayoutManager(new LinearLayoutManager(this));
        rvAlunos.setAdapter(adapter);

        adapter.setOnItemClickListener(aluno -> {
            Intent intent = new Intent(AlunoListActivity.this, MatriculaActivity.class);
            intent.putExtra("aluno_id", aluno.getId());
            startActivity(intent);
        });

        repository = FrequenciaRepository.getInstance(this);
        carregarTurmas();
    }

    private void carregarTurmas() {
        repository.getAllTurmasAtivas(turmasList -> {
            runOnUiThread(() -> {
                turmas.clear();
                turmas.addAll(turmasList);

                List<String> nomesTurmas = new ArrayList<>();
                nomesTurmas.add("Selecione uma turma");
                for (Turma t : turmas) {
                    nomesTurmas.add(t.getNome() + " - " + t.getTurno());
                }

                ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item, nomesTurmas);
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerTurma.setAdapter(spinnerAdapter);

                spinnerTurma.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (position > 0) {
                            carregarAlunosPorTurma(turmas.get(position - 1).getId());
                        } else {
                            rvAlunos.setVisibility(View.GONE);
                            tvEmpty.setVisibility(View.VISIBLE);
                            tvEmpty.setText("Selecione uma turma");
                            adapter.setAlunos(new ArrayList<>());
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // Não faz nada
                    }
                });
            });
        });
    }

    private void carregarAlunosPorTurma(long turmaId) {
        repository.getAlunosMatriculadosNaTurma(turmaId, matriculas -> {
            runOnUiThread(() -> {
                if (matriculas == null || matriculas.isEmpty()) {
                    rvAlunos.setVisibility(View.GONE);
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText("Nenhum aluno matriculado nesta turma.");
                    adapter.setAlunos(new ArrayList<>());
                } else {
                    rvAlunos.setVisibility(View.VISIBLE);
                    tvEmpty.setVisibility(View.GONE);
                    carregarDetalhesAlunos(matriculas);
                }
            });
        });
    }

    private void carregarDetalhesAlunos(List<Matricula> matriculas) {
        List<Aluno> alunosTemp = new ArrayList<>();

        for (Matricula m : matriculas) {
            repository.getAlunoById(m.getAlunoId(), aluno -> {
                runOnUiThread(() -> {
                    if (aluno != null && "ativo".equals(aluno.getStatus())) {
                        alunosTemp.add(aluno);
                        adapter.setAlunos(new ArrayList<>(alunosTemp));
                    }
                });
            });
        }
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
        if (spinnerTurma.getAdapter() != null && spinnerTurma.getSelectedItemPosition() > 0) {
            int position = spinnerTurma.getSelectedItemPosition();
            if (position > 0 && position - 1 < turmas.size()) {
                carregarAlunosPorTurma(turmas.get(position - 1).getId());
            }
        }
    }
}