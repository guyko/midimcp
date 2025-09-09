package com.guyko.mcp

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.guyko.models.PedalModel
import com.guyko.models.CCParameter
import com.guyko.models.MidiCommand
import com.guyko.models.MidiProgramChange
import com.guyko.models.MidiSysex
import com.guyko.persistence.PedalRepository
import com.guyko.midi.MidiExecutor
import com.guyko.midi.HardwareMidiExecutor
import com.guyko.pedals.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import io.github.oshai.kotlinlogging.KotlinLogging

class MCPServer(
    val pedalRepository: PedalRepository = PedalRepository(),
    private val midiExecutor: MidiExecutor = HardwareMidiExecutor()
) {
    private val logger = KotlinLogging.logger {}
    private val gson = Gson()
    private val reader = BufferedReader(InputStreamReader(System.`in`))
    private val writer = PrintWriter(System.out, true)
    
    fun start() {
        // Start MCP server with clean JSON output for Claude Desktop compatibility
        sendInitialHandshake()
        processMessages()
    }
    
    private fun sendInitialHandshake() {
        // Don't send anything initially - wait for initialize request from client
    }
    
    private fun processMessages() {
        while (true) {
            try {
                val line = reader.readLine() ?: break
                if (line.isBlank()) continue
                
                val message = JsonParser.parseString(line).asJsonObject
                val method = message.get("method")?.asString
                val id = message.get("id")?.asInt
                val params = message.get("params")?.asJsonObject
                
                when (method) {
                    "initialize" -> handleInitialize(id)
                    "tools/list" -> handleToolsList(id)
                    "tools/call" -> handleToolCall(id, params)
                    else -> sendError(id, "Unknown method: $method")
                }
            } catch (e: Exception) {
                // Don't send error responses for parsing failures as they may cause protocol issues
                // Just continue processing the next message
                continue
            }
        }
    }
    
    private fun handleInitialize(id: Int?) {
        val serverInfo = mapOf(
            "protocolVersion" to "2024-11-05",
            "capabilities" to mapOf(
                "tools" to mapOf<String, Any>()
            ),
            "serverInfo" to mapOf(
                "name" to "MIDI Guitar Pedal MCP Server",
                "version" to "1.0.0"
            )
        )
        
        sendResponse("initialize", serverInfo, id)
    }
    
    private fun handleToolsList(id: Int?) {
        val tools = listOf(
            mapOf(
                "name" to "add_pedal",
                "description" to "Add a new guitar pedal with its MIDI CC mappings",
                "inputSchema" to mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "manufacturer" to mapOf("type" to "string"),
                        "modelName" to mapOf("type" to "string"),
                        "version" to mapOf("type" to "string"),
                        "midiChannel" to mapOf("type" to "integer", "default" to 1),
                        "parameters" to mapOf(
                            "type" to "array",
                            "items" to mapOf(
                                "type" to "object",
                                "properties" to mapOf(
                                    "name" to mapOf("type" to "string"),
                                    "ccNumber" to mapOf("type" to "integer"),
                                    "minValue" to mapOf("type" to "integer", "default" to 0),
                                    "maxValue" to mapOf("type" to "integer", "default" to 127),
                                    "description" to mapOf("type" to "string"),
                                    "unit" to mapOf("type" to "string"),
                                    "category" to mapOf("type" to "string")
                                ),
                                "required" to listOf("name", "ccNumber")
                            )
                        )
                    ),
                    "required" to listOf("manufacturer", "modelName", "parameters")
                )
            ),
            mapOf(
                "name" to "get_pedal",
                "description" to "Get information about a specific pedal",
                "inputSchema" to mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "pedalId" to mapOf("type" to "string")
                    ),
                    "required" to listOf("pedalId")
                )
            ),
            mapOf(
                "name" to "list_pedals",
                "description" to "List all available pedals",
                "inputSchema" to mapOf(
                    "type" to "object",
                    "properties" to mapOf<String, Any>()
                )
            ),
            mapOf(
                "name" to "execute_midi_command",
                "description" to "Execute a MIDI CC command on a pedal",
                "inputSchema" to mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "pedalId" to mapOf("type" to "string"),
                        "ccNumber" to mapOf("type" to "integer", "minimum" to 0, "maximum" to 127),
                        "value" to mapOf("type" to "integer", "minimum" to 0, "maximum" to 127),
                        "description" to mapOf("type" to "string", "description" to "Optional description of what this command does")
                    ),
                    "required" to listOf("pedalId", "ccNumber", "value")
                )
            ),
            mapOf(
                "name" to "execute_midi_commands",
                "description" to "Execute multiple MIDI CC commands in sequence",
                "inputSchema" to mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "pedalId" to mapOf("type" to "string"),
                        "commands" to mapOf(
                            "type" to "array",
                            "items" to mapOf(
                                "type" to "object",
                                "properties" to mapOf(
                                    "ccNumber" to mapOf("type" to "integer", "minimum" to 0, "maximum" to 127),
                                    "value" to mapOf("type" to "integer", "minimum" to 0, "maximum" to 127),
                                    "description" to mapOf("type" to "string")
                                ),
                                "required" to listOf("ccNumber", "value")
                            )
                        )
                    ),
                    "required" to listOf("pedalId", "commands")
                )
            ),
            mapOf(
                "name" to "get_midi_status",
                "description" to "Get the status of the MIDI connection and executor",
                "inputSchema" to mapOf(
                    "type" to "object",
                    "properties" to mapOf<String, Any>()
                )
            ),
            mapOf(
                "name" to "rescan_midi_devices",
                "description" to "Re-scan for MIDI devices (useful if devices were connected after MCP server startup)",
                "inputSchema" to mapOf(
                    "type" to "object",
                    "properties" to mapOf<String, Any>()
                )
            ),
            mapOf(
                "name" to "execute_program_change",
                "description" to "Execute a MIDI program change to switch pedal preset",
                "inputSchema" to mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "pedalId" to mapOf("type" to "string"),
                        "program" to mapOf("type" to "integer", "minimum" to 0, "maximum" to 127),
                        "description" to mapOf("type" to "string", "description" to "Optional description of the preset")
                    ),
                    "required" to listOf("pedalId", "program")
                )
            ),
            mapOf(
                "name" to "generate_h90_program",
                "description" to "Generate H90 program file (pgm90 format) with dual algorithms and parameters. Creates binary program file for upload to Eventide H90.",
                "inputSchema" to mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "name" to mapOf("type" to "string", "description" to "Program name"),
                        "algorithmA" to mapOf(
                            "type" to "object",
                            "properties" to mapOf(
                                "algorithmNumber" to mapOf("type" to "integer", "minimum" to 0, "maximum" to 67, "description" to "Algorithm number (0-67)"),
                                "parameters" to mapOf("type" to "object", "description" to "Algorithm parameters as key-value pairs"),
                                "bypass" to mapOf("type" to "boolean", "default" to false),
                                "mix" to mapOf("type" to "number", "minimum" to 0, "maximum" to 1, "default" to 1.0)
                            ),
                            "required" to listOf("algorithmNumber")
                        ),
                        "algorithmB" to mapOf(
                            "type" to "object", 
                            "properties" to mapOf(
                                "algorithmNumber" to mapOf("type" to "integer", "minimum" to 0, "maximum" to 67, "description" to "Algorithm number (0-67)"),
                                "parameters" to mapOf("type" to "object", "description" to "Algorithm parameters as key-value pairs"),
                                "bypass" to mapOf("type" to "boolean", "default" to false),
                                "mix" to mapOf("type" to "number", "minimum" to 0, "maximum" to 1, "default" to 1.0)
                            ),
                            "required" to listOf("algorithmNumber")
                        ),
                        "routing" to mapOf(
                            "type" to "object",
                            "properties" to mapOf(
                                "mode" to mapOf("type" to "string", "enum" to listOf("SERIES_A_TO_B", "SERIES_B_TO_A", "PARALLEL", "SERIES_WITH_CROSSFADE"), "default" to "PARALLEL"),
                                "spilloverTime" to mapOf("type" to "number", "default" to 2.0)
                            )
                        ),
                        "globalParameters" to mapOf(
                            "type" to "object",
                            "properties" to mapOf(
                                "tempo" to mapOf("type" to "number", "default" to 120.0),
                                "tempoSync" to mapOf("type" to "boolean", "default" to false),
                                "killDry" to mapOf("type" to "boolean", "default" to false),
                                "programMix" to mapOf("type" to "number", "minimum" to 0, "maximum" to 1, "default" to 1.0),
                                "expressionPedal" to mapOf("type" to "number", "minimum" to 0, "maximum" to 1, "default" to 0.0)
                            )
                        )
                    ),
                    "required" to listOf("name", "algorithmA", "algorithmB")
                )
            ),
            mapOf(
                "name" to "send_sysex",
                "description" to "Send sysex data directly to a MIDI device. Used to upload presets or send custom sysex messages to guitar pedals.",
                "inputSchema" to mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "sysexData" to mapOf(
                            "type" to "string",
                            "description" to "Hexadecimal sysex data to send (e.g., 'F0 00 7F 00 01 F7')"
                        ),
                        "description" to mapOf(
                            "type" to "string",
                            "description" to "Optional description of what this sysex does"
                        )
                    ),
                    "required" to listOf("sysexData")
                )
            )
        )
        
        sendResponse("tools/list", mapOf("tools" to tools), id)
    }
    
    private fun handleToolCall(id: Int?, params: JsonObject?) {
        if (params == null) {
            logger.error { "MCP tool call failed: Missing parameters (id=$id)" }
            sendError(id, "Missing parameters")
            return
        }
        
        val toolName = params.get("name")?.asString
        val arguments = params.get("arguments")?.asJsonObject
        
        logger.info { "MCP tool call: '$toolName' (id=$id)" }
        logger.debug { "MCP tool arguments: ${arguments?.toString()}" }
        
        val startTime = System.currentTimeMillis()
        when (toolName) {
            "add_pedal" -> handleAddPedal(id, arguments)
            "get_pedal" -> handleGetPedal(id, arguments)
            "list_pedals" -> handleListPedals(id)
            "execute_midi_command" -> handleExecuteMidiCommand(id, arguments)
            "execute_midi_commands" -> handleExecuteMidiCommands(id, arguments)
            "execute_program_change" -> handleExecuteProgramChange(id, arguments)
            // Removed: sysex preset generation tools (moved to CC commands approach)
            // "generate_lvx_preset" -> handleGenerateLVXPreset(id, arguments)
            // "generate_mercury_x_preset" -> handleGenerateMercuryXPreset(id, arguments)
            // "generate_enzo_x_preset" -> handleGenerateEnzoXPreset(id, arguments)
            "generate_h90_program" -> handleGenerateH90Program(id, arguments)
            "send_sysex" -> handleSendSysex(id, arguments)
            "get_midi_status" -> handleGetMidiStatus(id)
            "rescan_midi_devices" -> handleRescanMidiDevices(id)
            else -> {
                logger.error { "MCP tool call failed: Unknown tool '$toolName' (id=$id)" }
                sendError(id, "Unknown tool: $toolName")
            }
        }
        val executionTime = System.currentTimeMillis() - startTime
        logger.info { "MCP tool call '$toolName' completed in ${executionTime}ms (id=$id)" }
    }
    
    private fun handleAddPedal(id: Int?, arguments: JsonObject?) {
        try {
            val manufacturer = arguments?.get("manufacturer")?.asString ?: ""
            val modelName = arguments?.get("modelName")?.asString ?: ""
            val version = arguments?.get("version")?.asString
            val midiChannel = arguments?.get("midiChannel")?.asInt ?: 1
            
            val parametersArray = arguments?.get("parameters")?.asJsonArray
            val parameters = parametersArray?.map { paramObj ->
                val param = paramObj.asJsonObject
                CCParameter(
                    name = param.get("name").asString,
                    ccNumber = param.get("ccNumber").asInt,
                    minValue = param.get("minValue")?.asInt ?: 0,
                    maxValue = param.get("maxValue")?.asInt ?: 127,
                    description = param.get("description")?.asString,
                    unit = param.get("unit")?.asString,
                    category = param.get("category")?.asString
                )
            } ?: emptyList()
            
            val pedalId = "${manufacturer.lowercase()}_${modelName.lowercase().replace(" ", "_")}"
            val pedal = PedalModel(
                id = pedalId,
                manufacturer = manufacturer,
                modelName = modelName,
                version = version,
                midiChannel = midiChannel,
                parameters = parameters
            )
            
            pedalRepository.save(pedal)
            
            sendResponse("tools/call", mapOf(
                "content" to listOf(mapOf(
                    "type" to "text",
                    "text" to "Successfully added pedal: $manufacturer $modelName with ${parameters.size} parameters"
                ))
            ), id)
        } catch (e: Exception) {
            sendError(id, "Error adding pedal: ${e.message}")
        }
    }
    
    private fun handleGetPedal(id: Int?, arguments: JsonObject?) {
        try {
            val pedalId = arguments?.get("pedalId")?.asString
            if (pedalId == null) {
                sendError(id, "Missing pedalId parameter")
                return
            }
            
            val pedal = pedalRepository.load(pedalId)
            if (pedal == null) {
                sendError(id, "Pedal not found: $pedalId")
                return
            }
            
            sendResponse("tools/call", mapOf(
                "content" to listOf(mapOf(
                    "type" to "text",
                    "text" to gson.toJson(pedal)
                ))
            ), id)
        } catch (e: Exception) {
            sendError(id, "Error getting pedal: ${e.message}")
        }
    }
    
    private fun handleListPedals(id: Int?) {
        try {
            val pedals = pedalRepository.listAll()
            val pedalSummaries = pedals.map { pedal ->
                "${pedal.id}: ${pedal.manufacturer} ${pedal.modelName} (${pedal.parameters.size} parameters)"
            }
            
            sendResponse("tools/call", mapOf(
                "content" to listOf(mapOf(
                    "type" to "text",
                    "text" to "Available pedals:\n" + pedalSummaries.joinToString("\n")
                ))
            ), id)
        } catch (e: Exception) {
            sendError(id, "Error listing pedals: ${e.message}")
        }
    }
    
    private fun handleExecuteMidiCommand(id: Int?, arguments: JsonObject?) {
        try {
            val pedalId = arguments?.get("pedalId")?.asString
            val ccNumber = arguments?.get("ccNumber")?.asInt
            val value = arguments?.get("value")?.asInt
            val description = arguments?.get("description")?.asString
            
            if (pedalId == null || ccNumber == null || value == null) {
                logger.error { "MCP execute_midi_command failed: Missing required parameters (pedalId=$pedalId, ccNumber=$ccNumber, value=$value)" }
                sendError(id, "Missing required parameters")
                return
            }
            
            val pedal = pedalRepository.load(pedalId)
            if (pedal == null) {
                logger.error { "MCP execute_midi_command failed: Pedal not found '$pedalId'" }
                sendError(id, "Pedal not found: $pedalId")
                return
            }
            
            logger.debug { "MCP execute_midi_command: Executing CC$ccNumber=$value on pedal '$pedalId' (${pedal.manufacturer} ${pedal.modelName})" }
            
            // Find parameter info if available
            val parameter = pedal.getParameterByCC(ccNumber)
            val parameterName = parameter?.name ?: "CC $ccNumber"
            
            val midiCommand = MidiCommand(
                channel = pedal.midiChannel,
                ccNumber = ccNumber,
                value = value,
                parameterName = parameterName,
                description = description ?: "Set $parameterName to $value"
            )
            
            val result = midiExecutor.executeCommand(midiCommand)
            
            if (result.success) {
                logger.info { "MCP execute_midi_command success: ${result.message}" }
            } else {
                logger.error { "MCP execute_midi_command failed: ${result.message}" }
            }
            
            sendResponse("tools/call", mapOf(
                "content" to listOf(mapOf(
                    "type" to "text",
                    "text" to buildString {
                        appendLine("MIDI Command Execution:")
                        appendLine("Status: ${if (result.success) "SUCCESS" else "FAILED"}")
                        appendLine("Pedal: ${pedal.manufacturer} ${pedal.modelName}")
                        appendLine("Parameter: $parameterName (CC $ccNumber)")
                        appendLine("Value: $value")
                        appendLine("Channel: ${pedal.midiChannel}")
                        appendLine("Message: ${result.message}")
                        if (result.executedCommand != null) {
                            val hexString = result.executedCommand.toMidiBytes().joinToString(" ") { "%02X".format(it) }
                            appendLine("MIDI Bytes: $hexString")
                        }
                    }
                ))
            ), id)
        } catch (e: Exception) {
            sendError(id, "Error executing MIDI command: ${e.message}")
        }
    }
    
    private fun handleExecuteMidiCommands(id: Int?, arguments: JsonObject?) {
        try {
            val pedalId = arguments?.get("pedalId")?.asString
            val commandsArray = arguments?.get("commands")?.asJsonArray
            
            if (pedalId == null || commandsArray == null) {
                sendError(id, "Missing required parameters")
                return
            }
            
            val pedal = pedalRepository.load(pedalId)
            if (pedal == null) {
                sendError(id, "Pedal not found: $pedalId")
                return
            }
            
            val midiCommands = commandsArray.map { cmdObj ->
                val cmd = cmdObj.asJsonObject
                val ccNumber = cmd.get("ccNumber").asInt
                val value = cmd.get("value").asInt
                val description = cmd.get("description")?.asString
                
                val parameter = pedal.getParameterByCC(ccNumber)
                val parameterName = parameter?.name ?: "CC $ccNumber"
                
                MidiCommand(
                    channel = pedal.midiChannel,
                    ccNumber = ccNumber,
                    value = value,
                    parameterName = parameterName,
                    description = description ?: "Set $parameterName to $value"
                )
            }
            
            val results = midiExecutor.executeCommands(midiCommands)
            
            sendResponse("tools/call", mapOf(
                "content" to listOf(mapOf(
                    "type" to "text",
                    "text" to buildString {
                        appendLine("MIDI Commands Execution:")
                        appendLine("Pedal: ${pedal.manufacturer} ${pedal.modelName}")
                        appendLine("Commands executed: ${results.size}")
                        appendLine("Successful: ${results.count { it.success }}")
                        appendLine("Failed: ${results.count { !it.success }}")
                        appendLine()
                        results.forEachIndexed { index, result ->
                            appendLine("Command ${index + 1}:")
                            appendLine("  Status: ${if (result.success) "SUCCESS" else "FAILED"}")
                            appendLine("  Message: ${result.message}")
                        }
                    }
                ))
            ), id)
        } catch (e: Exception) {
            sendError(id, "Error executing MIDI commands: ${e.message}")
        }
    }
    
    private fun handleExecuteProgramChange(id: Int?, arguments: JsonObject?) {
        try {
            val pedalId = arguments?.get("pedalId")?.asString
            val program = arguments?.get("program")?.asInt
            val description = arguments?.get("description")?.asString
            
            if (pedalId == null || program == null) {
                sendError(id, "Missing required parameters")
                return
            }
            
            val pedal = pedalRepository.load(pedalId)
            if (pedal == null) {
                sendError(id, "Pedal not found: $pedalId")
                return
            }
            
            val programChange = MidiProgramChange(
                channel = pedal.midiChannel,
                program = program,
                description = description ?: "Switch to preset $program"
            )
            
            val result = midiExecutor.executeProgramChange(programChange)
            
            sendResponse("tools/call", mapOf(
                "content" to listOf(mapOf(
                    "type" to "text",
                    "text" to buildString {
                        appendLine("MIDI Program Change Execution:")
                        appendLine("Status: ${if (result.success) "SUCCESS" else "FAILED"}")
                        appendLine("Pedal: ${pedal.manufacturer} ${pedal.modelName}")
                        appendLine("Program: $program")
                        appendLine("Channel: ${pedal.midiChannel}")
                        appendLine("Message: ${result.message}")
                        val hexString = programChange.toMidiBytes().joinToString(" ") { "%02X".format(it) }
                        appendLine("MIDI Bytes: $hexString")
                    }
                ))
            ), id)
        } catch (e: Exception) {
            sendError(id, "Error executing program change: ${e.message}")
        }
    }
    
    private fun handleGetMidiStatus(id: Int?) {
        try {
            val status = midiExecutor.getStatus()
            val isAvailable = midiExecutor.isAvailable()
            
            sendResponse("tools/call", mapOf(
                "content" to listOf(mapOf(
                    "type" to "text",
                    "text" to buildString {
                        appendLine("MIDI Executor Status:")
                        appendLine("Available: $isAvailable")
                        appendLine("Status: $status")
                    }
                ))
            ), id)
        } catch (e: Exception) {
            sendError(id, "Error getting MIDI status: ${e.message}")
        }
    }
    
    private fun handleRescanMidiDevices(id: Int?) {
        try {
            logger.info { "MCP rescan_midi_devices: Manually triggering MIDI device re-scan" }
            
            // If using HardwareMidiExecutor, trigger reconnection
            if (midiExecutor is com.guyko.midi.HardwareMidiExecutor) {
                (midiExecutor as com.guyko.midi.HardwareMidiExecutor).reconnect()
            }
            
            val newStatus = midiExecutor.getStatus()
            val isAvailable = midiExecutor.isAvailable()
            
            sendResponse("tools/call", mapOf(
                "content" to listOf(mapOf(
                    "type" to "text",
                    "text" to buildString {
                        appendLine("MIDI Device Re-scan Complete")
                        appendLine("")
                        appendLine("Connection Status: ${if (isAvailable) "CONNECTED ✅" else "DISCONNECTED ❌"}")
                        appendLine("Device Details: $newStatus")
                        appendLine("")
                        if (isAvailable) {
                            appendLine("MIDI devices are now ready for commands!")
                        } else {
                            appendLine("No MIDI devices found. Please check:")
                            appendLine("1. MIDI interface is connected and powered")
                            appendLine("2. Pedals are connected to MIDI interface")
                            appendLine("3. No other applications are using the MIDI interface")
                        }
                    }
                ))
            ), id)
        } catch (e: Exception) {
            sendError(id, "Error rescanning MIDI devices: ${e.message}")
        }
    }
    
    
    private fun sendResponse(method: String, result: Any, id: Int? = null) {
        val response = mutableMapOf<String, Any>(
            "jsonrpc" to "2.0",
            "result" to result
        )
        // Always include id for protocol compliance
        response["id"] = id ?: 0
        writer.println(gson.toJson(response))
    }
    
    private fun handleSendSysex(id: Int?, arguments: JsonObject?) {
        try {
            val sysexData = arguments?.get("sysexData")?.asString
            val description = arguments?.get("description")?.asString
            
            if (sysexData.isNullOrBlank()) {
                sendError(id, "Missing required parameter: sysexData")
                return
            }
            
            // Parse hex string to byte array
            val hexBytes = try {
                sysexData.replace(" ", "").replace("-", "").chunked(2).map { 
                    it.toInt(16).toByte() 
                }.toByteArray()
            } catch (e: Exception) {
                sendError(id, "Invalid sysex data format: ${e.message}")
                return
            }
            
            // Create MidiSysex object
            val sysex = MidiSysex(hexBytes)
            
            // Execute sysex transmission
            val result = midiExecutor.executeSysex(sysex)
            
            if (result.success) {
                sendResponse("tools/call", mapOf(
                    "content" to listOf(mapOf(
                        "type" to "text",
                        "text" to buildString {
                            appendLine("✅ Sysex transmission successful!")
                            if (description != null) {
                                appendLine("Description: $description")
                            }
                            appendLine("Bytes transmitted: ${result.bytesTransmitted}")
                            appendLine("Data sent: ${sysex.toHexString()}")
                            appendLine("Status: ${result.message}")
                        }
                    ))
                ), id)
            } else {
                sendResponse("tools/call", mapOf(
                    "content" to listOf(mapOf(
                        "type" to "text",
                        "text" to buildString {
                            appendLine("❌ Sysex transmission failed")
                            if (description != null) {
                                appendLine("Description: $description")
                            }
                            appendLine("Error: ${result.message}")
                            appendLine("Data attempted: ${sysex.toHexString()}")
                        }
                    ))
                ), id)
            }
        } catch (e: Exception) {
            sendError(id, "Failed to send sysex: ${e.message}")
        }
    }
    
    private fun sendError(id: Int?, message: String) {
        val error = mutableMapOf<String, Any>(
            "jsonrpc" to "2.0",
            "error" to mapOf(
                "code" to -1,
                "message" to message
            )
        )
        // Only include id if it's not null
        if (id != null) {
            error["id"] = id
        } else {
            error["id"] = 0  // Use 0 as default for protocol compliance
        }
        writer.println(gson.toJson(error))
    }
    
    private fun handleGenerateH90Program(id: Int?, arguments: JsonObject?) {
        try {
            val name = arguments?.get("name")?.asString
            if (name.isNullOrBlank()) {
                sendError(id, "Missing required parameter: name")
                return
            }
            
            // Parse algorithm A
            val algorithmAObj = arguments?.get("algorithmA")?.asJsonObject
            if (algorithmAObj == null) {
                sendError(id, "Missing required parameter: algorithmA")
                return
            }
            
            val algorithmANumber = algorithmAObj.get("algorithmNumber")?.asInt
            if (algorithmANumber == null) {
                sendError(id, "Missing algorithmA.algorithmNumber")
                return
            }
            
            val algorithmAParams = algorithmAObj.get("parameters")?.asJsonObject?.entrySet()?.associate { 
                it.key to it.value.asString 
            } ?: emptyMap()
            val algorithmABypass = algorithmAObj.get("bypass")?.asBoolean ?: false
            val algorithmAMix = algorithmAObj.get("mix")?.asDouble ?: 1.0
            
            // Parse algorithm B
            val algorithmBObj = arguments?.get("algorithmB")?.asJsonObject
            if (algorithmBObj == null) {
                sendError(id, "Missing required parameter: algorithmB")
                return
            }
            
            val algorithmBNumber = algorithmBObj.get("algorithmNumber")?.asInt
            if (algorithmBNumber == null) {
                sendError(id, "Missing algorithmB.algorithmNumber")
                return
            }
            
            val algorithmBParams = algorithmBObj.get("parameters")?.asJsonObject?.entrySet()?.associate {
                it.key to it.value.asString
            } ?: emptyMap()
            val algorithmBBypass = algorithmBObj.get("bypass")?.asBoolean ?: false
            val algorithmBMix = algorithmBObj.get("mix")?.asDouble ?: 1.0
            
            // Parse routing
            val routingObj = arguments?.get("routing")?.asJsonObject
            val routingMode = routingObj?.get("mode")?.asString?.let { 
                try { RoutingMode.valueOf(it) } catch (e: Exception) { RoutingMode.PARALLEL }
            } ?: RoutingMode.PARALLEL
            val spilloverTime = routingObj?.get("spilloverTime")?.asDouble ?: 2.0
            val routing = H90Routing(routingMode, spilloverTime)
            
            // Parse global parameters
            val globalObj = arguments?.get("globalParameters")?.asJsonObject
            val globalParams = H90GlobalParameters(
                tempo = globalObj?.get("tempo")?.asDouble ?: 120.0,
                tempoSync = globalObj?.get("tempoSync")?.asBoolean ?: false,
                killDry = globalObj?.get("killDry")?.asBoolean ?: false,
                presetMix = globalObj?.get("programMix")?.asDouble ?: 1.0,
                expressionPedal = globalObj?.get("expressionPedal")?.asDouble ?: 0.0
            )
            
            // Create H90 program
            val program = H90Preset.create(
                name = name,
                algorithmANumber = algorithmANumber,
                algorithmBNumber = algorithmBNumber,
                algorithmAParams = algorithmAParams,
                algorithmBParams = algorithmBParams,
                routing = routing,
                globalParams = globalParams
            )
            
            // Generate program file
            val programBytes = EventideH90PresetGenerator.generatePreset(program)
            val base64Data = java.util.Base64.getEncoder().encodeToString(programBytes)
            
            // Get algorithm info for response
            val algorithmAInfo = EventideH90AlgorithmMappings.getAlgorithmInfo(algorithmANumber)
            val algorithmBInfo = EventideH90AlgorithmMappings.getAlgorithmInfo(algorithmBNumber)
            
            sendResponse("tools/call", mapOf(
                "content" to listOf(mapOf(
                    "type" to "text",
                    "text" to buildString {
                        appendLine("✅ H90 Program Generated Successfully!")
                        appendLine("Program Name: $name")
                        appendLine("Algorithm A: ${algorithmAInfo?.name ?: "Unknown"} (${algorithmAInfo?.category ?: "N/A"})")
                        appendLine("Algorithm B: ${algorithmBInfo?.name ?: "Unknown"} (${algorithmBInfo?.category ?: "N/A"})")
                        appendLine("Routing: ${routing.mode}")
                        appendLine("File Size: ${programBytes.size} bytes")
                        appendLine()
                        appendLine("Base64 Program Data:")
                        appendLine(base64Data)
                        appendLine()
                        appendLine("Save this data to a .pgm90 file to upload to your H90.")
                    }
                ))
            ), id)
            
        } catch (e: Exception) {
            logger.error(e) { "Failed to generate H90 program (id=$id)" }
            sendError(id, "Failed to generate H90 program: ${e.message}")
        }
    }
}