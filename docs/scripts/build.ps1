#!/usr/bin/env pwsh
# Build script for e4mc Minecraft 1.7.10 with Java 8 setup (without JAVA_HOME)

Write-Host "Setting up Java 8 environment..."
$JavaPath = "C:\Program Files\Eclipse Adoptium\jdk-8.0.432.6-hotspot\bin"
$env:PATH = "$JavaPath;$env:PATH"

Write-Host "Java version check:"
& java -version

Write-Host "Starting Gradle build..."
& .\gradlew.bat clean setupDevWorkspace build

if ($LASTEXITCODE -eq 0) {
    Write-Host "Build successful!" -ForegroundColor Green
    Write-Host "JAR files can be found in build/libs/" -ForegroundColor Green
} else {
    Write-Host "Build failed with exit code $LASTEXITCODE" -ForegroundColor Red
}
