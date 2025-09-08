package com.guyko.pedals

import com.guyko.models.MidiSysex
import io.github.oshai.kotlinlogging.KotlinLogging

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
     * Initialize sysex with exact patterns from working Mercury X sysex
     * Based on analysis of MercuryX-out-sea.syx that works with the pedal
     */
    private fun initializeDefaults(sysexData: ByteArray) {
        // Copy exact working pattern from MercuryX-out-sea.syx
        
        // Position 8: Critical preset enable flag
        sysexData[8] = 0x01
        
        // Positions 9-11: Mix and trim settings from working file
        sysexData[9] = 0x70   // Mix
        sysexData[10] = 0x67  // Dry Trim (from working file, not 0x40)
        sysexData[11] = 0x00  // Wet Trim (from working file)
        
        // Positions 12-14: Critical control bytes
        sysexData[12] = 0x00
        sysexData[13] = 0x00
        sysexData[14] = 0x08  // Critical byte from working file (not 0x7F)
        
        // Positions 15-20: Level and reverb settings from working file
        sysexData[15] = 0x40  // Input level
        sysexData[16] = 0x46  // Output level (from working file)
        sysexData[17] = 0x00  // Reverb level (from working file)
        sysexData[18] = 0x7F  // Tone (from working file)
        sysexData[19] = 0x00  // Decay (from working file)
        sysexData[20] = 0x00  // Pre-delay (from working file)
        
        // Continue with exact pattern from working file
        sysexData[21] = 0x40
        sysexData[22] = 0x00
        sysexData[23] = 0x00
        sysexData[24] = 0x00
        sysexData[25] = 0x00
        sysexData[26] = 0x25
        sysexData[27] = 0x7F
        sysexData[28] = 0x40
        
        // Fill in more critical pattern bytes from working file
        val workingPattern1 = byteArrayOf(
            0x7F, 0x42, 0x2A, 0x00, 0x00, 0x00, 0x00, 0x16,
            0x10, 0x1E, 0x46, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x1D, 0x59, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x64, 0x00, 0x20, 0x00, 0x00
        )
        workingPattern1.copyInto(sysexData, 32)
        
        // Pattern at position 50-65 from working file
        val workingPattern2 = byteArrayOf(
            0x24, 0x36, 0x49, 0x5B, 0x6D, 0x7F, 0x55, 0x55,
            0x55, 0x55, 0x55, 0x55, 0x55, 0x55, 0x00, 0x00
        )
        workingPattern2.copyInto(sysexData, 80)
        
        // Critical bytes at position 107
        sysexData[107] = 0x4E
        sysexData[108] = 0x40
        sysexData[109] = 0x40
        
        // Pattern at position 115
        sysexData[115] = 0x20
        sysexData[116] = 0x7F
        sysexData[117] = 0x7B
        
        // Critical byte at position 123
        sysexData[123] = 0x40
        
        // Pattern at position 131-135
        sysexData[131] = 0x3F
        sysexData[132] = 0x7F
        sysexData[133] = 0x00
        sysexData[134] = 0x7F
        
        // Working step pattern at position 160 (this was correct)
        val stepPattern = byteArrayOf(
            0x01, 0x10, 0x20, 0x30, 0x40, 0x50, 0x60, 0x70,
            0x7F, 0x70, 0x60, 0x50, 0x40, 0x30, 0x20, 0x10,
            0x00, 0x00, 0x7F, 0x00, 0x00, 0x7F, 0x00, 0x00,
            0x7F, 0x00, 0x00, 0x7F, 0x00, 0x00, 0x7F, 0x00,
            0x00, 0x7F, 0x00, 0x00, 0x7F, 0x00, 0x00, 0x00,
            0x01, 0x4D, 0x01, 0x03, 0x00, 0x14, 0x00, 0x44,
            0x00, 0x39, 0x00, 0x3B
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