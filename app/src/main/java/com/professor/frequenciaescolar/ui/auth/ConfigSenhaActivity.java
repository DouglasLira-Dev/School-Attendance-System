package com.professor.frequenciaescolar.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.professor.frequenciaescolar.R;
import com.professor.frequenciaescolar.utils.SenhaManager;

public class ConfigSenhaActivity extends AppCompatActivity {

    private EditText etSenha;
    private EditText etConfirmarSenha;
    private Button btnSalvar;

    private SenhaManager senhaManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_senha);

        etSenha = findViewById(R.id.etSenha);
        etConfirmarSenha = findViewById(R.id.etConfirmarSenha);
        btnSalvar = findViewById(R.id.btnSalvar);

        senhaManager = new SenhaManager(this);

        btnSalvar.setOnClickListener(v -> salvarSenha());
    }

    private void salvarSenha() {
        String senha = etSenha.getText().toString();
        String confirmar = etConfirmarSenha.getText().toString();

        if (senha.isEmpty()) {
            etSenha.setError("Digite uma senha");
            return;
        }

        String erroSenha = validarForcaSenha(senha);
        if (erroSenha != null) {
            etSenha.setError(erroSenha);
            return;
        }

        if (!senha.equals(confirmar)) {
            etConfirmarSenha.setError("As senhas não coincidem");
            return;
        }

        senhaManager.salvarSenha(senha);

        Toast.makeText(this, "Senha salva com sucesso!", Toast.LENGTH_SHORT).show();

        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
    private String validarForcaSenha(String senha) {
        if (senha.length() < 6) {
            return "A senha deve ter pelo menos 6 caracteres";
        }
        boolean temNumero = senha.matches(".*[0-9].*");
        boolean temLetra = senha.matches(".*[a-zA-Z].*");
        if (!temNumero || !temLetra) {
            return "A senha deve conter letras e números";
        }
        return null; // null = senha válida
    }
}