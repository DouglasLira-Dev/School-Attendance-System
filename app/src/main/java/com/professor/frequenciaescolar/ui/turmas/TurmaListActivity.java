package com.professor.frequenciaescolar.ui.turmas;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.os.Environment;
import android.net.Uri;
import androidx.core.content.FileProvider;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.professor.frequenciaescolar.R;
import com.professor.frequenciaescolar.data.entities.Aluno;
import com.professor.frequenciaescolar.data.entities.Chamada;
import com.professor.frequenciaescolar.data.entities.Feriado;
import com.professor.frequenciaescolar.data.entities.Matricula;
import com.professor.frequenciaescolar.data.entities.Presenca;
import com.professor.frequenciaescolar.data.entities.Turma;
import com.professor.frequenciaescolar.data.repository.FrequenciaRepository;
import com.professor.frequenciaescolar.ui.alunos.AlunoAdapter;
import com.professor.frequenciaescolar.ui.alunos.AlunoListActivity;
import com.professor.frequenciaescolar.ui.backup.BackupRestoreActivity;
import com.professor.frequenciaescolar.ui.chamada.ChamadaActivity;
import com.professor.frequenciaescolar.ui.configuracoes.ConfiguracoesActivity;
import com.professor.frequenciaescolar.ui.graficos.GraficosFrequenciaActivity;
import com.professor.frequenciaescolar.ui.importar.ImportarAlunosActivity;
import com.professor.frequenciaescolar.ui.relatorios.RelatorioDashboardActivity;
import com.professor.frequenciaescolar.ui.risco.AlunosRiscoActivity;
import com.professor.frequenciaescolar.utils.ConfiguracoesManager;
import com.professor.frequenciaescolar.utils.NotificationHelper;
import com.professor.frequenciaescolar.utils.NotificationScheduler;
import com.professor.frequenciaescolar.data.database.AppDatabase;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public class TurmaListActivity extends AppCompatActivity {

    private RecyclerView rvTurmas;
    private TextView tvEmpty;
    private TurmaAdapter adapter;
    private FrequenciaRepository repository;
    private long turmaSelecionadaId = -1;
    private AppDatabase database;
    private SwipeRefreshLayout swipeRefresh;
    private LinearLayout layoutDetalhes;
    private RecyclerView rvAlunosTablet;
    private TextView tvTurmaSelecionada;
    private AlunoAdapter alunoAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_turma_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        swipeRefresh.setOnRefreshListener(() -> {
            carregarTurmas();
            swipeRefresh.setRefreshing(false);
        });

        rvTurmas = findViewById(R.id.rvTurmas);
        tvEmpty = findViewById(R.id.tvEmpty);

        // Verificar se é tablet e configurar painel de detalhes
        if (isTablet()) {
            layoutDetalhes = findViewById(R.id.layoutDetalhes);
            rvAlunosTablet = findViewById(R.id.rvAlunosTablet);
            tvTurmaSelecionada = findViewById(R.id.tvTurmaSelecionada);

            alunoAdapter = new AlunoAdapter();
            rvAlunosTablet.setLayoutManager(new LinearLayoutManager(this));
            rvAlunosTablet.setAdapter(alunoAdapter);
        }

        adapter = new TurmaAdapter();
        rvTurmas.setLayoutManager(new LinearLayoutManager(this));
        rvTurmas.setAdapter(adapter);

        // Clique normal - abrir lista de alunos (ou detalhes no tablet)
        adapter.setOnItemClickListener(turma -> {
            try {
                // Verificar se a turma ainda existe
                repository.getTurmaById(turma.getId(), turmaVerificada -> {
                    if (turmaVerificada == null) {
                        runOnUiThread(() -> {
                            Toast.makeText(TurmaListActivity.this, "Turma não encontrada", Toast.LENGTH_SHORT).show();
                            carregarTurmas();
                        });
                        return;
                    }

                    turmaSelecionadaId = turma.getId();
                    if (isTablet()) {
                        carregarDetalhesTurma(turma);
                    } else {
                        Intent intent = new Intent(TurmaListActivity.this, AlunoListActivity.class);
                        intent.putExtra("turma_id", turma.getId());
                        intent.putExtra("turma_nome", turma.getNome());
                        startActivity(intent);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Erro ao abrir turma", Toast.LENGTH_SHORT).show();
            }
        });

        // Clique longo - mostrar opções (Editar/Excluir)
        adapter.setOnItemLongClickListener(turma -> {
            String[] opcoes = {"Editar Turma", "Excluir Turma"};
            new AlertDialog.Builder(this)
                    .setTitle(turma.getNome())
                    .setItems(opcoes, (dialog, which) -> {
                        if (which == 0) {
                            Intent intent = new Intent(TurmaListActivity.this, TurmaFormActivity.class);
                            intent.putExtra("turma_id", turma.getId());
                            startActivity(intent);
                        } else if (which == 1) {
                            confirmarExclusaoTurma(turma);
                        }
                    })
                    .show();
        });

        repository = FrequenciaRepository.getInstance(this);
        database = AppDatabase.getInstance(this);
        carregarTurmas();

        NotificationHelper notificationHelper = new NotificationHelper(this);
        NotificationScheduler.agendarLembreteChamada(this);
        NotificationScheduler.agendarVerificacaoDiaria(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarTurmas();
    }

    // ==================== CARREGAR TURMAS ====================
    private void carregarTurmas() {
        repository.getAllTurmasAtivas(turmas -> {
            runOnUiThread(() -> {
                if (turmas == null || turmas.isEmpty()) {
                    rvTurmas.setVisibility(android.view.View.GONE);
                    tvEmpty.setVisibility(android.view.View.VISIBLE);
                    tvEmpty.setText("Nenhuma turma cadastrada.\nClique no + para adicionar");
                } else {
                    rvTurmas.setVisibility(android.view.View.VISIBLE);
                    tvEmpty.setVisibility(android.view.View.GONE);
                    adapter.setTurmas(turmas);
                }
            });
        });
    }

    // ==================== MÉTODO PARA VERIFICAR TABLET ====================
    private boolean isTablet() {
        return getResources().getBoolean(R.bool.isTablet);
    }

    // ==================== CARREGAR DETALHES DA TURMA (TABLET) ====================
    private void carregarDetalhesTurma(Turma turma) {
        if (!isTablet()) return;

        layoutDetalhes.setVisibility(View.VISIBLE);
        tvTurmaSelecionada.setText(turma.getNome() + " - " + turma.getTurno());

        // Carregar alunos da turma
        repository.getAlunosMatriculadosNaTurma(turma.getId(), matriculas -> {
            runOnUiThread(() -> {
                if (matriculas == null || matriculas.isEmpty()) {
                    alunoAdapter.setAlunos(new ArrayList<>());
                    return;
                }

                List<Aluno> alunos = new ArrayList<>();
                for (Matricula m : matriculas) {
                    repository.getAlunoById(m.getAlunoId(), aluno -> {
                        if (aluno != null && "ativo".equals(aluno.getStatus())) {
                            alunos.add(aluno);
                            alunoAdapter.setAlunos(new ArrayList<>(alunos));
                        }
                    });
                }
            });
        });
    }

    // ==================== MÉTODO DE EXCLUSÃO ====================
    private void confirmarExclusaoTurma(Turma turma) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir Turma")
                .setMessage("Tem certeza que deseja excluir a turma " + turma.getNome() + "?\n\n" +
                        "Esta ação irá desativar a turma. Alunos matriculados serão afetados.")
                .setPositiveButton("Excluir", (dialog, which) -> {
                    repository.desativarTurma(turma.getId(), () -> {
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Turma excluída com sucesso!", Toast.LENGTH_SHORT).show();
                            carregarTurmas();
                        });
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // ==================== EXPORTAÇÃO ====================
    private void exportarRelatorioTurma() {
        if (turmaSelecionadaId == -1) {
            Toast.makeText(this, "Selecione uma turma primeiro", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_exportar_turma, null);
        Spinner spinnerFormato = dialogView.findViewById(R.id.spinnerFormato);
        Spinner spinnerIncluir = dialogView.findViewById(R.id.spinnerIncluir);
        Button btnCancelar = dialogView.findViewById(R.id.btnCancelar);
        Button btnExportar = dialogView.findViewById(R.id.btnExportar);

        String[] formatos = {"PDF", "CSV (Excel)"};
        ArrayAdapter<String> formatoAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, formatos);
        formatoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFormato.setAdapter(formatoAdapter);

        String[] incluir = {"Todos os alunos", "Apenas alunos ativos", "Apenas alunos em risco"};
        ArrayAdapter<String> incluirAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, incluir);
        incluirAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerIncluir.setAdapter(incluirAdapter);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        btnCancelar.setOnClickListener(v -> dialog.dismiss());
        btnExportar.setOnClickListener(v -> {
            int formato = spinnerFormato.getSelectedItemPosition();
            int incluirOpcao = spinnerIncluir.getSelectedItemPosition();
            dialog.dismiss();
            gerarRelatorioTurma(formato, incluirOpcao);
        });

        dialog.show();
    }

    private void gerarRelatorioTurma(int formato, int incluirOpcao) {
        Toast.makeText(this, "Gerando relatório...", Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            try {
                Turma turma = database.turmaDao().getTurmaById(turmaSelecionadaId);
                List<Matricula> matriculas = database.matriculaDao().getAlunosMatriculadosNaTurma(turmaSelecionadaId);

                if (matriculas == null || matriculas.isEmpty()) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "⚠️ Esta turma não possui alunos para exportar", Toast.LENGTH_LONG).show();
                    });
                    return;
                }

                ConfiguracoesManager config = new ConfiguracoesManager(this);
                String dataInicio = config.getDataInicio();
                String dataFim = config.getDataFim();

                String dataInicioConv = converterData(dataInicio);
                String dataFimConv = converterData(dataFim);

                List<Feriado> feriados = database.feriadoDao().getFeriadosNoPeriodo(dataInicioConv, dataFimConv);
                List<Chamada> chamadas = database.chamadaDao().getChamadasPorPeriodo(dataInicioConv, dataFimConv);

                int totalDiasLetivos = config.calcularDiasLetivos(dataInicioConv, dataFimConv, feriados);

                List<AlunoExport> alunosExport = new ArrayList<>();

                for (Matricula m : matriculas) {
                    Aluno aluno = database.alunoDao().getAlunoById(m.getAlunoId());
                    if (aluno == null) continue;

                    if (incluirOpcao == 1 && !"ativo".equals(aluno.getStatus())) continue;

                    int presencas = 0;
                    int faltasJustificadas = 0;

                    for (Chamada c : chamadas) {
                        Presenca p = database.presencaDao().getPresencaByChamadaAndAluno(c.getId(), aluno.getId());
                        if (p != null) {
                            if (p.isPresente()) {
                                presencas++;
                            } else if (p.getJustificativa() != null && !p.getJustificativa().isEmpty()) {
                                faltasJustificadas++;
                            }
                        }
                    }

                    int diasConsiderados = config.isDesconsiderarJustificadas() ?
                            totalDiasLetivos - faltasJustificadas : totalDiasLetivos;
                    double frequencia = diasConsiderados > 0 ? (presencas * 100.0 / diasConsiderados) : 100;

                    if (incluirOpcao == 2 && frequencia >= 75) continue;

                    AlunoExport ae = new AlunoExport();
                    ae.nome = aluno.getNome();
                    ae.matricula = aluno.getMatricula();
                    ae.presencas = presencas;
                    ae.faltas = totalDiasLetivos - presencas;
                    ae.faltasJustificadas = faltasJustificadas;
                    ae.frequencia = frequencia;
                    alunosExport.add(ae);
                }

                if (alunosExport.isEmpty()) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "⚠️ Nenhum aluno atende aos critérios selecionados", Toast.LENGTH_LONG).show();
                    });
                    return;
                }

                if (formato == 0) {
                    gerarPDFTurma(turma, alunosExport, totalDiasLetivos, dataInicio, dataFim);
                } else {
                    gerarCSVTurma(turma, alunosExport, totalDiasLetivos, dataInicio, dataFim);
                }

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private void gerarPDFTurma(Turma turma, List<AlunoExport> alunos, int totalDias, String dataInicio, String dataFim) {
        // ... seu código existente (mantenha igual) ...
    }

    private void gerarCSVTurma(Turma turma, List<AlunoExport> alunos, int totalDias, String dataInicio, String dataFim) {
        // ... seu código existente (mantenha igual) ...
    }

    private void adicionarLinhaTabela(Table table, String label, String valor) {
        // ... seu código existente ...
    }

    private void adicionarCelulaCabecalho(Table table, String texto) {
        // ... seu código existente ...
    }

    private void compartilharArquivo(File file, String tipo) {
        // ... seu código existente ...
    }

    private String converterData(String data) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            SimpleDateFormat sdfOut = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            return sdfOut.format(sdf.parse(data));
        } catch (Exception e) {
            return data;
        }
    }

    // Classe interna para exportação
    private static class AlunoExport {
        String nome;
        String matricula;
        int presencas;
        int faltas;
        int faltasJustificadas;
        double frequencia;
    }

    // ==================== MENU ====================
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_turma_list, menu);
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            SpannableString s = new SpannableString(item.getTitle());
            s.setSpan(new ForegroundColorSpan(Color.WHITE), 0, s.length(), 0);
            item.setTitle(s);
        }
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
        } else if (itemId == R.id.action_configuracoes) {
            Intent intent = new Intent(this, ConfiguracoesActivity.class);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.action_graficos) {
            if (turmaSelecionadaId == -1) {
                Toast.makeText(this, "Selecione uma turma primeiro", Toast.LENGTH_SHORT).show();
                return true;
            }
            Intent intent = new Intent(this, GraficosFrequenciaActivity.class);
            intent.putExtra("turma_id", turmaSelecionadaId);
            intent.putExtra("aluno_id", -1L);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.action_importar) {
            Intent intent = new Intent(this, ImportarAlunosActivity.class);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.action_backup) {
            Intent intent = new Intent(this, BackupRestoreActivity.class);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.action_alunos_risco) {
            Intent intent = new Intent(this, AlunosRiscoActivity.class);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.action_exportar_turma) {
            exportarRelatorioTurma();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}