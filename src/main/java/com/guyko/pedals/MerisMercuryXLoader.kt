package com.guyko.pedals

import com.guyko.models.PedalModel
import com.guyko.models.CCParameter
import com.guyko.mcp.MCPServer

/**
 * Loader for the Meris Mercury X reverb pedal
 * A modular reverb system with 8 reverb structures and comprehensive MIDI control
 */
object MerisMercuryXLoader {
    
    fun loadMercuryX(server: MCPServer) {
        val mercuryX = createMerisMercuryXPedal()
        server.pedalRepository.save(mercuryX)
    }
    
    private fun createMerisMercuryXPedal(): PedalModel {
        val parameters = listOf(
            // CC 01 - Mix
            CCParameter(
                name = "Mix",
                ccNumber = 1,
                minValue = 0,
                maxValue = 127,
                description = "Wet/dry mix control",
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
                description = "Preamp coloration: 0-25=Off (bypassed), 26-51=Volume Pedal, 52-76=Tube (warm saturation), 77-102=Transistor (bright/punchy), 103-127=Op-Amp (clean/modern)",
                category = "Preamp"
            ),
            
            // CC 06 - Preamp Location
            CCParameter(
                name = "Preamp Location",
                ccNumber = 6,
                minValue = 0,
                maxValue = 127,
                description = "0-25=Pre+Dry, 26-51=Pre, 52-76=Feedback, 77-102=Pre Tank, 103-127=Post",
                category = "Preamp"
            ),
            
            // CC 07 - Gain/Volume Pedal Level
            CCParameter(
                name = "Gain/Volume Pedal Level",
                ccNumber = 7,
                minValue = 0,
                maxValue = 127,
                description = "Preamp gain or volume pedal level",
                unit = "dB",
                category = "Preamp"
            ),
            
            // CC 08 - Balance
            CCParameter(
                name = "Balance",
                ccNumber = 8,
                minValue = 0,
                maxValue = 127,
                description = "Left/right channel balance for volume pedal",
                unit = "%",
                category = "Preamp"
            ),
            
            // CC 11 - Preamp Level
            CCParameter(
                name = "Preamp Level",
                ccNumber = 11,
                minValue = 0,
                maxValue = 127,
                description = "Output level after preamp processing",
                unit = "dB",
                category = "Preamp"
            ),
            
            // CC 13 - Delay Structure
            CCParameter(
                name = "Delay Structure",
                ccNumber = 13,
                minValue = 0,
                maxValue = 127,
                description = "0-63=Standard, 64-127=Reverse",
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
            
            // CC 15 - Predelay Time
            CCParameter(
                name = "Predelay Time",
                ccNumber = 15,
                minValue = 0,
                maxValue = 127,
                description = "Predelay time in milliseconds (0=0ms, 64≈400ms, 127≈800ms). This is early reflection timing before reverb tank, not traditional delay repeats.",
                unit = "ms",
                category = "Predelay"
            ),
            
            // CC 16 - Predelay Type
            CCParameter(
                name = "Predelay Type",
                ccNumber = 16,
                minValue = 0,
                maxValue = 127,
                description = "Predelay character: 0-42=Digital (clean), 43-85=BBD (analog warmth), 86-127=Magnetic (tape saturation). Affects predelay tone, not reverb.",
                category = "Predelay"
            ),
            
            // CC 17 - Left Note Division
            CCParameter(
                name = "Left Note Division",
                ccNumber = 17,
                minValue = 0,
                maxValue = 127,
                description = "Note division for left predelay channel",
                category = "Timing"
            ),
            
            // CC 18 - Right Note Division
            CCParameter(
                name = "Right Note Division",
                ccNumber = 18,
                minValue = 0,
                maxValue = 127,
                description = "Note division for right predelay channel",
                category = "Timing"
            ),
            
            // CC 19 - Predelay Feedback
            CCParameter(
                name = "Predelay Feedback",
                ccNumber = 19,
                minValue = 0,
                maxValue = 127,
                description = "Predelay feedback amount (0=single reflection, 64=moderate repeats, 127=infinite). Creates predelay repeats before entering reverb tank.",
                unit = "%",
                category = "Predelay"
            ),
            
            // CC 20 - Cross Feedback
            CCParameter(
                name = "Cross Feedback",
                ccNumber = 20,
                minValue = 0,
                maxValue = 127,
                description = "Cross feedback between L/R predelay channels",
                unit = "%",
                category = "Delay"
            ),
            
            // CC 21 - Predelay Modulation
            CCParameter(
                name = "Predelay Modulation",
                ccNumber = 21,
                minValue = 0,
                maxValue = 127,
                description = "Predelay time modulation amount (0=off, 32=subtle, 64=moderate, 127=extreme warble). Adds movement to predelay reflections.",
                unit = "%",
                category = "Predelay"
            ),
            
            // CC 22 - Predelay Damping
            CCParameter(
                name = "Predelay Damping",
                ccNumber = 22,
                minValue = 0,
                maxValue = 127,
                description = "High frequency damping in predelay (0=bright, 64=natural, 127=very dark). Affects predelay tone rolloff with each repeat.",
                unit = "%",
                category = "Predelay"
            ),
            
            // CC 23 - Dry Blend
            CCParameter(
                name = "Dry Blend",
                ccNumber = 23,
                minValue = 0,
                maxValue = 127,
                description = "Amount of dry signal added to predelay output",
                unit = "%",
                category = "Mix"
            ),
            
            // CC 24 - Half Speed
            CCParameter(
                name = "Half Speed",
                ccNumber = 24,
                minValue = 0,
                maxValue = 127,
                description = "Predelay half speed mode",
                category = "Delay"
            ),
            
            // CC 28 - MIDI Clock
            CCParameter(
                name = "MIDI Clock",
                ccNumber = 28,
                minValue = 0,
                maxValue = 127,
                description = "0-42=Use Global, 43-85=Force Listen, 86-127=Force Ignore",
                category = "MIDI"
            ),
            
            // CC 32 - Reverb Structure
            CCParameter(
                name = "Reverb Structure",
                ccNumber = 32,
                minValue = 0,
                maxValue = 127,
                description = "Reverb algorithm: 0-15=Ultraplate (bright plate), 16-31=Cathedra (cathedral), 32-47=Spring (mechanical), 48-63=78 Room (vintage room), 64-79=78 Plate (vintage plate), 80-95=78 Hall (vintage hall), 96-111=Prism (shimmer), 112-127=Gravity (nonlinear)",
                category = "Reverb"
            ),
            
            // CC 33-41 - Reverb Parameters 1-9
            CCParameter("Reverb Parameter 1", 33, 0, 127, "Reverb structure parameter 1", category = "Reverb"),
            CCParameter("Reverb Parameter 2", 34, 0, 127, "Reverb structure parameter 2", category = "Reverb"),
            CCParameter("Reverb Parameter 3", 35, 0, 127, "Reverb structure parameter 3", category = "Reverb"),
            CCParameter("Reverb Parameter 4", 36, 0, 127, "Reverb structure parameter 4", category = "Reverb"),
            CCParameter("Reverb Parameter 5", 37, 0, 127, "Reverb structure parameter 5", category = "Reverb"),
            CCParameter("Reverb Parameter 6", 38, 0, 127, "Reverb structure parameter 6", category = "Reverb"),
            CCParameter("Reverb Parameter 7", 39, 0, 127, "Reverb structure parameter 7", category = "Reverb"),
            CCParameter("Reverb Parameter 8", 40, 0, 127, "Reverb structure parameter 8", category = "Reverb"),
            CCParameter("Reverb Parameter 9", 41, 0, 127, "Reverb structure parameter 9", category = "Reverb"),
            
            // CC 42 - Predelay Blend
            CCParameter(
                name = "Predelay Blend",
                ccNumber = 42,
                minValue = 0,
                maxValue = 127,
                description = "Amount of predelay reflections in parallel with reverb tank",
                unit = "%",
                category = "Reverb"
            ),
            
            // CC 43-45 - Gate Parameters
            CCParameter(
                name = "Gate Attack",
                ccNumber = 43,
                minValue = 0,
                maxValue = 127,
                description = "Reverb gate attack time",
                unit = "ms",
                category = "Gate"
            ),
            
            CCParameter(
                name = "Gate Hold",
                ccNumber = 44,
                minValue = 0,
                maxValue = 127,
                description = "Reverb gate hold time",
                unit = "ms",
                category = "Gate"
            ),
            
            CCParameter(
                name = "Gate Decay",
                ccNumber = 45,
                minValue = 0,
                maxValue = 127,
                description = "Reverb gate decay time",
                unit = "ms",
                category = "Gate"
            ),
            
            // CC 62 - Dynamics Type
            CCParameter(
                name = "Dynamics Type",
                ccNumber = 62,
                minValue = 0,
                maxValue = 127,
                description = "0-15=Off, 16-31=Compressor, 32-47=Compressor Link, 48-63=Swell, 64-79=Diffusion, 80-95=Limiter, 96-111=Limiter Link, 112-127=Freeze",
                category = "Dynamics"
            ),
            
            // CC 63 - Dynamics Location
            CCParameter(
                name = "Dynamics Location",
                ccNumber = 63,
                minValue = 0,
                maxValue = 127,
                description = "0-25=Pre+Dry, 26-51=Pre, 52-76=Feedback, 77-102=Pre Tank, 103-127=Post",
                category = "Dynamics"
            ),
            
            // CC 64-69 - Dynamics Parameters 1-6
            CCParameter("Dynamics Parameter 1", 64, 0, 127, "Dynamics processing parameter 1", category = "Dynamics"),
            CCParameter("Dynamics Parameter 2", 65, 0, 127, "Dynamics processing parameter 2", category = "Dynamics"),
            CCParameter("Dynamics Parameter 3", 66, 0, 127, "Dynamics processing parameter 3", category = "Dynamics"),
            CCParameter("Dynamics Parameter 4", 67, 0, 127, "Dynamics processing parameter 4", category = "Dynamics"),
            CCParameter("Dynamics Parameter 5", 68, 0, 127, "Dynamics processing parameter 5", category = "Dynamics"),
            CCParameter("Dynamics Parameter 6", 69, 0, 127, "Dynamics processing parameter 6", category = "Dynamics"),
            
            // CC 70 - Pitch Type
            CCParameter(
                name = "Pitch Type",
                ccNumber = 70,
                minValue = 0,
                maxValue = 127,
                description = "0-31=Off, 32-63=Poly Chroma, 64-95=Micro Shift, 96-127=Lo-Fi",
                category = "Pitch"
            ),
            
            // CC 71 - Pitch Location
            CCParameter(
                name = "Pitch Location",
                ccNumber = 71,
                minValue = 0,
                maxValue = 127,
                description = "0-25=Pre+Dry, 26-51=Pre, 52-76=Feedback, 77-102=Pre Tank, 103-127=Post",
                category = "Pitch"
            ),
            
            // CC 72-77 - Pitch Parameters 1-6
            CCParameter("Pitch Parameter 1", 72, 0, 127, "Pitch processing parameter 1", category = "Pitch"),
            CCParameter("Pitch Parameter 2", 73, 0, 127, "Pitch processing parameter 2", category = "Pitch"),
            CCParameter("Pitch Parameter 3", 74, 0, 127, "Pitch processing parameter 3", category = "Pitch"),
            CCParameter("Pitch Parameter 4", 75, 0, 127, "Pitch processing parameter 4", category = "Pitch"),
            CCParameter("Pitch Parameter 5", 76, 0, 127, "Pitch processing parameter 5", category = "Pitch"),
            CCParameter("Pitch Parameter 6", 77, 0, 127, "Pitch processing parameter 6", category = "Pitch"),
            
            // CC 78 - Filter Type
            CCParameter(
                name = "Filter Type",
                ccNumber = 78,
                minValue = 0,
                maxValue = 127,
                description = "0-31=Off, 32-63=Ladder, 64-95=State Variable, 96-127=Parametric",
                category = "Filter"
            ),
            
            // CC 79 - Filter Location
            CCParameter(
                name = "Filter Location",
                ccNumber = 79,
                minValue = 0,
                maxValue = 127,
                description = "0-25=Pre+Dry, 26-51=Pre, 52-76=Feedback, 77-102=Pre Tank, 103-127=Post",
                category = "Filter"
            ),
            
            // CC 80-85 - Filter Parameters 1-6
            CCParameter("Filter Parameter 1", 80, 0, 127, "Filter processing parameter 1", category = "Filter"),
            CCParameter("Filter Parameter 2", 81, 0, 127, "Filter processing parameter 2", category = "Filter"),
            CCParameter("Filter Parameter 3", 82, 0, 127, "Filter processing parameter 3", category = "Filter"),
            CCParameter("Filter Parameter 4", 83, 0, 127, "Filter processing parameter 4", category = "Filter"),
            CCParameter("Filter Parameter 5", 84, 0, 127, "Filter processing parameter 5", category = "Filter"),
            CCParameter("Filter Parameter 6", 85, 0, 127, "Filter processing parameter 6", category = "Filter"),
            
            // CC 86 - Modulation Type
            CCParameter(
                name = "Modulation Type",
                ccNumber = 86,
                minValue = 0,
                maxValue = 127,
                description = "0-21=Off, 22-42=79 Chorus, 43-63=Vibrato, 64-85=Vowel Mod, 86-106=Tremolo, 107-127=Hazy",
                category = "Modulation"
            ),
            
            // CC 87 - Modulation Location
            CCParameter(
                name = "Modulation Location",
                ccNumber = 87,
                minValue = 0,
                maxValue = 127,
                description = "0-25=Pre+Dry, 26-51=Pre, 52-76=Feedback, 77-102=Pre Tank, 103-127=Post",
                category = "Modulation"
            ),
            
            // CC 88-93 - Modulation Parameters 1-6
            CCParameter("Modulation Parameter 1", 88, 0, 127, "Modulation processing parameter 1", category = "Modulation"),
            CCParameter("Modulation Parameter 2", 89, 0, 127, "Modulation processing parameter 2", category = "Modulation"),
            CCParameter("Modulation Parameter 3", 90, 0, 127, "Modulation processing parameter 3", category = "Modulation"),
            CCParameter("Modulation Parameter 4", 91, 0, 127, "Modulation processing parameter 4", category = "Modulation"),
            CCParameter("Modulation Parameter 5", 92, 0, 127, "Modulation processing parameter 5", category = "Modulation"),
            CCParameter("Modulation Parameter 6", 93, 0, 127, "Modulation processing parameter 6", category = "Modulation"),
            
            // CC 117 - Toggle Tuner Mode
            CCParameter(
                name = "Toggle Tuner Mode",
                ccNumber = 117,
                minValue = 127,
                maxValue = 127,
                description = "Press = 127 to toggle tuner mode",
                category = "Control"
            ),
            
            // CC 118 - Trigger Hold Modifier
            CCParameter(
                name = "Trigger Hold Modifier",
                ccNumber = 118,
                minValue = 0,
                maxValue = 127,
                description = "Press = 127, Release = 0 (send release after every press)",
                category = "Control"
            )
        )
        
        return PedalModel(
            id = "meris_mercury_x",
            manufacturer = "Meris",
            modelName = "Mercury X",
            description = "Modular reverb system with PREDELAY + REVERB TANK architecture. Features 8 reverb structures and comprehensive processing elements. IMPORTANT: This is NOT a traditional delay pedal - it uses predelay (early reflections) that feeds into reverb algorithms. Predelay creates initial echoes before the reverb tank, not standalone delay repeats.",
            midiChannel = 1,
            parameters = parameters
        )
    }
}