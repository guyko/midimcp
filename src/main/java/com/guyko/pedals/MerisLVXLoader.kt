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
            // CC 01 - Mix
            CCParameter(
                name = "Mix",
                ccNumber = 1,
                minValue = 0,
                maxValue = 127,
                description = "Wet/dry mix balance. ACTUAL VALUES: CC 0=dry only, CC 64=50% (too low for most uses), CC 84=66% (balanced dry+delay), CC 127=wet only. For balanced mix, use CC 84. The LVX mix control is logarithmic, so 66% sounds like equal dry/delay levels.",
                unit = "%",
                category = "Global"
            ),
            
            // CC 02 - Dry Trim
            CCParameter(
                name = "Dry Trim",
                ccNumber = 2,
                minValue = 0,
                maxValue = 127,
                description = "Dry signal level trim",
                unit = "dB",
                category = "Global"
            ),
            
            // CC 03 - Wet Trim
            CCParameter(
                name = "Wet Trim",
                ccNumber = 3,
                minValue = 0,
                maxValue = 127,
                description = "Wet signal level trim",
                unit = "dB",
                category = "Global"
            ),
            
            // CC 04 - Expression Pedal
            CCParameter(
                name = "Expression Pedal",
                ccNumber = 4,
                minValue = 0,
                maxValue = 127,
                description = "Expression pedal input",
                unit = "%",
                category = "Expression"
            ),
            
            // CC 05 - Preamp Type
            CCParameter(
                name = "Preamp Type",
                ccNumber = 5,
                minValue = 0,
                maxValue = 127,
                description = "Preamp/processing: 0-18=OFF (clean), 19-36=Volume Pedal, 37-54=Tube (warm), 55-73=Transistor (bright), 74-91=Op-Amp, 92-109=Drive (overdrive), 110-127=Bitcrusher (lo-fi)",
                category = "Preamp"
            ),
            
            // CC 06 - Preamp Location
            CCParameter(
                name = "Preamp Location",
                ccNumber = 6,
                minValue = 0,
                maxValue = 127,
                description = "0-31=PRE+DRY, 32-63=PRE, 64-95=FDBK, 96-127=POST",
                category = "Preamp"
            ),
            
            // CC 07-12 - Preamp Parameters 1-6
            CCParameter(
                name = "Preamp Parameter 1",
                ccNumber = 7,
                minValue = 0,
                maxValue = 127,
                description = "Preamp parameter 1",
                category = "Preamp"
            ),
            
            CCParameter(
                name = "Preamp Parameter 2",
                ccNumber = 8,
                minValue = 0,
                maxValue = 127,
                description = "Preamp parameter 2",
                category = "Preamp"
            ),
            
            CCParameter(
                name = "Preamp Parameter 3",
                ccNumber = 9,
                minValue = 0,
                maxValue = 127,
                description = "Preamp parameter 3",
                category = "Preamp"
            ),
            
            CCParameter(
                name = "Preamp Parameter 4",
                ccNumber = 10,
                minValue = 0,
                maxValue = 127,
                description = "Preamp parameter 4",
                category = "Preamp"
            ),
            
            CCParameter(
                name = "Preamp Parameter 5",
                ccNumber = 11,
                minValue = 0,
                maxValue = 127,
                description = "Preamp parameter 5",
                category = "Preamp"
            ),
            
            CCParameter(
                name = "Preamp Parameter 6",
                ccNumber = 12,
                minValue = 0,
                maxValue = 127,
                description = "Preamp parameter 6",
                category = "Preamp"
            ),
            
            // CC 13 - Delay Structure
            CCParameter(
                name = "Delay Structure",
                ccNumber = 13,
                minValue = 0,
                maxValue = 127,
                description = "0-21=Standard, 22-42=Multitap, 43-63=Multifilter, 64-85=Poly, 86-106=Reverse, 107-127=Series. WARNING: Claude Desktop may unintentionally change this when only timing/modulation changes were requested.",
                category = "Delay"
            ),
            
            // CC 14 - Bypass
            CCParameter(
                name = "Bypass",
                ccNumber = 14,
                minValue = 0,
                maxValue = 127,
                description = "0-63=FX Bypass, 64-127=FX Enable",
                category = "Control"
            ),
            
            // CC 15 - Delay Time
            CCParameter(
                name = "Delay Time",
                ccNumber = 15,
                minValue = 0,
                maxValue = 127,
                description = "Primary delay time (0=shortest ~1ms, 64≈400ms, 127≈2sec). This controls the main delay repeat timing.",
                unit = "ms",
                category = "Delay"
            ),
            
            // CC 16 - Delay Engine
            CCParameter(
                name = "Delay Engine",
                ccNumber = 16,
                minValue = 0,
                maxValue = 127,
                description = "Delay engine type: 0-42=Digital (pristine, modern), 43-85=BBD (analog bucket brigade, darker), 86-127=Tape (vintage tape echo, warm saturation)",
                category = "Delay"
            ),
            
            // CC 17 - Left Note Division
            CCParameter(
                name = "Left Note Division",
                ccNumber = 17,
                minValue = 0,
                maxValue = 127,
                description = "Note division for left channel. ACTUAL VALUES: CC 0=Off, CC 8=whole, CC 16=dotted half, CC 24=half, CC 32=triplet half, CC 40=dotted quarter, CC 48=quarter, CC 56=triplet quarter, CC 64=dotted eighth, incrementing by 8 for each subdivision type.",
                category = "Timing"
            ),
            
            // CC 18 - Right Note Division
            CCParameter(
                name = "Right Note Division",
                ccNumber = 18,
                minValue = 0,
                maxValue = 127,
                description = "Note division for right channel. ACTUAL VALUES: CC 0=Off, CC 8=whole, CC 16=dotted half, CC 24=half, CC 32=triplet half, CC 40=dotted quarter, CC 48=quarter, CC 56=triplet quarter, CC 64=dotted eighth, incrementing by 8 for each subdivision type.",
                category = "Timing"
            ),
            
            // CC 19 - Feedback
            CCParameter(
                name = "Feedback",
                ccNumber = 19,
                minValue = 0,
                maxValue = 127,
                description = "Delay feedback amount (0=single repeat, 32=few repeats, 64=moderate trails, 95=long decay, 127=infinite/self-oscillation)",
                unit = "%",
                category = "Delay"
            ),
            
            // CC 20 - Cross Feedback
            CCParameter(
                name = "Cross Feedback",
                ccNumber = 20,
                minValue = 0,
                maxValue = 127,
                description = "Cross feedback between L/R channels",
                unit = "%",
                category = "Delay"
            ),
            
            // CC 21 - Delay Modulation
            CCParameter(
                name = "Delay Modulation",
                ccNumber = 21,
                minValue = 0,
                maxValue = 127,
                description = "Built-in delay time wobble/warble (0=off, 32=subtle, 64=moderate, 127=extreme). This is SEPARATE from CC86 Mod Type. For general 'modulation' effects, use CC86 Mod Type=Chorus instead of this parameter.",
                unit = "%",
                category = "Modulation"
            ),
            
            // CC 22-61 - Delay Parameters 1-40
            CCParameter("Delay Parameter 1", 22, 0, 127, "Delay parameter 1", category = "Delay"),
            CCParameter("Delay Parameter 2", 23, 0, 127, "Delay parameter 2", category = "Delay"),
            CCParameter("Delay Parameter 3", 24, 0, 127, "Delay parameter 3", category = "Delay"),
            CCParameter("Delay Parameter 4", 25, 0, 127, "Delay parameter 4", category = "Delay"),
            CCParameter("Delay Parameter 5", 26, 0, 127, "Delay parameter 5", category = "Delay"),
            CCParameter("Delay Parameter 6", 27, 0, 127, "Delay parameter 6", category = "Delay"),
            CCParameter("Delay Parameter 7", 28, 0, 127, "Delay parameter 7", category = "Delay"),
            CCParameter("Delay Parameter 8", 29, 0, 127, "Delay parameter 8", category = "Delay"),
            CCParameter("Delay Parameter 9", 30, 0, 127, "Delay parameter 9", category = "Delay"),
            CCParameter("Delay Parameter 10", 31, 0, 127, "Delay parameter 10", category = "Delay"),
            CCParameter("Delay Parameter 11", 32, 0, 127, "Delay parameter 11", category = "Delay"),
            CCParameter("Delay Parameter 12", 33, 0, 127, "Delay parameter 12", category = "Delay"),
            CCParameter("Delay Parameter 13", 34, 0, 127, "Delay parameter 13", category = "Delay"),
            CCParameter("Delay Parameter 14", 35, 0, 127, "Delay parameter 14", category = "Delay"),
            CCParameter("Delay Parameter 15", 36, 0, 127, "Delay parameter 15", category = "Delay"),
            CCParameter("Delay Parameter 16", 37, 0, 127, "Delay parameter 16", category = "Delay"),
            CCParameter("Delay Parameter 17", 38, 0, 127, "Delay parameter 17", category = "Delay"),
            CCParameter("Delay Parameter 18", 39, 0, 127, "Delay parameter 18", category = "Delay"),
            CCParameter("Delay Parameter 19", 40, 0, 127, "Delay parameter 19", category = "Delay"),
            CCParameter("Delay Parameter 20", 41, 0, 127, "Delay parameter 20", category = "Delay"),
            CCParameter("Delay Parameter 21", 42, 0, 127, "Delay parameter 21", category = "Delay"),
            CCParameter("Delay Parameter 22", 43, 0, 127, "Delay parameter 22", category = "Delay"),
            CCParameter("Delay Parameter 23", 44, 0, 127, "Delay parameter 23", category = "Delay"),
            CCParameter("Delay Parameter 24", 45, 0, 127, "Delay parameter 24", category = "Delay"),
            CCParameter("Delay Parameter 25", 46, 0, 127, "Delay parameter 25", category = "Delay"),
            CCParameter("Delay Parameter 26", 47, 0, 127, "Delay parameter 26", category = "Delay"),
            CCParameter("Delay Parameter 27", 48, 0, 127, "Delay parameter 27", category = "Delay"),
            CCParameter("Delay Parameter 28", 49, 0, 127, "Delay parameter 28", category = "Delay"),
            CCParameter("Delay Parameter 29", 50, 0, 127, "Delay parameter 29", category = "Delay"),
            CCParameter("Delay Parameter 30", 51, 0, 127, "Delay parameter 30", category = "Delay"),
            CCParameter("Delay Parameter 31", 52, 0, 127, "Delay parameter 31", category = "Delay"),
            CCParameter("Delay Parameter 32", 53, 0, 127, "Delay parameter 32", category = "Delay"),
            CCParameter("Delay Parameter 33", 54, 0, 127, "Delay parameter 33", category = "Delay"),
            CCParameter("Delay Parameter 34", 55, 0, 127, "Delay parameter 34", category = "Delay"),
            CCParameter("Delay Parameter 35", 56, 0, 127, "Delay parameter 35", category = "Delay"),
            CCParameter("Delay Parameter 36", 57, 0, 127, "Delay parameter 36", category = "Delay"),
            CCParameter("Delay Parameter 37", 58, 0, 127, "Delay parameter 37", category = "Delay"),
            CCParameter("Delay Parameter 38", 59, 0, 127, "Delay parameter 38", category = "Delay"),
            CCParameter("Delay Parameter 39", 60, 0, 127, "Delay parameter 39", category = "Delay"),
            CCParameter("Delay Parameter 40", 61, 0, 127, "Delay parameter 40", category = "Delay"),
            
            // CC 62 - Dynamic Type
            CCParameter(
                name = "Dynamic Type",
                ccNumber = 62,
                minValue = 0,
                maxValue = 127,
                description = "0-25=OFF, 26-51=Compressor, 52-76=Swell, 77-102=Diffusion, 103-127=Limiter",
                category = "Dynamics"
            ),
            
            // CC 63 - Dynamic Location
            CCParameter(
                name = "Dynamic Location",
                ccNumber = 63,
                minValue = 0,
                maxValue = 127,
                description = "0-31=PRE+DRY, 32-63=PRE, 64-95=FDBK, 96-127=POST",
                category = "Dynamics"
            ),
            
            // CC 64-69 - Dynamic Parameters 1-6
            CCParameter("Dynamic Parameter 1", 64, 0, 127, "Dynamic parameter 1", category = "Dynamics"),
            CCParameter("Dynamic Parameter 2", 65, 0, 127, "Dynamic parameter 2", category = "Dynamics"),
            CCParameter("Dynamic Parameter 3", 66, 0, 127, "Dynamic parameter 3", category = "Dynamics"),
            CCParameter("Dynamic Parameter 4", 67, 0, 127, "Dynamic parameter 4", category = "Dynamics"),
            CCParameter("Dynamic Parameter 5", 68, 0, 127, "Dynamic parameter 5", category = "Dynamics"),
            CCParameter("Dynamic Parameter 6", 69, 0, 127, "Dynamic parameter 6", category = "Dynamics"),
            
            // CC 70 - Pitch Type
            CCParameter(
                name = "Pitch Type",
                ccNumber = 70,
                minValue = 0,
                maxValue = 127,
                description = "0-21=OFF, 22-42=Poly Chroma, 43-63=Harmony, 64-85=Micro Tune, 86-106=Mono Chroma, 107-127=Lo-Fi",
                category = "Pitch"
            ),
            
            // CC 71 - Pitch Location
            CCParameter(
                name = "Pitch Location",
                ccNumber = 71,
                minValue = 0,
                maxValue = 127,
                description = "0-31=PRE+DRY, 32-63=PRE, 64-95=FDBK, 96-127=POST",
                category = "Pitch"
            ),
            
            // CC 72-77 - Pitch Parameters 1-6
            CCParameter("Pitch Parameter 1", 72, 0, 127, "Pitch parameter 1", category = "Pitch"),
            CCParameter("Pitch Parameter 2", 73, 0, 127, "Pitch parameter 2", category = "Pitch"),
            CCParameter("Pitch Parameter 3", 74, 0, 127, "Pitch parameter 3", category = "Pitch"),
            CCParameter("Pitch Parameter 4", 75, 0, 127, "Pitch parameter 4", category = "Pitch"),
            CCParameter("Pitch Parameter 5", 76, 0, 127, "Pitch parameter 5", category = "Pitch"),
            CCParameter("Pitch Parameter 6", 77, 0, 127, "Pitch parameter 6", category = "Pitch"),
            
            // CC 78 - Filter Type
            CCParameter(
                name = "Filter Type",
                ccNumber = 78,
                minValue = 0,
                maxValue = 127,
                description = "0-25=OFF, 26-51=Ladder, 52-76=State Var, 77-102=Comb, 103-127=Parametric",
                category = "Filter"
            ),
            
            // CC 79 - Filter Location
            CCParameter(
                name = "Filter Location",
                ccNumber = 79,
                minValue = 0,
                maxValue = 127,
                description = "0-31=PRE+DRY, 32-63=PRE, 64-95=FDBK, 96-127=POST",
                category = "Filter"
            ),
            
            // CC 80-85 - Filter Parameters 1-6
            CCParameter("Filter Parameter 1", 80, 0, 127, "Filter parameter 1", category = "Filter"),
            CCParameter("Filter Parameter 2", 81, 0, 127, "Filter parameter 2", category = "Filter"),
            CCParameter("Filter Parameter 3", 82, 0, 127, "Filter parameter 3", category = "Filter"),
            CCParameter("Filter Parameter 4", 83, 0, 127, "Filter parameter 4", category = "Filter"),
            CCParameter("Filter Parameter 5", 84, 0, 127, "Filter parameter 5", category = "Filter"),
            CCParameter("Filter Parameter 6", 85, 0, 127, "Filter parameter 6", category = "Filter"),
            
            // CC 86 - Mod Type
            CCParameter(
                name = "Mod Type",
                ccNumber = 86,
                minValue = 0,
                maxValue = 127,
                description = "Modulation effects type. ACTUAL VALUES: CC 0-15=OFF, CC 16-31=Chorus (CC 24=subtle chorus), CC 32-47=Flanger, CC 48-63=Dyn Flanger, CC 64-79=Cassette, CC 80-95=Barberpole, CC 96-111=Granulize, CC 112-127=Ring Mod. For 'subtle modulation', use CC 24 (Chorus). This is separate from CC21 Delay Modulation.",
                category = "Modulation"
            ),
            
            // CC 87 - Mod Location
            CCParameter(
                name = "Mod Location",
                ccNumber = 87,
                minValue = 0,
                maxValue = 127,
                description = "0-31=PRE+DRY, 32-63=PRE, 64-95=FDBK, 96-127=POST",
                category = "Modulation"
            ),
            
            // CC 88-93 - Mod Parameters 1-6
            CCParameter("Mod Parameter 1", 88, 0, 127, "Modulation parameter 1", category = "Modulation"),
            CCParameter("Mod Parameter 2", 89, 0, 127, "Modulation parameter 2", category = "Modulation"),
            CCParameter("Mod Parameter 3", 90, 0, 127, "Modulation parameter 3", category = "Modulation"),
            CCParameter("Mod Parameter 4", 91, 0, 127, "Modulation parameter 4", category = "Modulation"),
            CCParameter("Mod Parameter 5", 92, 0, 127, "Modulation parameter 5", category = "Modulation"),
            CCParameter("Mod Parameter 6", 93, 0, 127, "Modulation parameter 6", category = "Modulation"),
            
            // CC 94 - Looper Location
            CCParameter(
                name = "Looper Location",
                ccNumber = 94,
                minValue = 0,
                maxValue = 127,
                description = "0-25=PRE+DRY, 26-51=PRE, 52-76=Feedback, 77-102=POST, 103-127=POST MIX",
                category = "Looper"
            ),
            
            // CC 95 - Looper Level
            CCParameter(
                name = "Looper Level",
                ccNumber = 95,
                minValue = 0,
                maxValue = 127,
                description = "Looper level",
                unit = "%",
                category = "Looper"
            ),
            
            // CC 96 - Looper Feedback
            CCParameter(
                name = "Looper Feedback",
                ccNumber = 96,
                minValue = 0,
                maxValue = 127,
                description = "Looper feedback amount",
                unit = "%",
                category = "Looper"
            ),
            
            // CC 97 - Looper FX1 Select
            CCParameter(
                name = "Looper FX1 Select",
                ccNumber = 97,
                minValue = 0,
                maxValue = 127,
                description = "Looper FX1 selection",
                category = "Looper"
            ),
            
            // CC 98 - Looper FX2 Select
            CCParameter(
                name = "Looper FX2 Select",
                ccNumber = 98,
                minValue = 0,
                maxValue = 127,
                description = "Looper FX2 selection",
                category = "Looper"
            ),
            
            // CC 100 - Looper Record/Overdub Press
            CCParameter(
                name = "Looper Record/Overdub Press",
                ccNumber = 100,
                minValue = 0,
                maxValue = 127,
                description = "PRESS = 127",
                category = "Looper"
            ),
            
            // CC 101 - Looper Play/Stop Press
            CCParameter(
                name = "Looper Play/Stop Press",
                ccNumber = 101,
                minValue = 0,
                maxValue = 127,
                description = "PRESS = 127",
                category = "Looper"
            ),
            
            // CC 102 - Looper FX1 Press
            CCParameter(
                name = "Looper FX1 Press",
                ccNumber = 102,
                minValue = 0,
                maxValue = 127,
                description = "PRESS = 127",
                category = "Looper"
            ),
            
            // CC 103 - Looper FX2 Press
            CCParameter(
                name = "Looper FX2 Press",
                ccNumber = 103,
                minValue = 0,
                maxValue = 127,
                description = "PRESS = 127",
                category = "Looper"
            )
        )
        
        return PedalModel(
            id = "meris_lvx",
            manufacturer = "Meris",
            modelName = "LVX",
            version = "1.0.2b",
            midiChannel = 2,
            parameters = parameters,
            description = "Meris LVX Modular DELAY System - True delay pedal with multiple engines (Digital, BBD, Tape). Primary controls: Time (CC15), Engine (CC16), Feedback (CC19), Mix (CC1), Modulation (CC21). Unlike Mercury X, this creates traditional delay repeats with configurable feedback and modulation."
        )
    }
}