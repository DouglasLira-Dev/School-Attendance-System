package com.professor.frequenciaescolar.ui.importar;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.professor.frequenciaescolar.R;
import com.professor.frequenciaescolar.data.entities.Aluno;
import com.professor.frequenciaescolar.data.entities.Matricula;
import com.professor.frequenciaescolar.data.entities.Turma;
import com.professor.frequenciaescolar.data.repository.FrequenciaRepository;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
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

    private PreviewAdapter previewAdapter;
    private FrequenciaRepository repository;

    private List<AlunoPreview> previewList = new ArrayList<>();
    private List<String> erros = new ArrayList<>();
    private String arquivoSelecionadoNome = "";
    private Uri arquivoUri;

    private final ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    arquivoUri = result.getData().getData();
                    if (arquivoUri != null) {
                        arquivoSelecionadoNome = getFileName(arquivoUri);
                        tvArquivoSelecionado.setText("Arquivo: " + arquivoSelecionadoNome);
                        processarArquivo();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_importar_alunos);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

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

        repository = FrequenciaRepository.getInstance(this);

        previewAdapter = new PreviewAdapter(previewList);
        rvPreview.setLayoutManager(new LinearLayoutManager(this));
        rvPreview.setAdapter(previewAdapter);

        btnSelecionarArquivo.setOnClickListener(v -> selecionarArquivo());
        btnImportar.setOnClickListener(v -> confirmarImportacao());
        btnBaixarModelo.setOnClickListener(v -> baixarModeloCSV());
    }

    private void selecionarArquivo() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv");
        filePickerLauncher.launch(intent);
    }

    private void processarArquivo() {
        // ==================== TRY-WITH-RESOURCES ====================
        try (InputStream inputStream = getContentResolver().openInputStream(arquivoUri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            previewList.clear();
            erros.clear();
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (lineNumber == 1 && line.toLowerCase().contains("nome")) {
                    continue;
                }

                String[] campos = line.split(",");
                if (campos.length >= 5) {
                    String nome = campos[0].trim();
                    String matricula = campos[1].trim();
                    String responsavel = campos[2].trim();
                    String telefone = campos[3].trim();
                    String turmaNome = campos[4].trim();

                    if (nome.isEmpty()) {
                        erros.add("Linha " + lineNumber + ": Nome vazio");
                        continue;
                    }

                    if (matricula.isEmpty()) {
                        erros.add("Linha " + lineNumber + ": Matrícula vazia");
                        continue;
                    }

                    previewList.add(new AlunoPreview(nome, matricula, responsavel, telefone, turmaNome));
                } else {
                    erros.add("Linha " + lineNumber + ": Formato inválido (5 colunas esperadas)");
                }
            }

            tvResumo.setText(String.format("Total de alunos encontrados: %d", previewList.size()));
            previewAdapter.notifyDataSetChanged();

            if (!erros.isEmpty()) {
                tvErros.setVisibility(View.VISIBLE);
                tvErros.setText(String.format("⚠️ %d erro(s) encontrados:\n%s", erros.size(), String.join("\n", erros)));
            } else {
                tvErros.setVisibility(View.GONE);
            }

            cardPreview.setVisibility(View.VISIBLE);
            btnImportar.setVisibility(View.VISIBLE);
            btnImportar.setEnabled(!previewList.isEmpty());

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao ler arquivo: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void confirmarImportacao() {
        new AlertDialog.Builder(this)
                .setTitle("Confirmar Importação")
                .setMessage(String.format("Deseja importar %d alunos?\n\nTurmas que não existirem serão ignoradas.", previewList.size()))
                .setPositiveButton("Importar", (dialog, which) -> realizarImportacao())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void realizarImportacao() {
        progressBar.setVisibility(View.VISIBLE);
        tvProgresso.setVisibility(View.VISIBLE);
        btnImportar.setEnabled(false);
        btnSelecionarArquivo.setEnabled(false);

        repository.getAllTurmasAtivas(turmas -> {
            Map<String, Long> turmaMap = new HashMap<>();
            for (Turma t : turmas) {
                turmaMap.put(t.getNome(), t.getId());
            }

            String dataAtual = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            AtomicInteger importados = new AtomicInteger(0);
            AtomicInteger ignorados = new AtomicInteger(0);
            AtomicInteger processados = new AtomicInteger(0);

            for (AlunoPreview preview : previewList) {
                if (!turmaMap.containsKey(preview.turmaNome)) {
                    ignorados.incrementAndGet();
                    processados.incrementAndGet();
                    atualizarProgresso(processados.get(), previewList.size(),
                            "Turma não encontrada: " + preview.turmaNome);
                    continue;
                }

                repository.getAlunoByMatricula(preview.matricula, alunoExistente -> {
                    if (alunoExistente != null) {
                        ignorados.incrementAndGet();
                        processados.incrementAndGet();
                        atualizarProgresso(processados.get(), previewList.size(),
                                "Matrícula duplicada: " + preview.matricula);
                    } else {
                        Aluno novoAluno = new Aluno(preview.nome, preview.matricula,
                                preview.responsavel, preview.telefone, "ativo", true);
                        repository.insertAluno(novoAluno, () -> {
                            repository.getAlunoByMatricula(preview.matricula, aluno -> {
                                if (aluno != null) {
                                    long turmaId = turmaMap.get(preview.turmaNome);
                                    Matricula matriculaObj = new Matricula(aluno.getId(), turmaId, dataAtual, "ativa");
                                    repository.insertMatricula(matriculaObj, () -> {
                                        importados.incrementAndGet();
                                        processados.incrementAndGet();
                                        atualizarProgresso(processados.get(), previewList.size(),
                                                "Importado: " + preview.nome);
                                    });
                                }
                            });
                        });
                    }
                });
            }
        });
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
            FileOutputStream fos = openFileOutput(fileName, MODE_PRIVATE);
            OutputStreamWriter osw = new OutputStreamWriter(fos);

            osw.write("Nome,Matrícula,Responsável,Telefone,Turma\n");
            osw.write("João Silva,20260001,Maria Silva,(11)99999-9999,1º Ano A\n");
            osw.write("Maria Santos,20260002,José Santos,(11)88888-8888,1º Ano A\n");
            osw.write("Pedro Oliveira,20260003,Ana Oliveira,(11)77777-7777,2º Ano B\n");

            osw.flush();
            osw.close();
            fos.close();

            Toast.makeText(this, "Modelo CSV salvo!\nLocalize o arquivo na pasta interna do app.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao gerar modelo: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
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
            this.responsavel = responsavel;
            this.telefone = telefone;
            this.turmaNome = turmaNome;
        }
    }
}