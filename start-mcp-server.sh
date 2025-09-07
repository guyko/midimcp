#!/bin/bash

# For manual testing, show configuration info to stderr
if [ -t 1 ]; then
  {
    echo "Starting MIDI Guitar Pedal MCP Server..."
    echo "Connect from Claude Desktop using this configuration:"
    echo ""
    echo "Add to your Claude Desktop config:"
    echo "{"
    echo "  \"mcpServers\": {"
    echo "    \"midi-guitar-pedals\": {"
    echo "      \"command\": \"$PWD/start-mcp-server.sh\","
    echo "      \"args\": []"
    echo "    }"
    echo "  }"
    echo "}"
    echo ""
    echo "Server starting..."
  } >&2
fi

# Run the MCP server with complete output isolation for Claude Desktop compatibility
{
  mvn exec:java -Dexec.mainClass="com.guyko.App" \
    -q \
    -Dorg.slf4j.simpleLogger.defaultLogLevel=off \
    -Dmaven.plugin.validation=NONE \
    -Dexec.cleanupDaemonThreads=false
} 2>/dev/null