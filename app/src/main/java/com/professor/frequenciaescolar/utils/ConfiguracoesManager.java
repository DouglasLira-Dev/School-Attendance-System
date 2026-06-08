package com.professor.frequenciaescolar.utils;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.Calendar;

public class ConfiguracoesManager {

    private static final String PREF_NAME = "configuracoes_prefs";

    // Chaves
    private static final String KEY_DIAS_LETIVOS = "dias_letivos";
    private static final String KEY_DATA_INICIO = "data_inicio";
    private static final String KEY_DATA_FIM = "data_fim";
    private static final String KEY_HORARIO_LEMBRETE = "horario_lembrete";
    private static final String KEY_DESCONSIDERAR_JUSTIFICADAS = "desconsiderar_justificadas";

    private SharedPreferences preferences;

    public ConfiguracoesManager(Context context) {
        this.preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // Salvar dias letivos (bitmask)
    public void salvarDiasLetivos(boolean[] dias) {
        int mask = 0;
        for (int i = 0; i < dias.length; i++) {
            if (dias[i]) {
                mask |= (1 << i);
            }
        }
        preferences.edit().putInt(KEY_DIAS_LETIVOS, mask).apply();
    }

    // Recuperar dias letivos
    public boolean[] getDiasLetivos() {
        boolean[] dias = new boolean[7]; // 0=Domingo, 6=Sábado
        int mask = preferences.getInt(KEY_DIAS_LETIVOS, 0);

        // Padrão: Segunda a Sexta (dias 1 a 5)
        if (mask == 0) {
            dias[1] = true; // Segunda
            dias[2] = true; // Terça
            dias[3] = true; // Quarta
            dias[4] = true; // Quinta
            dias[5] = true; // Sexta
        } else {
            for (int i = 0; i < 7; i++) {
                dias[i] = (mask & (1 << i)) != 0;
            }
        }
        return dias;
    }

    // Verificar se um dia é letivo
    public boolean isDiaLetivo(Calendar data) {
        int diaSemana = data.get(Calendar.DAY_OF_WEEK) - 1; // 0=Domingo
        boolean[] dias = getDiasLetivos();
        return dias[diaSemana];
    }

    // Data início do período letivo
    public void setDataInicio(String data) {
        preferences.edit().putString(KEY_DATA_INICIO, data).apply();
    }

    public String getDataInicio() {
        return preferences.getString(KEY_DATA_INICIO, "01/02/2026");
    }

    // Data fim do período letivo
    public void setDataFim(String data) {
        preferences.edit().putString(KEY_DATA_FIM, data).apply();
    }

    public String getDataFim() {
        return preferences.getString(KEY_DATA_FIM, "15/12/2026");
    }

    // Horário do lembrete
    public void setHorarioLembrete(String horario) {
        preferences.edit().putString(KEY_HORARIO_LEMBRETE, horario).apply();
    }

    public String getHorarioLembrete() {
        return preferences.getString(KEY_HORARIO_LEMBRETE, "07:30");
    }

    // Desconsiderar faltas justificadas
    public void setDesconsiderarJustificadas(boolean desconsiderar) {
        preferences.edit().putBoolean(KEY_DESCONSIDERAR_JUSTIFICADAS, desconsiderar).apply();
    }

    public boolean isDesconsiderarJustificadas() {
        return preferences.getBoolean(KEY_DESCONSIDERAR_JUSTIFICADAS, true);
    }
}