package com.professor.frequenciaescolar.ui.chamada;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.professor.frequenciaescolar.R;
import com.professor.frequenciaescolar.data.entities.Aluno;
import com.professor.frequenciaescolar.data.entities.Chamada;
import com.professor.frequenciaescolar.data.entities.Matricula;
import com.professor.frequenciaescolar.data.entities.Presenca;
import com.professor.frequenciaescolar.data.entities.Turma;
import com.professor.frequenciaescolar.data.repository.FrequenciaRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ChamadaActivity extends AppCompatActivity {

    private Spinner spinnerTurma;
    private TextInputEditText etData;
    private RecyclerView rvAlunos;
    private TextView tvEmpty;
    private Button btnSalvar;
    private Button btnEditar;

    private FrequenciaRepository repository;
    private ChamadaAdapter adapter;
    private List<Turma> turmas = new ArrayList<>();
    private List<Aluno> alunos = new ArrayList<>();
    private long turmaSelecionadaId = -1;
    private Chamada chamadaExistente = null;

    private LocationManager locationManager;
    private double latitude = 0;
    private double longitude = 0;
    private static final int LOCATION_PERMISSION_REQUEST = 100;
    private Button btnMarcarTodos, btnDesmarcarTodos;
    private static final String KEY_PRESENCAS = "presencas_state";
    private static final String KEY_TURMA_ID = "turma_id_state";
    private boolean salvando = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chamada);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Inicializar views
        spinnerTurma = findViewById(R.id.spinnerTurma);
        etData = findViewById(R.id.etData);
        rvAlunos = findViewById(R.id.rvAlunos);
        tvEmpty = findViewById(R.id.tvEmpty);
        btnSalvar = findViewById(R.id.btnSalvar);
        btnEditar = findViewById(R.id.btnEditar);
        btnMarcarTodos = findViewById(R.id.btnMarcarTodos);
        btnDesmarcarTodos = findViewById(R.id.btnDesmarcarTodos);

        // Configurar data atual
        String dataAtual = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        etData.setText(dataAtual);
        etData.setFocusable(false);

        // Configurar RecyclerView
        adapter = new ChamadaAdapter();
        rvAlunos.setLayoutManager(new LinearLayoutManager(this));
        rvAlunos.setAdapter(adapter);

        // Restaurar estado salvo (rotação de tela)
        if (savedInstanceState != null) {
            turmaSelecionadaId = savedInstanceState.getLong(KEY_TURMA_ID, -1);
            long[] ids = savedInstanceState.getLongArray("presenca_ids");
            boolean[] valores = savedInstanceState.getBooleanArray("presenca_valores");
            if (ids != null && valores != null) {
                Map<Long, Boolean> presencasSalvas = new HashMap<>();
                for (int i = 0; i < ids.length; i++) {
                    presencasSalvas.put(ids[i], valores[i]);
                }
                adapter.carregarDadosExistentes(presencasSalvas, new HashMap<>());
            }
        }

        // Inicializar repository
        repository = FrequenciaRepository.getInstance(this);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Configurar clique na data para abrir DatePicker
        etData.setOnClickListener(v -> mostrarDatePicker());

        // Carregar turmas
        carregarTurmas();

        // Configurar botões
        btnSalvar.setOnClickListener(v -> salvarChamada(false));
        btnEditar.setOnClickListener(v -> salvarChamada(true));
        btnMarcarTodos.setOnClickListener(v -> marcarTodosPresentes());
        btnDesmarcarTodos.setOnClickListener(v -> desmarcarTodos());
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
                            carregarAlunos();
                            verificarChamadaExistente();
                        } else {
                            rvAlunos.setVisibility(View.GONE);
                            tvEmpty.setVisibility(View.VISIBLE);
                            tvEmpty.setText("Selecione uma turma");
                            adapter.setAlunos(new ArrayList<>());
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });
            });
        });
    }

    private void carregarAlunos() {
        repository.getAlunosMatriculadosNaTurma(turmaSelecionadaId, matriculas -> {
            if (matriculas == null || matriculas.isEmpty()) {
                runOnUiThread(() -> {
                    rvAlunos.setVisibility(View.GONE);
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText("Nenhum aluno matriculado nesta turma.");
                    adapter.setAlunos(new ArrayList<>());
                });
                return;
            }

            List<Aluno> alunosCarregados = new ArrayList<>();
            AtomicInteger contador = new AtomicInteger(0);
            int total = matriculas.size();

            for (Matricula m : matriculas) {
                repository.getAlunoById(m.getAlunoId(), aluno -> {
                    if (aluno != null && "ativo".equals(aluno.getStatus())) {
                        synchronized (alunosCarregados) {
                            alunosCarregados.add(aluno);
                        }
                    }
                    if (contador.incrementAndGet() == total) {
                        runOnUiThread(() -> {
                            alunos.clear();
                            alunos.addAll(alunosCarregados);
                            adapter.setAlunos(new ArrayList<>(alunos));

                            if (alunos.isEmpty()) {
                                rvAlunos.setVisibility(View.GONE);
                                tvEmpty.setVisibility(View.VISIBLE);
                                tvEmpty.setText("Nenhum aluno ativo nesta turma");
                            } else {
                                rvAlunos.setVisibility(View.VISIBLE);
                                tvEmpty.setVisibility(View.GONE);
                            }
                        });
                    }
                });
            }
        });
    }

    private void verificarChamadaExistente() {
        String data = etData.getText().toString();
        repository.getChamadaByTurmaAndData(turmaSelecionadaId, data, chamada -> {
            runOnUiThread(() -> {
                if (chamada != null) {
                    chamadaExistente = chamada;
                    btnSalvar.setVisibility(View.GONE);
                    btnEditar.setVisibility(View.VISIBLE);
                    carregarPresencasExistentes(chamada.getId());
                } else {
                    chamadaExistente = null;
                    btnSalvar.setVisibility(View.VISIBLE);
                    btnEditar.setVisibility(View.GONE);
                }
            });
        });
    }

    private void carregarPresencasExistentes(long chamadaId) {
        repository.getPresencasByChamada(chamadaId, presencas -> {
            runOnUiThread(() -> {
                Map<Long, Boolean> presencasMap = new HashMap<>();
                Map<Long, String> justificativasMap = new HashMap<>();

                for (Presenca p : presencas) {
                    presencasMap.put(p.getAlunoId(), p.isPresente());
                    if (p.getJustificativa() != null && !p.getJustificativa().isEmpty()) {
                        justificativasMap.put(p.getAlunoId(), p.getJustificativa());
                    }
                }

                adapter.carregarDadosExistentes(presencasMap, justificativasMap);
            });
        });
    }

    private void mostrarDatePicker() {
        android.app.DatePickerDialog datePickerDialog = new android.app.DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    String data = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    etData.setText(data);
                    if (turmaSelecionadaId != -1) {
                        verificarChamadaExistente();
                    }
                },
                java.util.Calendar.getInstance().get(java.util.Calendar.YEAR),
                java.util.Calendar.getInstance().get(java.util.Calendar.MONTH),
                java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void salvarChamada(boolean isEdicao) {
        // ==================== PROTEÇÃO CONTRA DUPLO CLIQUE ====================
        if (salvando) return;
        salvando = true;
        btnSalvar.setEnabled(false);
        btnEditar.setEnabled(false);

        try {
            if (turmaSelecionadaId == -1) {
                Toast.makeText(this, "Selecione uma turma", Toast.LENGTH_SHORT).show();
                resetarEstadoSalvamento();
                return;
            }

            if (adapter.getItemCount() == 0) {
                Toast.makeText(this, "Nenhum aluno para registrar", Toast.LENGTH_SHORT).show();
                resetarEstadoSalvamento();
                return;
            }

            if (!adapter.algumAlunoMarcado()) {
                Toast.makeText(this, "Marque a presença de pelo menos um aluno antes de salvar", Toast.LENGTH_LONG).show();
                resetarEstadoSalvamento();
                return;
            }

            // Verificar permissão de localização
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION_REQUEST);
                resetarEstadoSalvamento();
                return;
            }

            // Obter localização
            obterLocalizacao();

            String data = etData.getText().toString();
            String horario = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

            Map<Long, Boolean> presencas = adapter.getPresencas();
            Map<Long, String> justificativas = adapter.getJustificativas();

            if (isEdicao && chamadaExistente != null) {
                // Atualizar chamada existente
                chamadaExistente.setHorarioRegistro(horario);
                chamadaExistente.setLatitude(latitude);
                chamadaExistente.setLongitude(longitude);

                repository.updateChamada(chamadaExistente, () -> {
                    // Atualizar presenças
                    for (Aluno aluno : alunos) {
                        Boolean presente = presencas.get(aluno.getId());
                        if (presente != null) {
                            String justificativa = justificativas.get(aluno.getId());
                            repository.updatePresencaByChamadaAndAluno(chamadaExistente.getId(), aluno.getId(), presente, justificativa, null);
                        }
                    }
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Chamada atualizada com sucesso!", Toast.LENGTH_SHORT).show();
                        resetarEstadoSalvamento();
                        finish();
                    });
                });
            } else {
                // Criar nova chamada
                Chamada novaChamada = new Chamada(turmaSelecionadaId, data, horario, latitude, longitude, "manual");
                repository.insertChamada(novaChamada, chamadaId -> {
                    // Salvar presenças
                    for (Aluno aluno : alunos) {
                        Boolean presente = presencas.get(aluno.getId());
                        if (presente != null) {
                            String justificativa = justificativas.get(aluno.getId());
                            Presenca presenca = new Presenca(chamadaId, aluno.getId(), presente, justificativa != null ? justificativa : "");
                            repository.insertPresenca(presenca, null);
                        }
                    }
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Chamada salva com sucesso!", Toast.LENGTH_SHORT).show();
                        resetarEstadoSalvamento();
                        finish();
                    });
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            resetarEstadoSalvamento();
            Toast.makeText(this, "Erro ao salvar chamada: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // ==================== MÉTODO AUXILIAR PARA RESETAR ESTADO ====================
    private void resetarEstadoSalvamento() {
        salvando = false;
        btnSalvar.setEnabled(true);
        btnEditar.setEnabled(true);
    }

    private void obterLocalizacao() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            try {
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                } else {
                    Location networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (networkLocation != null) {
                        latitude = networkLocation.getLatitude();
                        longitude = networkLocation.getLongitude();
                    } else {
                        // Localização padrão se não conseguir obter
                        latitude = 0;
                        longitude = 0;
                    }
                }
            } catch (Exception e) {
                latitude = 0;
                longitude = 0;
                runOnUiThread(() ->
                        Toast.makeText(this, "Não foi possível obter localização. Chamada salva sem GPS.", Toast.LENGTH_LONG).show()
                );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                obterLocalizacao();
                Toast.makeText(this, "Permissão de localização concedida", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permissão de localização negada. A chamada será salva sem localização.", Toast.LENGTH_LONG).show();
                latitude = 0;
                longitude = 0;
            }
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

    private void marcarTodosPresentes() {
        if (alunos.isEmpty()) {
            Toast.makeText(this, "Nenhum aluno para marcar", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<Long, Boolean> presencas = new HashMap<>();
        for (Aluno aluno : alunos) {
            presencas.put(aluno.getId(), true);
            adapter.getJustificativas().put(aluno.getId(), "");
        }
        adapter.carregarDadosExistentes(presencas, new HashMap<>());
        adapter.notifyDataSetChanged();

        Toast.makeText(this, "✅ Todos os alunos marcados como presentes!", Toast.LENGTH_SHORT).show();
    }

    private void desmarcarTodos() {
        if (alunos.isEmpty()) {
            Toast.makeText(this, "Nenhum aluno para desmarcar", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<Long, Boolean> presencas = new HashMap<>();
        for (Aluno aluno : alunos) {
            presencas.put(aluno.getId(), false);
        }
        adapter.carregarDadosExistentes(presencas, adapter.getJustificativas());

        Toast.makeText(this, "❌ Todos os alunos desmarcados!", Toast.LENGTH_SHORT).show();
    }
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Salvar ID da turma selecionada
        outState.putLong(KEY_TURMA_ID, turmaSelecionadaId);
        // Salvar estado das presenças (converter Map para arrays)
        Map<Long, Boolean> presencas = adapter.getPresencas();
        long[] ids = new long[presencas.size()];
        boolean[] valores = new boolean[presencas.size()];
        int i = 0;
        for (Map.Entry<Long, Boolean> entry : presencas.entrySet()) {
            ids[i] = entry.getKey();
            valores[i] = entry.getValue();
            i++;
        }
        outState.putLongArray("presenca_ids", ids);
        outState.putBooleanArray("presenca_valores", valores);
    }
}