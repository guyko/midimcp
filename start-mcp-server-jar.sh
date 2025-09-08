#!/bin/bash

# For manual testing, show configuration info to stderr
if [ -t 1 ]; then
  {
    echo "Starting MIDI Guitar Pedal MCP Server (JAR mode)..."
    echo "Connect from Claude Desktop using this configuration:"
    echo ""
    echo "Add to your Claude Desktop config:"
    echo "{"
    echo "  \"mcpServers\": {"
    echo "    \"midi-guitar-pedals\": {"
    echo "      \"command\": \"$PWD/start-mcp-server-jar.sh\","
    echo "      \"args\": []"
    echo "    }"
    echo "  }"
    echo "}"
    echo ""
    echo "Server starting..."
  } >&2
fi

# Get the script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Build classpath including all dependencies
CLASSPATH="$SCRIPT_DIR/target/classes"
CLASSPATH="$CLASSPATH:$SCRIPT_DIR/target/midimcp-1.0-SNAPSHOT.jar"

# Add Maven dependencies to classpath
for jar in ~/.m2/repository/com/google/code/gson/gson/2.8.9/*.jar; do
  if [ -f "$jar" ]; then
    CLASSPATH="$CLASSPATH:$jar"
  fi
done

for jar in ~/.m2/repository/io/github/microutils/kotlin-logging/1.6.22/*.jar; do
  if [ -f "$jar" ]; then
    CLASSPATH="$CLASSPATH:$jar"
  fi
done

for jar in ~/.m2/repository/org/jetbrains/kotlin/kotlin-stdlib/1.3.0/*.jar; do
  if [ -f "$jar" ]; then
    CLASSPATH="$CLASSPATH:$jar"
  fi
done

# Run the MCP server with direct Java execution
exec java -cp "$CLASSPATH" \
  -Dorg.slf4j.simpleLogger.defaultLogLevel=off \
  -Djava.awt.headless=true \
  com.guyko.AppKt \
  2>/dev/null