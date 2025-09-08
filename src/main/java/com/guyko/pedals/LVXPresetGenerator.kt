package com.guyko.pedals

import com.guyko.models.MidiSysex
import io.github.oshai.kotlinlogging.KotlinLogging

/**
 * Generates Meris LVX preset sysex files from CC parameter values
 * AI assistants provide the parameter values, MCP generates the sysex
 */
object LVXPresetGenerator {
    private val logger = KotlinLogging.logger {}
    
    /**
     * LVX Sysex header - common to all LVX presets
     */
    private val LVX_SYSEX_HEADER = byteArrayOf(
        0xF0.toByte(), 0x00.toByte(), 0x02.toByte(), 0x10.toByte(), 
        0x00.toByte(), 0x02.toByte(), 0x00.toByte(), 0x26.toByte()
    )
    
    /**
     * Total sysex size for LVX presets (from analysis of existing files)
     */
    private const val LVX_SYSEX_SIZE = 231
    
    /**
     * Position where preset name starts in the sysex (observed from hex dumps)
     */
    private const val PRESET_NAME_POSITION = 212
    
    /**
     * Maximum length for preset names
     */
    private const val MAX_PRESET_NAME_LENGTH = 16
    
    /**
     * Map CC numbers to their byte positions in LVX sysex
     * Based on analysis of LVX sysex structure and MIDI CC table
     */
    private val ccToSysexPosition = mapOf(
        // Global Controls
        1 to 9,    // Mix
        2 to 10,   // Dry Trim  
        3 to 11,   // Wet Trim
        4 to 12,   // Expression Pedal
        
        // Preamp
        5 to 13,   // Preamp Type
        6 to 14,   // Preamp Location
        7 to 15,   // Gain/Volume/Sample Rate
        8 to 16,   // Balance/Bits
        11 to 17,  // Preamp Level
        
        // Delay Structure & Control
        13 to 18,  // Delay Structure
        14 to 19,  // Bypass
        15 to 20,  // Time
        16 to 21,  // Type
        17 to 22,  // Left Note Division
        18 to 23,  // Right Note Division
        19 to 24,  // Feedback
        20 to 25,  // Cross Feedback
        21 to 26,  // Delay Mod
        
        // Delay Parameters (22-61 map to positions 27-66)
        22 to 27, 23 to 28, 24 to 29, 25 to 30, 26 to 31, 27 to 32,
        28 to 33, 29 to 34, 30 to 35, 31 to 36, 32 to 37, 33 to 38,
        34 to 39, 35 to 40, 36 to 41, 37 to 42, 38 to 43, 39 to 44,
        40 to 45, 41 to 46, 42 to 47, 43 to 48, 44 to 49, 45 to 50,
        46 to 51, 47 to 52, 48 to 53, 49 to 54, 50 to 55, 51 to 56,
        52 to 57, 53 to 58, 54 to 59, 55 to 60, 56 to 61, 57 to 62,
        58 to 63, 59 to 64, 60 to 65, 61 to 66,
        
        // Dynamics
        62 to 67,  // Dynamics Type
        63 to 68,  // Dynamics Location
        64 to 69, 65 to 70, 66 to 71, 67 to 72, 68 to 73, 69 to 74,
        
        // Pitch
        70 to 75,  // Pitch Type
        71 to 76,  // Pitch Location
        72 to 77, 73 to 78, 74 to 79, 75 to 80, 76 to 81, 77 to 82,
        
        // Filter
        78 to 83,  // Filter Type
        79 to 84,  // Filter Location
        80 to 85, 81 to 86, 82 to 87, 83 to 88, 84 to 89, 85 to 90,
        
        // Modulation
        86 to 91,  // Mod Type
        87 to 92,  // Mod Location
        88 to 93, 89 to 94, 90 to 95, 91 to 96, 92 to 97, 93 to 98,
        
        // Looper
        94 to 99, 95 to 100, 96 to 101, 97 to 102, 98 to 103, 99 to 104,
        100 to 105, 101 to 106, 102 to 107, 103 to 108,
        
        // Control
        117 to 109, // Toggle Tuner Mode
        118 to 110  // Trigger Hold Modifier
    )
    
    /**
     * Generate LVX preset sysex from CC parameter map
     * @param parameters Map of CC number to value (0-127)
     * @param presetName Name for the preset (max 16 chars)
     * @return MidiSysex object ready for transmission
     */
    fun generatePreset(parameters: Map<Int, Int>, presetName: String): MidiSysex {
        logger.info { "Generating LVX preset '$presetName' with ${parameters.size} CC parameters" }
        logger.debug { "LVX CC parameters: ${parameters.map { (cc, value) -> "CC$cc=$value" }.joinToString(", ")}" }
        
        val sysexData = ByteArray(LVX_SYSEX_SIZE)
        
        // Set LVX header
        LVX_SYSEX_HEADER.copyInto(sysexData, 0)
        logger.debug { "LVX header set: ${LVX_SYSEX_HEADER.joinToString(" ") { "%02X".format(it) }}" }
        
        // Initialize with default values (based on analysis of existing presets)
        initializeDefaults(sysexData)
        logger.debug { "LVX defaults initialized" }
        
        // Map CC parameters to their sysex positions
        var mappedParams = 0
        var skippedParams = 0
        parameters.forEach { (ccNumber, value) ->
            val position = ccToSysexPosition[ccNumber]
            if (position != null && position < LVX_SYSEX_SIZE - 1) {
                require(value in 0..127) { "CC value must be 0-127, got $value for CC$ccNumber" }
                sysexData[position] = (value and 0x7F).toByte() // Ensure 7-bit
                logger.debug { "LVX mapped CC$ccNumber=$value to sysex position $position" }
                mappedParams++
            } else {
                logger.warn { "LVX CC$ccNumber not mapped to sysex position (unknown parameter)" }
                skippedParams++
            }
        }
        logger.info { "LVX parameter mapping complete: $mappedParams mapped, $skippedParams skipped" }
        
        // Add preset name
        addPresetName(sysexData, presetName)
        logger.debug { "LVX preset name '$presetName' added at position $PRESET_NAME_POSITION" }
        
        // Set sysex terminator
        sysexData[LVX_SYSEX_SIZE - 1] = 0xF7.toByte()
        
        val hexString = sysexData.joinToString(" ") { "%02X".format(it) }
        logger.info { "LVX preset sysex generated successfully: ${LVX_SYSEX_SIZE} bytes, name='$presetName'" }
        logger.debug { "LVX sysex data: $hexString" }
        
        return MidiSysex(
            data = sysexData,
            description = "LVX Preset generated from ${parameters.size} parameters",
            presetName = presetName
        )
    }
    
    /**
     * Initialize sysex with sensible default values
     * Based on common patterns observed in existing LVX presets
     */
    private fun initializeDefaults(sysexData: ByteArray) {
        // Set common default patterns found in existing presets
        
        // Default mix and levels
        sysexData[9] = 0x7F   // Mix - 100%
        sysexData[10] = 0x40  // Dry Trim - center
        sysexData[11] = 0x40  // Wet Trim - center
        
        // Default delay settings
        sysexData[18] = 0x00  // Standard delay structure
        sysexData[19] = 0x7F  // FX enabled
        sysexData[20] = 0x40  // Medium time
        sysexData[21] = 0x56  // Digital delay type (common default)
        sysexData[24] = 0x20  // Low feedback
        
        // Common pattern found in multiple presets
        val commonPattern = byteArrayOf(
            0x28, 0x6e, 0x32, 0x1e, 0x0a, 0x32, 0x0a, 0x3c, 
            0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f,
            0x00, 0x12, 0x24, 0x36, 0x49, 0x5b, 0x6d, 0x7f
        )
        commonPattern.copyInto(sysexData, 35)
        
        // Another common pattern (sequencer data)
        val sequencerPattern = byteArrayOf(
            0x55, 0x55, 0x55, 0x55, 0x55, 0x55, 0x55, 0x55
        )
        sequencerPattern.copyInto(sysexData, 59)
        
        // Default step values
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
            if (i < LVX_SYSEX_SIZE - 1) {
                sysexData[i] = 0x00
            }
        }
        
        // Write preset name
        truncatedName.toByteArray().forEachIndexed { index, byte ->
            val position = PRESET_NAME_POSITION + index
            if (position < LVX_SYSEX_SIZE - 1) {
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
            "time" to 15,            // Delay time
            "feedback" to 19,        // Delay feedback
            "delay_type" to 16,      // Delay engine type
            "modulation" to 21,      // Delay modulation
            "filter_freq" to 80,     // Filter frequency
            "filter_res" to 81,      // Filter resonance
            "dynamics_type" to 62,   // Dynamics processing
            "pitch_type" to 70,      // Pitch processing
            "mod_type" to 86         // Modulation type
        )
    }
}