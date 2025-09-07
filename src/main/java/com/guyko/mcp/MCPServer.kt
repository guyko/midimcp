package com.guyko.mcp

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.guyko.models.PedalModel
import com.guyko.models.CCParameter
import com.guyko.models.MidiCommand
import com.guyko.persistence.PedalRepository
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter

class MCPServer(val pedalRepository: PedalRepository = PedalRepository()) {
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
                "name" to "generate_midi_command",
                "description" to "Generate a MIDI command for a specific parameter change",
                "inputSchema" to mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "pedalId" to mapOf("type" to "string"),
                        "parameterName" to mapOf("type" to "string"),
                        "value" to mapOf("type" to "integer", "minimum" to 0, "maximum" to 127)
                    ),
                    "required" to listOf("pedalId", "parameterName", "value")
                )
            ),
            mapOf(
                "name" to "interpret_sound_request",
                "description" to "Interpret a natural language sound request and suggest parameter changes",
                "inputSchema" to mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "pedalId" to mapOf("type" to "string"),
                        "request" to mapOf("type" to "string", "description" to "Natural language description of desired sound change")
                    ),
                    "required" to listOf("pedalId", "request")
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
            "generate_midi_command" -> handleGenerateMidiCommand(id, arguments)
            "interpret_sound_request" -> handleInterpretSoundRequest(id, arguments)
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
    
    private fun handleGenerateMidiCommand(id: Int?, arguments: JsonObject?) {
        try {
            val pedalId = arguments?.get("pedalId")?.asString
            val parameterName = arguments?.get("parameterName")?.asString
            val value = arguments?.get("value")?.asInt
            
            if (pedalId == null || parameterName == null || value == null) {
                sendError(id, "Missing required parameters")
                return
            }
            
            val pedal = pedalRepository.load(pedalId)
            if (pedal == null) {
                sendError(id, "Pedal not found: $pedalId")
                return
            }
            
            val parameter = pedal.getParameterByName(parameterName)
            if (parameter == null) {
                sendError(id, "Parameter not found: $parameterName")
                return
            }
            
            val midiCommand = MidiCommand(
                channel = pedal.midiChannel,
                ccNumber = parameter.ccNumber,
                value = value.coerceIn(parameter.minValue, parameter.maxValue),
                parameterName = parameter.name,
                description = "Set ${parameter.name} to $value"
            )
            
            val midiBytes = midiCommand.toMidiBytes()
            val hexString = midiBytes.joinToString(" ") { "%02X".format(it) }
            
            sendResponse("tools/call", mapOf(
                "content" to listOf(mapOf(
                    "type" to "text",
                    "text" to "MIDI Command: $hexString\nChannel: ${midiCommand.channel}, CC: ${midiCommand.ccNumber}, Value: ${midiCommand.value}\nDescription: ${midiCommand.description}"
                ))
            ), id)
        } catch (e: Exception) {
            sendError(id, "Error generating MIDI command: ${e.message}")
        }
    }
    
    private fun handleInterpretSoundRequest(id: Int?, arguments: JsonObject?) {
        try {
            val pedalId = arguments?.get("pedalId")?.asString
            val request = arguments?.get("request")?.asString
            
            if (pedalId == null || request == null) {
                sendError(id, "Missing required parameters")
                return
            }
            
            val pedal = pedalRepository.load(pedalId)
            if (pedal == null) {
                sendError(id, "Pedal not found: $pedalId")
                return
            }
            
            val suggestions = interpretSoundRequest(request, pedal)
            
            sendResponse("tools/call", mapOf(
                "content" to listOf(mapOf(
                    "type" to "text",
                    "text" to suggestions
                ))
            ), id)
        } catch (e: Exception) {
            sendError(id, "Error interpreting sound request: ${e.message}")
        }
    }
    
    private fun interpretSoundRequest(request: String, pedal: PedalModel): String {
        val lowerRequest = request.toLowerCase()
        val suggestions = mutableListOf<String>()
        
        // Common sound descriptors and their parameter mappings
        when {
            lowerRequest.contains("brighter") || lowerRequest.contains("bright") -> {
                suggestions.add("• Increase Filter (CC ${pedal.getParameterByName("Filter")?.ccNumber}) to brighten the delay")
                suggestions.add("• Reduce Low Cut (CC ${pedal.getParameterByName("Low Cut")?.ccNumber}) to let more highs through")
            }
            lowerRequest.contains("darker") || lowerRequest.contains("warm") -> {
                suggestions.add("• Decrease Filter (CC ${pedal.getParameterByName("Filter")?.ccNumber}) to darken the delay")
                suggestions.add("• Increase Low Cut (CC ${pedal.getParameterByName("Low Cut")?.ccNumber}) to remove harsh highs")
            }
            lowerRequest.contains("more delay") || lowerRequest.contains("longer") -> {
                suggestions.add("• Increase Time (CC ${pedal.getParameterByName("Time")?.ccNumber}) for longer delay")
                suggestions.add("• Increase Feedback (CC ${pedal.getParameterByName("Feedback")?.ccNumber}) for more repeats")
            }
            lowerRequest.contains("less delay") || lowerRequest.contains("shorter") -> {
                suggestions.add("• Decrease Time (CC ${pedal.getParameterByName("Time")?.ccNumber}) for shorter delay")
                suggestions.add("• Decrease Feedback (CC ${pedal.getParameterByName("Feedback")?.ccNumber}) for fewer repeats")
            }
            lowerRequest.contains("spacey") || lowerRequest.contains("ambient") -> {
                suggestions.add("• Increase Mix (CC ${pedal.getParameterByName("Mix")?.ccNumber}) for more wet signal")
                suggestions.add("• Increase Diffusion (CC ${pedal.getParameterByName("Diffusion")?.ccNumber}) for spaciousness")
                suggestions.add("• Add some Mod Depth (CC ${pedal.getParameterByName("Mod Depth")?.ccNumber}) for movement")
            }
            lowerRequest.contains("slapback") || lowerRequest.contains("rockabilly") -> {
                suggestions.add("• Set Time (CC ${pedal.getParameterByName("Time")?.ccNumber}) to around 25-40 (80-120ms)")
                suggestions.add("• Set Feedback (CC ${pedal.getParameterByName("Feedback")?.ccNumber}) to low (10-30)")
                suggestions.add("• Set Mix (CC ${pedal.getParameterByName("Mix")?.ccNumber}) to around 20-40")
            }
            lowerRequest.contains("tape") || lowerRequest.contains("vintage") -> {
                suggestions.add("• Set Engine (CC ${pedal.getParameterByName("Engine")?.ccNumber}) to 1 for Tape engine")
                suggestions.add("• Add some Drive (CC ${pedal.getParameterByName("Drive")?.ccNumber}) for tape saturation")
                suggestions.add("• Reduce Filter (CC ${pedal.getParameterByName("Filter")?.ccNumber}) for vintage darkness")
            }
            lowerRequest.contains("clean") || lowerRequest.contains("digital") -> {
                suggestions.add("• Set Engine (CC ${pedal.getParameterByName("Engine")?.ccNumber}) to 2 for Digital engine")
                suggestions.add("• Set Drive (CC ${pedal.getParameterByName("Drive")?.ccNumber}) to minimum")
                suggestions.add("• Set Filter (CC ${pedal.getParameterByName("Filter")?.ccNumber}) high for clarity")
            }
            lowerRequest.contains("modulated") || lowerRequest.contains("chorus") -> {
                suggestions.add("• Increase Mod Rate (CC ${pedal.getParameterByName("Mod Rate")?.ccNumber}) for chorus-like movement")
                suggestions.add("• Increase Mod Depth (CC ${pedal.getParameterByName("Mod Depth")?.ccNumber}) for stronger modulation")
            }
            lowerRequest.contains("wide") || lowerRequest.contains("stereo") -> {
                suggestions.add("• Increase Stereo Width (CC ${pedal.getParameterByName("Stereo Width")?.ccNumber}) for wider image")
                suggestions.add("• Add Ping Pong (CC ${pedal.getParameterByName("Ping Pong")?.ccNumber}) for stereo movement")
            }
        }
        
        if (suggestions.isEmpty()) {
            suggestions.add("I couldn't interpret your specific request. Try descriptions like:")
            suggestions.add("• 'Make it brighter/darker'")
            suggestions.add("• 'More/less delay'")
            suggestions.add("• 'Make it spacey/ambient'")
            suggestions.add("• 'Give me a slapback sound'")
            suggestions.add("• 'Make it sound like tape/vintage'")
            suggestions.add("• 'Add modulation/chorus'")
            suggestions.add("• 'Make it wider/stereo'")
        }
        
        return "Sound Request: \"$request\"\n\nSuggested parameter changes:\n${suggestions.joinToString("\n")}"
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