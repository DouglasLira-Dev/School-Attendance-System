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

import com.google.common.collect.Table;
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

import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPage;
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream;
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle;
import com.tom_roush.pdfbox.pdmodel.font.PDFont;
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.wp.usermodel.Paragraph;
import org.apache.poi.xddf.usermodel.text.TextAlignment;

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
    private SwipeRefreshLayout swipeRefresh;
    private LinearLayout layoutDetalhes;
    private RecyclerView rvAlunosTablet;
    private TextView tvTurmaSelecionada;
    private AlunoAdapter alunoAdapter;
    private boolean carregouPelaVez = false;
    private AppDatabase database;
    private static boolean pdfBoxInicializado = false;

    private static final float MARGEM = 50f;
    private static final float TAM_FONTE_TITULO = 16f;
    private static final float TAM_FONTE_INFO = 10f;
    private static final float TAM_FONTE_TABELA = 9f;
    private static final float ALTURA_LINHA = 20f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_turma_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        if (swipeRefresh != null) {
            swipeRefresh.setOnRefreshListener(() -> {
                carregarTurmas();
                swipeRefresh.setRefreshing(false);
                    });
        }

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
        if (!carregouPelaVez) {
            carregouPelaVez = true;
        } else {
            carregarTurmas();
        }
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
                //  Buscar turma diretamente — sem callback aninhado
                Turma turma = database.turmaDao().getTurmaById(turmaSelecionadaId);
                if (turma == null) {
                    runOnUiThread(() -> Toast.makeText(this, "Turma não encontrada", Toast.LENGTH_LONG).show());
                    return;
                }

                //  Buscar matrículas da turma
                List<Matricula> matriculas = database.matriculaDao().getAlunosMatriculadosNaTurma(turmaSelecionadaId);
                if (matriculas == null || matriculas.isEmpty()) {
                    runOnUiThread(() -> Toast.makeText(this, "Esta turma não possui alunos para exportar", Toast.LENGTH_LONG).show());
                    return;
                }

                //  Configurações de período
                ConfiguracoesManager config = new ConfiguracoesManager(this);
                String dataInicio = config.getDataInicio();
                String dataFim = config.getDataFim();
                String dataInicioConv = converterData(dataInicio);
                String dataFimConv = converterData(dataFim);

                //  Feriados e chamadas — síncronos, sem callbacks
                List<Feriado> feriados = database.feriadoDao().getFeriadosNoPeriodo(dataInicioConv, dataFimConv);

                //  Chamadas filtradas pela turma (não por todas as turmas)
                List<Chamada> chamadas = database.chamadaDao().getChamadasPorTurma(turmaSelecionadaId);
                // Filtrar pelo período manualmente
                List<Chamada> chamadasNoPeriodo = new ArrayList<>();
                for (Chamada c : chamadas) {
                    if (c.getData().compareTo(dataInicioConv) >= 0 && c.getData().compareTo(dataFimConv) <= 0) {
                        chamadasNoPeriodo.add(c);
                    }
                }

                int totalDiasLetivos = config.calcularDiasLetivos(dataInicioConv, dataFimConv, feriados);

                //  Montar lista de exportação — tudo síncrono, sem while/sleep
                List<AlunoExport> alunosExport = new ArrayList<>();

                for (Matricula m : matriculas) {
                    Aluno aluno = database.alunoDao().getAlunoById(m.getAlunoId());
                    if (aluno == null) continue;

                    // Filtro: apenas ativos
                    if (incluirOpcao == 1 && !"ativo".equals(aluno.getStatus())) continue;

                    int presencas = 0;
                    int faltasJustificadas = 0;

                    for (Chamada c : chamadasNoPeriodo) {
                        Presenca p = database.presencaDao().getPresencaByChamadaAndAluno(c.getId(), aluno.getId());
                        if (p != null) {
                            if (p.isPresente()) {
                                presencas++;
                            } else if (p.getJustificativa() != null && !p.getJustificativa().isEmpty()) {
                                faltasJustificadas++;
                            }
                        }
                    }

                    int diasConsiderados = config.isDesconsiderarJustificadas()
                            ? totalDiasLetivos - faltasJustificadas
                            : totalDiasLetivos;
                    double frequencia = diasConsiderados > 0 ? (presencas * 100.0 / diasConsiderados) : 100;

                    // Filtro: apenas alunos em risco (abaixo de 75%)
                    if (incluirOpcao == 2 && frequencia >= 75) continue;

                    AlunoExport ae = new AlunoExport();
                    ae.nome = aluno.getNome();
                    ae.matricula = aluno.getMatricula();
                    ae.presencas = presencas;
                    ae.faltas = chamadasNoPeriodo.size() - presencas;
                    ae.faltasJustificadas = faltasJustificadas;
                    ae.frequencia = frequencia;
                    alunosExport.add(ae);
                }

                //  Verificar se tem dados para exportar
                if (alunosExport.isEmpty()) {
                    runOnUiThread(() -> Toast.makeText(this, "Nenhum aluno atende aos critérios selecionados", Toast.LENGTH_LONG).show());
                    return;
                }

                //  Gerar arquivo
                if (isDestroyed() || isFinishing()) return;

                if (formato == 0) {
                    gerarPDFTurma(turma, alunosExport, totalDiasLetivos, dataInicio, dataFim);
                } else {
                    gerarCSVTurma(turma, alunosExport, totalDiasLetivos, dataInicio, dataFim);
                }

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Erro ao gerar relatório: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    // ==================== GERAR PDF (via pdfbox-android) ====================

    private void gerarPDFTurma(Turma turma, List<AlunoExport> alunos, int totalDias, String dataInicio, String dataFim) {
        if (!pdfBoxInicializado) {
            PDFBoxResourceLoader.init(getApplicationContext());
            pdfBoxInicializado = true;
        }

        PDDocument document = new PDDocument();
        try {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "relatorio_turma_" + turma.getNome().replace(" ", "_") + "_" + timestamp + ".pdf";

            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs();
            }
            File pdfFile = new File(downloadsDir, fileName);

            PDFont fonteNormal = PDType1Font.HELVETICA;
            PDFont fonteNegrito = PDType1Font.HELVETICA_BOLD;

            // Larguras das colunas (Nº, Nome, Matrícula, Presenças, Faltas, Frequência)
            float larguraUtil = PDRectangle.A4.getWidth() - (MARGEM * 2);
            float[] proporcoes = {0.05f, 0.30f, 0.20f, 0.15f, 0.15f, 0.15f};
            float[] largurasColunas = new float[proporcoes.length];
            for (int i = 0; i < proporcoes.length; i++) {
                largurasColunas[i] = larguraUtil * proporcoes[i];
            }
            String[] cabecalhos = {"Nº", "Nome", "Matrícula", "Presenças", "Faltas", "Frequência"};

            PDPage paginaAtual = new PDPage(PDRectangle.A4);
            document.addPage(paginaAtual);
            PDPageContentStream cs = new PDPageContentStream(document, paginaAtual);

            float y = PDRectangle.A4.getHeight() - MARGEM;

            // ---------- Título ----------
            y = desenharTextoCentralizado(cs, "RELATÓRIO DE FREQUÊNCIA - TURMA", fonteNegrito, TAM_FONTE_TITULO, y, PDRectangle.A4.getWidth());
            y -= 20;

            // ---------- Bloco de informações ----------
            y = desenharLinhaInfo(cs, fonteNegrito, fonteNormal, "Turma:", turma.getNome() + " - " + turma.getTurno(), y);
            y = desenharLinhaInfo(cs, fonteNegrito, fonteNormal, "Período:", dataInicio + " a " + dataFim, y);
            y = desenharLinhaInfo(cs, fonteNegrito, fonteNormal, "Total de Dias Letivos:", String.valueOf(totalDias), y);
            y = desenharLinhaInfo(cs, fonteNegrito, fonteNormal, "Total de Alunos:", String.valueOf(alunos.size()), y);
            y = desenharLinhaInfo(cs, fonteNegrito, fonteNormal, "Data de Geração:",
                    new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date()), y);
            y -= 15;

            // ---------- Título da tabela ----------
            cs.beginText();
            cs.setFont(fonteNegrito, 12);
            cs.newLineAtOffset(MARGEM, y);
            cs.showText("LISTA DE ALUNOS");
            cs.endText();
            y -= ALTURA_LINHA;

            // ---------- Cabeçalho da tabela ----------
            y = desenharCabecalhoTabela(cs, cabecalhos, largurasColunas, fonteNegrito, y);

            // ---------- Linhas da tabela (com paginação) ----------
            int count = 1;
            for (AlunoExport ae : alunos) {
                if (y - ALTURA_LINHA < MARGEM) {
                    // Fecha página atual e abre uma nova
                    cs.close();
                    paginaAtual = new PDPage(PDRectangle.A4);
                    document.addPage(paginaAtual);
                    cs = new PDPageContentStream(document, paginaAtual);
                    y = PDRectangle.A4.getHeight() - MARGEM;
                    y = desenharCabecalhoTabela(cs, cabecalhos, largurasColunas, fonteNegrito, y);
                }

                String[] valores = {
                        String.valueOf(count++),
                        ae.nome,
                        ae.matricula,
                        String.valueOf(ae.presencas),
                        String.valueOf(ae.faltas),
                        String.format(Locale.getDefault(), "%.1f%%", ae.frequencia)
                };

                boolean destacar = ae.frequencia < 75;
                y = desenharLinhaTabela(cs, valores, largurasColunas, fonteNormal, y, destacar);
            }

            // ---------- Rodapé ----------
            if (y - 30 < MARGEM) {
                cs.close();
                paginaAtual = new PDPage(PDRectangle.A4);
                document.addPage(paginaAtual);
                cs = new PDPageContentStream(document, paginaAtual);
                y = PDRectangle.A4.getHeight() - MARGEM;
            }
            y -= 20;
            desenharTextoCentralizado(cs, "Documento gerado pelo Sistema de Frequência Escolar", fonteNormal, 8, y, PDRectangle.A4.getWidth());

            cs.close();
            document.save(pdfFile);

            runOnUiThread(() -> {
                String caminho = pdfFile.getAbsolutePath();
                Toast.makeText(this, "📄 PDF gerado: " + pdfFile.getName() + "\n📂 Salvo em: " + caminho, Toast.LENGTH_LONG).show();
                compartilharArquivo(pdfFile, "application/pdf");
            });

        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> Toast.makeText(this, "Erro ao gerar PDF: " + e.getMessage(), Toast.LENGTH_LONG).show());
        } finally {
            try {
                document.close();
            } catch (Exception ignored) {
            }
        }
    }

// ---------- Helpers de desenho ----------

    private float desenharTextoCentralizado(PDPageContentStream cs, String texto, PDFont fonte, float tamanho, float y, float larguraPagina) throws Exception {
        float larguraTexto = fonte.getStringWidth(texto) / 1000 * tamanho;
        float x = (larguraPagina - larguraTexto) / 2;
        cs.beginText();
        cs.setFont(fonte, tamanho);
        cs.newLineAtOffset(x, y);
        cs.showText(texto);
        cs.endText();
        return y - ALTURA_LINHA;
    }

    private float desenharLinhaInfo(PDPageContentStream cs, PDFont fonteNegrito, PDFont fonteNormal, String label, String valor, float y) throws Exception {
        cs.beginText();
        cs.setFont(fonteNegrito, TAM_FONTE_INFO);
        cs.newLineAtOffset(MARGEM, y);
        cs.showText(label);
        cs.endText();

        cs.beginText();
        cs.setFont(fonteNormal, TAM_FONTE_INFO);
        cs.newLineAtOffset(MARGEM + 140, y);
        cs.showText(valor);
        cs.endText();

        return y - 16;
    }

    private float desenharCabecalhoTabela(PDPageContentStream cs, String[] cabecalhos, float[] larguras, PDFont fonteNegrito, float y) throws Exception {
        float x = MARGEM;

        // Fundo cinza claro
        cs.setNonStrokingColor(0.85f, 0.85f, 0.85f);
        cs.addRect(MARGEM, y - ALTURA_LINHA + 4, somar(larguras), ALTURA_LINHA);
        cs.fill();
        cs.setNonStrokingColor(0f, 0f, 0f);

        for (int i = 0; i < cabecalhos.length; i++) {
            cs.beginText();
            cs.setFont(fonteNegrito, TAM_FONTE_TABELA);
            cs.newLineAtOffset(x + 3, y - ALTURA_LINHA + 8);
            cs.showText(cabecalhos[i]);
            cs.endText();
            x += larguras[i];
        }

        return y - ALTURA_LINHA;
    }

    private float desenharLinhaTabela(PDPageContentStream cs, String[] valores, float[] larguras, PDFont fonte, float y, boolean destacarFrequenciaBaixa) throws Exception {
        float x = MARGEM;

        for (int i = 0; i < valores.length; i++) {
            // Última coluna é a de frequência; destaca em vermelho se abaixo de 75%
            if (i == valores.length - 1 && destacarFrequenciaBaixa) {
                cs.setNonStrokingColor(0.85f, 0f, 0f);
                cs.addRect(x, y - ALTURA_LINHA + 4, larguras[i], ALTURA_LINHA);
                cs.fill();
                cs.setNonStrokingColor(1f, 1f, 1f);
            } else {
                cs.setNonStrokingColor(0f, 0f, 0f);
            }

            cs.beginText();
            cs.setFont(fonte, TAM_FONTE_TABELA);
            cs.newLineAtOffset(x + 3, y - ALTURA_LINHA + 8);
            cs.showText(valores[i]);
            cs.endText();

            x += larguras[i];
        }
        cs.setNonStrokingColor(0f, 0f, 0f);
        return y - ALTURA_LINHA;
    }

    private float somar(float[] valores) {
        float total = 0;
        for (float v : valores) total += v;
        return total;
    }

    // ==================== GERAR CSV ====================
    private void gerarCSVTurma(Turma turma, List<AlunoExport> alunos, int totalDias, String dataInicio, String dataFim) {
        try {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "relatorio_turma_" + turma.getNome().replace(" ", "_") + "_" + timestamp + ".csv";

            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs();
            }

            File csvFile = new File(downloadsDir, fileName);

            StringBuilder sb = new StringBuilder();

            sb.append("RELATÓRIO DE FREQUÊNCIA - TURMA\n");
            sb.append("Turma: ").append(turma.getNome()).append(" - ").append(turma.getTurno()).append("\n");
            sb.append("Período: ").append(dataInicio).append(" a ").append(dataFim).append("\n");
            sb.append("Total de Dias Letivos: ").append(totalDias).append("\n\n");

            sb.append("Nº;Nome;Matrícula;Presenças;Faltas;Frequência\n");

            int count = 1;
            for (AlunoExport ae : alunos) {
                sb.append(count++).append(";")
                        .append(ae.nome).append(";")
                        .append(ae.matricula).append(";")
                        .append(ae.presencas).append(";")
                        .append(ae.faltas).append(";")
                        .append(String.format("%.1f%%", ae.frequencia)).append("\n");
            }

            try (FileOutputStream fos = new FileOutputStream(csvFile);
                 OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {
                osw.write(sb.toString());
                osw.flush();
            }

            runOnUiThread(() -> {
                String caminho = csvFile.getAbsolutePath();
                Toast.makeText(this, "📊 CSV gerado: " + csvFile.getName() + "\n📂 Salvo em: " + caminho, Toast.LENGTH_LONG).show();
                compartilharArquivo(csvFile, "text/csv");
            });

        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> Toast.makeText(this, "Erro ao gerar CSV: " + e.getMessage(), Toast.LENGTH_LONG).show());
        }
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private void compartilharArquivo(File file, String tipo) {
        Uri uri = FileProvider.getUriForFile(this,
                getPackageName() + ".provider", file);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType(tipo);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(Intent.createChooser(shareIntent, "Compartilhar Relatório"));
    }

    // ==================== CONVERTER DATA ====================
    private String converterData(String data) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            SimpleDateFormat sdfOut = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            return sdfOut.format(sdf.parse(data));
        } catch (Exception e) {
            return data;
        }
    }

    // ==================== CLASSE INTERNA PARA EXPORTAÇÃO ====================
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