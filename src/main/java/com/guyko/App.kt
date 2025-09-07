package com.guyko

import com.guyko.mcp.MCPServer
import com.guyko.pedals.MerisLVXLoader
import com.guyko.pedals.MerisMercuryXLoader

fun main(args: Array<String>) {
    val server = MCPServer()
    
    // Load default pedal configurations
    MerisLVXLoader.loadMerisLVX(server)
    MerisMercuryXLoader.loadMercuryX(server)
    
    println("Starting MIDI Guitar Pedal MCP Server...")
    server.start()
}