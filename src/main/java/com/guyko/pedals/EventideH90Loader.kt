package com.guyko.pedals

import com.guyko.models.PedalModel
import com.guyko.models.CCParameter
import com.guyko.mcp.MCPServer
import com.guyko.persistence.PedalRepository

object EventideH90Loader {
    
    fun loadEventideH90(server: MCPServer) {
        val eventideH90 = createEventideH90Pedal()
        server.pedalRepository.save(eventideH90)
    }
    
    private fun createEventideH90Pedal(): PedalModel {
        val parameters = listOf(
            // Global Parameters
            CCParameter(
                name = "Program Change",
                ccNumber = 0,
                minValue = 0,
                maxValue = 127,
                description = "Select H90 Program (0-99)",
                category = "Global"
            ),
            
            CCParameter(
                name = "Preset Mix",
                ccNumber = 1,
                minValue = 0,
                maxValue = 127,
                description = "Overall wet/dry mix for the program",
                category = "Global"
            ),
            
            CCParameter(
                name = "Bypass",
                ccNumber = 2,
                minValue = 0,
                maxValue = 127,
                description = "Program bypass (0-63=bypass, 64-127=active)",
                category = "Global"
            ),
            
            CCParameter(
                name = "Tap Tempo",
                ccNumber = 3,
                minValue = 0,
                maxValue = 127,
                description = "Tap tempo input (send 127 to tap)",
                category = "Global"
            ),
            
            // Algorithm A Parameters
            CCParameter(
                name = "Algorithm A Select",
                ccNumber = 10,
                minValue = 0,
                maxValue = 127,
                description = "Select algorithm for slot A. Categories: Delay(0-12), Distortion(13-17), EQ(18), Harmonizer(19-33), Looper(34), Modulation(35-47), Multi(48), Reverb(49-62), Synth(63-65), Utility(66-67)",
                category = "Algorithm A"
            ),
            
            CCParameter(
                name = "Algorithm A Mix",
                ccNumber = 11,
                minValue = 0,
                maxValue = 127,
                description = "Mix level for Algorithm A",
                category = "Algorithm A"
            ),
            
            CCParameter(
                name = "Algorithm A Bypass",
                ccNumber = 12,
                minValue = 0,
                maxValue = 127,
                description = "Algorithm A bypass (0-63=bypass, 64-127=active)",
                category = "Algorithm A"
            ),
            
            // Algorithm A Parameter Controls (will map to different controls based on selected algorithm)
            CCParameter(
                name = "Algorithm A Param 1",
                ccNumber = 13,
                minValue = 0,
                maxValue = 127,
                description = "First parameter for Algorithm A (function depends on selected algorithm)",
                category = "Algorithm A"
            ),
            
            CCParameter(
                name = "Algorithm A Param 2",
                ccNumber = 14,
                minValue = 0,
                maxValue = 127,
                description = "Second parameter for Algorithm A (function depends on selected algorithm)",
                category = "Algorithm A"
            ),
            
            CCParameter(
                name = "Algorithm A Param 3",
                ccNumber = 15,
                minValue = 0,
                maxValue = 127,
                description = "Third parameter for Algorithm A (function depends on selected algorithm)",
                category = "Algorithm A"
            ),
            
            CCParameter(
                name = "Algorithm A Param 4",
                ccNumber = 16,
                minValue = 0,
                maxValue = 127,
                description = "Fourth parameter for Algorithm A (function depends on selected algorithm)",
                category = "Algorithm A"
            ),
            
            CCParameter(
                name = "Algorithm A Param 5",
                ccNumber = 17,
                minValue = 0,
                maxValue = 127,
                description = "Fifth parameter for Algorithm A (function depends on selected algorithm)",
                category = "Algorithm A"
            ),
            
            // Algorithm B Parameters
            CCParameter(
                name = "Algorithm B Select",
                ccNumber = 20,
                minValue = 0,
                maxValue = 127,
                description = "Select algorithm for slot B. Categories: Delay(0-12), Distortion(13-17), EQ(18), Harmonizer(19-33), Looper(34), Modulation(35-47), Multi(48), Reverb(49-62), Synth(63-65), Utility(66-67)",
                category = "Algorithm B"
            ),
            
            CCParameter(
                name = "Algorithm B Mix",
                ccNumber = 21,
                minValue = 0,
                maxValue = 127,
                description = "Mix level for Algorithm B",
                category = "Algorithm B"
            ),
            
            CCParameter(
                name = "Algorithm B Bypass",
                ccNumber = 22,
                minValue = 0,
                maxValue = 127,
                description = "Algorithm B bypass (0-63=bypass, 64-127=active)",
                category = "Algorithm B"
            ),
            
            // Algorithm B Parameter Controls
            CCParameter(
                name = "Algorithm B Param 1",
                ccNumber = 23,
                minValue = 0,
                maxValue = 127,
                description = "First parameter for Algorithm B (function depends on selected algorithm)",
                category = "Algorithm B"
            ),
            
            CCParameter(
                name = "Algorithm B Param 2",
                ccNumber = 24,
                minValue = 0,
                maxValue = 127,
                description = "Second parameter for Algorithm B (function depends on selected algorithm)",
                category = "Algorithm B"
            ),
            
            CCParameter(
                name = "Algorithm B Param 3",
                ccNumber = 25,
                minValue = 0,
                maxValue = 127,
                description = "Third parameter for Algorithm B (function depends on selected algorithm)",
                category = "Algorithm B"
            ),
            
            CCParameter(
                name = "Algorithm B Param 4",
                ccNumber = 26,
                minValue = 0,
                maxValue = 127,
                description = "Fourth parameter for Algorithm B (function depends on selected algorithm)",
                category = "Algorithm B"
            ),
            
            CCParameter(
                name = "Algorithm B Param 5",
                ccNumber = 27,
                minValue = 0,
                maxValue = 127,
                description = "Fifth parameter for Algorithm B (function depends on selected algorithm)",
                category = "Algorithm B"
            ),
            
            // Routing and Performance Parameters
            CCParameter(
                name = "Routing Mode",
                ccNumber = 30,
                minValue = 0,
                maxValue = 127,
                description = "Signal routing: 0-31=Series A→B, 32-63=Series B→A, 64-95=Parallel, 96-127=Series with crossfade",
                category = "Routing"
            ),
            
            CCParameter(
                name = "Expression Pedal",
                ccNumber = 4,
                minValue = 0,
                maxValue = 127,
                description = "Expression pedal input (mappable to various parameters)",
                category = "Expression"
            ),
            
            CCParameter(
                name = "HotSwitch 1",
                ccNumber = 31,
                minValue = 0,
                maxValue = 127,
                description = "HotSwitch 1 (programmable switch for performance control)",
                category = "Performance"
            ),
            
            CCParameter(
                name = "HotSwitch 2",
                ccNumber = 32,
                minValue = 0,
                maxValue = 127,
                description = "HotSwitch 2 (programmable switch for performance control)",
                category = "Performance"
            ),
            
            CCParameter(
                name = "HotSwitch 3",
                ccNumber = 33,
                minValue = 0,
                maxValue = 127,
                description = "HotSwitch 3 (programmable switch for performance control)",
                category = "Performance"
            ),
            
            CCParameter(
                name = "Kill Dry",
                ccNumber = 34,
                minValue = 0,
                maxValue = 127,
                description = "Kill dry signal (0-63=dry on, 64-127=dry off - wet only)",
                category = "Global"
            )
        )
        
        return PedalModel(
            id = "eventide_h90",
            manufacturer = "Eventide",
            modelName = "H90 Harmonizer",
            version = "1.11.4",
            midiChannel = 8,
            parameters = parameters,
            description = "Eventide H90 Harmonizer - Dual-algorithm harmonizer with 70+ algorithms across 9 categories. Supports complex routing (series, parallel), real-time algorithm switching, and expression pedal control. Features legendary Eventide pitch shifting, delays, reverbs, modulation, and unique multi-effects. Each program runs two algorithms simultaneously with independent parameter control. " +
                         "Algorithm Categories: Delay (${EventideH90AlgorithmMappings.getAlgorithmsByCategory("Delay").size} algorithms), " +
                         "Distortion (${EventideH90AlgorithmMappings.getAlgorithmsByCategory("Distortion").size} algorithms), " +
                         "EQ (${EventideH90AlgorithmMappings.getAlgorithmsByCategory("EQ").size} algorithms), " +
                         "Harmonizer (${EventideH90AlgorithmMappings.getAlgorithmsByCategory("Harmonizer").size} algorithms), " +
                         "Looper (${EventideH90AlgorithmMappings.getAlgorithmsByCategory("Looper").size} algorithms), " +
                         "Modulation (${EventideH90AlgorithmMappings.getAlgorithmsByCategory("Modulation").size} algorithms), " +
                         "Multi (${EventideH90AlgorithmMappings.getAlgorithmsByCategory("Multi").size} algorithms), " +
                         "Reverb (${EventideH90AlgorithmMappings.getAlgorithmsByCategory("Reverb").size} algorithms), " +
                         "Synth (${EventideH90AlgorithmMappings.getAlgorithmsByCategory("Synth").size} algorithms), " +
                         "Utility (${EventideH90AlgorithmMappings.getAlgorithmsByCategory("Utility").size} algorithms)"
        )
    }
    
    /**
     * Get algorithm number by name for CC commands
     */
    fun getAlgorithmNumber(algorithmName: String): Int? {
        return EventideH90AlgorithmMappings.findAlgorithmByName(algorithmName)?.first
    }
    
    /**
     * Get algorithm info by number
     */
    fun getAlgorithmInfo(algorithmNumber: Int): EventideH90AlgorithmMappings.AlgorithmInfo? {
        return EventideH90AlgorithmMappings.getAlgorithmInfo(algorithmNumber)
    }
    
    /**
     * Get all algorithms in a category
     */
    fun getAlgorithmsByCategory(category: String): Map<Int, EventideH90AlgorithmMappings.AlgorithmInfo> {
        return EventideH90AlgorithmMappings.getAlgorithmsByCategory(category)
    }
    
    /**
     * Suggest algorithms for a musical request
     */
    fun suggestAlgorithms(request: String): List<Pair<Int, EventideH90AlgorithmMappings.AlgorithmInfo>> {
        return EventideH90AlgorithmMappings.suggestAlgorithm(request)
    }
    
    /**
     * Get all available categories
     */
    fun getCategories(): Set<String> {
        return EventideH90AlgorithmMappings.getCategories()
    }
}