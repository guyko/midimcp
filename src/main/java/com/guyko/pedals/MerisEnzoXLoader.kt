package com.guyko.pedals

import com.guyko.models.PedalModel
import com.guyko.models.CCParameter
import com.guyko.mcp.MCPServer

/**
 * Loader for the Meris Enzo X modular instrument synthesizer
 * A polyphonic synthesizer with 5 synth modes, filters, envelopes, and processing elements
 */
object MerisEnzoXLoader {
    
    fun loadEnzoX(server: MCPServer) {
        val enzoX = createMerisEnzoXPedal()
        server.pedalRepository.save(enzoX)
    }
    
    private fun createMerisEnzoXPedal(): PedalModel {
        val parameters = listOf(
            // CC 01 - Mix
            CCParameter(
                name = "Mix",
                ccNumber = 1,
                minValue = 0,
                maxValue = 127,
                description = "Overall wet/dry mix control",
                unit = "%",
                category = "Mix"
            ),
            
            // CC 02 - Dry Trim
            CCParameter(
                name = "Dry Trim",
                ccNumber = 2,
                minValue = 0,
                maxValue = 127,
                description = "Dry signal level trim",
                unit = "dB",
                category = "Mix"
            ),
            
            // CC 03 - Wet Trim
            CCParameter(
                name = "Wet Trim",
                ccNumber = 3,
                minValue = 0,
                maxValue = 127,
                description = "Wet signal level trim",
                unit = "dB",
                category = "Mix"
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
            
            // CC 05 - Drive Type
            CCParameter(
                name = "Drive Type",
                ccNumber = 5,
                minValue = 0,
                maxValue = 127,
                description = "Input drive/saturation: 0-21=Off (clean), 22-42=Volume Pedal, 43-63=Tube (warm), 64-85=Transistor (bright), 86-106=Op-Amp (clean), 107-127=Bitcrusher (lo-fi digital)",
                category = "Drive"
            ),
            
            // CC 06 - Drive Location
            CCParameter(
                name = "Drive Location",
                ccNumber = 6,
                minValue = 0,
                maxValue = 127,
                description = "0-31=Pre+Dry, 32-63=Dry, 64-95=Pre Ambience, 96-127=Post Ambience",
                category = "Drive"
            ),
            
            // CC 07 - Gain/Volume/Sample Rate
            CCParameter(
                name = "Gain/Volume/Sample Rate",
                ccNumber = 7,
                minValue = 0,
                maxValue = 127,
                description = "Drive gain, volume pedal level, or bitcrusher sample rate",
                unit = "dB",
                category = "Drive"
            ),
            
            // CC 08 - Balance/Bits
            CCParameter(
                name = "Balance/Bits",
                ccNumber = 8,
                minValue = 0,
                maxValue = 127,
                description = "Volume pedal balance or bitcrusher bit depth",
                category = "Drive"
            ),
            
            // CC 09 - Drive Level
            CCParameter(
                name = "Drive Level",
                ccNumber = 9,
                minValue = 0,
                maxValue = 127,
                description = "Output level after drive processing",
                unit = "dB",
                category = "Drive"
            ),
            
            // CC 10 - Ambience Type
            CCParameter(
                name = "Ambience Type",
                ccNumber = 10,
                minValue = 0,
                maxValue = 127,
                description = "0-25=Off, 26-51=Echo, 52-76=Small Prism, 77-102=Medium Prism, 103-127=Large Prism",
                category = "Ambience"
            ),
            
            // CC 11 - Feedback/Decay
            CCParameter(
                name = "Feedback/Decay",
                ccNumber = 11,
                minValue = 0,
                maxValue = 127,
                description = "Echo feedback or reverb decay amount",
                unit = "%",
                category = "Ambience"
            ),
            
            // CC 12 - Half Speed
            CCParameter(
                name = "Half Speed",
                ccNumber = 12,
                minValue = 0,
                maxValue = 127,
                description = "0-63=Half Speed Off, 64-127=Half Speed On",
                category = "Ambience"
            ),
            
            // CC 13 - Ambience Mod
            CCParameter(
                name = "Ambience Mod",
                ccNumber = 13,
                minValue = 0,
                maxValue = 127,
                description = "Ambience modulation amount",
                unit = "%",
                category = "Ambience"
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
            
            // CC 15 - Time (Clock, 7 bit tempo)
            CCParameter(
                name = "Time",
                ccNumber = 15,
                minValue = 0,
                maxValue = 127,
                description = "Clock tempo setting",
                unit = "BPM",
                category = "Clock"
            ),
            
            // CC 16 - Ambience Highs
            CCParameter(
                name = "Ambience Highs",
                ccNumber = 16,
                minValue = 0,
                maxValue = 127,
                description = "High frequency content in ambience",
                unit = "%",
                category = "Ambience"
            ),
            
            // CC 17 - Echo Left Division
            CCParameter(
                name = "Echo Left Division",
                ccNumber = 17,
                minValue = 0,
                maxValue = 127,
                description = "Left channel echo note division",
                category = "Ambience"
            ),
            
            // CC 18 - Echo Right Division
            CCParameter(
                name = "Echo Right Division",
                ccNumber = 18,
                minValue = 0,
                maxValue = 127,
                description = "Right channel echo note division",
                category = "Ambience"
            ),
            
            // CC 19 - Ambience + Echo Mix
            CCParameter(
                name = "Ambience + Echo Mix",
                ccNumber = 19,
                minValue = 0,
                maxValue = 127,
                description = "Ambience and echo mix level",
                unit = "%",
                category = "Ambience"
            ),
            
            // CC 20 - Ambience Lows
            CCParameter(
                name = "Ambience Lows",
                ccNumber = 20,
                minValue = 0,
                maxValue = 127,
                description = "Low frequency content in ambience",
                unit = "%",
                category = "Ambience"
            ),
            
            // CC 21 - MIDI Clock
            CCParameter(
                name = "MIDI Clock",
                ccNumber = 21,
                minValue = 0,
                maxValue = 127,
                description = "0-42=Use Global, 43-85=Force Listen, 86-127=Force Ignore",
                category = "MIDI"
            ),
            
            // CC 22 - Synth Mode
            CCParameter(
                name = "Synth Mode",
                ccNumber = 22,
                minValue = 0,
                maxValue = 127,
                description = "Synthesizer tracking mode: 0-25=Mono Synth (monophonic tracking), 26-51=Poly Synth (polyphonic tracking), 52-76=Arp Synth (arpeggiator mode), 77-102=Dry Mono (dry+mono synth), 103-127=Dry Poly (dry+poly synth). This is the CORE parameter that determines how Enzo tracks your guitar.",
                category = "Synth Engine"
            ),
            
            // CC 23 - Synth Pitch
            CCParameter(
                name = "Synth Pitch",
                ccNumber = 23,
                minValue = 0,
                maxValue = 127,
                description = "Base pitch in semitones relative to input signal",
                unit = "semitones",
                category = "Oscillator"
            ),
            
            // CC 24 - OSC 1 Wave Shape
            CCParameter(
                name = "OSC 1 Wave Shape",
                ccNumber = 24,
                minValue = 0,
                maxValue = 127,
                description = "Oscillator 1 waveform: 0-42=Sawtooth (bright/classic), 43-85=Triangle (warm/smooth), 86-127=Square (hollow/woody). Sawtooth is most common for leads and basses.",
                category = "Oscillator"
            ),
            
            // CC 25 - OSC 2 Wave Shape
            CCParameter(
                name = "OSC 2 Wave Shape",
                ccNumber = 25,
                minValue = 0,
                maxValue = 127,
                description = "Oscillator 2 waveform: 0-42=Sawtooth, 43-85=Triangle, 86-127=Square. Often set different from OSC1 for harmonic richness.",
                category = "Oscillator"
            ),
            
            // CC 26 - OSC 2 Pitch Offset
            CCParameter(
                name = "OSC 2 Pitch Offset",
                ccNumber = 26,
                minValue = 0,
                maxValue = 127,
                description = "Oscillator 2 pitch offset",
                unit = "semitones",
                category = "Oscillator"
            ),
            
            // CC 27 - OSC 2 Detune
            CCParameter(
                name = "OSC 2 Detune",
                ccNumber = 27,
                minValue = 0,
                maxValue = 127,
                description = "Oscillator 2 detune amount",
                unit = "cents",
                category = "Oscillator"
            ),
            
            // CC 28 - Synth Glide (Portamento)
            CCParameter(
                name = "Synth Glide",
                ccNumber = 28,
                minValue = 0,
                maxValue = 127,
                description = "Portamento/glide between notes",
                unit = "seconds",
                category = "Oscillator"
            ),
            
            // CC 29 - OSC 1 Gain
            CCParameter(
                name = "OSC 1 Gain",
                ccNumber = 29,
                minValue = 0,
                maxValue = 127,
                description = "Oscillator 1 volume level",
                unit = "%",
                category = "Oscillator"
            ),
            
            // CC 30 - OSC 2 Gain
            CCParameter(
                name = "OSC 2 Gain",
                ccNumber = 30,
                minValue = 0,
                maxValue = 127,
                description = "Oscillator 2 volume level",
                unit = "%",
                category = "Oscillator"
            ),
            
            // CC 31 - XMOD
            CCParameter(
                name = "XMOD",
                ccNumber = 31,
                minValue = 0,
                maxValue = 127,
                description = "Cross modulation amount (OSC1 modulates OSC2)",
                unit = "%",
                category = "Oscillator"
            ),
            
            // CC 32 - ARP Mode
            CCParameter(
                name = "ARP Mode",
                ccNumber = 32,
                minValue = 0,
                maxValue = 127,
                description = "Arpeggiator mode selection",
                category = "Arpeggiator"
            ),
            
            // CC 33 - ARP Steps
            CCParameter(
                name = "ARP Steps",
                ccNumber = 33,
                minValue = 0,
                maxValue = 127,
                description = "Number of arpeggiator steps (1-16)",
                category = "Arpeggiator"
            ),
            
            // CC 34 - ARP Octaves
            CCParameter(
                name = "ARP Octaves",
                ccNumber = 34,
                minValue = 0,
                maxValue = 127,
                description = "Arpeggiator octave span (1-3)",
                category = "Arpeggiator"
            ),
            
            // CC 35 - Level
            CCParameter(
                name = "Level",
                ccNumber = 35,
                minValue = 0,
                maxValue = 127,
                description = "Overall oscillator output level",
                unit = "dB",
                category = "Oscillator"
            ),
            
            // CC 36 - Dry Blend
            CCParameter(
                name = "Dry Blend",
                ccNumber = 36,
                minValue = 0,
                maxValue = 127,
                description = "Amount of dry signal in dry modes",
                unit = "%",
                category = "Mix"
            ),
            
            // CC 37 - ARP Cycle Latch
            CCParameter(
                name = "ARP Cycle Latch",
                ccNumber = 37,
                minValue = 0,
                maxValue = 127,
                description = "0-63=Off, 64-127=On - cycle latch mode",
                category = "Arpeggiator"
            ),
            
            // CC 38 - Filter Type
            CCParameter(
                name = "Filter Type",
                ccNumber = 38,
                minValue = 0,
                maxValue = 127,
                description = "0-42=Ladder, 43-85=State Variable, 86-127=Twin",
                category = "Filter"
            ),
            
            // CC 39 - Filter Frequency
            CCParameter(
                name = "Filter Frequency",
                ccNumber = 39,
                minValue = 0,
                maxValue = 127,
                description = "Filter cutoff frequency (0=very dark/muffled, 64=balanced brightness, 127=very bright/open). This is the primary tone control for the synthesizer.",
                unit = "Hz",
                category = "Filter"
            ),
            
            // CC 40 - Filter Topology
            CCParameter(
                name = "Filter Topology",
                ccNumber = 40,
                minValue = 0,
                maxValue = 127,
                description = "0-42=Lowpass, 43-85=Bandpass, 86-127=Highpass",
                category = "Filter"
            ),
            
            // CC 41 - Filter Resonance
            CCParameter(
                name = "Filter Resonance",
                ccNumber = 41,
                minValue = 0,
                maxValue = 127,
                description = "Filter resonance amount (0=smooth, 32=slight emphasis, 64=noticeable peak, 95=aggressive resonance, 127=self-oscillation). Adds character to filter sweep.",
                unit = "%",
                category = "Filter"
            ),
            
            // CC 42 - Filter Noise
            CCParameter(
                name = "Filter Noise",
                ccNumber = 42,
                minValue = 0,
                maxValue = 127,
                description = "White noise mixed with oscillators",
                unit = "%",
                category = "Filter"
            ),
            
            // CC 43 - Twin Filter Spread
            CCParameter(
                name = "Twin Filter Spread",
                ccNumber = 43,
                minValue = 0,
                maxValue = 127,
                description = "Distance between twin filter peaks",
                unit = "%",
                category = "Filter"
            ),
            
            // CC 44 - Filter Envelope Amount
            CCParameter(
                name = "Filter Envelope Amount",
                ccNumber = 44,
                minValue = 0,
                maxValue = 127,
                description = "Filter envelope modulation amount",
                unit = "%",
                category = "Filter"
            ),
            
            // CC 46 - Filter Envelope Type
            CCParameter(
                name = "Filter Envelope Type",
                ccNumber = 46,
                minValue = 0,
                maxValue = 127,
                description = "0-63=ADSR, 64-127=Envelope Follower",
                category = "Filter"
            ),
            
            // CC 47 - Filter Attack Time
            CCParameter(
                name = "Filter Attack Time",
                ccNumber = 47,
                minValue = 0,
                maxValue = 127,
                description = "Filter envelope attack time",
                unit = "seconds",
                category = "Filter"
            ),
            
            // CC 48 - Filter Decay Time
            CCParameter(
                name = "Filter Decay Time",
                ccNumber = 48,
                minValue = 0,
                maxValue = 127,
                description = "Filter envelope decay time",
                unit = "seconds",
                category = "Filter"
            ),
            
            // CC 49 - Filter Sustain Time
            CCParameter(
                name = "Filter Sustain Time",
                ccNumber = 49,
                minValue = 0,
                maxValue = 127,
                description = "Filter envelope sustain time",
                unit = "seconds",
                category = "Filter"
            ),
            
            // CC 50 - Filter Sustain Level
            CCParameter(
                name = "Filter Sustain Level",
                ccNumber = 50,
                minValue = 0,
                maxValue = 127,
                description = "Filter envelope sustain level",
                unit = "%",
                category = "Filter"
            ),
            
            // CC 51 - Filter Release Time
            CCParameter(
                name = "Filter Release Time",
                ccNumber = 51,
                minValue = 0,
                maxValue = 127,
                description = "Filter envelope release time",
                unit = "seconds",
                category = "Filter"
            ),
            
            // CC 52 - Direction
            CCParameter(
                name = "Direction",
                ccNumber = 52,
                minValue = 0,
                maxValue = 127,
                description = "0-63=Up, 64-127=Down - envelope direction",
                category = "Envelope"
            ),
            
            // CC 53 - Depth
            CCParameter(
                name = "Depth",
                ccNumber = 53,
                minValue = 0,
                maxValue = 127,
                description = "Envelope modulation depth",
                unit = "%",
                category = "Envelope"
            ),
            
            // CC 54 - Note Persist
            CCParameter(
                name = "Note Persist",
                ccNumber = 54,
                minValue = 0,
                maxValue = 127,
                description = "How long synthesized notes sustain",
                unit = "%",
                category = "Envelope"
            ),
            
            // CC 55 - Amplitude Attack Time
            CCParameter(
                name = "Amplitude Attack Time",
                ccNumber = 55,
                minValue = 0,
                maxValue = 127,
                description = "Amplitude envelope attack time (0=instant/percussive, 32=quick swell, 64=medium pad attack, 95=slow string swell, 127=very slow fade-in). Critical for pad vs lead sounds.",
                unit = "seconds",
                category = "Envelope"
            ),
            
            // CC 56 - Amplitude Decay Time
            CCParameter(
                name = "Amplitude Decay Time",
                ccNumber = 56,
                minValue = 0,
                maxValue = 127,
                description = "Amplitude envelope decay time",
                unit = "seconds",
                category = "Envelope"
            ),
            
            // CC 57 - Amplitude Sustain Level
            CCParameter(
                name = "Amplitude Sustain Level",
                ccNumber = 57,
                minValue = 0,
                maxValue = 127,
                description = "Amplitude envelope sustain level",
                unit = "%",
                category = "Envelope"
            ),
            
            // CC 58 - Amplitude Sustain Time
            CCParameter(
                name = "Amplitude Sustain Time",
                ccNumber = 58,
                minValue = 0,
                maxValue = 127,
                description = "Amplitude envelope sustain time",
                unit = "seconds",
                category = "Envelope"
            ),
            
            // CC 59 - Amplitude Release Time
            CCParameter(
                name = "Amplitude Release Time",
                ccNumber = 59,
                minValue = 0,
                maxValue = 127,
                description = "Amplitude envelope release time",
                unit = "seconds",
                category = "Envelope"
            ),
            
            // CC 60 - Oscillator Mod Speed
            CCParameter(
                name = "Oscillator Mod Speed",
                ccNumber = 60,
                minValue = 0,
                maxValue = 127,
                description = "Oscillator modulation speed (vibrato)",
                unit = "Hz",
                category = "Oscillator"
            ),
            
            // CC 61 - Oscillator Mod Depth
            CCParameter(
                name = "Oscillator Mod Depth",
                ccNumber = 61,
                minValue = 0,
                maxValue = 127,
                description = "Oscillator modulation depth (vibrato)",
                unit = "%",
                category = "Oscillator"
            ),
            
            // CC 62 - Oscillator Mod Ramp Time
            CCParameter(
                name = "Oscillator Mod Ramp Time",
                ccNumber = 62,
                minValue = 0,
                maxValue = 127,
                description = "Time for modulation to reach full depth",
                unit = "seconds",
                category = "Oscillator"
            ),
            
            // CC 86 - Mod Type
            CCParameter(
                name = "Mod Type",
                ccNumber = 86,
                minValue = 0,
                maxValue = 127,
                description = "0-21=Off, 22-42=Chorus, 43-63=Flanger, 64-85=Vibrato, 86-106=Phaser, 107-127=Ring Mod",
                category = "Modulation"
            ),
            
            // CC 87 - Mod Location
            CCParameter(
                name = "Mod Location",
                ccNumber = 87,
                minValue = 0,
                maxValue = 127,
                description = "0-31=Pre+Dry, 32-63=Dry, 64-95=Pre Ambience, 96-127=Post Ambience",
                category = "Modulation"
            ),
            
            // CC 88 - Mod Speed/Frequency
            CCParameter(
                name = "Mod Speed/Frequency",
                ccNumber = 88,
                minValue = 0,
                maxValue = 127,
                description = "Modulation speed or ring mod frequency",
                unit = "Hz",
                category = "Modulation"
            ),
            
            // CC 89 - Mod Depth
            CCParameter(
                name = "Mod Depth",
                ccNumber = 89,
                minValue = 0,
                maxValue = 127,
                description = "Modulation effect depth",
                unit = "%",
                category = "Modulation"
            ),
            
            // CC 90 - Mod Mode/Waveshape/Stages
            CCParameter(
                name = "Mod Mode/Waveshape/Stages",
                ccNumber = 90,
                minValue = 0,
                maxValue = 127,
                description = "Modulation mode, ring mod waveshape, or phaser stages",
                category = "Modulation"
            ),
            
            // CC 91 - Mod Feedback
            CCParameter(
                name = "Mod Feedback",
                ccNumber = 91,
                minValue = 0,
                maxValue = 127,
                description = "Modulation feedback amount",
                unit = "%",
                category = "Modulation"
            ),
            
            // CC 92 - Mod Mix
            CCParameter(
                name = "Mod Mix",
                ccNumber = 92,
                minValue = 0,
                maxValue = 127,
                description = "Modulation wet/dry mix",
                unit = "%",
                category = "Modulation"
            ),
            
            // CC 93 - Mod Note Div
            CCParameter(
                name = "Mod Note Div",
                ccNumber = 93,
                minValue = 0,
                maxValue = 127,
                description = "Modulation note division for tempo sync",
                category = "Modulation"
            ),
            
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
            id = "meris_enzo_x",
            manufacturer = "Meris",
            modelName = "Enzo X",
            description = "Modular guitar SYNTHESIZER that tracks your playing and generates synthesizer sounds. Key controls: Synth Mode (CC22) determines tracking type, OSC Wave Shapes (CC24/25), Filter Frequency (CC39), Filter Resonance (CC41), Amp Attack (CC55). Creates synth pads, leads, and basses from guitar input. NOT a delay or reverb pedal.",
            midiChannel = 3,
            parameters = parameters
        )
    }
}