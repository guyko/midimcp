# MCP Registry Publication Guide

This document outlines how to publish the MIDI MCP server to the official Model Context Protocol (MCP) registry.

## Overview

This project has been configured for automated publication to the MCP registry using GitHub Actions. The setup includes:

- **Server Configuration**: `server.json` defining the MCP server metadata
- **Build System**: Maven with shade plugin for creating standalone MCPB packages  
- **CI/CD**: GitHub Actions workflow for automated testing, building, and publishing
- **Authentication**: GitHub OIDC for registry authentication

## Files Created/Modified

### 1. `server.json`
- **Purpose**: MCP registry metadata and configuration
- **Namespace**: `io.github.guyko/midimcp` (GitHub-based namespace)
- **Package Type**: MCPB (MCP Binary) distributed via GitHub releases
- **Tools Defined**: Lists all 9 available MCP tools (add_pedal, execute_midi_command, etc.)

### 2. `.github/workflows/publish-mcp.yml`
- **Purpose**: Automated CI/CD for MCP registry publishing
- **Triggers**: Git tags matching `v*` pattern (e.g., `v1.0.0`)
- **Steps**: Test → Build → Package → Release → Publish to Registry

### 3. `pom.xml` Updates
- **Version**: Changed from `1.0-SNAPSHOT` to `1.0.0`
- **Shade Plugin**: Added to create fat JAR with all dependencies
- **Output**: `midimcp-{version}-standalone.jar` suitable for MCPB packaging

## Publishing Process

### Automated Publishing (Recommended)

The project is set up for fully automated publishing:

1. **Create and Push a Version Tag**:
   ```bash
   git tag v1.0.0
   git push origin v1.0.0
   ```

2. **GitHub Actions Will**:
   - Run all tests (`mvn test`)
   - Build the project (`mvn clean compile package`)
   - Create MCPB package from standalone JAR
   - Calculate SHA256 hash for file integrity
   - Update `server.json` with actual version and hash
   - Create GitHub release with MCPB file
   - Install MCP Publisher CLI
   - Authenticate using GitHub OIDC
   - Publish to MCP registry

### Manual Publishing (Alternative)

If you prefer manual control:

1. **Install MCP Publisher**:
   ```bash
   brew install mcp-publisher
   ```

2. **Build the Project**:
   ```bash
   mvn clean compile package
   ```

3. **Create MCPB Package**:
   ```bash
   cp target/midimcp-1.0.0-standalone.jar midimcp-1.0.0.mcpb
   ```

4. **Calculate Hash and Update server.json**:
   ```bash
   SHA256=$(shasum -a 256 midimcp-1.0.0.mcpb | cut -d' ' -f1)
   # Update server.json with actual hash and release URL
   ```

5. **Authenticate and Publish**:
   ```bash
   mcp-publisher login github
   mcp-publisher publish
   ```

## Registry Information

- **Registry URL**: https://registry.modelcontextprotocol.io
- **Server Name**: `io.github.guyko/midimcp`
- **Package Type**: MCPB (MCP Binary)
- **Authentication**: GitHub OIDC (automated) or GitHub OAuth (manual)

## Installation by Users

Once published, users can install the MCP server via:

```bash
# Via MCP registry (once published)
mcp install io.github.guyko/midimcp
```

Or manually by downloading the MCPB file from GitHub releases.

## Key Features Advertised

The registry listing will highlight:

- **Guitar Pedal MIDI Management**: Persistent knowledge of pedal CC mappings
- **Supported Pedals**: Meris LVX Delay, Mercury X Reverb, Enzo X Synthesizer, Neural DSP Quad Cortex
- **Preset Creation**: Natural language to MIDI CC command translation
- **Real-time Control**: Live MIDI device communication and status monitoring
- **9 MCP Tools**: Complete API for pedal management and MIDI execution

## Next Steps

1. **Test the Build**: Ensure `mvn clean compile package` works locally
2. **Create First Release**: Push `v1.0.0` tag to trigger automated publishing
3. **Monitor Workflow**: Check GitHub Actions for successful completion
4. **Verify Registry**: Search for the server at https://registry.modelcontextprotocol.io
5. **Update Version**: For future releases, increment version in `pom.xml` and create new tags

## Architecture Notes

- **MCPB Format**: Currently uses the standalone JAR as MCPB (may need refinement)
- **Transport**: STDIO-based communication
- **Dependencies**: All bundled in standalone JAR for easy distribution
- **Validation**: Package integrity verified via SHA256 hash

This setup provides a professional, automated pipeline for maintaining the MCP server in the official registry while ensuring code quality through automated testing.