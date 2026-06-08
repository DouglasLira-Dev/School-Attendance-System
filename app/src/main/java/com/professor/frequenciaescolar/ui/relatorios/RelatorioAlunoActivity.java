package com.professor.frequenciaescolar.ui.relatorios;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.professor.frequenciaescolar.R;
import com.professor.frequenciaescolar.data.entities.Aluno;
import com.professor.frequenciaescolar.data.entities.Chamada;
import com.professor.frequenciaescolar.data.entities.Matricula;
import com.professor.frequenciaescolar.data.entities.Presenca;
import com.professor.frequenciaescolar.data.entities.Turma;
import com.professor.frequenciaescolar.data.repository.FrequenciaRepository;
import com.professor.frequenciaescolar.utils.ConfiguracoesManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RelatorioAlunoActivity extends AppCompatActivity {

    private TextView tvAlunoNome, tvAlunoMatricula, tvAlunoTurma;
    private TextView tvTotalDias, tvPresencas, tvFaltasJustificadas, tvFaltasNaoJustificadas, tvPercentual;
    private TextInputEditText etDataInicio, etDataFim;
    private Button btnFiltrar, btnExportar;
    private RecyclerView rvHistorico;
    private TextView tvEmpty;

    private FrequenciaRepository repository;
    private ConfiguracoesManager configManager;
    private HistoricoAdapter adapter;

    private long alunoId;
    private Aluno aluno;
    private Turma turmaAtual;
    private List<HistoricoAdapter.ItemHistorico> historico = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relatorio_aluno);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Inicializar views
        tvAlunoNome = findViewById(R.id.tvAlunoNome);
        tvAlunoMatricula = findViewById(R.id.tvAlunoMatricula);
        tvAlunoTurma = findViewById(R.id.tvAlunoTurma);
        tvTotalDias = findViewById(R.id.tvTotalDias);
        tvPresencas = findViewById(R.id.tvPresencas);
        tvFaltasJustificadas = findViewById(R.id.tvFaltasJustificadas);
        tvFaltasNaoJustificadas = findViewById(R.id.tvFaltasNaoJustificadas);
        tvPercentual = findViewById(R.id.tvPercentual);
        etDataInicio = findViewById(R.id.etDataInicio);
        etDataFim = findViewById(R.id.etDataFim);
        btnFiltrar = findViewById(R.id.btnFiltrar);
        btnExportar = findViewById(R.id.btnExportar);
        rvHistorico = findViewById(R.id.rvHistorico);
        tvEmpty = findViewById(R.id.tvEmpty);

        // Configurar RecyclerView
        adapter = new HistoricoAdapter();
        rvHistorico.setLayoutManager(new LinearLayoutManager(this));
        rvHistorico.setAdapter(adapter);

        repository = FrequenciaRepository.getInstance(this);
        configManager = new ConfiguracoesManager(this);

        // Receber ID do aluno
        alunoId = getIntent().getLongExtra("aluno_id", -1);

        if (alunoId == -1) {
            Toast.makeText(this, "Erro: Aluno não encontrado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Configurar data atual (últimos 30 dias)
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        etDataFim.setText(sdf.format(calendar.getTime()));
        calendar.add(Calendar.DAY_OF_MONTH, -30);
        etDataInicio.setText(sdf.format(calendar.getTime()));

        // Configurar DatePickers
        etDataInicio.setOnClickListener(v -> mostrarDatePicker(etDataInicio));
        etDataFim.setOnClickListener(v -> mostrarDatePicker(etDataFim));

        // Carregar dados do aluno
        carregarDadosAluno();

        btnFiltrar.setOnClickListener(v -> carregarHistorico());
        btnExportar.setOnClickListener(v -> exportarRelatorio());
    }

    private void carregarDadosAluno() {
        repository.getAlunoById(alunoId, aluno -> {
            runOnUiThread(() -> {
                if (aluno != null) {
                    this.aluno = aluno;
                    tvAlunoNome.setText(aluno.getNome());
                    tvAlunoMatricula.setText("Matrícula: " + aluno.getMatricula());

                    carregarTurmaAtual();
                    carregarHistorico();
                } else {
                    Toast.makeText(this, "Aluno não encontrado", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        });
    }

    private void carregarTurmaAtual() {
        repository.getMatriculaAtivaByAluno(alunoId, matricula -> {
            if (matricula != null) {
                repository.getTurmaById(matricula.getTurmaId(), turma -> {
                    runOnUiThread(() -> {
                        if (turma != null) {
                            turmaAtual = turma;
                            tvAlunoTurma.setText("Turma: " + turma.getNome() + " - " + turma.getTurno());
                        }
                    });
                });
            } else {
                tvAlunoTurma.setText("Turma: Nenhuma turma ativa");
            }
        });
    }

    private void carregarHistorico() {
        String dataInicioStr = etDataInicio.getText().toString();
        String dataFimStr = etDataFim.getText().toString();

        // Converter para formato yyyy-MM-dd
        String dataInicio = converterData(dataInicioStr);
        String dataFim = converterData(dataFimStr);

        repository.getChamadasPorPeriodo(dataInicio, dataFim, chamadas -> {
            runOnUiThread(() -> {
                if (chamadas == null || chamadas.isEmpty()) {
                    rvHistorico.setVisibility(android.view.View.GONE);
                    tvEmpty.setVisibility(android.view.View.VISIBLE);
                    calcularResumo(new ArrayList<>(), new ArrayList<>());
                    return;
                }

                carregarPresencas(chamadas);
            });
        });
    }

    private void carregarPresencas(List<Chamada> chamadas) {
        historico.clear();
        final int[] presencas = {0};
        final int[] faltasJustificadas = {0};
        final int[] faltasNaoJustificadas = {0};
        final int[] processadas = {0};
        final int total = chamadas.size();

        for (Chamada c : chamadas) {
            repository.getPresencasByChamada(c.getId(), presencasList -> {
                for (Presenca p : presencasList) {
                    if (p.getAlunoId() == alunoId) {
                        boolean isJustificada = p.getJustificativa() != null && !p.getJustificativa().isEmpty();

                        if (p.isPresente()) {
                            presencas[0]++;
                            historico.add(new HistoricoAdapter.ItemHistorico(c.getData(), c.getHorario(), true, false, ""));
                        } else if (isJustificada) {
                            faltasJustificadas[0]++;
                            historico.add(new HistoricoAdapter.ItemHistorico(c.getData(), c.getHorario(), false, true, p.getJustificativa()));
                        } else {
                            faltasNaoJustificadas[0]++;
                            historico.add(new HistoricoAdapter.ItemHistorico(c.getData(), c.getHorario(), false, false, ""));
                        }
                        break;
                    }
                }
                processadas[0]++;

                if (processadas[0] == total) {
                    runOnUiThread(() -> {
                        historico.sort((a, b) -> b.getData().compareTo(a.getData()));
                        adapter.setItens(historico);

                        if (historico.isEmpty()) {
                            rvHistorico.setVisibility(android.view.View.GONE);
                            tvEmpty.setVisibility(android.view.View.VISIBLE);
                        } else {
                            rvHistorico.setVisibility(android.view.View.VISIBLE);
                            tvEmpty.setVisibility(android.view.View.GONE);
                        }

                        calcularResumo(chamadas, presencas[0], faltasJustificadas[0], faltasNaoJustificadas[0]);
                    });
                }
            });
        }
    }

    private void calcularResumo(List<Chamada> chamadas, int presencas, int faltasJustificadas, int faltasNaoJustificadas) {
        int totalDias = chamadas.size();
        boolean desconsiderarJustificadas = configManager.isDesconsiderarJustificadas();

        int diasConsiderados = totalDias;
        if (desconsiderarJustificadas) {
            diasConsiderados = totalDias - faltasJustificadas;
        }

        double percentual = diasConsiderados > 0 ? (presencas * 100.0 / diasConsiderados) : 100;

        runOnUiThread(() -> {
            tvTotalDias.setText(String.valueOf(totalDias));
            tvPresencas.setText(String.valueOf(presencas));
            tvFaltasJustificadas.setText(String.valueOf(faltasJustificadas));
            tvFaltasNaoJustificadas.setText(String.valueOf(faltasNaoJustificadas));
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

    private void calcularResumo(List<Chamada> chamadas, List<Presenca> presencasList) {
        // Método sobrecarregado para compatibilidade
        calcularResumo(chamadas, 0, 0, 0);
    }

    private void mostrarDatePicker(TextInputEditText campo) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    String data = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year);
                    campo.setText(data);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private String converterData(String data) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date date = sdf.parse(data);
            SimpleDateFormat sdfOut = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            return sdfOut.format(date);
        } catch (Exception e) {
            return "";
        }
    }

    private void exportarRelatorio() {
        Toast.makeText(this, "Exportando relatório...", Toast.LENGTH_SHORT).show();
        // TODO: Implementar exportação PDF
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