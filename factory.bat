@echo off
set JAVA_HOME=C:\Users\User\.jdks\temurin-17.0.19
set PATH=%JAVA_HOME%\bin;%PATH%
set MAIN_CLASS=%~dp0build\classes\java\main\com\codefab\Main.class

if "%1"=="-b" (
    shift
    goto build
)
if exist "%MAIN_CLASS%" goto run

:build
echo Building...
call "%~dp0gradlew.bat" classes -q
if %errorlevel% neq 0 (
    echo Build failed
    exit /b 1
)

:run
"%JAVA_HOME%\bin\java.exe" -classpath "%~dp0build\classes\java\main" com.codefab.Main %*