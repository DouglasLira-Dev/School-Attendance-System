package com.professor.frequenciaescolar.ui.turmas;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.professor.frequenciaescolar.R;
import com.professor.frequenciaescolar.data.entities.Turma;
import com.professor.frequenciaescolar.data.repository.FrequenciaRepository;
import com.professor.frequenciaescolar.ui.alunos.AlunoListActivity;
import com.professor.frequenciaescolar.ui.chamada.ChamadaActivity;
import com.professor.frequenciaescolar.ui.configuracoes.ConfiguracoesActivity;
import com.professor.frequenciaescolar.ui.graficos.GraficosFrequenciaActivity;
import com.professor.frequenciaescolar.ui.importar.ImportarAlunosActivity;
import com.professor.frequenciaescolar.ui.relatorios.RelatorioDashboardActivity;
import com.professor.frequenciaescolar.utils.NotificationHelper;
import com.professor.frequenciaescolar.utils.NotificationScheduler;

public class TurmaListActivity extends AppCompatActivity {

    private RecyclerView rvTurmas;
    private TextView tvEmpty;
    private TurmaAdapter adapter;
    private FrequenciaRepository repository;
    private long turmaSelecionadaId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_turma_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        rvTurmas = findViewById(R.id.rvTurmas);
        tvEmpty = findViewById(R.id.tvEmpty);

        adapter = new TurmaAdapter();
        rvTurmas.setLayoutManager(new LinearLayoutManager(this));
        rvTurmas.setAdapter(adapter);

        // Clique normal - abrir lista de alunos
        adapter.setOnItemClickListener(turma -> {
            turmaSelecionadaId = turma.getId();
            Intent intent = new Intent(TurmaListActivity.this, AlunoListActivity.class);
            intent.putExtra("turma_id", turma.getId());
            intent.putExtra("turma_nome", turma.getNome());
            startActivity(intent);
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
            // Não tem return aqui - o método é void
        });

        repository = FrequenciaRepository.getInstance(this);
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

    private void carregarTurmas() {
        repository.getAllTurmasAtivas(turmas -> {
            runOnUiThread(() -> {
                if (turmas == null || turmas.isEmpty()) {
                    rvTurmas.setVisibility(android.view.View.GONE);
                    tvEmpty.setVisibility(android.view.View.VISIBLE);
                } else {
                    rvTurmas.setVisibility(android.view.View.VISIBLE);
                    tvEmpty.setVisibility(android.view.View.GONE);
                    adapter.setTurmas(turmas);
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

    // ==================== MENU ====================

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_turma_list, menu);
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
            // Verificar se há uma turma selecionada


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
        }

        return super.onOptionsItemSelected(item);
    }
}