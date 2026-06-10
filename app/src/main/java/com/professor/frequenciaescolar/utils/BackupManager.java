package com.professor.frequenciaescolar.utils;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import com.professor.frequenciaescolar.data.database.AppDatabase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BackupManager {

    private static final String BACKUP_FOLDER = "FrequenciaEscolarBackups";
    private static final String DATABASE_NAME = "frequencia_escolar.db";

    private Context context;

    public BackupManager(Context context) {
        this.context = context;
    }

    // Criar backup manual
    public File criarBackup() throws IOException {
        File backupDir = getBackupDirectory();
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String backupFileName = "backup_" + timestamp + ".db";
        File backupFile = new File(backupDir, backupFileName);

        File currentDb = context.getDatabasePath(DATABASE_NAME);

        if (currentDb.exists()) {
            FileChannel source = new FileInputStream(currentDb).getChannel();
            FileChannel destination = new FileOutputStream(backupFile).getChannel();
            destination.transferFrom(source, 0, source.size());
            source.close();
            destination.close();
            return backupFile;
        }

        return null;
    }

    // Restaurar backup
    public boolean restaurarBackup(File backupFile) throws IOException {
        File currentDb = context.getDatabasePath(DATABASE_NAME);

        // Fechar conexão com o banco
        AppDatabase.destroyInstance();

        // Copiar backup para o local do banco
        FileChannel source = new FileInputStream(backupFile).getChannel();
        FileChannel destination = new FileOutputStream(currentDb).getChannel();
        destination.transferFrom(source, 0, source.size());
        source.close();
        destination.close();

        return true;
    }

    // Listar backups existentes
    public List<File> listarBackups() {
        File backupDir = getBackupDirectory();
        if (!backupDir.exists()) {
            return new ArrayList<>();
        }

        File[] files = backupDir.listFiles((dir, name) -> name.endsWith(".db"));
        if (files != null) {
            Arrays.sort(files, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
            return Arrays.asList(files);
        }

        return new ArrayList<>();
    }

    // Excluir backup
    public boolean excluirBackup(File backupFile) {
        return backupFile.delete();
    }

    // Obter diretório de backups
    public File getBackupDirectory() {
        File backupDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS), BACKUP_FOLDER);

        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }

        return backupDir;
    }

    // Formatar tamanho do arquivo
    public static String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.1f KB", size / 1024.0);
        } else {
            return String.format("%.1f MB", size / (1024.0 * 1024.0));
        }
    }

    // Formatar data do arquivo
    public static String formatFileDate(File file) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date(file.lastModified()));
    }
}