package com.professor.frequenciaescolar.ui.importar;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.professor.frequenciaescolar.R;
import com.professor.frequenciaescolar.data.entities.Aluno;
import com.professor.frequenciaescolar.data.entities.Matricula;
import com.professor.frequenciaescolar.data.entities.Turma;
import com.professor.frequenciaescolar.data.repository.FrequenciaRepository;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ImportarAlunosActivity extends AppCompatActivity {

    private TextView tvArquivoSelecionado;
    private TextView tvResumo;
    private TextView tvErros;
    private Button btnSelecionarArquivo;
    private Button btnImportar;
    private Button btnBaixarModelo;
    private ProgressBar progressBar;
    private TextView tvProgresso;
    private MaterialCardView cardPreview;
    private RecyclerView rvPreview;
    private Spinner spinnerTurma;

    private PreviewAdapter previewAdapter;
    private FrequenciaRepository repository;

    private List<AlunoPreview> previewList = new ArrayList<>();
    private List<String> erros = new ArrayList<>();
    private String arquivoSelecionadoNome = "";
    private Uri arquivoUri;
    private long turmaIdRecebida = -1;
    private long turmaSelecionadaId = -1;
    private List<Turma> turmasList = new ArrayList<>();

    private final ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    arquivoUri = result.getData().getData();
                    if (arquivoUri != null) {
                        arquivoSelecionadoNome = getFileName(arquivoUri);
                        tvArquivoSelecionado.setText("Arquivo: " + arquivoSelecionadoNome);
                        Toast.makeText(this, "📂 Arquivo selecionado: " + arquivoSelecionadoNome, Toast.LENGTH_LONG).show();
                        processarArquivo();
                    } else {
                        Toast.makeText(this, "Erro: URI do arquivo é nula", Toast.LENGTH_SHORT).show();
                    }
                } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                    Toast.makeText(this, "Seleção de arquivo cancelada", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Erro ao selecionar arquivo (código: " + result.getResultCode() + ")", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_importar_alunos);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao carregar layout: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        try {
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }

            // Receber turma da Intent
            turmaIdRecebida = getIntent().getLongExtra("turma_id", -1);

            // Inicializar views
            tvArquivoSelecionado = findViewById(R.id.tvArquivoSelecionado);
            tvResumo = findViewById(R.id.tvResumo);
            tvErros = findViewById(R.id.tvErros);
            btnSelecionarArquivo = findViewById(R.id.btnSelecionarArquivo);
            btnImportar = findViewById(R.id.btnImportar);
            btnBaixarModelo = findViewById(R.id.btnBaixarModelo);
            progressBar = findViewById(R.id.progressBar);
            tvProgresso = findViewById(R.id.tvProgresso);
            cardPreview = findViewById(R.id.cardPreview);
            rvPreview = findViewById(R.id.rvPreview);
            spinnerTurma = findViewById(R.id.spinnerTurma);

            // Verificar se o spinner foi encontrado
            if (spinnerTurma == null) {
                Toast.makeText(this, "Erro: Spinner não encontrado", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            repository = FrequenciaRepository.getInstance(this);

            previewAdapter = new PreviewAdapter(previewList);
            rvPreview.setLayoutManager(new LinearLayoutManager(this));
            rvPreview.setAdapter(previewAdapter);

            // Carregar turmas no Spinner
            carregarTurmasSpinner();

            btnSelecionarArquivo.setOnClickListener(v -> selecionarArquivo());
            btnImportar.setOnClickListener(v -> confirmarImportacao());
            btnBaixarModelo.setOnClickListener(v -> baixarModeloCSV());

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao inicializar: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void carregarTurmasSpinner() {
        try {
            repository.getAllTurmasAtivas(turmas -> {
                runOnUiThread(() -> {
                    try {
                        this.turmasList = turmas;

                        List<String> nomesTurmas = new ArrayList<>();
                        nomesTurmas.add("Selecione uma turma");

                        int posicaoSelecionada = 0;

                        for (int i = 0; i < turmas.size(); i++) {
                            Turma t = turmas.get(i);
                            nomesTurmas.add(t.getNome() + " - " + t.getTurno());

                            if (t.getId() == turmaIdRecebida) {
                                posicaoSelecionada = i + 1;
                            }
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                                android.R.layout.simple_spinner_item, nomesTurmas);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerTurma.setAdapter(adapter);

                        if (posicaoSelecionada > 0 && posicaoSelecionada < turmas.size() + 1) {
                            spinnerTurma.setSelection(posicaoSelecionada);
                            turmaSelecionadaId = turmas.get(posicaoSelecionada - 1).getId();
                        }

                        spinnerTurma.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                if (position > 0 && position - 1 < turmas.size()) {
                                    turmaSelecionadaId = turmas.get(position - 1).getId();
                                } else {
                                    turmaSelecionadaId = -1;
                                }
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {}
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(ImportarAlunosActivity.this, "Erro ao carregar turmas: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao carregar turmas: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void selecionarArquivo() {
        try {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");

            filePickerLauncher.launch(intent);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao abrir seletor: " + e.getMessage(), Toast.LENGTH_SHORT).show();

            try {
                Intent fallback = new Intent(Intent.ACTION_GET_CONTENT);
                fallback.setType("*/*");
                fallback.addCategory(Intent.CATEGORY_OPENABLE);
                filePickerLauncher.launch(fallback);
            } catch (Exception ex) {
                Toast.makeText(this, "Erro ao abrir seletor alternativo", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void processarArquivo() {
        try {
            if (arquivoUri == null) {
                Toast.makeText(this, "Nenhum arquivo selecionado", Toast.LENGTH_SHORT).show();
                return;
            }

            String fileName = getFileName(arquivoUri);
            if (fileName == null || fileName.isEmpty()) {
                Toast.makeText(this, "Arquivo inválido", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(this, "Lendo arquivo: " + fileName, Toast.LENGTH_SHORT).show();

            try (InputStream inputStream = getContentResolver().openInputStream(arquivoUri);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

                previewList.clear();
                erros.clear();

                // ==================== DETECTAR SEPARADOR ====================
                String separador = detectarSeparador(reader);
                Toast.makeText(this, "Separador detectado: " + separador, Toast.LENGTH_SHORT).show();

                String line;
                int lineNumber = 0;

                while ((line = reader.readLine()) != null) {
                    lineNumber++;

                    // Pular cabeçalho
                    if (lineNumber == 1) {
                        String linhaLower = line.toLowerCase();
                        if (linhaLower.contains("nome") || linhaLower.contains("matricula") ||
                                linhaLower.contains("responsavel") || linhaLower.contains("turma")) {
                            continue;
                        }
                    }

                    // Usar o separador detectado
                    String[] campos = line.split(separador);

                    // Se não funcionou com o separador detectado, tentar vírgula
                    if (campos.length < 2) {
                        campos = line.split(",");
                    }
                    // Se não funcionou, tentar ponto e vírgula
                    if (campos.length < 2) {
                        campos = line.split(";");
                    }
                    // Se não funcionou, tentar tab
                    if (campos.length < 2) {
                        campos = line.split("\t");
                    }

                    if (campos.length < 2) {
                        erros.add("Linha " + lineNumber + ": Formato inválido (" + campos.length + " colunas)");
                        continue;
                    }

                    // Limpar campos (remover aspas e espaços)
                    for (int i = 0; i < campos.length; i++) {
                        campos[i] = campos[i].trim().replaceAll("^\"|\"$", "");
                    }

                    String nome = campos.length > 0 ? campos[0].trim() : "";
                    String matricula = campos.length > 1 ? campos[1].trim() : "";
                    String responsavel = campos.length > 2 ? campos[2].trim() : "";
                    String telefone = campos.length > 3 ? campos[3].trim() : "";
                    String turmaNome = campos.length > 4 ? campos[4].trim() : "";

                    if (nome.isEmpty()) {
                        erros.add("Linha " + lineNumber + ": Nome vazio");
                        continue;
                    }

                    if (matricula.isEmpty()) {
                        erros.add("Linha " + lineNumber + ": Matrícula vazia");
                        continue;
                    }

                    previewList.add(new AlunoPreview(nome, matricula, responsavel, telefone, turmaNome));
                }

                // Se não leu nenhuma linha
                if (lineNumber == 0) {
                    erros.add("Arquivo vazio ou não pôde ser lido");
                }

                // Atualizar UI
                tvResumo.setText(String.format("Total de alunos encontrados: %d", previewList.size()));
                previewAdapter.notifyDataSetChanged();

                if (!erros.isEmpty()) {
                    tvErros.setVisibility(View.VISIBLE);
                    tvErros.setText(String.format("⚠️ %d erro(s) encontrados:\n%s",
                            erros.size(), String.join("\n", erros)));
                } else {
                    tvErros.setVisibility(View.GONE);
                }

                cardPreview.setVisibility(previewList.isEmpty() ? View.GONE : View.VISIBLE);
                btnImportar.setVisibility(previewList.isEmpty() ? View.GONE : View.VISIBLE);
                btnImportar.setEnabled(!previewList.isEmpty());

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Erro ao ler arquivo: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao processar arquivo: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private String detectarSeparador(BufferedReader reader) throws IOException {
        reader.mark(1024);
        String firstLine = reader.readLine();
        reader.reset();

        if (firstLine != null) {
            if (firstLine.contains(";")) return ";";
            if (firstLine.contains("\t")) return "\t";
            if (firstLine.contains("|")) return "|";
            if (firstLine.contains(",")) return ",";
        }
        return ",";
    }

    private void confirmarImportacao() {
        // Obter a turma diretamente do Spinner no momento da confirmação
        int position = spinnerTurma.getSelectedItemPosition();
        if (position <= 0 || position - 1 >= turmasList.size()) {
            Toast.makeText(this, "Selecione uma turma para importar os alunos", Toast.LENGTH_SHORT).show();
            return;
        }

        Turma turmaSelecionada = turmasList.get(position - 1);
        long turmaId = turmaSelecionada.getId();

        if (previewList.isEmpty()) {
            Toast.makeText(this, "Nenhum aluno para importar", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Confirmar Importação")
                .setMessage(String.format("Deseja importar %d alunos para a turma %s?", previewList.size(), turmaSelecionada.getNome()))
                .setPositiveButton("Importar", (dialog, which) -> realizarImportacao(turmaId, turmaSelecionada.getNome()))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void realizarImportacao(long turmaId, String turmaNome) {
        if (turmaId == -1) {
            Toast.makeText(this, "Erro: turma inválida", Toast.LENGTH_SHORT).show();
            return;
        }

        if (previewList.isEmpty()) {
            Toast.makeText(this, "Nenhum aluno para importar", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        tvProgresso.setVisibility(View.VISIBLE);
        btnImportar.setEnabled(false);
        btnSelecionarArquivo.setEnabled(false);

        final long turmaFinalId = turmaId;
        String dataAtual = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        AtomicInteger importados = new AtomicInteger(0);
        AtomicInteger ignorados = new AtomicInteger(0);
        AtomicInteger processados = new AtomicInteger(0);
        final int total = previewList.size();

        android.util.Log.d("Importacao", "Iniciando importação para turma: " + turmaFinalId);

        for (AlunoPreview preview : previewList) {
            final String matriculaClean = preview.matricula.trim();
            final String nomeClean = preview.nome.trim();

            android.util.Log.d("Importacao", "Processando: " + nomeClean + " (" + matriculaClean + ")");

            // 1. Verificar se o aluno já existe
            repository.getAlunoByMatricula(matriculaClean, alunoExistente -> {
                if (alunoExistente != null) {
                    // Aluno já existe → verificar se já tem alguma matrícula ativa
                    repository.getMatriculaAtivaByAluno(alunoExistente.getId(), matriculaAtiva -> {
                        if (matriculaAtiva != null) {
                            // Já tem matrícula ativa → NÃO PERMITIR a nova matrícula
                            String nomeTurmaAtual = getTurmaNome(matriculaAtiva.getTurmaId());
                            ignorados.incrementAndGet();
                            processados.incrementAndGet();
                            atualizarProgresso(processados.get(), total,
                                    "❌ " + nomeClean + " já está matriculado em " + nomeTurmaAtual);
                            android.util.Log.d("Importacao", "Aluno já possui matrícula ativa em outra turma: " + nomeTurmaAtual);
                        } else {
                            // Aluno não tem matrícula ativa → permitir criar nova
                            Matricula novaMatricula = new Matricula(
                                    alunoExistente.getId(),
                                    turmaFinalId,
                                    dataAtual,
                                    "ativa"
                            );
                            repository.insertMatricula(novaMatricula, () -> {
                                importados.incrementAndGet();
                                processados.incrementAndGet();
                                atualizarProgresso(processados.get(), total,
                                        "✅ " + nomeClean + " → " + turmaNome);
                                android.util.Log.d("Importacao", "Matrícula criada para " + nomeClean + " na turma " + turmaFinalId);
                            });
                        }
                    });
                    return;
                }

                // 2. Aluno não existe → criar novo e matricular
                Aluno novoAluno = new Aluno(
                        nomeClean,
                        matriculaClean,
                        preview.responsavel != null ? preview.responsavel.trim() : "",
                        preview.telefone != null ? preview.telefone.trim() : "",
                        "ativo", true
                );

                repository.insertAlunoAndGetId(novoAluno, alunoId -> {
                    android.util.Log.d("Importacao", "ID retornado para " + nomeClean + ": " + alunoId);
                    if (alunoId != null && alunoId > 0) {
                        Matricula matriculaObj = new Matricula(
                                alunoId,
                                turmaFinalId,
                                dataAtual,
                                "ativa"
                        );
                        repository.insertMatricula(matriculaObj, () -> {
                            importados.incrementAndGet();
                            processados.incrementAndGet();
                            atualizarProgresso(processados.get(), total,
                                    "✅ " + nomeClean + " → " + turmaNome);
                            android.util.Log.d("Importacao", "Novo aluno e matrícula criados para " + nomeClean);
                        });
                    } else {
                        ignorados.incrementAndGet();
                        processados.incrementAndGet();
                        atualizarProgresso(processados.get(), total,
                                "❌ Erro ao inserir aluno: " + nomeClean);
                        android.util.Log.e("Importacao", "Falha ao inserir aluno: " + matriculaClean);
                    }
                });
            });
        }
    }

    private void atualizarProgresso(int atual, int total, String mensagem) {
        runOnUiThread(() -> {
            int progresso = (atual * 100) / total;
            progressBar.setProgress(progresso);
            tvProgresso.setText(String.format("Processando... %d/%d - %s", atual, total, mensagem));

            if (atual == total) {
                finalizarImportacao();
            }
        });
    }

    private void finalizarImportacao() {
        progressBar.setVisibility(View.GONE);
        tvProgresso.setVisibility(View.GONE);

        new AlertDialog.Builder(this)
                .setTitle("Importação Concluída")
                .setMessage("Importação finalizada! Volte para a lista de alunos para ver os registros.")
                .setPositiveButton("OK", (dialog, which) -> finish())
                .show();
    }

    private void baixarModeloCSV() {
        try {
            String fileName = "modelo_importacao_alunos.csv";
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs();
            }

            File modeloFile = new File(downloadsDir, fileName);

            try (FileOutputStream fos = new FileOutputStream(modeloFile);
                 OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {
                osw.write("Nome,Matrícula,Responsável,Telefone,Turma\n");
                osw.write("João Silva,20260001,Maria Silva,(11)99999-9999,1º Ano A\n");
                osw.write("Maria Santos,20260002,José Santos,(11)88888-8888,1º Ano A\n");
                osw.write("Pedro Oliveira,20260003,Ana Oliveira,(11)77777-7777,2º Ano B\n");
            }

            Toast.makeText(this, "✅ Modelo salvo em: Downloads/" + fileName, Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "❌ Erro ao gerar modelo: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private String getTurmaNome(long turmaId) {
        for (Turma t : turmasList) {
            if (t.getId() == turmaId) {
                return t.getNome() + " - " + t.getTurno();
            }
        }
        return "Turma " + turmaId;
    }

    private String getFileName(Uri uri) {
        String fileName = "arquivo.csv";
        try {
            String[] projection = {android.provider.OpenableColumns.DISPLAY_NAME};
            android.database.Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                fileName = cursor.getString(0);
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileName;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class AlunoPreview {
        public String nome;
        public String matricula;
        public String responsavel;
        public String telefone;
        public String turmaNome;

        AlunoPreview(String nome, String matricula, String responsavel, String telefone, String turmaNome) {
            this.nome = nome;
            this.matricula = matricula;
            this.responsavel = responsavel != null ? responsavel : "";
            this.telefone = telefone != null ? telefone : "";
            this.turmaNome = turmaNome != null ? turmaNome : "";
        }
    }
}