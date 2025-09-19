@rem
@rem E4MC Build Wrapper - JAVA_HOME Independent
@rem Calls the improved gradlew-java8.bat wrapper
@rem

@echo off
echo E4MC Build Tool - Java 8 Independent Build
echo.

@rem Call our improved Gradle wrapper
call "%~dp0gradlew-java8.bat" %*
