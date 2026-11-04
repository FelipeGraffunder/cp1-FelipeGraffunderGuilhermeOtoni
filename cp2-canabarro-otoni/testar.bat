@echo off
setlocal
cd /d "%~dp0"
set "CP2_JAVA=java"
set "CP2_JAVAC=javac"
if defined JAVA_HOME (
    set "CP2_JAVA=%JAVA_HOME%\bin\java.exe"
    set "CP2_JAVAC=%JAVA_HOME%\bin\javac.exe"
) else (
    for /d %%J in ("%ProgramFiles%\Java\jdk-*") do (
        if exist "%%~J\bin\javac.exe" (
            set "CP2_JAVA=%%~J\bin\java.exe"
            set "CP2_JAVAC=%%~J\bin\javac.exe"
        )
    )
)
if not exist build mkdir build
"%CP2_JAVAC%" --release 17 -encoding UTF-8 -d build @fontes.txt
if errorlevel 1 exit /b 1
"%CP2_JAVA%" -cp build ParserTest
if errorlevel 1 exit /b 1
exit /b 0
