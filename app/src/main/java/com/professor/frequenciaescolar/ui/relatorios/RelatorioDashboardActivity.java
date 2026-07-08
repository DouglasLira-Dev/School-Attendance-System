package com.professor.frequenciaescolar.ui.relatorios;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.professor.frequenciaescolar.R;
import com.professor.frequenciaescolar.data.entities.Matricula;
import com.professor.frequenciaescolar.data.entities.Presenca;
import com.professor.frequenciaescolar.data.entities.Turma;
import com.professor.frequenciaescolar.data.repository.FrequenciaRepository;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPage;
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream;
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle;
import com.tom_roush.pdfbox.pdmodel.font.PDFont;
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
    private Spinner spinnerStatus;
    private String turmaNomeSelecionada = "";

    private static final int PERMISSION_REQUEST_CODE = 100;
    private static boolean pdfBoxInicializado = false;
    private static final float MARGEM = 50f;
    private static final float ALTURA_LINHA = 18f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relatorio_dashboard);

        if (!pdfBoxInicializado) {
            PDFBoxResourceLoader.init(getApplicationContext());
            pdfBoxInicializado = true;
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Inicializar views
        spinnerTurma = findViewById(R.id.spinnerTurma);
        spinnerStatus = findViewById(R.id.spinnerStatus);
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

        // Configurar opções de status
        String[] statusOptions = {"Todos", "Ativos", "Transferidos", "Expulsos"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statusOptions);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);

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
                            turmaNomeSelecionada = turmas.get(position - 1).getNome();
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

            // Obter filtro de status selecionado
            int statusPosition = spinnerStatus.getSelectedItemPosition();
            String statusFiltro = "";
            switch (statusPosition) {
                case 1:
                    statusFiltro = "ativo";
                    break;
                case 2:
                    statusFiltro = "transferido";
                    break;
                case 3:
                    statusFiltro = "expulso";
                    break;
                default:
                    statusFiltro = ""; // Todos
                    break;
            }

            List<Long> alunosIds = new ArrayList<>();

            for (Matricula m : matriculas) {
                alunosIds.add(m.getAlunoId());
            }

            // Buscar chamadas no período
            String finalStatusFiltro = statusFiltro;
            repository.getChamadasPorPeriodo(dataInicio, dataFim, chamadas -> {
                int totalChamadas = chamadas != null ? chamadas.size() : 0;

                runOnUiThread(() -> tvTotalAulas.setText(String.valueOf(totalChamadas)));

                Map<Long, Integer> faltasPorAluno = new HashMap<>();
                for (Long alunoId : alunosIds) {
                    faltasPorAluno.put(alunoId, 0);
                }

                if (chamadas != null && !chamadas.isEmpty()) {
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
                            processarResultados(alunosIds, faltasPorAluno, totalChamadas, finalStatusFiltro);
                        });
                    }
                } else {
                    processarResultados(alunosIds, faltasPorAluno, totalChamadas, finalStatusFiltro);
                }
            });
        });
    }

    private void processarResultados(List<Long> alunosIds, Map<Long, Integer> faltasPorAluno, int totalChamadas, String statusFiltro) {
        final double[] somaPercentuais = {0};
        final int[] count = {0};
        final List<RelatorioAdapter.AlunoFrequencia> baixaFrequenciaTemp = new ArrayList<>();
        final int[] processados = {0};

        for (Long alunoId : alunosIds) {
            repository.getAlunoById(alunoId, aluno -> {
                runOnUiThread(() -> {
                    if (aluno != null) {
                        boolean statusMatch = true;
                        if (!statusFiltro.isEmpty()) {
                            statusMatch = aluno.getStatus().equalsIgnoreCase(statusFiltro);
                        }

                        if (!statusMatch) {
                            processados[0]++;
                            if (processados[0] == alunosIds.size()) {
                                double media = count[0] > 0 ? somaPercentuais[0] / count[0] : 0;
                                tvMediaTurma.setText(String.format("%.1f%%", media));
                                adapter.setAlunos(baixaFrequenciaTemp);
                                if (baixaFrequenciaTemp.isEmpty()) {
                                    tvEmptyBaixaFrequencia.setVisibility(View.VISIBLE);
                                } else {
                                    tvEmptyBaixaFrequencia.setVisibility(View.GONE);
                                }
                            }
                            return;
                        }

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

    // ==================== EXPORTAÇÃO PDF ====================

    private void exportarPDF() {
        if (turmaSelecionadaId == -1) {
            Toast.makeText(this, "Selecione uma turma primeiro", Toast.LENGTH_SHORT).show();
            return;
        }

        if (alunosBaixaFrequencia.isEmpty()) {
            Toast.makeText(this, "Nenhum dado para exportar", Toast.LENGTH_SHORT).show();
            return;
        }

        // Verificar permissão
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
            return;
        }

        PDDocument document = new PDDocument();
        try {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "relatorio_frequencia_" + turmaNomeSelecionada.replace(" ", "_") + "_" + timestamp + ".pdf";

            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs();
            }
            File pdfFile = new File(downloadsDir, fileName);

            // Página em paisagem (equivalente ao PageSize.A4.rotate() do iText)
            PDRectangle paisagem = new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth());

            PDFont fonteNormal = PDType1Font.HELVETICA;
            PDFont fonteNegrito = PDType1Font.HELVETICA_BOLD;

            PDPage pagina = new PDPage(paisagem);
            document.addPage(pagina);
            PDPageContentStream cs = new PDPageContentStream(document, pagina);

            float y = paisagem.getHeight() - MARGEM;

            // ---------- Título ----------
            y = desenharTextoCentralizado(cs, "RELATÓRIO DE FREQUÊNCIA", fonteNegrito, 16, y, paisagem.getWidth());
            y -= 20;

            // ---------- Informações ----------
            y = desenharLinhaInfo(cs, fonteNegrito, fonteNormal, "Turma:", turmaNomeSelecionada, y);
            y = desenharLinhaInfo(cs, fonteNegrito, fonteNormal, "Período:", etDataInicio.getText().toString() + " a " + etDataFim.getText().toString(), y);
            y = desenharLinhaInfo(cs, fonteNegrito, fonteNormal, "Total de Aulas:", tvTotalAulas.getText().toString(), y);
            y = desenharLinhaInfo(cs, fonteNegrito, fonteNormal, "Média da Turma:", tvMediaTurma.getText().toString(), y);
            y = desenharLinhaInfo(cs, fonteNegrito, fonteNormal, "Data de Geração:",
                    new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date()), y);
            y -= 15;

            // ---------- Título da tabela ----------
            cs.beginText();
            cs.setFont(fonteNegrito, 12);
            cs.newLineAtOffset(MARGEM, y);
            cs.showText("LISTA DE ALUNOS COM BAIXA FREQUÊNCIA");
            cs.endText();
            y -= ALTURA_LINHA;

            // ---------- Tabela ----------
            float larguraUtil = paisagem.getWidth() - (MARGEM * 2);
            float[] proporcoes = {0.05f, 0.25f, 0.20f, 0.15f, 0.15f, 0.20f};
            float[] largurasColunas = new float[proporcoes.length];
            for (int i = 0; i < proporcoes.length; i++) {
                largurasColunas[i] = larguraUtil * proporcoes[i];
            }
            String[] cabecalhos = {"Nº", "Nome", "Matrícula", "Presenças", "Faltas", "Frequência"};

            y = desenharCabecalhoTabela(cs, cabecalhos, largurasColunas, fonteNegrito, y);

            int count = 1;
            for (RelatorioAdapter.AlunoFrequencia af : alunosBaixaFrequencia) {
                if (y - ALTURA_LINHA < MARGEM) {
                    cs.close();
                    pagina = new PDPage(paisagem);
                    document.addPage(pagina);
                    cs = new PDPageContentStream(document, pagina);
                    y = paisagem.getHeight() - MARGEM;
                    y = desenharCabecalhoTabela(cs, cabecalhos, largurasColunas, fonteNegrito, y);
                }

                String[] valores = {
                        String.valueOf(count++),
                        af.getNome(),
                        af.getMatricula(),
                        String.valueOf(af.getTotalAulas() - af.getFaltas()),
                        String.valueOf(af.getFaltas()),
                        String.format(Locale.getDefault(), "%.1f%%", af.getPercentual())
                };

                float[] corDestaque = af.getPercentual() < 60 ? new float[]{0.80f, 0.10f, 0.10f}
                        : af.getPercentual() < 75 ? new float[]{0.90f, 0.55f, 0.10f} : null;

                y = desenharLinhaTabela(cs, valores, largurasColunas, fonteNormal, y, corDestaque);
            }

            // ---------- Rodapé ----------
            if (y - 30 < MARGEM) {
                cs.close();
                pagina = new PDPage(paisagem);
                document.addPage(pagina);
                cs = new PDPageContentStream(document, pagina);
                y = paisagem.getHeight() - MARGEM;
            }
            y -= 20;
            desenharTextoCentralizado(cs, "Documento gerado pelo Sistema de Frequência Escolar", fonteNormal, 8, y, paisagem.getWidth());

            cs.close();
            document.save(pdfFile);

            Toast.makeText(this, "PDF gerado: " + pdfFile.getName(), Toast.LENGTH_LONG).show();
            compartilharArquivo(pdfFile, "application/pdf");

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao gerar PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            try {
                document.close();
            } catch (Exception ignored) {
            }
        }
    }

    // ==================== EXPORTAÇÃO XLSX ====================

    private void exportarXLSX() {
        if (turmaSelecionadaId == -1) {
            Toast.makeText(this, "Selecione uma turma primeiro", Toast.LENGTH_SHORT).show();
            return;
        }

        if (alunosBaixaFrequencia.isEmpty()) {
            Toast.makeText(this, "Nenhum dado para exportar", Toast.LENGTH_SHORT).show();
            return;
        }

        // Verificar permissão
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
            return;
        }

        try {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "relatorio_frequencia_" + turmaNomeSelecionada.replace(" ", "_") + "_" + timestamp + ".xlsx";

            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs();
            }

            File excelFile = new File(downloadsDir, fileName);
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Relatório de Frequência");

            // Cabeçalho
            Row headerRow = sheet.createRow(0);
            String[] colunas = {"Nº", "Nome", "Matrícula", "Presenças", "Faltas", "Frequência (%)"};
            for (int i = 0; i < colunas.length; i++) {
                headerRow.createCell(i).setCellValue(colunas[i]);
                sheet.setColumnWidth(i, 20 * 256);
            }

            // Dados
            int rowNum = 1;
            int count = 1;
            for (RelatorioAdapter.AlunoFrequencia af : alunosBaixaFrequencia) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(count++);
                row.createCell(1).setCellValue(af.getNome());
                row.createCell(2).setCellValue(af.getMatricula());
                row.createCell(3).setCellValue(af.getTotalAulas() - af.getFaltas());
                row.createCell(4).setCellValue(af.getFaltas());
                row.createCell(5).setCellValue(af.getPercentual());
            }

            // Escrever arquivo
            FileOutputStream fileOut = new FileOutputStream(excelFile);
            workbook.write(fileOut);
            fileOut.close();
            workbook.close();

            Toast.makeText(this, "Excel gerado: " + excelFile.getName(), Toast.LENGTH_LONG).show();
            compartilharArquivo(excelFile, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao gerar Excel: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // ==================== MÉTODOS AUXILIARES ====================

    // ---------- Helpers de desenho (pdfbox-android) ----------

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
        cs.setFont(fonteNegrito, 10);
        cs.newLineAtOffset(MARGEM, y);
        cs.showText(label);
        cs.endText();

        cs.beginText();
        cs.setFont(fonteNormal, 10);
        cs.newLineAtOffset(MARGEM + 150, y);
        cs.showText(valor != null ? valor : "-");
        cs.endText();

        return y - 16;
    }

    private float desenharCabecalhoTabela(PDPageContentStream cs, String[] cabecalhos, float[] larguras, PDFont fonteNegrito, float y) throws Exception {
        float x = MARGEM;

        cs.setNonStrokingColor(0.85f, 0.85f, 0.85f);
        cs.addRect(MARGEM, y - ALTURA_LINHA + 4, somar(larguras), ALTURA_LINHA);
        cs.fill();
        cs.setNonStrokingColor(0f, 0f, 0f);

        for (int i = 0; i < cabecalhos.length; i++) {
            cs.beginText();
            cs.setFont(fonteNegrito, 9);
            cs.newLineAtOffset(x + 3, y - ALTURA_LINHA + 8);
            cs.showText(cabecalhos[i]);
            cs.endText();
            x += larguras[i];
        }

        return y - ALTURA_LINHA;
    }

    private float desenharLinhaTabela(PDPageContentStream cs, String[] valores, float[] larguras, PDFont fonte, float y, float[] corDestaque) throws Exception {
        float x = MARGEM;

        for (int i = 0; i < valores.length; i++) {
            if (i == valores.length - 1 && corDestaque != null) {
                cs.setNonStrokingColor(corDestaque[0], corDestaque[1], corDestaque[2]);
                cs.addRect(x, y - ALTURA_LINHA + 4, larguras[i], ALTURA_LINHA);
                cs.fill();
                cs.setNonStrokingColor(1f, 1f, 1f);
            } else {
                cs.setNonStrokingColor(0f, 0f, 0f);
            }

            cs.beginText();
            cs.setFont(fonte, 9);
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

    private void compartilharArquivo(File file, String tipo) {
        Uri uri = FileProvider.getUriForFile(this,
                getPackageName() + ".provider", file);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType(tipo);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(Intent.createChooser(shareIntent, "Compartilhar Relatório"));
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