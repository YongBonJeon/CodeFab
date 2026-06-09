@echo off
setlocal enabledelayedexpansion
set JAVA_HOME=C:\Users\User\.jdks\temurin-17.0.19
set PATH=%JAVA_HOME%\bin;%PATH%
set "FHOME=%~dp0"
set "FHOME=%FHOME:~0,-1%"
set MAIN_CLASS=%FHOME%\build\classes\java\main\com\codefab\Main.class

if "%1"=="-b" (
    shift
    goto build
)
if not exist "%MAIN_CLASS%" goto build
goto run

:build
echo Building...
call "%FHOME%\gradlew.bat" classes -q
if !errorlevel! neq 0 (
    echo Build failed.
    exit /b 1
)
echo Build OK
echo.

:run
"%JAVA_HOME%\bin\java.exe" -Dfactory.home="%FHOME%" -classpath "%FHOME%\build\classes\java\main" com.codefab.Main %1 %2 %3 %4 %5 %6 %7 %8 %9