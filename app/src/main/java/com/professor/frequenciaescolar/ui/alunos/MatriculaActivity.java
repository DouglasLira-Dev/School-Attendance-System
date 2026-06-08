package com.professor.frequenciaescolar.ui.alunos;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.professor.frequenciaescolar.R;
import com.professor.frequenciaescolar.data.entities.Aluno;
import com.professor.frequenciaescolar.data.entities.Matricula;
import com.professor.frequenciaescolar.data.entities.MovimentacaoAluno;
import com.professor.frequenciaescolar.data.entities.Turma;
import com.professor.frequenciaescolar.data.repository.FrequenciaRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MatriculaActivity extends AppCompatActivity {

    private TextView tvAlunoNome;
    private Spinner spinnerTurma;
    private Button btnTransferirTurma;
    private Button btnTransferirEscola;
    private Button btnExpulsar;
    private Button btnDesistente;

    private FrequenciaRepository repository;
    private Aluno aluno;
    private List<Turma> turmas = new ArrayList<>();
    private long turmaAtualId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matricula_form);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        tvAlunoNome = findViewById(R.id.tvAlunoNome);
        spinnerTurma = findViewById(R.id.spinnerTurma);
        btnTransferirTurma = findViewById(R.id.btnMatricular);
        btnTransferirEscola = findViewById(R.id.btnTransferirEscola);
        btnExpulsar = findViewById(R.id.btnExpulsar);
        btnDesistente = findViewById(R.id.btnDesistente);

        repository = FrequenciaRepository.getInstance(this);

        long alunoId = getIntent().getLongExtra("aluno_id", -1);
        if (alunoId != -1) {
            carregarAluno(alunoId);
        }

        btnTransferirTurma.setOnClickListener(v -> transferirTurma());
        btnTransferirEscola.setOnClickListener(v -> transferirEscola());
        btnExpulsar.setOnClickListener(v -> expulsarAluno());
        btnDesistente.setOnClickListener(v -> desistenteAluno());
    }

    private void carregarAluno(long alunoId) {
        repository.getAlunoById(alunoId, aluno -> {
            runOnUiThread(() -> {
                if (aluno != null) {
                    this.aluno = aluno;
                    tvAlunoNome.setText(aluno.getNome());

                    repository.getMatriculaAtivaByAluno(alunoId, matricula -> {
                        if (matricula != null) {
                            turmaAtualId = matricula.getTurmaId();
                            carregarTurmas();
                        }
                    });
                }
            });
        });
    }

    private void carregarTurmas() {
        repository.getAllTurmasAtivas(turmas -> {
            runOnUiThread(() -> {
                this.turmas = turmas;
                List<String> nomesTurmas = new ArrayList<>();
                for (Turma t : turmas) {
                    if (t.getId() != turmaAtualId) {
                        nomesTurmas.add(t.getNome() + " - " + t.getTurno());
                    }
                }

                if (nomesTurmas.isEmpty()) {
                    nomesTurmas.add("Nenhuma turma disponível");
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item, nomesTurmas);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerTurma.setAdapter(adapter);
            });
        });
    }

    private void transferirTurma() {
        int position = spinnerTurma.getSelectedItemPosition();
        if (position < 0 || spinnerTurma.getAdapter().getCount() == 0) {
            Toast.makeText(this, "Nenhuma turma disponível para transferência", Toast.LENGTH_SHORT).show();
            return;
        }

        // Filtrar a turma selecionada (ignorando o primeiro item se for "nenhuma turma")
        List<Turma> turmasDisponiveis = new ArrayList<>();
        for (Turma t : turmas) {
            if (t.getId() != turmaAtualId) {
                turmasDisponiveis.add(t);
            }
        }

        if (position >= turmasDisponiveis.size()) {
            return;
        }

        long novaTurmaId = turmasDisponiveis.get(position).getId();
        String dataAtual = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        repository.desativarMatriculaAtiva(aluno.getId(), "transferida", () -> {
            Matricula novaMatricula = new Matricula(aluno.getId(), novaTurmaId, dataAtual, "ativa");
            repository.insertMatricula(novaMatricula, () -> {
                MovimentacaoAluno mov = new MovimentacaoAluno(
                        aluno.getId(), turmaAtualId, novaTurmaId, dataAtual,
                        "transferencia_turma", null, "Transferido de turma"
                );
                repository.insertMovimentacao(mov, () -> {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Aluno transferido de turma com sucesso!", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                });
            });
        });
    }

    private void transferirEscola() {
        String dataAtual = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        new AlertDialog.Builder(this)
                .setTitle("Transferência de Escola")
                .setMessage("Confirmar transferência deste aluno para outra escola?")
                .setPositiveButton("Confirmar", (dialog, which) -> {
                    repository.desativarMatriculaAtiva(aluno.getId(), "transferida", () -> {
                        repository.desativarAluno(aluno.getId(), "transferido", () -> {
                            MovimentacaoAluno mov = new MovimentacaoAluno(
                                    aluno.getId(), turmaAtualId, -1, dataAtual,
                                    "transferencia_escola", "Outra Escola", "Transferido para outra escola"
                            );
                            repository.insertMovimentacao(mov, () -> {
                                runOnUiThread(() -> {
                                    Toast.makeText(this, "Aluno transferido com sucesso!", Toast.LENGTH_SHORT).show();
                                    finish();
                                });
                            });
                        });
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void expulsarAluno() {
        String dataAtual = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        new AlertDialog.Builder(this)
                .setTitle("Expulsão")
                .setMessage("Confirmar expulsão deste aluno?")
                .setPositiveButton("Confirmar", (dialog, which) -> {
                    repository.desativarMatriculaAtiva(aluno.getId(), "expulsa", () -> {
                        repository.desativarAluno(aluno.getId(), "expulso", () -> {
                            MovimentacaoAluno mov = new MovimentacaoAluno(
                                    aluno.getId(), turmaAtualId, -1, dataAtual,
                                    "expulsao", null, "Expulso por motivo disciplinar"
                            );
                            repository.insertMovimentacao(mov, () -> {
                                runOnUiThread(() -> {
                                    Toast.makeText(this, "Aluno expulso com sucesso!", Toast.LENGTH_SHORT).show();
                                    finish();
                                });
                            });
                        });
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void desistenteAluno() {
        String dataAtual = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        new AlertDialog.Builder(this)
                .setTitle("Desistência")
                .setMessage("Confirmar desistência deste aluno?")
                .setPositiveButton("Confirmar", (dialog, which) -> {
                    repository.desativarMatriculaAtiva(aluno.getId(), "desistente", () -> {
                        repository.desativarAluno(aluno.getId(), "desistente", () -> {
                            MovimentacaoAluno mov = new MovimentacaoAluno(
                                    aluno.getId(), turmaAtualId, -1, dataAtual,
                                    "desistencia", null, "Aluno desistiu do curso"
                            );
                            repository.insertMovimentacao(mov, () -> {
                                runOnUiThread(() -> {
                                    Toast.makeText(this, "Desistência registrada com sucesso!", Toast.LENGTH_SHORT).show();
                                    finish();
                                });
                            });
                        });
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
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