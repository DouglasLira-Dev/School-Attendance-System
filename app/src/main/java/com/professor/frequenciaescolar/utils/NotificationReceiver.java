package com.professor.frequenciaescolar.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

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
        // TODO: Implementar verificação de faltas consecutivas e ausência mensal
        // Esta parte será integrada com o Repository
        NotificationHelper notificationHelper = new NotificationHelper(context);

        // Exemplo de verificação (a implementação completa virá na integração)
        // notificationHelper.notificarFaltasConsecutivas("João Silva", 3);
        // notificationHelper.notificarAusenciaMensal("Maria Santos", 25.5);
    }
}