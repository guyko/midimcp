package com.guyko.pedals

import com.guyko.models.PedalModel
import com.guyko.models.CCParameter
import com.guyko.mcp.MCPServer
import com.guyko.persistence.PedalRepository

object MerisLVXLoader {
    
    fun loadMerisLVX(server: MCPServer) {
        val merisLVX = createMerisLVXPedal()
        server.pedalRepository.save(merisLVX)
    }
    
    private fun createMerisLVXPedal(): PedalModel {
        val parameters = listOf(
            // Engine Selection
            CCParameter(
                name = "Engine",
                ccNumber = 0,
                minValue = 0,
                maxValue = 5,
                description = "Delay engine type: 0=Vintage, 1=Tape, 2=Digital, 3=Sweep, 4=Reverse, 5=Pitch",
                category = "Engine"
            ),
            
            // Global Controls
            CCParameter(
                name = "Mix",
                ccNumber = 1,
                minValue = 0,
                maxValue = 127,
                description = "Wet/dry mix balance",
                unit = "%",
                category = "Global"
            ),
            
            CCParameter(
                name = "Level",
                ccNumber = 2,
                minValue = 0,
                maxValue = 127,
                description = "Output level",
                unit = "dB",
                category = "Global"
            ),
            
            // Delay Controls
            CCParameter(
                name = "Time",
                ccNumber = 3,
                minValue = 0,
                maxValue = 127,
                description = "Delay time",
                unit = "ms",
                category = "Delay"
            ),
            
            CCParameter(
                name = "Feedback",
                ccNumber = 4,
                minValue = 0,
                maxValue = 127,
                description = "Delay feedback amount",
                unit = "%",
                category = "Delay"
            ),
            
            CCParameter(
                name = "Filter",
                ccNumber = 5,
                minValue = 0,
                maxValue = 127,
                description = "Delay filter frequency",
                unit = "Hz",
                category = "Delay"
            ),
            
            // Low Cut
            CCParameter(
                name = "Low Cut",
                ccNumber = 6,
                minValue = 0,
                maxValue = 127,
                description = "High-pass filter for delay signal",
                unit = "Hz",
                category = "Filter"
            ),
            
            // Modulation
            CCParameter(
                name = "Mod Rate",
                ccNumber = 7,
                minValue = 0,
                maxValue = 127,
                description = "Modulation rate",
                unit = "Hz",
                category = "Modulation"
            ),
            
            CCParameter(
                name = "Mod Depth",
                ccNumber = 8,
                minValue = 0,
                maxValue = 127,
                description = "Modulation depth",
                unit = "%",
                category = "Modulation"
            ),
            
            // Stereo Controls
            CCParameter(
                name = "Stereo Width",
                ccNumber = 9,
                minValue = 0,
                maxValue = 127,
                description = "Stereo field width",
                unit = "%",
                category = "Stereo"
            ),
            
            CCParameter(
                name = "Ping Pong",
                ccNumber = 10,
                minValue = 0,
                maxValue = 127,
                description = "Ping pong delay amount",
                unit = "%",
                category = "Stereo"
            ),
            
            // Advanced Controls
            CCParameter(
                name = "Diffusion",
                ccNumber = 11,
                minValue = 0,
                maxValue = 127,
                description = "Delay signal diffusion",
                unit = "%",
                category = "Advanced"
            ),
            
            CCParameter(
                name = "Smear",
                ccNumber = 12,
                minValue = 0,
                maxValue = 127,
                description = "Delay signal smearing",
                unit = "%",
                category = "Advanced"
            ),
            
            CCParameter(
                name = "Drive",
                ccNumber = 13,
                minValue = 0,
                maxValue = 127,
                description = "Input drive amount",
                unit = "%",
                category = "Drive"
            ),
            
            // Tap Tempo
            CCParameter(
                name = "Tap Tempo",
                ccNumber = 14,
                minValue = 0,
                maxValue = 127,
                description = "Tap tempo (momentary)",
                category = "Timing"
            ),
            
            // Preset Controls
            CCParameter(
                name = "Preset",
                ccNumber = 15,
                minValue = 0,
                maxValue = 15,
                description = "Preset selection (0-15)",
                category = "Preset"
            ),
            
            // Expression Controls
            CCParameter(
                name = "Expression",
                ccNumber = 16,
                minValue = 0,
                maxValue = 127,
                description = "Expression pedal input",
                unit = "%",
                category = "Expression"
            ),
            
            // Bypass
            CCParameter(
                name = "Bypass",
                ccNumber = 102,
                minValue = 0,
                maxValue = 127,
                description = "Bypass control (0-63 = bypass, 64-127 = active)",
                category = "Control"
            ),
            
            // Freeze
            CCParameter(
                name = "Freeze",
                ccNumber = 103,
                minValue = 0,
                maxValue = 127,
                description = "Freeze delay signal",
                category = "Control"
            )
        )
        
        return PedalModel(
            id = "meris_lvx",
            manufacturer = "Meris",
            modelName = "LVX",
            version = "1.0.2b",
            midiChannel = 1,
            parameters = parameters,
            description = "Meris LVX Modular Delay System - Advanced stereo delay with multiple engines"
        )
    }
}