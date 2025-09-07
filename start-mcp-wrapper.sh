#!/bin/bash

# Wrapper script to ensure MCP server stays alive for Claude Desktop

# Change to the project directory
cd "$(dirname "${BASH_SOURCE[0]}")"

# Set up environment to prevent early exit
export MAVEN_OPTS="-Xms64m -Xmx512m"

# Use exec to replace the shell process entirely, ensuring proper signal handling
exec mvn exec:java \
  -Dexec.mainClass="com.guyko.App" \
  -Dexec.args="" \
  -Dexec.cleanupDaemonThreads=false \
  -Dorg.slf4j.simpleLogger.defaultLogLevel=off \
  -Dmaven.plugin.validation=NONE \
  -q \
  2>/dev/null