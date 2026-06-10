package com.professor.frequenciaescolar.workers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.professor.frequenciaescolar.utils.BackupManager;

import java.io.File;

public class BackupWorker extends Worker {

    public BackupWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            BackupManager backupManager = new BackupManager(getApplicationContext());
            File backupFile = backupManager.criarBackup();

            if (backupFile != null) {
                Log.d("BackupWorker", "Backup automático criado: " + backupFile.getName());
                return Result.success();
            } else {
                return Result.failure();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.failure();
        }
    }
}