package com.guyko.pedals

import com.guyko.models.MidiSysex
import mu.KotlinLogging

/**
 * Generates Meris Enzo X synthesizer preset sysex files from CC parameter values
 * AI assistants provide the parameter values, MCP generates the sysex
 */
object EnzoXPresetGenerator {
    private val logger = KotlinLogging.logger {}
    
    /**
     * Enzo X Sysex header - common to all Enzo X presets
     */
    private val ENZO_X_SYSEX_HEADER = byteArrayOf(
        0xF0.toByte(), 0x00.toByte(), 0x02.toByte(), 0x10.toByte(), 
        0x00.toByte(), 0x02.toByte(), 0x02.toByte(), 0x26.toByte()
    )
    
    /**
     * Total sysex size for Enzo X presets (from analysis of existing files)
     */
    private const val ENZO_X_SYSEX_SIZE = 231
    
    /**
     * Position where preset name starts in the sysex (observed from hex dumps)
     */
    private const val PRESET_NAME_POSITION = 212
    
    /**
     * Maximum length for preset names
     */
    private const val MAX_PRESET_NAME_LENGTH = 16
    
    /**
     * Map CC numbers to their byte positions in Enzo X sysex
     * Based on analysis of Enzo X sysex structure and MIDI CC table
     */
    private val ccToSysexPosition = mapOf(
        // Global Controls
        1 to 9,    // Mix
        2 to 10,   // Dry Trim  
        3 to 11,   // Wet Trim
        4 to 12,   // Expression Pedal
        
        // Synth Mode & Control
        5 to 13,   // Synth Mode (Mono/Poly/Arp/Dry Mono/Dry Poly)
        6 to 14,   // Bypass
        7 to 15,   // Input Level
        8 to 16,   // Output Level
        9 to 17,   // Synth Level
        10 to 18,  // Dry Level
        
        // Oscillator 1
        11 to 19,  // Osc 1 Waveform
        12 to 20,  // Osc 1 Octave
        13 to 21,  // Osc 1 Semi
        14 to 22,  // Osc 1 Cents
        15 to 23,  // Osc 1 Level
        16 to 24,  // Osc 1 Phase
        
        // Oscillator 2
        17 to 25,  // Osc 2 Waveform
        18 to 26,  // Osc 2 Octave
        19 to 27,  // Osc 2 Semi
        20 to 28,  // Osc 2 Cents
        21 to 29,  // Osc 2 Level
        22 to 30,  // Osc 2 Phase
        
        // Filter
        23 to 31,  // Filter Type
        24 to 32,  // Filter Cutoff
        25 to 33,  // Filter Resonance
        26 to 34,  // Filter Drive
        27 to 35,  // Filter Envelope Amount
        28 to 36,  // Filter Key Track
        
        // Amplifier Envelope (ADSR)
        29 to 37,  // Amp Attack
        30 to 38,  // Amp Decay
        31 to 39,  // Amp Sustain
        32 to 40,  // Amp Release
        
        // Filter Envelope (ADSR)
        33 to 41,  // Filter Attack
        34 to 42,  // Filter Decay
        35 to 43,  // Filter Sustain
        36 to 44,  // Filter Release
        
        // LFO 1
        37 to 45,  // LFO 1 Waveform
        38 to 46,  // LFO 1 Rate
        39 to 47,  // LFO 1 Depth
        40 to 48,  // LFO 1 Destination
        41 to 49,  // LFO 1 Phase
        
        // LFO 2
        42 to 50,  // LFO 2 Waveform
        43 to 51,  // LFO 2 Rate
        44 to 52,  // LFO 2 Depth
        45 to 53,  // LFO 2 Destination
        46 to 54,  // LFO 2 Phase
        
        // Processing Elements - Drive
        47 to 55,  // Drive Type
        48 to 56,  // Drive Location
        49 to 57,  // Drive Amount
        50 to 58,  // Drive Tone
        51 to 59,  // Drive Level
        52 to 60,  // Drive Character
        
        // Processing Elements - Ambience
        53 to 61,  // Ambience Type
        54 to 62,  // Ambience Location
        55 to 63,  // Ambience Size
        56 to 64,  // Ambience Decay
        57 to 65,  // Ambience Level
        58 to 66,  // Ambience Character
        
        // Modulation Effects
        59 to 67,  // Mod FX Type (Chorus/Flanger/Vibrato/Phaser/Ring Mod)
        60 to 68,  // Mod FX Location
        61 to 69,  // Mod FX Rate
        62 to 70,  // Mod FX Depth
        63 to 71,  // Mod FX Feedback
        64 to 72,  // Mod FX Mix
        
        // Arpeggiator
        65 to 73,  // Arp Pattern
        66 to 74,  // Arp Rate
        67 to 75,  // Arp Gate
        68 to 76,  // Arp Octaves
        69 to 77,  // Arp Direction
        70 to 78,  // Arp Latch
        
        // MIDI Keyboard Control
        71 to 79,  // Polyphony
        72 to 80,  // Velocity Sensitivity
        73 to 81,  // Aftertouch Amount
        74 to 82,  // Pitch Bend Range
        75 to 83,  // Glide Time
        76 to 84,  // Glide Mode
        
        // Expression Controls
        77 to 85, 78 to 86, 79 to 87, 80 to 88, 81 to 89, 82 to 90,
        83 to 91, 84 to 92, 85 to 93, 86 to 94, 87 to 95, 88 to 96,
        
        // Advanced Controls
        89 to 97,  // Gate Mode
        90 to 98,  // Gate Threshold
        91 to 99,  // MIDI Channel
        92 to 100, // Program Change Enable
        93 to 101, // Tuner Mode
        94 to 102, // Expression Mode
        95 to 103, // Expression Range
        96 to 104, // Tempo Sync
        97 to 105, // Clock Source
        98 to 106, // MIDI Clock Sync
        99 to 107, // Preset Save Mode
        100 to 108 // Backup/Restore
    )
    
    /**
     * Generate Enzo X preset sysex from CC parameter map
     * @param parameters Map of CC number to value (0-127)
     * @param presetName Name for the preset (max 16 chars)
     * @return MidiSysex object ready for transmission
     */
    fun generatePreset(parameters: Map<Int, Int>, presetName: String): MidiSysex {
        logger.info { "Generating Enzo X preset '$presetName' with ${parameters.size} CC parameters" }
        logger.debug { "Enzo X CC parameters: ${parameters.map { (cc, value) -> "CC$cc=$value" }.joinToString(", ")}" }
        
        val sysexData = ByteArray(ENZO_X_SYSEX_SIZE)
        
        // Set Enzo X header
        ENZO_X_SYSEX_HEADER.copyInto(sysexData, 0)
        logger.debug { "Enzo X header set: ${ENZO_X_SYSEX_HEADER.joinToString(" ") { "%02X".format(it) }}" }
        
        // Initialize with default values (based on analysis of existing presets)
        initializeDefaults(sysexData)
        logger.debug { "Enzo X defaults initialized" }
        
        // Map CC parameters to their sysex positions
        var mappedParams = 0
        var skippedParams = 0
        parameters.forEach { (ccNumber, value) ->
            val position = ccToSysexPosition[ccNumber]
            if (position != null && position < ENZO_X_SYSEX_SIZE - 1) {
                require(value in 0..127) { "CC value must be 0-127, got $value for CC$ccNumber" }
                sysexData[position] = (value and 0x7F).toByte() // Ensure 7-bit
                logger.debug { "Enzo X mapped CC$ccNumber=$value to sysex position $position" }
                mappedParams++
            } else {
                logger.warn { "Enzo X CC$ccNumber not mapped to sysex position (unknown parameter)" }
                skippedParams++
            }
        }
        logger.info { "Enzo X parameter mapping complete: $mappedParams mapped, $skippedParams skipped" }
        
        // Add preset name
        addPresetName(sysexData, presetName)
        logger.debug { "Enzo X preset name '$presetName' added at position $PRESET_NAME_POSITION" }
        
        // Set sysex terminator
        sysexData[ENZO_X_SYSEX_SIZE - 1] = 0xF7.toByte()
        
        val hexString = sysexData.joinToString(" ") { "%02X".format(it) }
        logger.info { "Enzo X preset sysex generated successfully: ${ENZO_X_SYSEX_SIZE} bytes, name='$presetName'" }
        logger.debug { "Enzo X sysex data: $hexString" }
        
        return MidiSysex(
            data = sysexData,
            description = "Enzo X Preset generated from ${parameters.size} parameters",
            presetName = presetName
        )
    }
    
    /**
     * Initialize sysex with sensible default values
     * Based on common patterns observed in existing Enzo X presets
     */
    private fun initializeDefaults(sysexData: ByteArray) {
        // Set common default patterns found in existing presets
        
        // Default mix and levels
        sysexData[9] = 0x60   // Mix - 75%
        sysexData[10] = 0x40  // Dry Trim - center
        sysexData[11] = 0x40  // Wet Trim - center
        
        // Default synth settings
        sysexData[13] = 0x01  // Poly mode
        sysexData[14] = 0x7F  // FX enabled
        sysexData[15] = 0x40  // Medium input level
        sysexData[16] = 0x40  // Medium output level
        sysexData[17] = 0x60  // High synth level
        sysexData[18] = 0x20  // Low dry level
        
        // Default oscillator settings
        sysexData[19] = 0x00  // Osc 1 - Sawtooth
        sysexData[20] = 0x40  // Osc 1 - Center octave
        sysexData[23] = 0x7F  // Osc 1 - Full level
        
        sysexData[25] = 0x01  // Osc 2 - Square
        sysexData[26] = 0x40  // Osc 2 - Center octave
        sysexData[29] = 0x40  // Osc 2 - Half level
        
        // Default filter settings
        sysexData[31] = 0x00  // Low pass filter
        sysexData[32] = 0x60  // Medium-high cutoff
        sysexData[33] = 0x20  // Low resonance
        
        // Default ADSR settings
        sysexData[37] = 0x10  // Fast attack
        sysexData[38] = 0x40  // Medium decay
        sysexData[39] = 0x60  // High sustain
        sysexData[40] = 0x30  // Medium release
        
        // Common pattern found in multiple presets
        val commonPattern = byteArrayOf(
            0x55, 0x55, 0x55, 0x55, 0x55, 0x55, 0x55, 0x55
        )
        commonPattern.copyInto(sysexData, 59)
        
        // Default processing disabled
        sysexData[67] = 0x00  // Drive off
        sysexData[68] = 0x00  // Ambience off
        sysexData[69] = 0x00  // Mod FX off
        
        // Default step values (keyboard control)
        val stepPattern = byteArrayOf(
            0x10, 0x20, 0x30, 0x40, 0x50, 0x60, 0x70, 0x7f,
            0x70, 0x60, 0x50, 0x40, 0x30, 0x20, 0x10, 0x00
        )
        stepPattern.copyInto(sysexData, 160)
    }
    
    /**
     * Add preset name to sysex data at the correct position
     */
    private fun addPresetName(sysexData: ByteArray, name: String) {
        val truncatedName = if (name.length > MAX_PRESET_NAME_LENGTH) {
            name.substring(0, MAX_PRESET_NAME_LENGTH)
        } else {
            name
        }
        
        // Clear name area
        for (i in PRESET_NAME_POSITION until PRESET_NAME_POSITION + MAX_PRESET_NAME_LENGTH) {
            if (i < ENZO_X_SYSEX_SIZE - 1) {
                sysexData[i] = 0x00
            }
        }
        
        // Write preset name
        truncatedName.toByteArray().forEachIndexed { index, byte ->
            val position = PRESET_NAME_POSITION + index
            if (position < ENZO_X_SYSEX_SIZE - 1) {
                sysexData[position] = byte
            }
        }
    }
    
    /**
     * Create a basic preset template for AI assistants to reference
     * Returns the CC parameters that are most commonly used for sound design
     */
    fun getTemplateParameters(): Map<String, Int> {
        return mapOf(
            "mix" to 1,              // Overall wet/dry mix
            "synth_mode" to 5,       // Mono/Poly/Arp mode
            "osc1_waveform" to 11,   // Primary oscillator
            "osc2_waveform" to 17,   // Secondary oscillator
            "filter_cutoff" to 24,   // Filter frequency
            "filter_resonance" to 25,// Filter resonance
            "amp_attack" to 29,      // Amplitude envelope attack
            "amp_decay" to 30,       // Amplitude envelope decay
            "amp_sustain" to 31,     // Amplitude envelope sustain
            "amp_release" to 32,     // Amplitude envelope release
            "lfo1_rate" to 38,       // Primary LFO speed
            "drive_amount" to 49     // Drive/distortion amount
        )
    }
}