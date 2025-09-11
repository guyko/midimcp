package com.guyko.pedals

import com.google.gson.Gson
import com.google.gson.JsonObject
import java.io.File

/**
 * Complete H90 algorithm mappings with parameter details for AI translation
 */
object EventideH90AlgorithmMappings {
    
    data class AlgorithmInfo(
        val name: String,
        val category: String,
        val description: String,
        val keyParameters: Map<String, ParameterInfo>
    )
    
    data class ParameterInfo(
        val displayName: String,
        val description: String,
        val range: String,
        val musicalFunction: String
    )
    
    // Algorithms loaded from JSON at runtime - no fallback
    
    /**
     * Get algorithm info by algorithm number
     */
    fun getAlgorithmInfo(algorithmNumber: Int): AlgorithmInfo? {
        return getAllAlgorithms()[algorithmNumber]
    }
    
    /**
     * Get algorithms by category
     */
    fun getAlgorithmsByCategory(category: String): Map<Int, AlgorithmInfo> {
        return getAllAlgorithms().filter { it.value.category == category }
    }
    
    /**
     * Search algorithms by name
     */
    fun findAlgorithmByName(name: String): Pair<Int, AlgorithmInfo>? {
        return getAllAlgorithms().entries.find { it.value.name.equals(name, ignoreCase = true) }?.let { it.key to it.value }
    }
    
    /**
     * Get all categories
     */
    fun getCategories(): Set<String> {
        return getAllAlgorithms().values.map { it.category }.toSet()
    }
    
    /**
     * Suggest algorithm based on musical request
     */
    fun suggestAlgorithm(request: String): List<Pair<Int, AlgorithmInfo>> {
        val requestLower = request.lowercase()
        val suggestions = mutableListOf<Pair<Int, AlgorithmInfo>>()
        
        // Direct keyword matching
        when {
            "delay" in requestLower || "echo" in requestLower -> {
                suggestions.addAll(getAlgorithmsByCategory("Delay").toList())
            }
            "reverb" in requestLower || "space" in requestLower || "ambient" in requestLower -> {
                suggestions.addAll(getAlgorithmsByCategory("Reverb").toList())
            }
            "harmony" in requestLower || "pitch" in requestLower || "octave" in requestLower -> {
                suggestions.addAll(getAlgorithmsByCategory("Harmonizer").toList())
            }
            "chorus" in requestLower || "flange" in requestLower || "phase" in requestLower || "modulation" in requestLower -> {
                suggestions.addAll(getAlgorithmsByCategory("Modulation").toList())
            }
            "distortion" in requestLower || "overdrive" in requestLower || "fuzz" in requestLower -> {
                suggestions.addAll(getAlgorithmsByCategory("Distortion").toList())
            }
            "synth" in requestLower || "synthesizer" in requestLower -> {
                suggestions.addAll(getAlgorithmsByCategory("Synth").toList())
            }
        }
        
        return suggestions.distinctBy { it.first }
    }
    
    /**
     * Load algorithms from JSON configuration file
     */
    fun loadFromJson(jsonFilePath: String): Map<Int, AlgorithmInfo> {
        val jsonFile = File(jsonFilePath)
        if (!jsonFile.exists()) {
            throw IllegalStateException("H90 algorithms JSON file not found: $jsonFilePath - MCP server cannot start without algorithm definitions")
        }
        
        try {
            
            val gson = Gson()
            val jsonRoot = gson.fromJson(jsonFile.readText(), JsonObject::class.java)
            val algorithmsJson = jsonRoot.getAsJsonObject("algorithms")
            
            val loadedAlgorithms = mutableMapOf<Int, AlgorithmInfo>()
            
            for ((key, value) in algorithmsJson.entrySet()) {
                val algorithmNumber = key.toInt()
                val algorithmObj = value.asJsonObject
                
                val name = algorithmObj.get("name").asString
                val category = algorithmObj.get("category").asString
                val description = algorithmObj.get("description").asString
                
                val parameters = mutableMapOf<String, ParameterInfo>()
                val parametersObj = algorithmObj.getAsJsonObject("parameters")
                
                for ((paramKey, paramValue) in parametersObj.entrySet()) {
                    val paramObj = paramValue.asJsonObject
                    parameters[paramKey] = ParameterInfo(
                        displayName = paramObj.get("displayName").asString,
                        description = paramObj.get("description").asString,
                        range = paramObj.get("range").asString,
                        musicalFunction = paramObj.get("musicalFunction").asString
                    )
                }
                
                loadedAlgorithms[algorithmNumber] = AlgorithmInfo(
                    name = name,
                    category = category,
                    description = description,
                    keyParameters = parameters
                )
            }
            
            // Return loaded algorithms directly
            return loadedAlgorithms
            
        } catch (e: Exception) {
            throw IllegalStateException("Error loading H90 algorithms from JSON: ${e.message} - MCP server cannot start", e)
        }
    }
    
    /**
     * Get the complete algorithm set (loads from JSON by default)
     */
    fun getAllAlgorithms(): Map<Int, AlgorithmInfo> {
        return loadFromJson("data/pedals/h90_algorithms.json")
    }
}