# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Kotlin-based Maven project called "midimcp" - a local MCP (Model Context Protocol) server that provides persistent knowledge of MIDI guitar pedals. The server allows users to interact with their guitar pedals using natural language through AI assistants, translating verbal sound descriptions into specific MIDI CC commands.

## Build Commands

- **Build project**: `mvn compile`
- **Run MCP server**: `mvn exec:java` or `./run-server.sh`
- **Run tests**: `mvn test`
- **Package**: `mvn package`
- **Clean**: `mvn clean`

## Project Architecture

### Core Components

1. **Data Models** (`src/main/java/com/guyko/models/`)
   - `PedalModel`: Represents a guitar pedal with its MIDI CC mappings
   - `CCParameter`: Represents individual MIDI CC parameters (name, CC number, value ranges)
   - `MidiCommand`: Represents generated MIDI commands with byte conversion

2. **Persistence Layer** (`src/main/java/com/guyko/persistence/`)
   - `PedalRepository`: JSON-based storage for pedal configurations using Gson
   - Data stored in `data/pedals/` directory as JSON files

3. **MCP Server** (`src/main/java/com/guyko/mcp/`)
   - `MCPServer`: Main MCP protocol implementation with JSON-RPC communication
   - Handles tool calls for pedal management and MIDI command generation

4. **Pedal Configurations** (`src/main/java/com/guyko/pedals/`)
   - `MerisLVXLoader`: Pre-configured Meris LVX delay pedal with full CC table

### Available MCP Tools

- `add_pedal`: Add new guitar pedals with MIDI CC mappings
- `get_pedal`: Retrieve specific pedal information
- `list_pedals`: List all available pedals
- `generate_midi_command`: Generate MIDI CC commands for parameter changes
- `interpret_sound_request`: Convert natural language sound requests to parameter suggestions

### Natural Language Processing

The server includes intelligent sound request interpretation for common guitar effects terminology:
- Brightness/darkness adjustments
- Delay timing and feedback changes
- Ambient/spacey effects
- Vintage tape simulation
- Stereo width and modulation

## Dependencies

- Kotlin 1.3.0 standard library (JDK8)
- Gson 2.8.9 for JSON serialization
- kotlin-logging for logging
- JUnit 4.12 for testing
- Mockito for mocking

## Getting Started

1. Build the project: `mvn compile`
2. Run the MCP server: `mvn exec:java`
3. The server will start with pre-loaded Meris LVX pedal configuration
4. Connect your AI assistant to interact via MCP protocol

## Development Environment

- IntelliJ IDEA project configuration included
- Maven build system with Kotlin compilation
- Git configuration with appropriate ignores for Maven/IntelliJ artifacts