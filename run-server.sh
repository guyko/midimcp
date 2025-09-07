#!/bin/bash

# Build the project
mvn compile

# Run the MCP server
mvn exec:java -Dexec.mainClass="com.guyko.AppKt"