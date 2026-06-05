@echo off
rem Factory control shell launcher (Windows)
rem   factory               - prompt (REPL) mode
rem   factory run [path]     - file mode
rem   factory debug [path]   - debug mode
setlocal
set "DIR=%~dp0"
set "JAR=%DIR%build\libs\factory.jar"

if not exist "%JAR%" (
  echo [factory] building...
  call "%DIR%gradlew.bat" -p "%DIR%" jar
)

java -Dfile.encoding=UTF-8 -jar "%JAR%" %*
endlocal
