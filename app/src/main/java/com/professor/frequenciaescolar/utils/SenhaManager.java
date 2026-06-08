package com.professor.frequenciaescolar.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import android.util.Base64;

public class SenhaManager {

    private static final String PREF_NAME = "frequencia_prefs";
    private static final String KEY_SENHA = "senha_criptografada";
    private static final String KEY_IV = "iv";
    private static final String ALIAS = "FrequenciaEscolarKey";

    private Context context;
    private SharedPreferences preferences;

    public SenhaManager(Context context) {
        this.context = context;
        this.preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // Verificar se é o primeiro acesso
    public boolean isPrimeiroAcesso() {
        return !preferences.contains(KEY_SENHA);
    }

    // Salvar senha criptografada
    public void salvarSenha(String senha) {
        try {
            SecretKey key = getOrCreateKey();
            Cipher cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" +
                    KeyProperties.BLOCK_MODE_CBC + "/" +
                    KeyProperties.ENCRYPTION_PADDING_PKCS7);
            cipher.init(Cipher.ENCRYPT_MODE, key);

            byte[] iv = cipher.getIV();
            byte[] encrypted = cipher.doFinal(senha.getBytes());

            preferences.edit()
                    .putString(KEY_SENHA, Base64.encodeToString(encrypted, Base64.DEFAULT))
                    .putString(KEY_IV, Base64.encodeToString(iv, Base64.DEFAULT))
                    .apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Verificar senha
    public boolean verificarSenha(String senha) {
        try {
            String encryptedStr = preferences.getString(KEY_SENHA, null);
            String ivStr = preferences.getString(KEY_IV, null);

            if (encryptedStr == null || ivStr == null) {
                return false;
            }

            SecretKey key = getOrCreateKey();
            Cipher cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" +
                    KeyProperties.BLOCK_MODE_CBC + "/" +
                    KeyProperties.ENCRYPTION_PADDING_PKCS7);

            byte[] iv = Base64.decode(ivStr, Base64.DEFAULT);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);

            byte[] encrypted = Base64.decode(encryptedStr, Base64.DEFAULT);
            byte[] decrypted = cipher.doFinal(encrypted);
            String senhaSalva = new String(decrypted);

            return senhaSalva.equals(senha);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private SecretKey getOrCreateKey() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);

        if (!keyStore.containsAlias(ALIAS)) {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
            keyGenerator.init(new KeyGenParameterSpec.Builder(ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());
            return keyGenerator.generateKey();
        }

        return (SecretKey) keyStore.getKey(ALIAS, null);
    }
}