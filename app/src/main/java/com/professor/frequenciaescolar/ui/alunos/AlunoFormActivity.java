package com.professor.frequenciaescolar.ui.alunos;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.professor.frequenciaescolar.R;
import com.professor.frequenciaescolar.data.entities.Aluno;
import com.professor.frequenciaescolar.data.entities.Matricula;
import com.professor.frequenciaescolar.data.entities.Turma;
import com.professor.frequenciaescolar.data.repository.FrequenciaRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AlunoFormActivity extends AppCompatActivity {

    private EditText etNome;
    private EditText etMatricula;
    private EditText etResponsavel;
    private EditText etTelefone;
    private Spinner spinnerTurma;
    private Button btnSalvar;

    private FrequenciaRepository repository;
    private Aluno alunoAtual;
    private long alunoId = -1;
    private List<Turma> turmas = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aluno_form);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        etNome = findViewById(R.id.etNome);
        etMatricula = findViewById(R.id.etMatricula);
        etResponsavel = findViewById(R.id.etResponsavel);
        etTelefone = findViewById(R.id.etTelefone);
        spinnerTurma = findViewById(R.id.spinnerTurma);
        btnSalvar = findViewById(R.id.btnSalvar);

        repository = FrequenciaRepository.getInstance(this);
        carregarTurmas();

        if (getIntent().hasExtra("aluno_id")) {
            alunoId = getIntent().getLongExtra("aluno_id", -1);
            carregarAluno();
        }

        btnSalvar.setOnClickListener(v -> salvarAluno());
    }

    private void carregarTurmas() {
        repository.getAllTurmasAtivas(turmas -> {
            runOnUiThread(() -> {
                this.turmas = turmas;
                List<String> nomesTurmas = new ArrayList<>();
                nomesTurmas.add("Selecione uma turma");
                for (Turma t : turmas) {
                    nomesTurmas.add(t.getNome() + " - " + t.getTurno());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item, nomesTurmas);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerTurma.setAdapter(adapter);
            });
        });
    }

    private void carregarAluno() {
        repository.getAlunoById(alunoId, aluno -> {
            runOnUiThread(() -> {
                if (aluno != null) {
                    alunoAtual = aluno;
                    etNome.setText(aluno.getNome());
                    etMatricula.setText(aluno.getMatricula());
                    etResponsavel.setText(aluno.getResponsavel());
                    etTelefone.setText(aluno.getTelefone());

                    // Carregar turma atual do aluno
                    repository.getMatriculaAtivaByAluno(alunoId, matricula -> {
                        if (matricula != null) {
                            for (int i = 0; i < turmas.size(); i++) {
                                if (turmas.get(i).getId() == matricula.getTurmaId()) {
                                    spinnerTurma.setSelection(i + 1);
                                    break;
                                }
                            }
                        }
                    });
                }
            });
        });
    }

    private void salvarAluno() {
        String nome = etNome.getText().toString().trim();
        String matricula = etMatricula.getText().toString().trim();
        String responsavel = etResponsavel.getText().toString().trim();
        String telefone = etTelefone.getText().toString().trim();
        int turmaPosition = spinnerTurma.getSelectedItemPosition();

        if (nome.isEmpty()) {
            etNome.setError("Nome é obrigatório");
            return;
        }

        if (matricula.isEmpty()) {
            etMatricula.setError("Matrícula é obrigatória");
            return;
        }

        if (responsavel.isEmpty()) {
            etResponsavel.setError("Responsável é obrigatório");
            return;
        }

        if (turmaPosition == 0) {
            Toast.makeText(this, "Selecione uma turma", Toast.LENGTH_SHORT).show();
            return;
        }

        long turmaId = turmas.get(turmaPosition - 1).getId();
        String dataAtual = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        if (alunoAtual == null) {
            // Criar novo aluno
            Aluno novoAluno = new Aluno(nome, matricula, responsavel, telefone, "ativo", true);
            repository.insertAluno(novoAluno, () -> {
                // Buscar o ID do aluno recém-criado
                repository.getAlunoByMatricula(matricula, aluno -> {
                    runOnUiThread(() -> {
                        if (aluno != null) {
                            Matricula matriculaObj = new Matricula(aluno.getId(), turmaId, dataAtual, "ativa");
                            repository.insertMatricula(matriculaObj, () -> {
                                runOnUiThread(() -> {
                                    Toast.makeText(this, "Aluno salvo com sucesso!", Toast.LENGTH_SHORT).show();
                                    finish();
                                });
                            });
                        }
                    });
                });
            });
        } else {
            // Atualizar aluno existente
            alunoAtual.setNome(nome);
            alunoAtual.setMatricula(matricula);
            alunoAtual.setResponsavel(responsavel);
            alunoAtual.setTelefone(telefone);
            repository.updateAluno(alunoAtual, () -> {
                // Atualizar matrícula se necessário
                repository.getMatriculaAtivaByAluno(alunoAtual.getId(), matriculaAtual -> {
                    if (matriculaAtual != null && matriculaAtual.getTurmaId() != turmaId) {
                        repository.desativarMatriculaAtiva(alunoAtual.getId(), "transferida", () -> {
                            Matricula novaMatricula = new Matricula(alunoAtual.getId(), turmaId, dataAtual, "ativa");
                            repository.insertMatricula(novaMatricula, () -> {
                                runOnUiThread(() -> {
                                    Toast.makeText(this, "Aluno atualizado com sucesso!", Toast.LENGTH_SHORT).show();
                                    finish();
                                });
                            });
                        });
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Aluno atualizado com sucesso!", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    }
                });
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}