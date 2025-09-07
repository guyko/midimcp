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
    
    init {
        Files.createDirectories(Paths.get(dataDir))
    }
    
    fun save(pedal: PedalModel) {
        val file = File(dataDir, "${pedal.id}.json")
        file.writeText(gson.toJson(pedal))
    }
    
    fun load(pedalId: String): PedalModel? {
        val file = File(dataDir, "$pedalId.json")
        if (!file.exists()) return null
        
        return try {
            gson.fromJson(file.readText(), PedalModel::class.java)
        } catch (e: Exception) {
            null
        }
    }
    
    fun listAll(): List<PedalModel> {
        val dir = File(dataDir)
        return dir.listFiles { file -> file.extension == "json" }
            ?.mapNotNull { file -> 
                val pedalId = file.nameWithoutExtension
                load(pedalId)
            } ?: emptyList()
    }
    
    fun delete(pedalId: String): Boolean {
        val file = File(dataDir, "$pedalId.json")
        return file.delete()
    }
}