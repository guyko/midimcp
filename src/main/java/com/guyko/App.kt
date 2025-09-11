package com.guyko

import com.guyko.mcp.MCPServer

fun main(args: Array<String>) {
    val server = MCPServer()
    
    // Pedal configurations are now auto-loaded from JSON files in data/pedals/
    // by the PedalRepository during initialization
    
    // Start MCP server (no console output for Claude Desktop compatibility)
    server.start()
}