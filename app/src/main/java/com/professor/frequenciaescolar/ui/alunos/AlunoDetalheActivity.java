package com.professor.frequenciaescolar.ui.alunos;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.professor.frequenciaescolar.R;
import com.professor.frequenciaescolar.data.entities.Aluno;
import com.professor.frequenciaescolar.data.entities.Chamada;
import com.professor.frequenciaescolar.data.entities.Matricula;
import com.professor.frequenciaescolar.data.entities.Presenca;
import com.professor.frequenciaescolar.data.entities.Turma;
import com.professor.frequenciaescolar.data.repository.FrequenciaRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AlunoDetalheActivity extends AppCompatActivity {

    private TextView tvNome, tvMatricula, tvResponsavel, tvTelefone, tvStatus;
    private TextView tvTurma;
    private TextView tvTotalAulas, tvPresencas, tvFaltas, tvPercentual;
    private Button btnEditar, btnExcluir;

    private FrequenciaRepository repository;
    private long alunoId;
    private Aluno aluno;
    private Turma turmaAtual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aluno_detalhe);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Inicializar views
        tvNome = findViewById(R.id.tvNome);
        tvMatricula = findViewById(R.id.tvMatricula);
        tvResponsavel = findViewById(R.id.tvResponsavel);
        tvTelefone = findViewById(R.id.tvTelefone);
        tvStatus = findViewById(R.id.tvStatus);
        tvTurma = findViewById(R.id.tvTurma);
        tvTotalAulas = findViewById(R.id.tvTotalAulas);
        tvPresencas = findViewById(R.id.tvPresencas);
        tvFaltas = findViewById(R.id.tvFaltas);
        tvPercentual = findViewById(R.id.tvPercentual);
        btnEditar = findViewById(R.id.btnEditar);
        btnExcluir = findViewById(R.id.btnExcluir);

        repository = FrequenciaRepository.getInstance(this);

        alunoId = getIntent().getLongExtra("aluno_id", -1);

        if (alunoId != -1) {
            carregarDadosAluno();
        } else {
            Toast.makeText(this, "Erro: Aluno não encontrado", Toast.LENGTH_SHORT).show();
            finish();
        }

        btnEditar.setOnClickListener(v -> editarAluno());
        btnExcluir.setOnClickListener(v -> confirmarExclusao());
    }

    private void carregarDadosAluno() {
        repository.getAlunoById(alunoId, aluno -> {
            runOnUiThread(() -> {
                if (aluno != null) {
                    this.aluno = aluno;

                    tvNome.setText(aluno.getNome());
                    tvMatricula.setText(aluno.getMatricula());
                    tvResponsavel.setText(aluno.getResponsavel());
                    tvTelefone.setText(aluno.getTelefone());

                    String status = aluno.getStatus();
                    tvStatus.setText(status);
                    if ("ativo".equals(status)) {
                        tvStatus.setTextColor(getColor(android.R.color.holo_green_dark));
                    } else {
                        tvStatus.setTextColor(getColor(android.R.color.holo_red_dark));
                    }

                    carregarTurmaAtual();
                    carregarFrequencia();
                } else {
                    Toast.makeText(AlunoDetalheActivity.this, "Aluno não encontrado", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        });
    }

    private void carregarTurmaAtual() {
        repository.getMatriculaAtivaByAluno(alunoId, matricula -> {
            runOnUiThread(() -> {
                if (matricula != null) {
                    repository.getTurmaById(matricula.getTurmaId(), turma -> {
                        runOnUiThread(() -> {
                            if (turma != null) {
                                turmaAtual = turma;
                                tvTurma.setText(turma.getNome() + " - " + turma.getTurno());
                            }
                        });
                    });
                } else {
                    tvTurma.setText("Nenhuma turma ativa");
                }
            });
        });
    }

    private void carregarFrequencia() {
        // Buscar todas as chamadas para calcular frequência
        // Por simplicidade, vamos buscar chamadas dos últimos 30 dias
        String dataFim = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new java.util.Date());

        repository.getChamadasPorPeriodo("2000-01-01", dataFim, chamadas -> {
            runOnUiThread(() -> {
                if (chamadas == null || chamadas.isEmpty()) {
                    tvTotalAulas.setText("0");
                    tvPresencas.setText("0");
                    tvFaltas.setText("0");
                    tvPercentual.setText("100%");
                    return;
                }

                final int[] totalChamadas = {chamadas.size()};
                final int[] presencasCount = {0};
                final int[] processadas = {0};

                for (Chamada c : chamadas) {
                    repository.getPresencasByChamada(c.getId(), presencas -> {
                        for (Presenca p : presencas) {
                            if (p.getAlunoId() == alunoId) {
                                if (p.isPresente()) {
                                    presencasCount[0]++;
                                }
                                break;
                            }
                        }
                        processadas[0]++;

                        if (processadas[0] == totalChamadas[0]) {
                            runOnUiThread(() -> {
                                int faltas = totalChamadas[0] - presencasCount[0];
                                double percentual = totalChamadas[0] > 0 ? (presencasCount[0] * 100.0 / totalChamadas[0]) : 100;

                                tvTotalAulas.setText(String.valueOf(totalChamadas[0]));
                                tvPresencas.setText(String.valueOf(presencasCount[0]));
                                tvFaltas.setText(String.valueOf(faltas));
                                tvPercentual.setText(String.format("%.1f%%", percentual));

                                if (percentual >= 80) {
                                    tvPercentual.setTextColor(getColor(android.R.color.holo_green_dark));
                                } else if (percentual >= 60) {
                                    tvPercentual.setTextColor(getColor(android.R.color.holo_orange_dark));
                                } else {
                                    tvPercentual.setTextColor(getColor(android.R.color.holo_red_dark));
                                }
                            });
                        }
                    });
                }
            });
        });
    }

    private void editarAluno() {
        Intent intent = new Intent(this, AlunoFormActivity.class);
        intent.putExtra("aluno_id", alunoId);
        startActivity(intent);
    }

    private void confirmarExclusao() {
        new AlertDialog.Builder(this)
                .setTitle("Excluir Aluno")
                .setMessage("Tem certeza que deseja excluir o aluno " + aluno.getNome() + "?\n\n" +
                        "Esta ação irá:\n" +
                        "• Remover o aluno do sistema\n" +
                        "• Manter o histórico de chamadas\n" +
                        "• Desfazer a matrícula atual")
                .setPositiveButton("Excluir", (dialog, which) -> excluirAluno())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void excluirAluno() {
        // Desativar aluno (soft delete)
        repository.desativarAluno(alunoId, "excluido", () -> {
            runOnUiThread(() -> {
                Toast.makeText(this, "Aluno excluído com sucesso!", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (alunoId != -1) {
            carregarDadosAluno();
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