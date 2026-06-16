package com.professor.frequenciaescolar.ui.backup;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.professor.frequenciaescolar.R;
import com.professor.frequenciaescolar.utils.BackupManager;
import com.professor.frequenciaescolar.utils.BackupReceiver;
import com.professor.frequenciaescolar.workers.BackupWorker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BackupRestoreActivity extends AppCompatActivity {

    private Button btnBackupManual, btnRestore, btnConectarDrive;
    private SwitchCompat switchBackupAuto;
    private Spinner spinnerFrequencia;
    private TextView tvStatusDrive;
    private RecyclerView rvBackups;
    private TextView tvEmptyBackups;

    private BackupManager backupManager;
    private BackupAdapter backupAdapter;
    private GoogleSignInClient googleSignInClient;
    private Drive driveService;
    private SharedPreferences prefs;

    private static final int REQUEST_CODE_SIGN_IN = 1001;

    private final ActivityResultLauncher<Intent> restoreFileLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        confirmarRestauracao(uri);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup_restore);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        btnBackupManual = findViewById(R.id.btnBackupManual);
        btnRestore = findViewById(R.id.btnRestore);
        btnConectarDrive = findViewById(R.id.btnConectarDrive);
        switchBackupAuto = findViewById(R.id.switchBackupAuto);
        spinnerFrequencia = findViewById(R.id.spinnerFrequencia);
        tvStatusDrive = findViewById(R.id.tvStatusDrive);
        rvBackups = findViewById(R.id.rvBackups);
        tvEmptyBackups = findViewById(R.id.tvEmptyBackups);

        backupManager = new BackupManager(this);
        prefs = getSharedPreferences("backup_prefs", MODE_PRIVATE);

        // Configurar spinner de frequência
        String[] frequencias = {"Diário", "Semanal", "Mensal"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, frequencias);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFrequencia.setAdapter(adapter);

        // Carregar configurações
        carregarConfiguracoes();

        // Configurar RecyclerView
        backupAdapter = new BackupAdapter(backupManager.listarBackups(), backupFile -> {
            confirmarRestauracaoLocal(backupFile);
        });
        rvBackups.setLayoutManager(new LinearLayoutManager(this));
        rvBackups.setAdapter(backupAdapter);

        // Configurar Google Drive
        configurarGoogleDrive();

        btnBackupManual.setOnClickListener(v -> fazerBackupManual());
        btnRestore.setOnClickListener(v -> selecionarArquivoRestore());
        btnConectarDrive.setOnClickListener(v -> conectarGoogleDrive());
        switchBackupAuto.setOnCheckedChangeListener((buttonView, isChecked) -> {
            salvarConfiguracoes();
            if (isChecked) {
                agendarBackupAutomatico();
            } else {
                cancelarBackupAutomatico();
            }
        });

        atualizarListaBackups();
        // Iniciar backup automático se ativado
        configurarBackupAutomatico();
    }

    private void carregarConfiguracoes() {
        switchBackupAuto.setChecked(prefs.getBoolean("backup_auto", false));
        int posicao = prefs.getInt("frequencia_backup", 1);
        spinnerFrequencia.setSelection(posicao);

        boolean driveConectado = prefs.getBoolean("drive_conectado", false);
        if (driveConectado) {
            String email = prefs.getString("drive_email", "");
            tvStatusDrive.setText("Conectado: " + email);
            tvStatusDrive.setVisibility(View.VISIBLE);
            btnConectarDrive.setText("Desconectar");
        }
    }
    private void salvarConfiguracoes() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("backup_auto", switchBackupAuto.isChecked());
        editor.putInt("frequencia_backup", spinnerFrequencia.getSelectedItemPosition());
        editor.apply();
    }
    private void configurarBackupAutomatico() {
        SharedPreferences prefs = getSharedPreferences("backup_prefs", MODE_PRIVATE);
        boolean backupAuto = prefs.getBoolean("backup_auto", true);

        if (backupAuto) {
            agendarBackupAutomatico();
        }
    }

    private void agendarBackupAutomatico() {
        // Backup diário às 02:00 da manhã
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 2);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        Intent intent = new Intent(this, BackupReceiver.class);
        intent.setAction("BACKUP_AUTO");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(), pendingIntent);
        } else {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
        }
    }

    private void cancelarBackupAutomatico() {
        Intent intent = new Intent(this, BackupReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    private void fazerBackupManual() {
        try {
            File backupFile = backupManager.criarBackup();
            if (backupFile != null) {
                Toast.makeText(this, "Backup criado: " + backupFile.getName(), Toast.LENGTH_LONG).show();
                atualizarListaBackups();

                // Se conectado ao Drive, enviar também
                if (prefs.getBoolean("drive_conectado", false)) {
                    enviarParaDrive(backupFile);
                }
            } else {
                Toast.makeText(this, "Erro ao criar backup", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void selecionarArquivoRestore() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/octet-stream");
        restoreFileLauncher.launch(intent);
    }

    private void confirmarRestauracao(Uri uri) {
        new AlertDialog.Builder(this)
                .setTitle("Restaurar Backup")
                .setMessage("ATENÇÃO: Esta ação irá substituir todos os dados atuais.\n\nDeseja continuar?")
                .setPositiveButton("Restaurar", (dialog, which) -> restaurarBackup(uri))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void confirmarRestauracaoLocal(File backupFile) {
        new AlertDialog.Builder(this)
                .setTitle("Restaurar Backup")
                .setMessage("Restaurar backup de " + BackupManager.formatFileDate(backupFile) + "?\n\nATENÇÃO: Dados atuais serão substituídos.")
                .setPositiveButton("Restaurar", (dialog, which) -> restaurarBackupLocal(backupFile))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void restaurarBackup(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            File tempFile = new File(getCacheDir(), "temp_restore.db");
            FileOutputStream fos = new FileOutputStream(tempFile);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
            fos.close();
            inputStream.close();

            backupManager.restaurarBackup(tempFile);
            new AlertDialog.Builder(this)
                    .setTitle("Backup Restaurado")
                    .setMessage("Backup restaurado com sucesso! O app será reiniciado.")
                    .setPositiveButton("OK", (d, w) -> reiniciarApp())
                    .setCancelable(false)
                    .show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao restaurar: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void restaurarBackupLocal(File backupFile) {
        try {
            backupManager.restaurarBackup(backupFile);
            new AlertDialog.Builder(this)
                    .setTitle("Backup Restaurado")
                    .setMessage("Backup restaurado com sucesso! O app será reiniciado.")
                    .setPositiveButton("OK", (d, w) -> reiniciarApp())
                    .setCancelable(false)
                    .show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao restaurar: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void configurarGoogleDrive() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void conectarGoogleDrive() {
        if (prefs.getBoolean("drive_conectado", false)) {
            // Desconectar
            googleSignInClient.signOut().addOnCompleteListener(task -> {
                prefs.edit().putBoolean("drive_conectado", false).remove("drive_email").apply();
                tvStatusDrive.setVisibility(View.GONE);
                btnConectarDrive.setText("Conectar ao Google Drive");
                Toast.makeText(this, "Desconectado do Google Drive", Toast.LENGTH_SHORT).show();
            });
        } else {
            // Conectar
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, REQUEST_CODE_SIGN_IN);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SIGN_IN && resultCode == RESULT_OK) {
            GoogleSignIn.getSignedInAccountFromIntent(data).addOnSuccessListener(account -> {
                GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                        this, Collections.singleton(DriveScopes.DRIVE_FILE));
                credential.setSelectedAccount(account.getAccount());
                driveService = new Drive.Builder(AndroidHttp.newCompatibleTransport(),
                        new GsonFactory(), credential)
                        .setApplicationName("FrequenciaEscolar")
                        .build();

                prefs.edit().putBoolean("drive_conectado", true)
                        .putString("drive_email", account.getEmail()).apply();
                tvStatusDrive.setText("Conectado: " + account.getEmail());
                tvStatusDrive.setVisibility(View.VISIBLE);
                btnConectarDrive.setText("Desconectar");
                Toast.makeText(this, "Conectado ao Google Drive", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void enviarParaDrive(File backupFile) {
        if (driveService == null) {
            Toast.makeText(this, "Conecte-se ao Google Drive primeiro", Toast.LENGTH_SHORT).show();
            return;
        }

        // Mostrar progresso
        Toast.makeText(this, "Enviando para o Google Drive...", Toast.LENGTH_SHORT).show();
        btnBackupManual.setEnabled(false);

        // MOVER para thread de background:
        new Thread(() -> {
            try {
                com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
                fileMetadata.setName(backupFile.getName());
                fileMetadata.setParents(Collections.singletonList("root"));

                FileContent mediaContent = new FileContent("application/octet-stream", backupFile);

                driveService.files()
                        .create(fileMetadata, mediaContent)
                        .setFields("id")
                        .execute();

                runOnUiThread(() -> {
                    btnBackupManual.setEnabled(true);
                    Toast.makeText(this, "Backup enviado para o Google Drive!", Toast.LENGTH_LONG).show();
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    btnBackupManual.setEnabled(true);
                    Toast.makeText(this, "Erro ao enviar para Drive: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void atualizarListaBackups() {
        List<File> backups = backupManager.listarBackups();
        backupAdapter.updateList(backups);
        if (backups.isEmpty()) {
            tvEmptyBackups.setVisibility(View.VISIBLE);
            rvBackups.setVisibility(View.GONE);
        } else {
            tvEmptyBackups.setVisibility(View.GONE);
            rvBackups.setVisibility(View.VISIBLE);
        }
    }
    private void reiniciarApp() {
        Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        atualizarListaBackups();
    }
}