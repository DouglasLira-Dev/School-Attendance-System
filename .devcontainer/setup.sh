#!/bin/bash
set -e

echo ""
echo "=================================================="
echo "  Configurando ambiente Android no Codespace..."
echo "=================================================="
echo ""

# ── 1. Dependências do sistema ──────────────────────
echo "📦 [1/5] Instalando dependências do sistema..."
sudo apt-get update -qq
sudo apt-get install -y wget unzip curl adb

# ── 2. Android SDK Command Line Tools ───────────────
echo "📱 [2/5] Baixando Android SDK Command Line Tools..."
sudo mkdir -p /opt/android-sdk/cmdline-tools
cd /tmp
wget -q https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip \
     -O cmdline-tools.zip
unzip -q cmdline-tools.zip -d /opt/android-sdk/cmdline-tools
sudo mv /opt/android-sdk/cmdline-tools/cmdline-tools \
        /opt/android-sdk/cmdline-tools/latest
sudo chmod -R 777 /opt/android-sdk

# ── 3. Variáveis de ambiente permanentes ────────────
echo "🔧 [3/5] Configurando variáveis de ambiente..."
{
  echo 'export ANDROID_SDK_ROOT=/opt/android-sdk'
  echo 'export ANDROID_HOME=/opt/android-sdk'
  echo 'export PATH=$PATH:/opt/android-sdk/cmdline-tools/latest/bin'
  echo 'export PATH=$PATH:/opt/android-sdk/platform-tools'
  echo 'export PATH=$PATH:/opt/android-sdk/build-tools/34.0.0'
} >> ~/.bashrc
source ~/.bashrc

# ── 4. Aceitar licenças ──────────────────────────────
echo "✅ [4/5] Aceitando licenças do Android SDK..."
yes | /opt/android-sdk/cmdline-tools/latest/bin/sdkmanager \
      --licenses > /dev/null 2>&1 || true

# ── 5. Instalar plataformas e ferramentas ────────────
echo "📲 [5/5] Instalando Android SDK components..."
/opt/android-sdk/cmdline-tools/latest/bin/sdkmanager \
  "platform-tools" \
  "build-tools;34.0.0" \
  "platforms;android-35" \
  "platforms;android-24"

echo ""
echo "=================================================="
echo "  ✅ Setup concluído com sucesso!"
echo "  Para buildar: ./gradlew assembleDebug"
echo "  APK gerado em: app/build/outputs/apk/debug/"
echo "=================================================="
