@echo off
echo Setting up E4MC 1.7.10 Build Environment (without JAVA_HOME)
echo.

:: Navigate to project directory
cd /d "%~dp0"

echo Current Java version:
java -version
echo.

echo Gradle version:
gradlew.bat --version

:: Try clean first
echo.
echo Cleaning previous builds...
gradlew.bat clean

:: Try setup dev workspace
echo.
echo Setting up development workspace...
gradlew.bat setupDevWorkspace

:: Build the project
echo.
echo Building project...
gradlew.bat build

if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Build failed
    pause
    exit /b 1
)

echo.
echo Build completed successfully!
echo JAR files are located in: build\libs\
echo.

:: List generated JAR files
if exist "build\libs\*.jar" (
    dir build\libs\*.jar /b
) else (
    echo No JAR files found in build\libs\
)

echo.
echo Build script completed!
pause
