package com.professor.frequenciaescolar.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.professor.frequenciaescolar.ui.chamada.ChamadaActivity;
import com.professor.frequenciaescolar.ui.relatorios.RelatorioDashboardActivity;

public class NotificationHelper {

    private static final String CHANNEL_ID = "frequencia_escolar_channel";
    private static final String CHANNEL_NAME = "Frequência Escolar";
    private static final String CHANNEL_DESCRIPTION = "Notificações do sistema de frequência";

    private Context context;
    private NotificationManager notificationManager;

    public NotificationHelper(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        criarCanalNotificacao();
    }

    private void criarCanalNotificacao() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESCRIPTION);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 500, 200, 500});
            notificationManager.createNotificationChannel(channel);
        }
    }

    // Lembrete para fazer a chamada
    public void notificarLembreteChamada() {
        Intent intent = new Intent(context, ChamadaActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_menu_edit)
                .setContentTitle("📋 Lembrete de Chamada")
                .setContentText("Não se esqueça de registrar a chamada de hoje!")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Registre a presença dos alunos para manter o controle de frequência atualizado."))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        notificationManager.notify(1, builder.build());
    }

    // Alerta de faltas consecutivas
    public void notificarFaltasConsecutivas(String alunoNome, int faltasConsecutivas, String nome) {
        Intent intent = new Intent(context, RelatorioDashboardActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 2, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_notify_error)
                .setContentTitle("⚠️ Atenção: Faltas Consecutivas")
                .setContentText(String.format("%s atingiu %d falta(s) consecutiva(s)!", alunoNome, faltasConsecutivas))
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(String.format("O aluno %s está com %d faltas consecutivas. Verifique a situação.", alunoNome, faltasConsecutivas)))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        notificationManager.notify(2, builder.build());
    }

    // Alerta de ausência mensal
    public void notificarAusenciaMensal(String alunoNome, double percentualAusencia) {
        Intent intent = new Intent(context, RelatorioDashboardActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 3, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_notify_error)
                .setContentTitle("📊 Alerta de Ausência Mensal")
                .setContentText(String.format("%s está com %.1f%% de ausência neste mês!", alunoNome, percentualAusencia))
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(String.format("O aluno %s atingiu %.1f%% de ausência no mês. Considere entrar em contato com o responsável.", alunoNome, percentualAusencia)))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        notificationManager.notify(3, builder.build());
    }

    // Alerta de frequência baixa
    public void notificarFrequenciaBaixa(String alunoNome, double percentual, int faltas, int totalAulas) {
        Intent intent = new Intent(context, RelatorioDashboardActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 4, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_notify_error)
                .setContentTitle("⚠️ Frequência Baixa Detectada")
                .setContentText(String.format("%s está com %.1f%% de frequência!", alunoNome, percentual))
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(String.format("O aluno %s tem %.1f%% de frequência (%d faltas em %d aulas). O mínimo recomendado é 80%%.",
                                alunoNome, percentual, faltas, totalAulas)))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        notificationManager.notify(4, builder.build());
    }

    // Resumo semanal
    public void notificarResumoSemanal(String mensagem) {
        Intent intent = new Intent(context, RelatorioDashboardActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 5, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_notify_sync)
                .setContentTitle("📈 Resumo Semanal de Frequência")
                .setContentText("Confira o resumo da semana")
                .setStyle(new NotificationCompat.BigTextStyle().bigText(mensagem))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        notificationManager.notify(5, builder.build());
    }
}