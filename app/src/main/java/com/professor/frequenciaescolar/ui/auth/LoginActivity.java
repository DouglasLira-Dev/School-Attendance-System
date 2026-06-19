package com.professor.frequenciaescolar.ui.auth;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;

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
    private TextView tvEsqueciSenha;

    private SenhaManager senhaManager;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etSenha = findViewById(R.id.etSenha);
        btnEntrar = findViewById(R.id.btnEntrar);
        btnBiometria = findViewById(R.id.btnBiometria);
        tvMensagem = findViewById(R.id.tvMensagem);
        tvEsqueciSenha = findViewById(R.id.tvEsqueciSenha);

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

        // Clique do "Esqueci minha senha"
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
                    Toast.makeText(LoginActivity.this, "✅ Autenticado com sucesso!", Toast.LENGTH_SHORT).show();
                    abrirTurmas();
                });
            }

            @Override
            public void onAuthenticationFailed() {
                runOnUiThread(() -> {
                    tvMensagem.setText("❌ Falha na autenticação. Tente novamente.");
                    tvMensagem.setVisibility(android.view.View.VISIBLE);
                });
            }

            // ==================== ADICIONADO: Tratamento de erros ====================
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                runOnUiThread(() -> {
                    String mensagem = "❌ Erro: " + errString;
                    tvMensagem.setText(mensagem);
                    tvMensagem.setVisibility(android.view.View.VISIBLE);
                    Toast.makeText(LoginActivity.this, mensagem, Toast.LENGTH_LONG).show();
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
        // ==================== VERIFICAÇÃO COMPLETA ====================
        BiometricManager biometricManager = BiometricManager.from(this);
        int canAuthenticate = biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG |
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
        );

        if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
            btnBiometria.setEnabled(true);
            btnBiometria.setText("Usar Impressão Digital");
        } else {
            btnBiometria.setEnabled(false);
            btnBiometria.setText("Biometria não disponível");
            // Opcional: mostrar motivo
            String motivo;
            switch (canAuthenticate) {
                case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                    motivo = "Dispositivo não suporta biometria";
                    break;
                case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                    motivo = "Hardware de biometria indisponível";
                    break;
                case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                    motivo = "Nenhuma impressão digital cadastrada";
                    break;
                default:
                    motivo = "Biometria não disponível";
            }
            tvMensagem.setText("ℹ️ " + motivo);
            tvMensagem.setVisibility(android.view.View.VISIBLE);
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
        // ==================== VERIFICAÇÃO DE NULL ====================
        if (biometricPrompt == null || promptInfo == null) {
            Toast.makeText(this, "Biometria não inicializada", Toast.LENGTH_SHORT).show();
            return;
        }

        // Verifica novamente se o dispositivo suporta
        BiometricManager biometricManager = BiometricManager.from(this);
        int canAuthenticate = biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG |
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
        );

        if (canAuthenticate != BiometricManager.BIOMETRIC_SUCCESS) {
            Toast.makeText(this, "Biometria não disponível no momento", Toast.LENGTH_SHORT).show();
            return;
        }

        // Inicia a autenticação
        biometricPrompt.authenticate(promptInfo);
    }

    private void abrirTurmas() {
        Intent intent = new Intent(this, TurmaListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        tvMensagem.setVisibility(android.view.View.GONE);
    }
}