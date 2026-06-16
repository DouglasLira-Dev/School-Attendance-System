package com.professor.frequenciaescolar.ui.alunos;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
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
    private long turmaIdRecebida = -1;
    private String turmaNomeRecebida;
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

        turmaIdRecebida = getIntent().getLongExtra("turma_id", -1);
        turmaNomeRecebida = getIntent().getStringExtra("turma_nome");

        repository = FrequenciaRepository.getInstance(this);
        carregarTurmas();

        if (getIntent().hasExtra("aluno_id")) {
            alunoId = getIntent().getLongExtra("aluno_id", -1);
            carregarAluno();
        }

        btnSalvar.setOnClickListener(v -> salvarAluno());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                confirmarSaida();
            }
        });
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

                if (turmaIdRecebida != -1) {
                    for (int i = 0; i < turmas.size(); i++) {
                        if (turmas.get(i).getId() == turmaIdRecebida) {
                            spinnerTurma.setSelection(i + 1);
                            break;
                        }
                    }
                }
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

        if (turmaPosition == 0) {
            Toast.makeText(this, "Selecione uma turma", Toast.LENGTH_SHORT).show();
            return;
        }

        long turmaId = turmas.get(turmaPosition - 1).getId();
        String dataAtual = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        if (alunoAtual == null) {
            // Verificar matrícula duplicada
            repository.getAlunoByMatricula(matricula, alunoExistente -> {
                runOnUiThread(() -> {
                    if (alunoExistente != null) {
                        etMatricula.setError("⚠️ Matrícula já cadastrada para outro aluno");
                        etMatricula.requestFocus();
                        return;
                    }

                    Aluno novoAluno = new Aluno(nome, matricula, responsavel, telefone, "ativo", true);
                    repository.insertAluno(novoAluno, () -> {
                        repository.getAlunoByMatricula(matricula, aluno -> {
                            runOnUiThread(() -> {
                                if (aluno != null) {
                                    Matricula matriculaObj = new Matricula(aluno.getId(), turmaId, dataAtual, "ativa");
                                    repository.insertMatricula(matriculaObj, () -> {
                                        runOnUiThread(() -> {
                                            Toast.makeText(this, "✅ Aluno salvo com sucesso!", Toast.LENGTH_SHORT).show();
                                            finish();
                                        });
                                    });
                                }
                            });
                        });
                    });
                });
            });
        } else {
            // Atualizar aluno existente
            if (!alunoAtual.getMatricula().equals(matricula)) {
                repository.getAlunoByMatricula(matricula, alunoExistente -> {
                    runOnUiThread(() -> {
                        if (alunoExistente != null && alunoExistente.getId() != alunoAtual.getId()) {
                            etMatricula.setError("⚠️ Matrícula já cadastrada para outro aluno");
                            etMatricula.requestFocus();
                            return;
                        }
                        atualizarAluno(nome, matricula, responsavel, telefone, turmaId, dataAtual);
                    });
                });
            } else {
                atualizarAluno(nome, matricula, responsavel, telefone, turmaId, dataAtual);
            }
        }
    }

    private void atualizarAluno(String nome, String matricula, String responsavel, String telefone, long turmaId, String dataAtual) {
        alunoAtual.setNome(nome);
        alunoAtual.setMatricula(matricula);
        alunoAtual.setResponsavel(responsavel);
        alunoAtual.setTelefone(telefone);

        repository.updateAluno(alunoAtual, () -> {
            repository.getMatriculaAtivaByAluno(alunoAtual.getId(), matriculaAtual -> {
                if (matriculaAtual != null && matriculaAtual.getTurmaId() != turmaId) {
                    repository.desativarMatriculaAtiva(alunoAtual.getId(), "transferida", () -> {
                        Matricula novaMatricula = new Matricula(alunoAtual.getId(), turmaId, dataAtual, "ativa");
                        repository.insertMatricula(novaMatricula, () -> {
                            runOnUiThread(() -> {
                                Toast.makeText(this, "✅ Aluno atualizado com sucesso!", Toast.LENGTH_SHORT).show();
                                finish();
                            });
                        });
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "✅ Aluno atualizado com sucesso!", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }
            });
        });
    }

    // ==================== MÉTODO PARA VERIFICAR SE HÁ DADOS PREENCHIDOS ====================
    private boolean temDadosPreenchidos() {
        return !etNome.getText().toString().trim().isEmpty()
                || !etMatricula.getText().toString().trim().isEmpty()
                || !etResponsavel.getText().toString().trim().isEmpty()
                || !etTelefone.getText().toString().trim().isEmpty();
    }

    // ==================== CONFIRMAR SAÍDA ====================
    private void confirmarSaida() {
        if (temDadosPreenchidos()) {
            new AlertDialog.Builder(this)
                    .setTitle("Descartar alterações?")
                    .setMessage("Os dados preenchidos serão perdidos.")
                    .setPositiveButton("Descartar", (d, w) -> finish())
                    .setNegativeButton("Continuar editando", null)
                    .show();
        } else {
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            confirmarSaida();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}