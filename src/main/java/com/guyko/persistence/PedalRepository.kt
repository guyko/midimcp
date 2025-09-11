package com.guyko.persistence

import com.guyko.models.PedalModel
import com.guyko.models.CCParameter
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

class PedalRepository(private val dataDir: String = "data/pedals") {
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val pedals = mutableMapOf<String, PedalModel>()
    
    init {
        Files.createDirectories(Paths.get(dataDir))
        loadAllPedals()
    }
    
    /**
     * Load all JSON files from the data directory on startup
     */
    private fun loadAllPedals() {
        val dir = File(dataDir)
        dir.listFiles { file -> 
            file.extension == "json" && 
            !file.name.startsWith("h90_algorithms") // Skip algorithm mapping files
        }?.forEach { file ->
            try {
                val pedal = gson.fromJson(file.readText(), PedalModel::class.java)
                pedals[pedal.id] = pedal
                // Silent loading - no stdout output for Claude Desktop compatibility
            } catch (e: Exception) {
                // Silent error handling - log to stderr if needed
                System.err.println("Failed to load pedal from ${file.name}: ${e.message}")
            }
        }
    }
    
    fun save(pedal: PedalModel) {
        val file = File(dataDir, "${pedal.id}.json")
        file.writeText(gson.toJson(pedal))
        // Update in-memory cache
        pedals[pedal.id] = pedal
    }
    
    fun load(pedalId: String): PedalModel? {
        return pedals[pedalId]
    }
    
    fun listAll(): List<PedalModel> {
        return pedals.values.toList()
    }
    
    fun delete(pedalId: String): Boolean {
        val file = File(dataDir, "$pedalId.json")
        val deleted = file.delete()
        if (deleted) {
            pedals.remove(pedalId)
        }
        return deleted
    }
}