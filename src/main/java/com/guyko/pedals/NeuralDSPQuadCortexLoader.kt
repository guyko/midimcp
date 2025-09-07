package com.guyko.pedals

import com.guyko.models.PedalModel
import com.guyko.models.CCParameter
import com.guyko.mcp.MCPServer

/**
 * Loader for the Neural DSP Quad Cortex floor modeler
 * A powerful floor modeler with 6 DSP cores, 4-instrument processing, and comprehensive MIDI control
 */
object NeuralDSPQuadCortexLoader {
    
    fun loadQuadCortex(server: MCPServer) {
        val quadCortex = createNeuralDSPQuadCortexPedal()
        server.pedalRepository.save(quadCortex)
    }
    
    private fun createNeuralDSPQuadCortexPedal(): PedalModel {
        val parameters = listOf(
            // CC 00 - Bank Select (MSB)
            CCParameter(
                name = "Bank Select (MSB)",
                ccNumber = 0,
                minValue = 0,
                maxValue = 1,
                description = "0=Preset group 0-127, 1=Preset group 128-256",
                category = "Program"
            ),
            
            // CC 01 - Expression Pedal 1
            CCParameter(
                name = "Expression Pedal 1",
                ccNumber = 1,
                minValue = 0,
                maxValue = 127,
                description = "Expression pedal 1 input",
                unit = "%",
                category = "Expression"
            ),
            
            // CC 02 - Expression Pedal 2
            CCParameter(
                name = "Expression Pedal 2",
                ccNumber = 2,
                minValue = 0,
                maxValue = 127,
                description = "Expression pedal 2 input",
                unit = "%",
                category = "Expression"
            ),
            
            // CC 32 - Bank Select (LSB) - Setlist
            CCParameter(
                name = "Bank Select (LSB) - Setlist",
                ccNumber = 32,
                minValue = 0,
                maxValue = 12,
                description = "Setlist change (used with Program Change)",
                category = "Program"
            ),
            
            // CC 35-42 - Footswitch A-H Enable/Bypass
            CCParameter(
                name = "Footswitch A Enable/Bypass",
                ccNumber = 35,
                minValue = 0,
                maxValue = 127,
                description = "Enable/bypass footswitch A (all modes)",
                category = "Footswitch"
            ),
            
            CCParameter(
                name = "Footswitch B Enable/Bypass",
                ccNumber = 36,
                minValue = 0,
                maxValue = 127,
                description = "Enable/bypass footswitch B (all modes)",
                category = "Footswitch"
            ),
            
            CCParameter(
                name = "Footswitch C Enable/Bypass",
                ccNumber = 37,
                minValue = 0,
                maxValue = 127,
                description = "Enable/bypass footswitch C (all modes)",
                category = "Footswitch"
            ),
            
            CCParameter(
                name = "Footswitch D Enable/Bypass",
                ccNumber = 38,
                minValue = 0,
                maxValue = 127,
                description = "Enable/bypass footswitch D (all modes)",
                category = "Footswitch"
            ),
            
            CCParameter(
                name = "Footswitch E Enable/Bypass",
                ccNumber = 39,
                minValue = 0,
                maxValue = 127,
                description = "Enable/bypass footswitch E (all modes)",
                category = "Footswitch"
            ),
            
            CCParameter(
                name = "Footswitch F Enable/Bypass",
                ccNumber = 40,
                minValue = 0,
                maxValue = 127,
                description = "Enable/bypass footswitch F (all modes)",
                category = "Footswitch"
            ),
            
            CCParameter(
                name = "Footswitch G Enable/Bypass",
                ccNumber = 41,
                minValue = 0,
                maxValue = 127,
                description = "Enable/bypass footswitch G (all modes)",
                category = "Footswitch"
            ),
            
            CCParameter(
                name = "Footswitch H Enable/Bypass",
                ccNumber = 42,
                minValue = 0,
                maxValue = 127,
                description = "Enable/bypass footswitch H (all modes)",
                category = "Footswitch"
            ),
            
            // CC 43 - Scene Select
            CCParameter(
                name = "Scene Select",
                ccNumber = 43,
                minValue = 0,
                maxValue = 7,
                description = "Scene select A-H",
                category = "Scene"
            ),
            
            // CC 44 - Tempo BPM
            CCParameter(
                name = "Tempo BPM",
                ccNumber = 44,
                minValue = 0,
                maxValue = 127,
                description = "Tempo in BPM",
                unit = "BPM",
                category = "Tempo"
            ),
            
            // CC 45 - Tuner Screen
            CCParameter(
                name = "Tuner Screen",
                ccNumber = 45,
                minValue = 0,
                maxValue = 127,
                description = "0-63=Tuner Off, 64-127=Tuner On",
                category = "Control"
            ),
            
            // CC 46 - Gig View Screen
            CCParameter(
                name = "Gig View Screen",
                ccNumber = 46,
                minValue = 0,
                maxValue = 127,
                description = "0-63=Gig View Off, 64-127=Gig View On",
                category = "Control"
            ),
            
            // CC 47 - Change Modes
            CCParameter(
                name = "Change Modes",
                ccNumber = 47,
                minValue = 0,
                maxValue = 2,
                description = "0=Preset Mode, 1=Scene Mode, 2=Stomp Mode",
                category = "Mode"
            ),
            
            // CC 48-61 - Looper X Controls
            CCParameter(
                name = "Looper X Parameter Editor Menu",
                ccNumber = 48,
                minValue = 0,
                maxValue = 127,
                description = "0-63=Opens Looper X (Perform mode), 64-127=Closes Looper X",
                category = "Looper"
            ),
            
            CCParameter(
                name = "Duplicate/Stop Duplicate",
                ccNumber = 49,
                minValue = 64,
                maxValue = 127,
                description = "Trigger duplicate/stop duplicate",
                category = "Looper"
            ),
            
            CCParameter(
                name = "Enable/Disable One Shot",
                ccNumber = 50,
                minValue = 64,
                maxValue = 127,
                description = "Enable/disable one shot mode",
                category = "Looper"
            ),
            
            CCParameter(
                name = "Enable/Disable Half Speed",
                ccNumber = 51,
                minValue = 64,
                maxValue = 127,
                description = "Enable/disable half speed playback",
                category = "Looper"
            ),
            
            CCParameter(
                name = "Punch Feature",
                ccNumber = 52,
                minValue = 0,
                maxValue = 127,
                description = "0-63=Punch Out, 64-127=Punch In/Punch Out",
                category = "Looper"
            ),
            
            CCParameter(
                name = "Record/Stop",
                ccNumber = 53,
                minValue = 0,
                maxValue = 127,
                description = "0-63=Stop recording, 64-127=Record/Overdub/Stop",
                category = "Looper"
            ),
            
            CCParameter(
                name = "Play/Stop",
                ccNumber = 54,
                minValue = 64,
                maxValue = 127,
                description = "Play/stop looper playback",
                category = "Looper"
            ),
            
            CCParameter(
                name = "Enable/Disable Reverse",
                ccNumber = 55,
                minValue = 64,
                maxValue = 127,
                description = "Enable/disable reverse playback",
                category = "Looper"
            ),
            
            CCParameter(
                name = "Undo/Redo",
                ccNumber = 56,
                minValue = 64,
                maxValue = 127,
                description = "Undo/redo last action",
                category = "Looper"
            ),
            
            CCParameter(
                name = "Duplicate Mode Parameter",
                ccNumber = 57,
                minValue = 0,
                maxValue = 1,
                description = "0=Free, 1=Sync",
                category = "Looper"
            ),
            
            CCParameter(
                name = "Quantize Parameter",
                ccNumber = 58,
                minValue = 0,
                maxValue = 9,
                description = "0=Off, 1-8=1-8 Beats, 9=16 Beats",
                category = "Looper"
            ),
            
            CCParameter(
                name = "MIDI Clock Start",
                ccNumber = 59,
                minValue = 0,
                maxValue = 1,
                description = "0=Off, 1=On",
                category = "Looper"
            ),
            
            CCParameter(
                name = "Perform/Params Mode",
                ccNumber = 60,
                minValue = 0,
                maxValue = 1,
                description = "0=Perform Mode, 1=Params Mode",
                category = "Looper"
            ),
            
            CCParameter(
                name = "Routing Mode Parameter",
                ccNumber = 61,
                minValue = 0,
                maxValue = 13,
                description = "Routing mode 0-13: Grid > I/Os > Multi Out",
                category = "Looper"
            ),
            
            // CC 62 - Ignore Duplicate PC
            CCParameter(
                name = "Ignore Duplicate PC",
                ccNumber = 62,
                minValue = 0,
                maxValue = 127,
                description = "0-63=Off, 64-127=On - ignore duplicate program changes",
                category = "MIDI"
            )
        )
        
        return PedalModel(
            id = "neural_dsp_quad_cortex",
            manufacturer = "Neural DSP",
            modelName = "Quad Cortex",
            description = "Most powerful floor modeler with 6 DSP cores, 4-instrument processing, Neural Capture technology, and comprehensive scene/stomp/preset modes",
            midiChannel = 4,
            parameters = parameters
        )
    }
}