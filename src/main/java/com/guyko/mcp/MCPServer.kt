package com.guyko.mcp

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.guyko.models.PedalModel
import com.guyko.models.CCParameter
import com.guyko.models.MidiCommand
import com.guyko.models.MidiProgramChange
import com.guyko.persistence.PedalRepository
import com.guyko.midi.MidiExecutor
import com.guyko.midi.HardwareMidiExecutor
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter

class MCPServer(
    val pedalRepository: PedalRepository = PedalRepository(),
    private val midiExecutor: MidiExecutor = HardwareMidiExecutor()
) {
    private val gson = Gson()
    private val reader = BufferedReader(InputStreamReader(System.`in`))
    private val writer = PrintWriter(System.out, true)
    
    fun start() {
        sendInitialHandshake()
        processMessages()
    }
    
    private fun sendInitialHandshake() {
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
        
        sendResponse("initialize", serverInfo)
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
                    "tools/list" -> handleToolsList(id)
                    "tools/call" -> handleToolCall(id, params)
                    else -> sendError(id, "Unknown method: $method")
                }
            } catch (e: Exception) {
                sendError(null, "Error processing message: ${e.message}")
            }
        }
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
            )
        )
        
        sendResponse("tools/list", mapOf("tools" to tools), id)
    }
    
    private fun handleToolCall(id: Int?, params: JsonObject?) {
        if (params == null) {
            sendError(id, "Missing parameters")
            return
        }
        
        val toolName = params.get("name")?.asString
        val arguments = params.get("arguments")?.asJsonObject
        
        when (toolName) {
            "add_pedal" -> handleAddPedal(id, arguments)
            "get_pedal" -> handleGetPedal(id, arguments)
            "list_pedals" -> handleListPedals(id)
            "execute_midi_command" -> handleExecuteMidiCommand(id, arguments)
            "execute_midi_commands" -> handleExecuteMidiCommands(id, arguments)
            "execute_program_change" -> handleExecuteProgramChange(id, arguments)
            "get_midi_status" -> handleGetMidiStatus(id)
            else -> sendError(id, "Unknown tool: $toolName")
        }
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
            
            val pedalId = "${manufacturer.toLowerCase()}_${modelName.toLowerCase().replace(" ", "_")}"
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
                sendError(id, "Missing required parameters")
                return
            }
            
            val pedal = pedalRepository.load(pedalId)
            if (pedal == null) {
                sendError(id, "Pedal not found: $pedalId")
                return
            }
            
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
            
            sendResponse("tools/call", mapOf(
                "content" to listOf(mapOf(
                    "type" to "text",
                    "text" to buildString {
                        appendln("MIDI Command Execution:")
                        appendln("Status: ${if (result.success) "SUCCESS" else "FAILED"}")
                        appendln("Pedal: ${pedal.manufacturer} ${pedal.modelName}")
                        appendln("Parameter: $parameterName (CC $ccNumber)")
                        appendln("Value: $value")
                        appendln("Channel: ${pedal.midiChannel}")
                        appendln("Message: ${result.message}")
                        if (result.executedCommand != null) {
                            val hexString = result.executedCommand.toMidiBytes().joinToString(" ") { "%02X".format(it) }
                            appendln("MIDI Bytes: $hexString")
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
                        appendln("MIDI Commands Execution:")
                        appendln("Pedal: ${pedal.manufacturer} ${pedal.modelName}")
                        appendln("Commands executed: ${results.size}")
                        appendln("Successful: ${results.count { it.success }}")
                        appendln("Failed: ${results.count { !it.success }}")
                        appendln()
                        results.forEachIndexed { index, result ->
                            appendln("Command ${index + 1}:")
                            appendln("  Status: ${if (result.success) "SUCCESS" else "FAILED"}")
                            appendln("  Message: ${result.message}")
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
                        appendln("MIDI Program Change Execution:")
                        appendln("Status: ${if (result.success) "SUCCESS" else "FAILED"}")
                        appendln("Pedal: ${pedal.manufacturer} ${pedal.modelName}")
                        appendln("Program: $program")
                        appendln("Channel: ${pedal.midiChannel}")
                        appendln("Message: ${result.message}")
                        val hexString = programChange.toMidiBytes().joinToString(" ") { "%02X".format(it) }
                        appendln("MIDI Bytes: $hexString")
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
                        appendln("MIDI Executor Status:")
                        appendln("Available: $isAvailable")
                        appendln("Status: $status")
                    }
                ))
            ), id)
        } catch (e: Exception) {
            sendError(id, "Error getting MIDI status: ${e.message}")
        }
    }
    
    
    private fun sendResponse(method: String, result: Any, id: Int? = null) {
        val response = mutableMapOf<String, Any>(
            "jsonrpc" to "2.0",
            "result" to result
        )
        if (id != null) {
            response["id"] = id
        }
        writer.println(gson.toJson(response))
    }
    
    private fun sendError(id: Int?, message: String) {
        val error = mapOf(
            "jsonrpc" to "2.0",
            "error" to mapOf(
                "code" to -1,
                "message" to message
            ),
            "id" to id
        )
        writer.println(gson.toJson(error))
    }
}