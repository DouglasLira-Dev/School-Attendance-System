package com.professor.frequenciaescolar.ui.risco;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;
import android.os.Environment;
import android.net.Uri;
import androidx.core.content.FileProvider;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.professor.frequenciaescolar.R;
import com.professor.frequenciaescolar.data.database.AppDatabase;
import com.professor.frequenciaescolar.data.entities.Aluno;
import com.professor.frequenciaescolar.data.entities.Chamada;
import com.professor.frequenciaescolar.data.entities.Feriado;
import com.professor.frequenciaescolar.data.entities.Matricula;
import com.professor.frequenciaescolar.data.entities.Presenca;
import com.professor.frequenciaescolar.data.entities.Turma;
import com.professor.frequenciaescolar.utils.ConfiguracoesManager;
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
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class AlunosRiscoActivity extends AppCompatActivity {

    private Spinner spinnerTurma;
    private Spinner spinnerTipoPeriodo;
    private TextInputEditText etDataEspecifica;
    private TextInputEditText etSemanaInicio, etSemanaFim;
    private Spinner spinnerMes, spinnerAno;
    private Spinner spinnerBimestre, spinnerAnoBimestre;
    private Button btnFiltrar;
    private Button btnExportarPDF, btnExportarCSV;
    private RecyclerView rvAlunosRisco;
    private TextView tvEmpty;
    private TextView tvTotalAlunos, tvEmRisco, tvMediaTurma;

    private View layoutDataEspecifica, layoutSemana, layoutMes, layoutBimestre;

    private AppDatabase database;
    private ConfiguracoesManager configManager;
    private AlunoRiscoAdapter adapter;
    private List<AlunoRiscoAdapter.AlunoRisco> alunosRisco = new ArrayList<>();

    private List<Turma> turmas = new ArrayList<>();
    private long turmaSelecionadaId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alunos_risco);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Inicializar views
        spinnerTurma = findViewById(R.id.spinnerTurma);
        spinnerTipoPeriodo = findViewById(R.id.spinnerTipoPeriodo);
        etDataEspecifica = findViewById(R.id.etDataEspecifica);
        etSemanaInicio = findViewById(R.id.etSemanaInicio);
        etSemanaFim = findViewById(R.id.etSemanaFim);
        spinnerMes = findViewById(R.id.spinnerMes);
        spinnerAno = findViewById(R.id.spinnerAno);
        spinnerBimestre = findViewById(R.id.spinnerBimestre);
        spinnerAnoBimestre = findViewById(R.id.spinnerAnoBimestre);
        btnFiltrar = findViewById(R.id.btnFiltrar);
        btnExportarPDF = findViewById(R.id.btnExportarPDF);
        btnExportarCSV = findViewById(R.id.btnExportarCSV);
        rvAlunosRisco = findViewById(R.id.rvAlunosRisco);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvTotalAlunos = findViewById(R.id.tvTotalAlunos);
        tvEmRisco = findViewById(R.id.tvEmRisco);
        tvMediaTurma = findViewById(R.id.tvMediaTurma);

        layoutDataEspecifica = findViewById(R.id.layoutDataEspecifica);
        layoutSemana = findViewById(R.id.layoutSemana);
        layoutMes = findViewById(R.id.layoutMes);
        layoutBimestre = findViewById(R.id.layoutBimestre);

        database = AppDatabase.getInstance(this);
        configManager = new ConfiguracoesManager(this);

        // Configurar RecyclerView
        adapter = new AlunoRiscoAdapter();
        rvAlunosRisco.setLayoutManager(new LinearLayoutManager(this));
        rvAlunosRisco.setAdapter(adapter);

        // Configurar spinners
        configurarSpinners();
        configurarTipoPeriodoSpinner();
        configurarDatePickers();

        // Carregar turmas
        carregarTurmas();

        btnFiltrar.setOnClickListener(v -> filtrarAlunosRisco());
        btnExportarPDF.setOnClickListener(v -> exportarPDF());
        btnExportarCSV.setOnClickListener(v -> exportarCSV());
    }

    private void configurarSpinners() {
        // Meses
        String[] meses = {"Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
                "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"};
        ArrayAdapter<String> mesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, meses);
        mesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMes.setAdapter(mesAdapter);

        // Anos (2020-2030)
        List<String> anos = new ArrayList<>();
        for (int i = 2020; i <= 2030; i++) {
            anos.add(String.valueOf(i));
        }
        ArrayAdapter<String> anoAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, anos);
        anoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAno.setAdapter(anoAdapter);
        spinnerAnoBimestre.setAdapter(anoAdapter);

        // Definir ano atual
        int anoAtual = Calendar.getInstance().get(Calendar.YEAR);
        int anoIndex = anos.indexOf(String.valueOf(anoAtual));
        spinnerAno.setSelection(anoIndex);
        spinnerAnoBimestre.setSelection(anoIndex);

        // Bimestres
        String[] bimestres = {"1º Bimestre", "2º Bimestre", "3º Bimestre", "4º Bimestre"};
        ArrayAdapter<String> bimestreAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, bimestres);
        bimestreAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBimestre.setAdapter(bimestreAdapter);
    }

    private void configurarTipoPeriodoSpinner() {
        String[] tipos = {"Dia Específico", "Semana", "Mês", "Bimestre"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tipos);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoPeriodo.setAdapter(adapter);

        spinnerTipoPeriodo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                layoutDataEspecifica.setVisibility(View.GONE);
                layoutSemana.setVisibility(View.GONE);
                layoutMes.setVisibility(View.GONE);
                layoutBimestre.setVisibility(View.GONE);

                switch (position) {
                    case 0:
                        layoutDataEspecifica.setVisibility(View.VISIBLE);
                        break;
                    case 1:
                        layoutSemana.setVisibility(View.VISIBLE);
                        definirSemanaAtual();
                        break;
                    case 2:
                        layoutMes.setVisibility(View.VISIBLE);
                        break;
                    case 3:
                        layoutBimestre.setVisibility(View.VISIBLE);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void definirSemanaAtual() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        // Primeiro dia da semana (segunda-feira)
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        etSemanaInicio.setText(sdf.format(cal.getTime()));

        // Último dia da semana (domingo)
        cal.add(Calendar.DAY_OF_WEEK, 6);
        etSemanaFim.setText(sdf.format(cal.getTime()));
    }

    private void configurarDatePickers() {
        etDataEspecifica.setOnClickListener(v -> mostrarDatePicker(etDataEspecifica));
        etSemanaInicio.setOnClickListener(v -> mostrarDatePicker(etSemanaInicio));
        etSemanaFim.setOnClickListener(v -> mostrarDatePicker(etSemanaFim));
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

    private void carregarTurmas() {
        new Thread(() -> {
            turmas = database.turmaDao().getAllTurmasAtivas();
            runOnUiThread(() -> {
                List<String> nomesTurmas = new ArrayList<>();
                nomesTurmas.add("Selecione uma turma");
                for (Turma t : turmas) {
                    nomesTurmas.add(t.getNome() + " - " + t.getTurno());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item, nomesTurmas);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerTurma.setAdapter(adapter);

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
        }).start();
    }

    private void filtrarAlunosRisco() {
        if (turmaSelecionadaId == -1) {
            Toast.makeText(this, "Selecione uma turma", Toast.LENGTH_SHORT).show();
            return;
        }

        String dataInicio = "";
        String dataFim = "";
        int tipoPeriodo = spinnerTipoPeriodo.getSelectedItemPosition();

        switch (tipoPeriodo) {
            case 0: // Dia específico
                String data = etDataEspecifica.getText().toString();
                if (data.isEmpty()) {
                    Toast.makeText(this, "Selecione uma data", Toast.LENGTH_SHORT).show();
                    return;
                }
                dataInicio = converterData(data);
                dataFim = dataInicio;
                break;
            case 1: // Semana
                String inicio = etSemanaInicio.getText().toString();
                String fim = etSemanaFim.getText().toString();
                if (inicio.isEmpty() || fim.isEmpty()) {
                    Toast.makeText(this, "Selecione o período da semana", Toast.LENGTH_SHORT).show();
                    return;
                }
                dataInicio = converterData(inicio);
                dataFim = converterData(fim);
                break;
            case 2: // Mês
                int mes = spinnerMes.getSelectedItemPosition() + 1;
                int ano = Integer.parseInt(spinnerAno.getSelectedItem().toString());
                dataInicio = String.format(Locale.getDefault(), "%04d-%02d-01", ano, mes);
                int ultimoDia = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH);
                dataFim = String.format(Locale.getDefault(), "%04d-%02d-%02d", ano, mes, ultimoDia);
                break;
            case 3: // Bimestre
                int bimestre = spinnerBimestre.getSelectedItemPosition() + 1;
                int anoB = Integer.parseInt(spinnerAnoBimestre.getSelectedItem().toString());
                int mesInicio, mesFim;
                switch (bimestre) {
                    case 1: mesInicio = 1; mesFim = 2; break;
                    case 2: mesInicio = 3; mesFim = 4; break;
                    case 3: mesInicio = 5; mesFim = 6; break;
                    default: mesInicio = 7; mesFim = 8; break;
                }
                dataInicio = String.format(Locale.getDefault(), "%04d-%02d-01", anoB, mesInicio);
                dataFim = String.format(Locale.getDefault(), "%04d-%02d-28", anoB, mesFim);
                break;
        }

        calcularAlunosRisco(dataInicio, dataFim);
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

    private String formatarData(String data) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat sdfOut = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            return sdfOut.format(sdf.parse(data));
        } catch (Exception e) {
            return data;
        }
    }

    private void calcularAlunosRisco(String dataInicio, String dataFim) {
        new Thread(() -> {
            try {
                // Buscar alunos da turma
                List<Matricula> matriculas = database.matriculaDao().getAlunosMatriculadosNaTurma(turmaSelecionadaId);
                if (matriculas == null || matriculas.isEmpty()) {
                    runOnUiThread(() -> {
                        alunosRisco.clear();
                        adapter.setAlunos(alunosRisco);
                        tvEmpty.setVisibility(View.VISIBLE);
                        rvAlunosRisco.setVisibility(View.GONE);
                        tvTotalAlunos.setText("0");
                        tvEmRisco.setText("0");
                        tvMediaTurma.setText("0%");
                    });
                    return;
                }

                // Buscar feriados no período
                List<Feriado> feriados = database.feriadoDao().getFeriadosNoPeriodo(dataInicio, dataFim);

                // Buscar chamadas no período
                List<Chamada> chamadas = database.chamadaDao().getChamadasPorPeriodo(dataInicio, dataFim);

                Turma turma = database.turmaDao().getTurmaById(turmaSelecionadaId);
                String turmaNome = turma != null ? turma.getNome() + " - " + turma.getTurno() : "";

                // Calcular total de dias letivos
                int totalDiasLetivos = configManager.calcularDiasLetivos(dataInicio, dataFim, feriados);

                List<AlunoRiscoAdapter.AlunoRisco> riscos = new ArrayList<>();
                int totalAlunos = 0;
                double somaFrequencias = 0;

                for (Matricula m : matriculas) {
                    Aluno aluno = database.alunoDao().getAlunoById(m.getAlunoId());
                    if (aluno == null || !"ativo".equals(aluno.getStatus())) continue;

                    totalAlunos++;

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

                    int faltasNaoJustificadas = totalDiasLetivos - presencas - faltasJustificadas;
                    int diasConsiderados = configManager.isDesconsiderarJustificadas() ?
                            totalDiasLetivos - faltasJustificadas : totalDiasLetivos;

                    double frequencia = diasConsiderados > 0 ? (presencas * 100.0 / diasConsiderados) : 100;
                    somaFrequencias += frequencia;

                    // Verificar se está em risco (< 75%)
                    if (frequencia < 75) {
                        AlunoRiscoAdapter.AlunoRisco ar = new AlunoRiscoAdapter.AlunoRisco();
                        ar.setId(aluno.getId());
                        ar.setNome(aluno.getNome());
                        ar.setMatricula(aluno.getMatricula());
                        ar.setTurma(turmaNome);
                        ar.setFrequencia(frequencia);
                        ar.setPresencas(presencas);
                        ar.setFaltas(totalDiasLetivos - presencas);
                        ar.setFaltasJustificadas(faltasJustificadas);
                        ar.setTotalDias(totalDiasLetivos);
                        riscos.add(ar);
                    }
                }

                double mediaTurma = totalAlunos > 0 ? somaFrequencias / totalAlunos : 0;
                double finalMediaTurma = mediaTurma;
                int finalTotalAlunos = totalAlunos;

                runOnUiThread(() -> {
                    alunosRisco.clear();
                    alunosRisco.addAll(riscos);
                    adapter.setAlunos(alunosRisco);

                    if (alunosRisco.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        rvAlunosRisco.setVisibility(View.GONE);
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                        rvAlunosRisco.setVisibility(View.VISIBLE);
                    }

                    tvTotalAlunos.setText(String.valueOf(finalTotalAlunos));
                    tvEmRisco.setText(String.valueOf(alunosRisco.size()));
                    tvMediaTurma.setText(String.format("%.1f%%", finalMediaTurma));
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Erro ao calcular: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private void exportarPDF() {
        if (alunosRisco.isEmpty()) {
            Toast.makeText(this, "Nenhum aluno em risco para exportar", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "alunos_risco_" + timestamp + ".pdf";

            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs();
            }

            File pdfFile = new File(downloadsDir, fileName);
            PdfWriter writer = new PdfWriter(new FileOutputStream(pdfFile));
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc, PageSize.A4);
            document.setMargins(50, 50, 50, 50);

            // Título
            PdfFont boldFont = PdfFontFactory.createFont();
            Paragraph titulo = new Paragraph("LISTA DE ALUNOS EM RISCO")
                    .setFont(boldFont)
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(titulo);

            // Informações
            Table infoTable = new Table(UnitValue.createPercentArray(new float[]{30, 70}));
            infoTable.setWidth(UnitValue.createPercentValue(100));
            infoTable.setMarginBottom(20);

            String turmaNome = !turmas.isEmpty() && turmaSelecionadaId != -1 ?
                    turmas.stream().filter(t -> t.getId() == turmaSelecionadaId).findFirst().map(Turma::getNome).orElse("") : "";

            adicionarLinhaTabela(infoTable, "Turma:", turmaNome);
            adicionarLinhaTabela(infoTable, "Total de Alunos em Risco:", String.valueOf(alunosRisco.size()));
            adicionarLinhaTabela(infoTable, "Data de Geração:", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date()));

            document.add(infoTable);

            // Tabela de Alunos
            Paragraph tabelaTitle = new Paragraph("ALUNOS COM FREQUÊNCIA < 75%")
                    .setFont(boldFont)
                    .setFontSize(14)
                    .setMarginTop(10)
                    .setMarginBottom(10);
            document.add(tabelaTitle);

            Table alunoTable = new Table(UnitValue.createPercentArray(new float[]{5, 30, 20, 15, 15, 15}));
            alunoTable.setWidth(UnitValue.createPercentValue(100));
            alunoTable.setMarginBottom(20);

            // Cabeçalho
            adicionarCelulaCabecalho(alunoTable, "Nº");
            adicionarCelulaCabecalho(alunoTable, "Nome");
            adicionarCelulaCabecalho(alunoTable, "Matrícula");
            adicionarCelulaCabecalho(alunoTable, "Presenças");
            adicionarCelulaCabecalho(alunoTable, "Faltas");
            adicionarCelulaCabecalho(alunoTable, "Frequência");

            // Dados
            int count = 1;
            for (AlunoRiscoAdapter.AlunoRisco ar : alunosRisco) {
                alunoTable.addCell(new Cell().add(new Paragraph(String.valueOf(count++))));
                alunoTable.addCell(new Cell().add(new Paragraph(ar.getNome())));
                alunoTable.addCell(new Cell().add(new Paragraph(ar.getMatricula())));
                alunoTable.addCell(new Cell().add(new Paragraph(String.valueOf(ar.getPresencas()))));
                alunoTable.addCell(new Cell().add(new Paragraph(String.valueOf(ar.getFaltas()))));
                Cell freqCell = new Cell().add(new Paragraph(String.format("%.1f%%", ar.getFrequencia())));
                if (ar.getFrequencia() < 50) {
                    freqCell.setBackgroundColor(ColorConstants.RED);
                    freqCell.setFontColor(ColorConstants.WHITE);
                } else if (ar.getFrequencia() < 65) {
                    freqCell.setBackgroundColor(ColorConstants.ORANGE);
                    freqCell.setFontColor(ColorConstants.WHITE);
                } else {
                    freqCell.setBackgroundColor(ColorConstants.YELLOW);
                }
                alunoTable.addCell(freqCell);
            }

            document.add(alunoTable);

            // Rodapé
            Paragraph footer = new Paragraph("Documento gerado pelo Sistema de Frequência Escolar")
                    .setFontSize(8)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(30);
            document.add(footer);

            document.close();

            Toast.makeText(this, "PDF gerado: " + pdfFile.getName(), Toast.LENGTH_LONG).show();
            compartilharArquivo(pdfFile, "application/pdf");

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao gerar PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void exportarCSV() {
        if (alunosRisco.isEmpty()) {
            Toast.makeText(this, "Nenhum aluno em risco para exportar", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "alunos_risco_" + timestamp + ".csv";

            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs();
            }

            File csvFile = new File(downloadsDir, fileName);
            FileOutputStream fos = new FileOutputStream(csvFile);
            OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);

            StringBuilder sb = new StringBuilder();

            // Cabeçalho
            sb.append("LISTA DE ALUNOS EM RISCO\n");
            sb.append("Turma: ").append(turmas.stream().filter(t -> t.getId() == turmaSelecionadaId).findFirst().map(Turma::getNome).orElse("")).append("\n");
            sb.append("Data de Geração: ").append(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date())).append("\n\n");

            // Colunas
            sb.append("Nº;Nome;Matrícula;Presenças;Faltas;Frequência\n");

            // Dados
            int count = 1;
            for (AlunoRiscoAdapter.AlunoRisco ar : alunosRisco) {
                sb.append(count++).append(";")
                        .append(ar.getNome()).append(";")
                        .append(ar.getMatricula()).append(";")
                        .append(ar.getPresencas()).append(";")
                        .append(ar.getFaltas()).append(";")
                        .append(String.format("%.1f%%", ar.getFrequencia())).append("\n");
            }

            osw.write(sb.toString());
            osw.flush();
            osw.close();
            fos.close();

            Toast.makeText(this, "CSV gerado: " + csvFile.getName(), Toast.LENGTH_LONG).show();
            compartilharArquivo(csvFile, "text/csv");

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao gerar CSV: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void adicionarLinhaTabela(Table table, String label, String valor) {
        table.addCell(new Cell().add(new Paragraph(label).setBold()));
        table.addCell(new Cell().add(new Paragraph(valor)));
    }

    private void adicionarCelulaCabecalho(Table table, String texto) {
        Cell cell = new Cell().add(new Paragraph(texto).setBold());
        cell.setBackgroundColor(ColorConstants.LIGHT_GRAY);
        cell.setTextAlignment(TextAlignment.CENTER);
        table.addCell(cell);
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