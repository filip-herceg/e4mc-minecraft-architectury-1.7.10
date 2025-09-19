@echo off
echo Starting E4MC 1.7.10 Direct Build
echo.

cd /d "%~dp0"

:: Use Java 8 directly with Gradle
set "JAVA_EXE=C:\Program Files\Eclipse Adoptium\jdk-8.0.432.6-hotspot\bin\java.exe"

if not exist "%JAVA_EXE%" (
    echo ERROR: Java 8 not found at expected location
    echo Please install Eclipse Adoptium JDK 8.0.432.6
    pause
    exit /b 1
)

echo Using Java: %JAVA_EXE%
"%JAVA_EXE%" -version
echo.

:: Try to build directly with Java 8
echo Building with forced Java 8...
"%JAVA_EXE%" -cp gradle\wrapper\gradle-wrapper.jar org.gradle.wrapper.GradleWrapperMain clean build -x setupDevWorkspace

if %ERRORLEVEL% NEQ 0 (
    echo Build failed, trying alternative approach...
    echo.
    echo Attempting simple compilation...
    
    :: Create output directory
    mkdir build\classes\main 2>nul
    
    :: Simple compilation test
    "%JAVA_EXE%" -cp . -d build\classes\main src\main\java\link\e4mc\*.java
    
    if %ERRORLEVEL% EQU 0 (
        echo Basic compilation successful!
        echo Classes compiled to: build\classes\main
    ) else (
        echo Compilation failed
    )
)

echo.
echo Check build\libs\ for generated JAR files:
if exist "build\libs\*.jar" (
    dir build\libs\*.jar /b
) else (
    echo No JAR files found
)

pause
