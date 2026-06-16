package com.professor.frequenciaescolar.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;
import com.professor.frequenciaescolar.R;
import com.professor.frequenciaescolar.utils.SenhaManager;

public class EsqueciSenhaActivity extends AppCompatActivity {

    private TextInputEditText etNovaSenha;
    private TextInputEditText etConfirmarSenha;
    private Button btnRedefinir;
    private Button btnVoltarLogin;
    private TextView tvMensagem;

    private SenhaManager senhaManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_esqueci_senha);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        etNovaSenha = findViewById(R.id.etNovaSenha);
        etConfirmarSenha = findViewById(R.id.etConfirmarSenha);
        btnRedefinir = findViewById(R.id.btnRedefinir);
        btnVoltarLogin = findViewById(R.id.btnVoltarLogin);
        tvMensagem = findViewById(R.id.tvMensagem);

        senhaManager = new SenhaManager(this);

        btnRedefinir.setOnClickListener(v -> redefinirSenha());
        btnVoltarLogin.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void redefinirSenha() {
        String novaSenha = etNovaSenha.getText().toString().trim();
        String confirmarSenha = etConfirmarSenha.getText().toString().trim();

        // ==================== VALIDAÇÕES ====================

        // Verificar se a nova senha foi digitada
        if (novaSenha.isEmpty()) {
            etNovaSenha.setError("Digite a nova senha");
            etNovaSenha.requestFocus();
            return;
        }

        // Verificar se a nova senha tem pelo menos 4 caracteres
        if (novaSenha.length() < 4) {
            etNovaSenha.setError("A nova senha deve ter pelo menos 4 caracteres");
            etNovaSenha.requestFocus();
            return;
        }

        // Verificar se as senhas coincidem
        if (!novaSenha.equals(confirmarSenha)) {
            etConfirmarSenha.setError("As senhas não coincidem");
            etConfirmarSenha.requestFocus();
            return;
        }

        // ==================== REDEFINIR SENHA ====================
        senhaManager.salvarSenha(novaSenha);

        tvMensagem.setVisibility(View.VISIBLE);
        tvMensagem.setText("✅ Senha redefinida com sucesso!");
        tvMensagem.setTextColor(getColor(android.R.color.holo_green_dark));

        // Limpar campos
        etNovaSenha.setText("");
        etConfirmarSenha.setText("");

        // Mostrar diálogo de sucesso
        new AlertDialog.Builder(this)
                .setTitle("✅ Sucesso!")
                .setMessage("Sua senha foi redefinida com sucesso.\n\nUse a nova senha para fazer o login.")
                .setPositiveButton("Ir para o Login", (dialog, which) -> {
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}