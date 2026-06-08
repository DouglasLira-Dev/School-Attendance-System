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

        // Configurar data atual
        String dataAtual = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        etData.setText(dataAtual);
        etData.setFocusable(false);

        // Configurar RecyclerView
        adapter = new ChamadaAdapter();
        rvAlunos.setLayoutManager(new LinearLayoutManager(this));
        rvAlunos.setAdapter(adapter);

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
            runOnUiThread(() -> {
                if (matriculas == null || matriculas.isEmpty()) {
                    rvAlunos.setVisibility(View.GONE);
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText("Nenhum aluno matriculado nesta turma");
                    adapter.setAlunos(new ArrayList<>());
                    return;
                }

                alunos.clear();
                List<Aluno> alunosTemp = new ArrayList<>();
                AtomicInteger contador = new AtomicInteger(0);
                int total = matriculas.size();

                for (Matricula m : matriculas) {
                    repository.getAlunoById(m.getAlunoId(), aluno -> {
                        runOnUiThread(() -> {
                            if (aluno != null && "ativo".equals(aluno.getStatus())) {
                                alunosTemp.add(aluno);
                            }
                            contador.incrementAndGet();

                            if (contador.get() == total) {
                                alunos.clear();
                                alunos.addAll(alunosTemp);
                                adapter.setAlunos(new ArrayList<>(alunos));

                                if (alunos.isEmpty()) {
                                    rvAlunos.setVisibility(View.GONE);
                                    tvEmpty.setVisibility(View.VISIBLE);
                                    tvEmpty.setText("Nenhum aluno ativo nesta turma");
                                } else {
                                    rvAlunos.setVisibility(View.VISIBLE);
                                    tvEmpty.setVisibility(View.GONE);
                                }
                            }
                        });
                    });
                }
            });
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
        if (turmaSelecionadaId == -1) {
            Toast.makeText(this, "Selecione uma turma", Toast.LENGTH_SHORT).show();
            return;
        }

        if (adapter.getItemCount() == 0) {
            Toast.makeText(this, "Nenhum aluno para registrar", Toast.LENGTH_SHORT).show();
            return;
        }

        // Verificar permissão de localização
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
            // Chamar novamente após permissão
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
                    finish();
                });
            });
        }
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
                        latitude = -23.5505;
                        longitude = -46.6333;
                    }
                }
            } catch (Exception e) {
                latitude = -23.5505;
                longitude = -46.6333;
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
}