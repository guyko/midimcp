package com.guyko

import com.guyko.mcp.MCPServer
import com.guyko.pedals.MerisLVXLoader
import com.guyko.pedals.MerisMercuryXLoader
import com.guyko.pedals.MerisEnzoXLoader
import com.guyko.pedals.NeuralDSPQuadCortexLoader
import com.guyko.pedals.EventideH90Loader

fun main(args: Array<String>) {
    val server = MCPServer()
    
    // Load default pedal configurations
    MerisLVXLoader.loadMerisLVX(server)
    MerisMercuryXLoader.loadMercuryX(server)
    MerisEnzoXLoader.loadEnzoX(server)
    NeuralDSPQuadCortexLoader.loadQuadCortex(server)
    EventideH90Loader.loadEventideH90(server)
    
    // Start MCP server (no console output for Claude Desktop compatibility)
    server.start()
}