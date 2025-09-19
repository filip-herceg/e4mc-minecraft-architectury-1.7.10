@echo off
cd /d "d:\Development\games\Minecraft\e4mc-minecraft-architectury-1.7.10"
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-8.0.462.8-hotspot
set PATH=%JAVA_HOME%\bin;%PATH%
echo Building e4mc mod for Minecraft 1.7.10...
gradlew.bat clean build
pause
