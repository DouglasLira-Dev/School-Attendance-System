package com.professor.frequenciaescolar.ui.relatorios;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.professor.frequenciaescolar.R;
import com.professor.frequenciaescolar.data.entities.Aluno;
import com.professor.frequenciaescolar.data.entities.Matricula;
import com.professor.frequenciaescolar.data.entities.Presenca;
import com.professor.frequenciaescolar.data.entities.Turma;
import com.professor.frequenciaescolar.data.repository.FrequenciaRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class RelatorioDashboardActivity extends AppCompatActivity {

    private Spinner spinnerTurma;
    private TextInputEditText etDataInicio;
    private TextInputEditText etDataFim;
    private Button btnGerarRelatorio;
    private Button btnExportarPDF;
    private Button btnExportarXLSX;
    private RecyclerView rvAlunosBaixaFrequencia;
    private TextView tvEmptyBaixaFrequencia;
    private TextView tvMediaTurma;
    private TextView tvTotalAulas;

    private FrequenciaRepository repository;
    private List<Turma> turmas = new ArrayList<>();
    private long turmaSelecionadaId = -1;
    private RelatorioAdapter adapter;
    private List<RelatorioAdapter.AlunoFrequencia> alunosBaixaFrequencia = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relatorio_dashboard);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Inicializar views
        spinnerTurma = findViewById(R.id.spinnerTurma);
        etDataInicio = findViewById(R.id.etDataInicio);
        etDataFim = findViewById(R.id.etDataFim);
        btnGerarRelatorio = findViewById(R.id.btnGerarRelatorio);
        btnExportarPDF = findViewById(R.id.btnExportarPDF);
        btnExportarXLSX = findViewById(R.id.btnExportarXLSX);
        rvAlunosBaixaFrequencia = findViewById(R.id.rvAlunosBaixaFrequencia);
        tvEmptyBaixaFrequencia = findViewById(R.id.tvEmptyBaixaFrequencia);
        tvMediaTurma = findViewById(R.id.tvMediaTurma);
        tvTotalAulas = findViewById(R.id.tvTotalAulas);

        // Configurar datas padrão (últimos 30 dias)
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        etDataFim.setText(sdf.format(calendar.getTime()));
        calendar.add(Calendar.DAY_OF_MONTH, -30);
        etDataInicio.setText(sdf.format(calendar.getTime()));

        // Configurar DatePicker
        etDataInicio.setOnClickListener(v -> mostrarDatePicker(etDataInicio));
        etDataFim.setOnClickListener(v -> mostrarDatePicker(etDataFim));

        // Configurar RecyclerView
        adapter = new RelatorioAdapter();
        rvAlunosBaixaFrequencia.setLayoutManager(new LinearLayoutManager(this));
        rvAlunosBaixaFrequencia.setAdapter(adapter);

        // Inicializar repository
        repository = FrequenciaRepository.getInstance(this);

        // Carregar turmas
        carregarTurmas();

        // Configurar botões
        btnGerarRelatorio.setOnClickListener(v -> gerarRelatorio());
        btnExportarPDF.setOnClickListener(v -> exportarPDF());
        btnExportarXLSX.setOnClickListener(v -> exportarXLSX());
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
                            turmaSelecionadaId = turmas.get(position - 1).getId();
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });
            });
        });
    }

    private void mostrarDatePicker(TextInputEditText campo) {
        android.app.DatePickerDialog datePickerDialog = new android.app.DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    String data = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    campo.setText(data);
                },
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void gerarRelatorio() {
        if (turmaSelecionadaId == -1) {
            Toast.makeText(this, "Selecione uma turma", Toast.LENGTH_SHORT).show();
            return;
        }

        String dataInicio = etDataInicio.getText().toString();
        String dataFim = etDataFim.getText().toString();

        if (dataInicio.isEmpty() || dataFim.isEmpty()) {
            Toast.makeText(this, "Selecione o período", Toast.LENGTH_SHORT).show();
            return;
        }

        // Buscar alunos da turma
        repository.getAlunosMatriculadosNaTurma(turmaSelecionadaId, matriculas -> {
            if (matriculas == null || matriculas.isEmpty()) {
                runOnUiThread(() -> {
                    tvEmptyBaixaFrequencia.setVisibility(View.VISIBLE);
                    tvEmptyBaixaFrequencia.setText("Nenhum aluno matriculado");
                    adapter.setAlunos(new ArrayList<>());
                    tvTotalAulas.setText("0");
                    tvMediaTurma.setText("0%");
                });
                return;
            }

            List<Long> alunosIds = new ArrayList<>();
            Map<Long, Aluno> alunosMap = new HashMap<>();

            for (Matricula m : matriculas) {
                alunosIds.add(m.getAlunoId());
            }

            // Buscar chamadas no período
            repository.getChamadasPorPeriodo(dataInicio, dataFim, chamadas -> {
                int totalChamadas = chamadas != null ? chamadas.size() : 0;

                runOnUiThread(() -> tvTotalAulas.setText(String.valueOf(totalChamadas)));

                Map<Long, Integer> faltasPorAluno = new HashMap<>();
                for (Long alunoId : alunosIds) {
                    faltasPorAluno.put(alunoId, 0);
                }

                if (chamadas != null && !chamadas.isEmpty()) {
                    // Para cada chamada, buscar as presenças
                    for (com.professor.frequenciaescolar.data.entities.Chamada c : chamadas) {
                        repository.getPresencasByChamada(c.getId(), presencas -> {
                            if (presencas != null) {
                                for (Presenca p : presencas) {
                                    int faltasAtuais = faltasPorAluno.getOrDefault(p.getAlunoId(), 0);
                                    if (!p.isPresente()) {
                                        faltasPorAluno.put(p.getAlunoId(), faltasAtuais + 1);
                                    }
                                }
                            }

                            // Processar resultados após cada chamada (simplificado)
                            processarResultados(alunosIds, faltasPorAluno, totalChamadas);
                        });
                    }
                } else {
                    processarResultados(alunosIds, faltasPorAluno, totalChamadas);
                }
            });
        });
    }

    private void processarResultados(List<Long> alunosIds, Map<Long, Integer> faltasPorAluno, int totalChamadas) {
        final double[] somaPercentuais = {0};
        final int[] count = {0};
        final List<RelatorioAdapter.AlunoFrequencia> baixaFrequenciaTemp = new ArrayList<>();
        final int[] processados = {0};

        for (Long alunoId : alunosIds) {
            repository.getAlunoById(alunoId, aluno -> {
                runOnUiThread(() -> {
                    if (aluno != null && "ativo".equals(aluno.getStatus())) {
                        int faltas = faltasPorAluno.getOrDefault(alunoId, 0);
                        RelatorioAdapter.AlunoFrequencia af = new RelatorioAdapter.AlunoFrequencia(
                                alunoId, aluno.getNome(), aluno.getMatricula(), faltas, totalChamadas
                        );

                        somaPercentuais[0] += af.getPercentual();
                        count[0]++;

                        if (af.getPercentual() < 80) {
                            baixaFrequenciaTemp.add(af);
                        }
                    }

                    processados[0]++;

                    if (processados[0] == alunosIds.size()) {
                        // Todos os alunos foram processados
                        double media = count[0] > 0 ? somaPercentuais[0] / count[0] : 0;
                        tvMediaTurma.setText(String.format("%.1f%%", media));

                        adapter.setAlunos(baixaFrequenciaTemp);
                        if (baixaFrequenciaTemp.isEmpty()) {
                            tvEmptyBaixaFrequencia.setVisibility(View.VISIBLE);
                        } else {
                            tvEmptyBaixaFrequencia.setVisibility(View.GONE);
                        }
                    }
                });
            });
        }
    }

    private void exportarPDF() {
        Toast.makeText(this, "Funcionalidade em desenvolvimento", Toast.LENGTH_SHORT).show();
    }

    private void exportarXLSX() {
        Toast.makeText(this, "Funcionalidade em desenvolvimento", Toast.LENGTH_SHORT).show();
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