#!/usr/bin/env bash
set -eu
cd "$(dirname "$0")"
mkdir -p build
if [ -n "${JAVA_HOME:-}" ]; then
    CP2_JAVA="$JAVA_HOME/bin/java"
    CP2_JAVAC="$JAVA_HOME/bin/javac"
else
    CP2_JAVA=java
    CP2_JAVAC=javac
fi
"$CP2_JAVAC" --release 17 -encoding UTF-8 -d build @fontes.txt
"$CP2_JAVA" -cp build ParserTest
