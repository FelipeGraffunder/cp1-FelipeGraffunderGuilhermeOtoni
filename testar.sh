#!/usr/bin/env bash
set -euo pipefail
cd -- "$(dirname -- "$0")"
mkdir -p build evidencias
if command -v javac >/dev/null 2>&1; then
    javac -encoding UTF-8 -d build @fontes.txt
else
    java -m jdk.compiler/com.sun.tools.javac.Main -encoding UTF-8 -d build @fontes.txt
fi
java -cp build ScannerTest 2>&1 | tee evidencias/testes-local.txt
