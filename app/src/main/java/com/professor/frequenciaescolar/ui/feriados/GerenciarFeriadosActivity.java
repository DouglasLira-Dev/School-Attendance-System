package com.professor.frequenciaescolar.ui.feriados;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.professor.frequenciaescolar.R;
import com.professor.frequenciaescolar.data.database.AppDatabase;
import com.professor.frequenciaescolar.data.entities.Feriado;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class GerenciarFeriadosActivity extends AppCompatActivity {

    private TextInputEditText etData;
    private EditText etNome;
    private CheckBox chkRecorrente;
    private Button btnAdicionar;
    private RecyclerView rvFeriados;
    private TextView tvEmpty;

    private AppDatabase database;
    private List<Feriado> feriados = new ArrayList<>();
    private FeriadoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gerenciar_feriados);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        etData = findViewById(R.id.etData);
        etNome = findViewById(R.id.etNome);
        chkRecorrente = findViewById(R.id.chkRecorrente);
        btnAdicionar = findViewById(R.id.btnAdicionar);
        rvFeriados = findViewById(R.id.rvFeriados);
        tvEmpty = findViewById(R.id.tvEmpty);

        database = AppDatabase.getInstance(this);

        etData.setOnClickListener(v -> mostrarDatePicker());

        adapter = new FeriadoAdapter(feriados, feriado -> {
            confirmarExclusao(feriado);
        });
        rvFeriados.setLayoutManager(new LinearLayoutManager(this));
        rvFeriados.setAdapter(adapter);

        btnAdicionar.setOnClickListener(v -> adicionarFeriado());

        carregarFeriados();
    }

    private void mostrarDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    String data = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year);
                    etData.setText(data);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void adicionarFeriado() {
        String dataStr = etData.getText().toString().trim();
        String nome = etNome.getText().toString().trim();
        boolean recorrente = chkRecorrente.isChecked();

        if (dataStr.isEmpty()) {
            etData.setError("Selecione uma data");
            return;
        }

        if (nome.isEmpty()) {
            etNome.setError("Digite o nome do feriado");
            return;
        }

        // Converter data para formato yyyy-MM-dd
        String dataConvertida = converterData(dataStr);

        Feriado feriado = new Feriado(dataConvertida, nome, recorrente);

        new Thread(() -> {
            if (database.feriadoDao().getFeriadoByData(dataConvertida) != null) {
                runOnUiThread(() -> Toast.makeText(this, "Feriado já cadastrado!", Toast.LENGTH_SHORT).show());
                return;
            }

            database.feriadoDao().insert(feriado);
            runOnUiThread(() -> {
                Toast.makeText(this, "Feriado adicionado!", Toast.LENGTH_SHORT).show();
                etData.setText("");
                etNome.setText("");
                chkRecorrente.setChecked(false);
                carregarFeriados();
            });
        }).start();
    }

    private void confirmarExclusao(Feriado feriado) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir Feriado")
                .setMessage("Excluir \"" + feriado.getNome() + "\"?")
                .setPositiveButton("Excluir", (dialog, which) -> {
                    new Thread(() -> {
                        database.feriadoDao().delete(feriado);
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Feriado removido!", Toast.LENGTH_SHORT).show();
                            carregarFeriados();
                        });
                    }).start();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void carregarFeriados() {
        new Thread(() -> {
            List<Feriado> lista = database.feriadoDao().getAllFeriados();
            runOnUiThread(() -> {
                feriados.clear();
                feriados.addAll(lista);
                adapter.notifyDataSetChanged();

                if (feriados.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    rvFeriados.setVisibility(View.GONE);
                } else {
                    tvEmpty.setVisibility(View.GONE);
                    rvFeriados.setVisibility(View.VISIBLE);
                }
            });
        }).start();
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}