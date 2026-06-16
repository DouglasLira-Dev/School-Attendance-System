package com.professor.frequenciaescolar.ui.turmas;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.professor.frequenciaescolar.R;
import com.professor.frequenciaescolar.data.entities.Turma;
import com.professor.frequenciaescolar.data.repository.FrequenciaRepository;

public class TurmaFormActivity extends AppCompatActivity {

    private EditText etNome;
    private EditText etTurno;
    private EditText etAnoLetivo;
    private Button btnSalvar;

    private FrequenciaRepository repository;
    private Turma turmaAtual;
    private long turmaId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_turma_form);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        etNome = findViewById(R.id.etNome);
        etTurno = findViewById(R.id.etTurno);
        etAnoLetivo = findViewById(R.id.etAnoLetivo);
        btnSalvar = findViewById(R.id.btnSalvar);

        repository = FrequenciaRepository.getInstance(this);

        if (getIntent().hasExtra("turma_id")) {
            turmaId = getIntent().getLongExtra("turma_id", -1);
            carregarTurma();
        }

        btnSalvar.setOnClickListener(v -> salvarTurma());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                confirmarSaida();
            }
        });

    }

    private void carregarTurma() {
        repository.getTurmaById(turmaId, turma -> {
            runOnUiThread(() -> {
                if (turma != null) {
                    turmaAtual = turma;
                    etNome.setText(turma.getNome());
                    etTurno.setText(turma.getTurno());
                    etAnoLetivo.setText(String.valueOf(turma.getAnoLetivo()));
                }
            });
        });
    }

    private void salvarTurma() {
        String nome = etNome.getText().toString().trim();
        String turno = etTurno.getText().toString().trim();
        String anoLetivoStr = etAnoLetivo.getText().toString().trim();

        if (nome.isEmpty()) {
            etNome.setError("Nome é obrigatório");
            return;
        }

        if (turno.isEmpty()) {
            etTurno.setError("Turno é obrigatório");
            return;
        }

        if (anoLetivoStr.isEmpty()) {
            etAnoLetivo.setError("Ano letivo é obrigatório");
            return;
        }

        int anoLetivo;
        try {
            anoLetivo = Integer.parseInt(anoLetivoStr);
        } catch (NumberFormatException e) {
            etAnoLetivo.setError("Ano inválido");
            return;
        }

        if (turmaAtual == null) {
            repository.getTurmaPorNomeETurno(nome, turno, turmaExistente -> {
                runOnUiThread(() -> {
                    if (turmaExistente != null) {
                        etNome.setError("⚠️ Já existe uma turma com este nome e turno");
                        etNome.requestFocus();
                        return;
                    }

                    Turma novaTurma = new Turma(nome, turno, anoLetivo, true);
                    repository.insertTurma(novaTurma, () -> {
                        runOnUiThread(() -> {
                            Toast.makeText(this, "✅ Turma salva com sucesso!", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    });
                });
            });
            return;
        }

        if (!turmaAtual.getNome().equals(nome) || !turmaAtual.getTurno().equals(turno)) {
            repository.getTurmaPorNomeETurno(nome, turno, turmaExistente -> {
                runOnUiThread(() -> {
                    if (turmaExistente != null && turmaExistente.getId() != turmaAtual.getId()) {
                        etNome.setError("⚠️ Já existe uma turma com este nome e turno");
                        etNome.requestFocus();
                        return;
                    }
                    atualizarTurma(nome, turno, anoLetivo);
                });
            });
        } else {
            atualizarTurma(nome, turno, anoLetivo);
        }
    }

    private void atualizarTurma(String nome, String turno, int anoLetivo) {
        turmaAtual.setNome(nome);
        turmaAtual.setTurno(turno);
        turmaAtual.setAnoLetivo(anoLetivo);

        repository.updateTurma(turmaAtual, () -> {
            runOnUiThread(() -> {
                Toast.makeText(this, "✅ Turma atualizada com sucesso!", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    // ==================== MÉTODO PARA VERIFICAR SE HÁ DADOS PREENCHIDOS ====================
    private boolean temDadosPreenchidos() {
        return !etNome.getText().toString().trim().isEmpty()
                || !etTurno.getText().toString().trim().isEmpty()
                || !etAnoLetivo.getText().toString().trim().isEmpty();
    }

    // ==================== CONFIRMAR SAÍDA ====================
    private void confirmarSaida() {
        if (temDadosPreenchidos()) {
            new AlertDialog.Builder(this)
                    .setTitle("Descartar alterações?")
                    .setMessage("Os dados preenchidos serão perdidos.")
                    .setPositiveButton("Descartar", (d, w) -> finish())
                    .setNegativeButton("Continuar editando", null)
                    .show();
        } else {
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            confirmarSaida();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}