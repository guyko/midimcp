# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Kotlin-based Maven project called "midimcp" that appears to be a MIDI-related MCP (Model Context Protocol) implementation. The project is in early development stages with minimal code currently present.

## Build Commands

- **Build project**: `mvn compile`
- **Run tests**: `mvn test`
- **Package**: `mvn package`
- **Clean**: `mvn clean`

## Project Structure

- **Language**: Kotlin 1.3.0 with Java interop
- **Build Tool**: Maven
- **Main Source**: `src/main/java/com/guyko/`
- **Test Source**: `src/test/java/com/guyko/`
- **Main Class**: `com.guyko.App.kt` - simple Hello World application

## Dependencies

- Kotlin standard library (JDK8)
- JUnit 4.12 for testing
- Mockito for mocking
- kotlin-logging for logging
- javax.mail for email functionality

## Development Environment

- IntelliJ IDEA project (`.idea/` directory present)
- Maven compilation configured for both Kotlin and Java sources
- Target and build artifacts ignored in git