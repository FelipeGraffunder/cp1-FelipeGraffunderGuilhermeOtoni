@echo off
setlocal
cd /d "%~dp0"
if not exist build mkdir build
set "JAVAC=javac"
set "JAVA=java"
if defined JAVA_HOME set "JAVAC=%JAVA_HOME%\bin\javac.exe"
if defined JAVA_HOME set "JAVA=%JAVA_HOME%\bin\java.exe"
"%JAVAC%" -encoding UTF-8 -d build src/*.java src/ast/*.java src/visitor/*.java test/*.java
if errorlevel 1 exit /b 1
"%JAVA%" -cp build ParserTest
if errorlevel 1 exit /b 1
"%JAVA%" -cp build AstTest
exit /b %errorlevel%
