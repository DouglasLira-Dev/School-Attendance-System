package com.professor.frequenciaescolar.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.professor.frequenciaescolar.ui.backup.BackupRestoreActivity;

import java.io.File;

public class BackupReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals("BACKUP_AUTO")) {
            realizarBackupAutomatico(context);
        }
    }

    private void realizarBackupAutomatico(Context context) {
        try {
            BackupManager backupManager = new BackupManager(context);
            File backupFile = backupManager.criarBackup();

            if (backupFile != null) {
                Toast.makeText(context, "✅ Backup automático realizado: " + backupFile.getName(), Toast.LENGTH_LONG).show();

                // Tentar enviar para Google Drive automaticamente
                BackupRestoreActivity backupRestore = new BackupRestoreActivity();
                // Nota: Isso depende de como você implementou o Drive
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}