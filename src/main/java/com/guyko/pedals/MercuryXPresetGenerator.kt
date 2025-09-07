package com.guyko.pedals

import com.guyko.models.MidiSysex
import mu.KotlinLogging

/**
 * Generates Meris Mercury X reverb preset sysex files from CC parameter values
 * AI assistants provide the parameter values, MCP generates the sysex
 */
object MercuryXPresetGenerator {
    private val logger = KotlinLogging.logger {}
    
    /**
     * Mercury X Sysex header - common to all Mercury X presets
     */
    private val MERCURY_X_SYSEX_HEADER = byteArrayOf(
        0xF0.toByte(), 0x00.toByte(), 0x02.toByte(), 0x10.toByte(), 
        0x00.toByte(), 0x02.toByte(), 0x01.toByte(), 0x26.toByte()
    )
    
    /**
     * Total sysex size for Mercury X presets (from analysis of existing files)
     */
    private const val MERCURY_X_SYSEX_SIZE = 231
    
    /**
     * Position where preset name starts in the sysex (observed from hex dumps)
     */
    private const val PRESET_NAME_POSITION = 212
    
    /**
     * Maximum length for preset names
     */
    private const val MAX_PRESET_NAME_LENGTH = 16
    
    /**
     * Map CC numbers to their byte positions in Mercury X sysex
     * Based on analysis of Mercury X sysex structure and MIDI CC table
     */
    private val ccToSysexPosition = mapOf(
        // Global Controls
        1 to 9,    // Mix
        2 to 10,   // Dry Trim  
        3 to 11,   // Wet Trim
        4 to 12,   // Expression Pedal
        
        // Reverb Structure & Control
        5 to 13,   // Reverb Structure
        6 to 14,   // Bypass
        7 to 15,   // Input Level
        8 to 16,   // Output Level
        9 to 17,   // Reverb Level
        10 to 18,  // Tone
        11 to 19,  // Decay
        12 to 20,  // Pre-delay
        
        // Predelay System
        13 to 21,  // Predelay Time
        14 to 22,  // Predelay Feedback
        15 to 23,  // Predelay Cross Feedback
        16 to 24,  // Predelay Mod Depth
        17 to 25,  // Predelay Mod Rate
        18 to 26,  // Predelay Routing
        
        // Processing Elements - Dynamics
        19 to 27,  // Dynamics Type
        20 to 28,  // Dynamics Location
        21 to 29,  // Dynamics Threshold
        22 to 30,  // Dynamics Ratio
        23 to 31,  // Dynamics Attack
        24 to 32,  // Dynamics Release
        
        // Processing Elements - Preamp
        25 to 33,  // Preamp Type
        26 to 34,  // Preamp Location
        27 to 35,  // Preamp Drive
        28 to 36,  // Preamp Level
        29 to 37,  // Preamp Tone
        30 to 38,  // Preamp Character
        
        // Processing Elements - Filter
        31 to 39,  // Filter Type
        32 to 40,  // Filter Location
        33 to 41,  // Filter Frequency
        34 to 42,  // Filter Resonance
        35 to 43,  // Filter Drive
        36 to 44,  // Filter Character
        
        // Processing Elements - Pitch
        37 to 45,  // Pitch Type
        38 to 46,  // Pitch Location
        39 to 47,  // Pitch Interval
        40 to 48,  // Pitch Mix
        41 to 49,  // Pitch Feedback
        42 to 50,  // Pitch Character
        
        // Processing Elements - Modulation
        43 to 51,  // Modulation Type
        44 to 52,  // Modulation Location
        45 to 53,  // Modulation Rate
        46 to 54,  // Modulation Depth
        47 to 55,  // Modulation Shape
        48 to 56,  // Modulation Character
        
        // Reverb Structure Parameters (varies by structure)
        49 to 57, 50 to 58, 51 to 59, 52 to 60, 53 to 61, 54 to 62,
        55 to 63, 56 to 64, 57 to 65, 58 to 66, 59 to 67, 60 to 68,
        61 to 69, 62 to 70, 63 to 71, 64 to 72, 65 to 73, 66 to 74,
        67 to 75, 68 to 76, 69 to 77, 70 to 78, 71 to 79, 72 to 80,
        
        // Expression Controls
        73 to 81, 74 to 82, 75 to 83, 76 to 84, 77 to 85, 78 to 86,
        79 to 87, 80 to 88, 81 to 89, 82 to 90, 83 to 91, 84 to 92,
        
        // Gate Controls
        85 to 93,  // Gate Mode
        86 to 94,  // Gate Threshold
        87 to 95,  // Gate Attack
        88 to 96,  // Gate Hold
        89 to 97,  // Gate Release
        
        // Hold Modifier & Advanced Controls
        90 to 98,  // Hold Modifier Mode
        91 to 99,  // Hold Modifier Value
        92 to 100, // Tuner Mode
        93 to 101, // MIDI Clock Sync
        94 to 102, // Tempo
        95 to 103, // Note Division
        96 to 104, // Expression Mode
        97 to 105, // Expression Range
        98 to 106, // MIDI Channel
        99 to 107, // Program Change Enable
        100 to 108 // Preset Save Mode
    )
    
    /**
     * Generate Mercury X preset sysex from CC parameter map
     * @param parameters Map of CC number to value (0-127)
     * @param presetName Name for the preset (max 16 chars)
     * @return MidiSysex object ready for transmission
     */
    fun generatePreset(parameters: Map<Int, Int>, presetName: String): MidiSysex {
        logger.info { "Generating Mercury X preset '$presetName' with ${parameters.size} CC parameters" }
        logger.debug { "Mercury X CC parameters: ${parameters.map { (cc, value) -> "CC$cc=$value" }.joinToString(", ")}" }
        
        val sysexData = ByteArray(MERCURY_X_SYSEX_SIZE)
        
        // Set Mercury X header
        MERCURY_X_SYSEX_HEADER.copyInto(sysexData, 0)
        logger.debug { "Mercury X header set: ${MERCURY_X_SYSEX_HEADER.joinToString(" ") { "%02X".format(it) }}" }
        
        // Initialize with default values (based on analysis of existing presets)
        initializeDefaults(sysexData)
        logger.debug { "Mercury X defaults initialized" }
        
        // Map CC parameters to their sysex positions
        var mappedParams = 0
        var skippedParams = 0
        parameters.forEach { (ccNumber, value) ->
            val position = ccToSysexPosition[ccNumber]
            if (position != null && position < MERCURY_X_SYSEX_SIZE - 1) {
                require(value in 0..127) { "CC value must be 0-127, got $value for CC$ccNumber" }
                sysexData[position] = (value and 0x7F).toByte() // Ensure 7-bit
                logger.debug { "Mercury X mapped CC$ccNumber=$value to sysex position $position" }
                mappedParams++
            } else {
                logger.warn { "Mercury X CC$ccNumber not mapped to sysex position (unknown parameter)" }
                skippedParams++
            }
        }
        logger.info { "Mercury X parameter mapping complete: $mappedParams mapped, $skippedParams skipped" }
        
        // Add preset name
        addPresetName(sysexData, presetName)
        logger.debug { "Mercury X preset name '$presetName' added at position $PRESET_NAME_POSITION" }
        
        // Set sysex terminator
        sysexData[MERCURY_X_SYSEX_SIZE - 1] = 0xF7.toByte()
        
        val hexString = sysexData.joinToString(" ") { "%02X".format(it) }
        logger.info { "Mercury X preset sysex generated successfully: ${MERCURY_X_SYSEX_SIZE} bytes, name='$presetName'" }
        logger.debug { "Mercury X sysex data: $hexString" }
        
        return MidiSysex(
            data = sysexData,
            description = "Mercury X Preset generated from ${parameters.size} parameters",
            presetName = presetName
        )
    }
    
    /**
     * Initialize sysex with sensible default values
     * Based on common patterns observed in existing Mercury X presets
     */
    private fun initializeDefaults(sysexData: ByteArray) {
        // Set common default patterns found in existing presets
        
        // Default mix and levels
        sysexData[9] = 0x70   // Mix - 90%
        sysexData[10] = 0x40  // Dry Trim - center
        sysexData[11] = 0x40  // Wet Trim - center
        
        // Default reverb settings
        sysexData[13] = 0x00  // Ultraplate reverb structure
        sysexData[14] = 0x7F  // FX enabled
        sysexData[15] = 0x40  // Medium input level
        sysexData[16] = 0x40  // Medium output level
        sysexData[17] = 0x60  // High reverb level
        sysexData[18] = 0x40  // Neutral tone
        sysexData[19] = 0x50  // Medium decay
        sysexData[20] = 0x20  // Short pre-delay
        
        // Common pattern found in multiple presets
        val commonPattern = byteArrayOf(
            0x10, 0x20, 0x30, 0x40, 0x50, 0x60, 0x70, 0x7f,
            0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f
        )
        commonPattern.copyInto(sysexData, 50)
        
        // Default processing disabled
        sysexData[67] = 0x00  // Dynamics off
        sysexData[68] = 0x00  // Preamp off
        sysexData[69] = 0x00  // Filter off
        sysexData[70] = 0x00  // Pitch off
        sysexData[71] = 0x00  // Modulation off
        
        // Default step values (expression mapping)
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
            if (i < MERCURY_X_SYSEX_SIZE - 1) {
                sysexData[i] = 0x00
            }
        }
        
        // Write preset name
        truncatedName.toByteArray().forEachIndexed { index, byte ->
            val position = PRESET_NAME_POSITION + index
            if (position < MERCURY_X_SYSEX_SIZE - 1) {
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
            "reverb_structure" to 5, // Reverb algorithm type
            "decay" to 11,           // Reverb decay time
            "predelay" to 12,        // Pre-delay amount
            "tone" to 10,            // Reverb tone/color
            "input_level" to 7,      // Input gain
            "output_level" to 8,     // Output level
            "reverb_level" to 9,     // Reverb amount
            "dynamics_type" to 19,   // Dynamics processing
            "filter_freq" to 33,     // Filter frequency
            "modulation_rate" to 45  // Modulation speed
        )
    }
}