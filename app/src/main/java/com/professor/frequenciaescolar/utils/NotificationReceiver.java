package com.professor.frequenciaescolar.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.professor.frequenciaescolar.data.database.AppDatabase;
import com.professor.frequenciaescolar.data.entities.Aluno;
import com.professor.frequenciaescolar.data.entities.Chamada;
import com.professor.frequenciaescolar.data.entities.Feriado;
import com.professor.frequenciaescolar.data.entities.Matricula;
import com.professor.frequenciaescolar.data.entities.Presenca;
import com.professor.frequenciaescolar.data.entities.Turma;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null) {
            switch (intent.getAction()) {
                case "LEMBRETE_CHAMADA":
                    NotificationHelper notificationHelper = new NotificationHelper(context);
                    notificationHelper.notificarLembreteChamada();
                    break;

                case "VERIFICACAO_DIARIA":
                    // Disparar verificação de faltas (será implementado)
                    verificarFaltas(context);
                    break;
            }
        }
    }

    private void verificarFaltas(Context context) {
        new Thread(() -> {
            try {
                AppDatabase database = AppDatabase.getInstance(context);
                ConfiguracoesManager configManager = new ConfiguracoesManager(context);
                NotificationHelper notificationHelper = new NotificationHelper(context);

                // Buscar data atual
                String dataAtual = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

                // Buscar últimos 30 dias
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DAY_OF_MONTH, -30);
                String dataInicio = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.getTime());

                // Buscar feriados
                List<Feriado> feriados = database.feriadoDao().getFeriadosNoPeriodo(dataInicio, dataAtual);

                // Buscar chamadas do período
                List<Chamada> chamadas = database.chamadaDao().getChamadasPorPeriodo(dataInicio, dataAtual);

                // Buscar todas as turmas
                List<Turma> turmas = database.turmaDao().getAllTurmasAtivas();

                for (Turma turma : turmas) {
                    List<Matricula> matriculas = database.matriculaDao().getAlunosMatriculadosNaTurma(turma.getId());

                    for (Matricula m : matriculas) {
                        Aluno aluno = database.alunoDao().getAlunoById(m.getAlunoId());
                        if (aluno == null || !"ativo".equals(aluno.getStatus())) continue;

                        // Calcular faltas consecutivas
                        int faltasConsecutivas = 0;
                        int faltasMes = 0;
                        int totalDias = 0;

                        for (Chamada c : chamadas) {
                            Presenca p = database.presencaDao().getPresencaByChamadaAndAluno(c.getId(), aluno.getId());
                            if (p != null && !p.isPresente()) {
                                faltasConsecutivas++;
                                faltasMes++;
                            } else if (p != null && p.isPresente()) {
                                faltasConsecutivas = 0;
                            }
                            totalDias++;
                        }

                        // Verificar 3 faltas consecutivas
                        if (faltasConsecutivas >= 3) {
                            notificationHelper.notificarFaltasConsecutivas(aluno.getNome(), faltasConsecutivas, turma.getNome());
                        }

                        // Verificar 20% de ausência no mês
                        if (totalDias > 0) {
                            double percentualAusencia = (faltasMes * 100.0) / totalDias;
                            if (percentualAusencia >= 20) {
                                notificationHelper.notificarAusenciaMensal(aluno.getNome(), percentualAusencia);
                            }
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}