#!/bin/bash
set -e

echo "📦 Instalando dependências básicas..."
sudo apt-get update -qq
sudo apt-get install -y wget unzip curl

echo "📱 Baixando Android SDK..."
sudo mkdir -p /opt/android-sdk/cmdline-tools
cd /tmp
wget -q https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -O cmdline-tools.zip
unzip -q cmdline-tools.zip -d /tmp/sdk-extract
sudo mv /tmp/sdk-extract/cmdline-tools /opt/android-sdk/cmdline-tools/latest
sudo chmod -R 777 /opt/android-sdk

echo "✅ Aceitando licenças..."
yes | /opt/android-sdk/cmdline-tools/latest/bin/sdkmanager --licenses > /dev/null 2>&1 || true

echo "📲 Instalando SDK components..."
/opt/android-sdk/cmdline-tools/latest/bin/sdkmanager \
  "platform-tools" \
  "build-tools;34.0.0" \
  "platforms;android-35"

echo 'export ANDROID_SDK_ROOT=/opt/android-sdk' >> ~/.bashrc
echo 'export ANDROID_HOME=/opt/android-sdk' >> ~/.bashrc
echo 'export PATH=$PATH:/opt/android-sdk/cmdline-tools/latest/bin:/opt/android-sdk/platform-tools:/opt/android-sdk/build-tools/34.0.0' >> ~/.bashrc

echo "✅ Pronto!"
