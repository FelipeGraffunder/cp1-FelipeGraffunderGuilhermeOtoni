@echo off
setlocal
cd /d "%~dp0"
if not exist build mkdir build
if not exist evidencias mkdir evidencias
where javac >nul 2>&1
if errorlevel 1 (
    java -m jdk.compiler/com.sun.tools.javac.Main -encoding UTF-8 -d build @fontes.txt
) else (
    javac -encoding UTF-8 -d build @fontes.txt
)
if errorlevel 1 exit /b 1
java -cp build ScannerTest >evidencias\testes-local.txt 2>&1
set resultado=%errorlevel%
type evidencias\testes-local.txt
exit /b %resultado%
