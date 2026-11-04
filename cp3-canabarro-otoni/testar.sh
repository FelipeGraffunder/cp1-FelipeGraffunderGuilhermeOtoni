#!/bin/sh
set -eu
cd "$(dirname "$0")"
mkdir -p build
find src test -name '*.java' > build/fontes.txt
"${JAVA_HOME:+$JAVA_HOME/bin/}javac" -encoding UTF-8 -d build @build/fontes.txt
"${JAVA_HOME:+$JAVA_HOME/bin/}java" -cp build ParserTest
"${JAVA_HOME:+$JAVA_HOME/bin/}java" -cp build AstTest
