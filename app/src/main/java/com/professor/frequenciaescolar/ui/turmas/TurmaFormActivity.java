package com.professor.frequenciaescolar.ui.turmas;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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

        // Configurar toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Inicializar views
        etNome = findViewById(R.id.etNome);
        etTurno = findViewById(R.id.etTurno);
        etAnoLetivo = findViewById(R.id.etAnoLetivo);
        btnSalvar = findViewById(R.id.btnSalvar);

        // Inicializar repository
        repository = FrequenciaRepository.getInstance(this);

        // Verificar se é edição
        if (getIntent().hasExtra("turma_id")) {
            turmaId = getIntent().getLongExtra("turma_id", -1);
            carregarTurma();
        }

        // Configurar botão salvar
        btnSalvar.setOnClickListener(v -> salvarTurma());
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

        // Validações
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
            // Criar nova turma
            Turma novaTurma = new Turma(nome, turno, anoLetivo, true);
            repository.insertTurma(novaTurma, () -> {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Turma salva com sucesso!", Toast.LENGTH_SHORT).show();
                    finish();
                });
            });
        } else {
            // Atualizar turma existente
            turmaAtual.setNome(nome);
            turmaAtual.setTurno(turno);
            turmaAtual.setAnoLetivo(anoLetivo);
            repository.updateTurma(turmaAtual, () -> {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Turma atualizada com sucesso!", Toast.LENGTH_SHORT).show();
                    finish();
                });
            });
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