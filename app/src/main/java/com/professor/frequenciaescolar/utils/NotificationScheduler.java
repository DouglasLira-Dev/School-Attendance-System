package com.professor.frequenciaescolar.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.Calendar;

public class NotificationScheduler {

    private static final int LEMBRETE_CHAMADA_REQUEST_CODE = 100;
    private static final int VERIFICACAO_DIARIA_REQUEST_CODE = 101;

    public static void agendarLembreteChamada(Context context) {
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager == null) return;

            // Configurar para às 7:30 da manhã
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 7);
            calendar.set(Calendar.MINUTE, 30);
            calendar.set(Calendar.SECOND, 0);
            // Se já passou do horário, agendar para amanhã
            if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }

            Intent intent = new Intent(context, NotificationReceiver.class);
            intent.setAction("LEMBRETE_CHAMADA");
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    LEMBRETE_CHAMADA_REQUEST_CODE,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            agendarComSeguranca(alarmManager, calendar.getTimeInMillis(), pendingIntent);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public static void agendarVerificacaoDiaria(Context context) {
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager == null) return;

            // Configurar para às 20:00 (8 da noite) para verificar faltas do dia
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 20);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);

            if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }

            Intent intent = new Intent(context, NotificationReceiver.class);
            intent.setAction("VERIFICACAO_DIARIA");
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    VERIFICACAO_DIARIA_REQUEST_CODE,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            agendarComSeguranca(alarmManager, calendar.getTimeInMillis(), pendingIntent);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public static void cancelarLembreteChamada(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                LEMBRETE_CHAMADA_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(pendingIntent);
    }

    private static void agendarComSeguranca(AlarmManager alarmManager, long triggerAtMillis, PendingIntent pendingIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+ exige checar a permissão antes de agendar exato
            if (alarmManager.canScheduleExactAlarms()) {
                try {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
                } catch (SecurityException e) {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
                }
            } else {
                // Sem permissão: fallback não-exato (o app continua funcionando)
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            }
        } else {
            // Abaixo do Android 12 não há restrição de permissão para alarme exato
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
        }
    }
}