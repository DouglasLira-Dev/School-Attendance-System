package com.professor.frequenciaescolar.ui.configuracoes;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.app.AlarmManager;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;
import com.professor.frequenciaescolar.R;
import com.professor.frequenciaescolar.ui.feriados.GerenciarFeriadosActivity;
import com.professor.frequenciaescolar.utils.ConfiguracoesManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ConfiguracoesActivity extends AppCompatActivity {

    private CheckBox[] chkDias = new CheckBox[7];
    private TextInputEditText etDataInicio, etDataFim, etHorarioLembrete;
    private CheckBox chkDesconsiderarJustificadas;
    private Button btnSalvar, btnFeriados, btnPermissaoAlarme;

    private ConfiguracoesManager configManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracoes);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Inicializar views
        chkDias[0] = findViewById(R.id.chkDomingo);
        chkDias[1] = findViewById(R.id.chkSegunda);
        chkDias[2] = findViewById(R.id.chkTerca);
        chkDias[3] = findViewById(R.id.chkQuarta);
        chkDias[4] = findViewById(R.id.chkQuinta);
        chkDias[5] = findViewById(R.id.chkSexta);
        chkDias[6] = findViewById(R.id.chkSabado);

        etDataInicio = findViewById(R.id.etDataInicio);
        etDataFim = findViewById(R.id.etDataFim);
        etHorarioLembrete = findViewById(R.id.etHorarioLembrete);
        chkDesconsiderarJustificadas = findViewById(R.id.chkDesconsiderarJustificadas);
        btnSalvar = findViewById(R.id.btnSalvar);
        btnFeriados = findViewById(R.id.btnFeriados);


        configManager = new ConfiguracoesManager(this);

        carregarConfiguracoes();
        configurarDatePickers();
        configurarTimePicker();

        Button btnFeriados = findViewById(R.id.btnFeriados);
        btnFeriados.setOnClickListener(v -> {
            Intent intent = new Intent(ConfiguracoesActivity.this, GerenciarFeriadosActivity.class);
            startActivity(intent);
        });

        Button btnPermissaoAlarme = findViewById(R.id.btnPermissaoAlarme);
        btnPermissaoAlarme.setOnClickListener(v -> {
            AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
                startActivity(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM));
            } else {
                Toast.makeText(this, "Permissão já concedida", Toast.LENGTH_SHORT).show();
            }
        });

        btnSalvar.setOnClickListener(v -> salvarConfiguracoes());
    }

    private void carregarConfiguracoes() {
        // Carregar dias letivos
        boolean[] dias = configManager.getDiasLetivos();
        for (int i = 0; i < 7; i++) {
            chkDias[i].setChecked(dias[i]);
        }

        // Carregar período letivo
        etDataInicio.setText(configManager.getDataInicio());
        etDataFim.setText(configManager.getDataFim());
        etHorarioLembrete.setText(configManager.getHorarioLembrete());
        chkDesconsiderarJustificadas.setChecked(configManager.isDesconsiderarJustificadas());
    }

    private void salvarConfiguracoes() {
        // Salvar dias letivos
        boolean[] dias = new boolean[7];
        for (int i = 0; i < 7; i++) {
            dias[i] = chkDias[i].isChecked();
        }
        configManager.salvarDiasLetivos(dias);

        // Salvar período letivo
        configManager.setDataInicio(etDataInicio.getText().toString());
        configManager.setDataFim(etDataFim.getText().toString());
        configManager.setHorarioLembrete(etHorarioLembrete.getText().toString());
        configManager.setDesconsiderarJustificadas(chkDesconsiderarJustificadas.isChecked());

        Toast.makeText(this, "Configurações salvas com sucesso!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void configurarDatePickers() {
        etDataInicio.setOnClickListener(v -> mostrarDatePicker(etDataInicio));
        etDataFim.setOnClickListener(v -> mostrarDatePicker(etDataFim));
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

    private void configurarTimePicker() {
        etHorarioLembrete.setOnClickListener(v -> {
            String[] horaMinuto = etHorarioLembrete.getText().toString().split(":");
            int hora = Integer.parseInt(horaMinuto[0]);
            int minuto = Integer.parseInt(horaMinuto[1]);

            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    (view, hourOfDay, minute) -> {
                        String horario = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                        etHorarioLembrete.setText(horario);
                    }, hora, minuto, true);
            timePickerDialog.show();
        });
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