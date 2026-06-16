package com.professor.frequenciaescolar.ui.auth;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.annotation.NonNull;

import com.professor.frequenciaescolar.R;
import com.professor.frequenciaescolar.ui.turmas.TurmaListActivity;
import com.professor.frequenciaescolar.utils.SenhaManager;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    private EditText etSenha;
    private Button btnEntrar;
    private Button btnBiometria;
    private TextView tvMensagem;

    private SenhaManager senhaManager;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private TextView tvEsqueciSenha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etSenha = findViewById(R.id.etSenha);
        btnEntrar = findViewById(R.id.btnEntrar);
        btnBiometria = findViewById(R.id.btnBiometria);
        tvMensagem = findViewById(R.id.tvMensagem);

        senhaManager = new SenhaManager(this);

        // Verificar se é primeiro acesso
        if (senhaManager.isPrimeiroAcesso()) {
            startActivity(new Intent(this, ConfigSenhaActivity.class));
            finish();
            return;
        }

        // Configurar biometria
        configurarBiometria();

        // Verificar se biometria está disponível
        verificarDisponibilidadeBiometria();

        tvEsqueciSenha = findViewById(R.id.tvEsqueciSenha);
        tvEsqueciSenha.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, EsqueciSenhaActivity.class);
            startActivity(intent);
        });

        btnEntrar.setOnClickListener(v -> autenticarSenha());
        btnBiometria.setOnClickListener(v -> autenticarBiometria());
    }

    private void configurarBiometria() {
        Executor executor = Executors.newSingleThreadExecutor();

        biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                runOnUiThread(() -> {
                    Toast.makeText(LoginActivity.this, "Autenticado com sucesso!", Toast.LENGTH_SHORT).show();
                    abrirTurmas();
                });
            }

            @Override
            public void onAuthenticationFailed() {
                runOnUiThread(() -> {
                    tvMensagem.setText("Falha na autenticação. Tente novamente.");
                    tvMensagem.setVisibility(android.view.View.VISIBLE);
                });
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Autenticação Biométrica")
                .setSubtitle("Use sua impressão digital ou FaceID para acessar")
                .setDescription("Confirme sua identidade para acessar o aplicativo")
                .setNegativeButtonText("Cancelar")
                .build();
    }

    private void verificarDisponibilidadeBiometria() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Verificar se o dispositivo suporta biometria
            btnBiometria.setEnabled(true);
        } else {
            btnBiometria.setEnabled(false);
            btnBiometria.setText("Biometria não suportada");
        }
    }

    private void autenticarSenha() {
        String senha = etSenha.getText().toString();

        if (senha.isEmpty()) {
            etSenha.setError("Digite a senha");
            return;
        }

        if (senhaManager.verificarSenha(senha)) {
            abrirTurmas();
        } else {
            Toast.makeText(this, "Senha incorreta", Toast.LENGTH_SHORT).show();
            etSenha.setText("");
        }
    }

    private void autenticarBiometria() {
        biometricPrompt.authenticate(promptInfo);
    }

    private void abrirTurmas() {
        Intent intent = new Intent(this, TurmaListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}