@rem
@rem E4MC Custom Gradle Wrapper - JAVA_HOME Independent
@rem Based on Gradle Wrapper but with hardcoded Java 8 paths
@rem

@if "%DEBUG%"=="" @echo off
@rem ##########################################################################
@rem
@rem  E4MC Gradle startup script for Windows (Java 8 specific)
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%

@rem Resolve any "." and ".." in APP_HOME to make it shorter.
for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi

@rem Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS="-Xmx2048m" "-Xms512m" "-XX:MaxPermSize=256m"

@rem Find Java 8 executable - try multiple common locations
set JAVA_EXE=

@rem Try Eclipse Adoptium OpenJDK 8 (most common)
if exist "C:\Program Files\Eclipse Adoptium\jdk-8.0.432.6-hotspot\bin\java.exe" (
    set JAVA_EXE=C:\Program Files\Eclipse Adoptium\jdk-8.0.432.6-hotspot\bin\java.exe
    goto found_java
)

@rem Try other Eclipse Adoptium versions
for /d %%d in ("C:\Program Files\Eclipse Adoptium\jdk-8*") do (
    if exist "%%d\bin\java.exe" (
        set JAVA_EXE=%%d\bin\java.exe
        goto found_java
    )
)

@rem Try Oracle JDK 8
for /d %%d in ("C:\Program Files\Java\jdk1.8*") do (
    if exist "%%d\bin\java.exe" (
        set JAVA_EXE=%%d\bin\java.exe
        goto found_java
    )
)

@rem Try Oracle JRE 8
for /d %%d in ("C:\Program Files\Java\jre1.8*") do (
    if exist "%%d\bin\java.exe" (
        set JAVA_EXE=%%d\bin\java.exe
        goto found_java
    )
)

@rem Try 32-bit installations
for /d %%d in ("C:\Program Files (x86)\Java\jdk1.8*") do (
    if exist "%%d\bin\java.exe" (
        set JAVA_EXE=%%d\bin\java.exe
        goto found_java
    )
)

for /d %%d in ("C:\Program Files (x86)\Java\jre1.8*") do (
    if exist "%%d\bin\java.exe" (
        set JAVA_EXE=%%d\bin\java.exe
        goto found_java
    )
)

@rem Try system PATH as last resort
java.exe -version >NUL 2>&1
if %ERRORLEVEL% equ 0 (
    set JAVA_EXE=java.exe
    echo WARNING: Using system Java from PATH - may not be Java 8
    goto found_java
)

@rem No Java found
echo.
echo ERROR: No Java 8 installation found!
echo.
echo Please install Java 8 (OpenJDK recommended) in one of these locations:
echo   - C:\Program Files\Eclipse Adoptium\jdk-8.*
echo   - C:\Program Files\Java\jdk1.8.*
echo   - C:\Program Files\Java\jre1.8.*
echo.
echo Or ensure java.exe is in your PATH.
echo.
goto fail

:found_java
echo Using Java: %JAVA_EXE%
"%JAVA_EXE%" -version
echo.

@rem Extract Java home directory from JAVA_EXE path
for %%i in ("%JAVA_EXE%") do set JAVA_DIR=%%~dpi
set JAVA_DIR=%JAVA_DIR:~0,-1%
for %%i in ("%JAVA_DIR%") do set JAVA_HOME_DIR=%%~dpi
set JAVA_HOME_DIR=%JAVA_HOME_DIR:~0,-1%

echo Detected Java Home: %JAVA_HOME_DIR%
echo.

@rem Setup the command line
set CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar

@rem Add Java home to Gradle opts dynamically
set GRADLE_OPTS=%GRADLE_OPTS% -Dorg.gradle.java.home="%JAVA_HOME_DIR%"

@rem Execute Gradle
echo Executing: "%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %GRADLE_OPTS% "-Dorg.gradle.appname=%APP_BASE_NAME%" -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*
echo.
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %GRADLE_OPTS% "-Dorg.gradle.appname=%APP_BASE_NAME%" -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*

:end
@rem End local scope for the variables with windows NT shell
if %ERRORLEVEL% equ 0 goto mainEnd

:fail
rem Set variable GRADLE_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
set EXIT_CODE=%ERRORLEVEL%
if %EXIT_CODE% equ 0 set EXIT_CODE=1
if not ""=="%GRADLE_EXIT_CONSOLE%" exit %EXIT_CODE%
exit /b %EXIT_CODE%

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
