package com.guyko.pedals

import java.util.*
import java.util.Base64
import com.google.gson.Gson

/**
 * Generates H90 preset files in pgm90 format
 */
object EventideH90PresetGenerator {
    
    private val ALGORITHM_PRODUCT_IDS = mapOf(
        // Delay algorithms
        "Delay" to "com.eventide.h9.delay",
        "Band Delay" to "com.eventide.h9.banddelay",
        "Bouquet Delay" to "com.eventide.h9.bouquetdelay", 
        "Digital Delay" to "com.eventide.h9.digitaldelay",
        "Ducked Delay" to "com.eventide.h9.duckeddelay",
        "Filter Pong" to "com.eventide.h9.filterpong",
        "Head Space" to "com.eventide.h9.headspace",
        "Mod Delay" to "com.eventide.h9.moddelay",
        "MultiTap" to "com.eventide.h9.multitap",
        "Reverse" to "com.eventide.h9.reverse",
        "Tape Echo" to "com.eventide.h9.tapeecho",
        "UltraTap" to "com.eventide.h9.ultratap",
        "Vintage Delay" to "com.eventide.h9.vintagedelay",
        
        // Distortion algorithms
        "Aggravate" to "com.eventide.h9.aggravate",
        "CrushStation" to "com.eventide.h9.crushstation",
        "PitchFuzz" to "com.eventide.h9.pitchfuzz",
        "Sculpt" to "com.eventide.h9.sculpt",
        "WeedWacker" to "com.eventide.h9.weedwacker",
        
        // EQ algorithms
        "EQ Compressor" to "com.eventide.h9.eqcompressor",
        
        // Harmonizer algorithms
        "Diatonic" to "com.eventide.h9.diatonic",
        "H910 H949" to "com.eventide.h9.h910h949",
        "HarModulator" to "com.eventide.h9.harmodulator",
        "HarPeggiator" to "com.eventide.h9.harpeggiator",
        "Crystals" to "com.eventide.h9.crystals",
        "MicroPitch" to "com.eventide.h9.micropitch",
        "Octaver" to "com.eventide.h9.octaver",
        "PitchFlex" to "com.eventide.h9.pitchflex",
        "PolyFlex" to "com.eventide.h9.polyflex",
        "Polyphony" to "com.eventide.h9.polyphony",
        "Prism Shift" to "com.eventide.h9.prismshift",
        "Quadravox" to "com.eventide.h9.quadravox",
        "Resonator" to "com.eventide.h9.resonator",
        "Harmonizer+" to "com.eventide.h9.harmonizerplus",
        "VocalShift" to "com.eventide.h9.vocalshift",
        "VocalShiftMIDI" to "com.eventide.h9.vocalshiftmidi",
        "VocalTune" to "com.eventide.h9.vocaltune",
        "Quadravox+" to "com.eventide.h9.quadravoxplus",
        
        // Looper
        "Looper" to "com.eventide.h9.looper",
        
        // Modulation algorithms
        "Chorus" to "com.eventide.h9.chorus",
        "Flanger" to "com.eventide.h9.flanger",
        "Harmadillo" to "com.eventide.h9.harmadillo",
        "Instant Flanger" to "com.eventide.h9.instantflanger",
        "Instant Phaser" to "com.eventide.h9.instantphaser",
        "Even-Vibe" to "com.eventide.h9.evenvibe",
        "Q-Wah" to "com.eventide.h9.qwah",
        "RingMod" to "com.eventide.h9.ringmod",
        "Sticky Tape" to "com.eventide.h9.stickytape",
        "TremoloPan" to "com.eventide.h9.tremolopan",
        "Tricerachorus" to "com.eventide.h9.tricerachorus",
        "ModFilter" to "com.eventide.h9.modfilter", 
        "Phaser" to "com.eventide.h9.phaser",
        "Rotary" to "com.eventide.h9.rotary",
        "Undulator" to "com.eventide.h9.undulator",
        "Vibrato" to "com.eventide.h9.vibrato",
        
        // Multi algorithms
        "SpaceTime" to "com.eventide.h9.spacetime",
        
        // Reverb algorithms  
        "Blackhole" to "com.eventide.h9.blackhole",
        "DualVerb" to "com.eventide.h9.dualverb",
        "DynaVerb" to "com.eventide.h9.dynaverb",
        "Hall" to "com.eventide.h9.hall",
        "MangledVerb" to "com.eventide.h9.mangledverb",
        "ModEchoVerb" to "com.eventide.h9.modechoverb",
        "Plate" to "com.eventide.h9.plate",
        "Reverse Reverb" to "com.eventide.h9.reversereverb",
        "Room" to "com.eventide.h9.room",
        "SP2016 Reverb" to "com.eventide.h9.sp2016reverb",
        "Spring" to "com.eventide.h9.spring",
        "Shimmer" to "com.eventide.h9.shimmer",
        "TremoloVerb" to "com.eventide.h9.tremoloverb",
        "Wormhole" to "com.eventide.h9.wormhole",
        
        // Synth algorithms
        "HotSawz" to "com.eventide.h9.hotsawz",
        "PolySynth" to "com.eventide.h9.polysynth",
        "Synthonizer" to "com.eventide.h9.synthonizer",
        
        // Utility algorithms
        "Mute" to "com.eventide.h9.mute",
        "Thru" to "com.eventide.h9.thru"
    )
    
    /**
     * Generates a pgm90 preset file by copying and modifying a working preset
     */
    fun generatePreset(preset: H90Preset): ByteArray {
        // Use working template but fix algorithm IDs properly
        return modifyWorkingPreset(preset)
    }
    
    private fun modifyWorkingPreset(preset: H90Preset): ByteArray {
        // Use the working dft.pgm90 file as template (has correct Crystals + TremoloPan algorithms)
        val templatePath = System.getProperty("user.home") + "/Documents/dft.pgm90"
        val templateFile = java.io.File(templatePath)
        
        if (!templateFile.exists()) {
            // Fallback to any working preset if dft.pgm90 not found
            val presetsDir = java.io.File(System.getProperty("user.home") + "/Downloads/h90/presets")
            val workingPreset = presetsDir.listFiles()?.firstOrNull { it.name.endsWith(".pgm90") }
            if (workingPreset == null) {
                throw RuntimeException("No working H90 preset found to use as template. Please ensure dft.pgm90 exists in Documents folder.")
            }
            return modifyPresetFile(workingPreset.readBytes(), preset)
        }
        
        return modifyPresetFile(templateFile.readBytes(), preset)
    }
    
    private fun modifyPresetFile(templateBytes: ByteArray, preset: H90Preset): ByteArray {
        // Generate new algorithm JSON
        val algorithmAJson = generateAlgorithmJson(preset.algorithmA, preset.globalParameters)
        val algorithmBJson = generateAlgorithmJson(preset.algorithmB, preset.globalParameters)
        
        // Use binary-safe replacement to avoid UTF-8 corruption
        return replaceAlgorithmDataBinary(templateBytes, algorithmAJson, algorithmBJson, preset.name)
    }
    
    
    private fun replaceAlgorithmDataBinary(bytes: ByteArray, algAJson: String, algBJson: String, presetName: String): ByteArray {
        // Find and replace base64 algorithm data at the byte level to avoid encoding issues
        val result = bytes.copyOf()
        
        // Look for the base64 patterns in the binary data
        val algABytes = algAJson.toByteArray()
        val algBBytes = algBJson.toByteArray()
        
        // Find the first base64 encoded JSON block and replace with Algorithm A
        val eyJPattern = "eyJ".toByteArray()
        
        for (i in 0 until result.size - 3) {
            // Look for "eyJ" pattern
            if (result[i] == eyJPattern[0] && 
                result[i + 1] == eyJPattern[1] && 
                result[i + 2] == eyJPattern[2]) {
                
                // Find the end of this base64 block (look for padding or next non-base64 char)
                var j = i + 3
                while (j < result.size) {
                    val char = result[j].toInt().toChar()
                    if (char !in 'A'..'Z' && char !in 'a'..'z' && char !in '0'..'9' && char != '+' && char != '/' && char != '=') {
                        break
                    }
                    j++
                }
                
                // Replace this base64 block with Algorithm A
                val newResult = ByteArray(result.size - (j - i) + algABytes.size)
                
                // Copy before the match
                System.arraycopy(result, 0, newResult, 0, i)
                // Copy replacement
                System.arraycopy(algABytes, 0, newResult, i, algABytes.size)
                // Copy after the match
                System.arraycopy(result, j, newResult, i + algABytes.size, result.size - j)
                
                // Now find and replace the second base64 block with Algorithm B
                return replaceSecondAlgorithm(newResult, algBBytes, presetName)
            }
        }
        
        // If no base64 blocks found, return original with name replacement
        return replacePresetName(result, presetName)
    }
    
    private fun replaceSecondAlgorithm(bytes: ByteArray, algBBytes: ByteArray, presetName: String): ByteArray {
        val result = bytes.copyOf()
        val eyJPattern = "eyJ".toByteArray()
        
        // Find the second base64 encoded JSON block
        for (i in 0 until result.size - 3) {
            if (result[i] == eyJPattern[0] && 
                result[i + 1] == eyJPattern[1] && 
                result[i + 2] == eyJPattern[2]) {
                
                // Find the end of this base64 block
                var j = i + 3
                while (j < result.size) {
                    val char = result[j].toInt().toChar()
                    if (char !in 'A'..'Z' && char !in 'a'..'z' && char !in '0'..'9' && char != '+' && char != '/' && char != '=') {
                        break
                    }
                    j++
                }
                
                // Replace this base64 block with Algorithm B
                val newResult = ByteArray(result.size - (j - i) + algBBytes.size)
                
                // Copy before the match
                System.arraycopy(result, 0, newResult, 0, i)
                // Copy replacement
                System.arraycopy(algBBytes, 0, newResult, i, algBBytes.size)
                // Copy after the match
                System.arraycopy(result, j, newResult, i + algBBytes.size, result.size - j)
                
                // Replace preset name
                return replacePresetName(newResult, presetName)
            }
        }
        
        // If no second algorithm found, just replace preset name
        return replacePresetName(result, presetName)
    }
    
    private fun replacePresetName(bytes: ByteArray, presetName: String): ByteArray {
        val result = bytes.copyOf()
        
        // Replace the preset name "dft" with our preset name (truncated to fit)
        val dftBytes = "dft".toByteArray()
        val nameBytes = presetName.take(3).toByteArray()
        
        for (i in 0..result.size - dftBytes.size) {
            if (result.sliceArray(i until i + dftBytes.size).contentEquals(dftBytes)) {
                System.arraycopy(nameBytes, 0, result, i, minOf(nameBytes.size, dftBytes.size))
                break
            }
        }
        
        return result
    }
    
    private fun buildCompletePresetStructure(preset: H90Preset, algAJson: String, algBJson: String): ByteArray {
        val buffer = mutableListOf<Byte>()
        
        // Start with working preset header structure
        buffer.addAll(createWorkingHeader())
        
        // Add all required parameter objects (30 of them)
        buffer.addAll(createAllParameterObjects())
        
        // Add algorithm JSON data sections
        buffer.addAll(algAJson.toByteArray().toList())
        
        // Padding
        while (buffer.size % 4 != 0) buffer.add(0x00)
        
        buffer.addAll(algBJson.toByteArray().toList())
        
        // Padding
        while (buffer.size % 4 != 0) buffer.add(0x00)
        
        // Add all required metadata sections
        buffer.addAll(createCompleteMetadata(preset))
        
        return buffer.toByteArray()
    }
    
    private fun createWorkingHeader(): List<Byte> {
        return listOf(
            // Exact header from working TDrive Delay preset
            0x10, 0x00, 0x00, 0x00, 0x0C, 0x00, 0x16, 0x00,
            0x04, 0x00, 0x08, 0x00, 0x0C, 0x00, 0x10, 0x00,
            0x0C, 0x00, 0x00, 0x00, 0x28, 0x12, 0x00, 0x00,
            0xA0, 0x0C, 0x00, 0x00, 0x58, 0x07, 0x00, 0x00,
            0x14, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0E, 0x00,
            0x10, 0x00, 0x04, 0x00, 0x08, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x0C, 0x00, 0x0E, 0x00, 0x00, 0x00
        ).map { it.toByte() }
    }
    
    private fun createAllParameterObjects(): List<Byte> {
        val objects = mutableListOf<Byte>()
        
        // Create 30 parameter objects as found in working preset
        val parameterNames = listOf(
            "alg-in-gain-obj", "level2Out-obj", "midEQLvl2-obj", "tone2Control-obj",
            "drive2-obj", "stage2bypass-obj", "level1Out-obj", "midEQLvl1-obj", 
            "tone1Control-obj", "drive1-obj", "gateThr-obj", "mix-obj",
            "level1Out-obj", "midEQLvl1-obj", "tone1Control-obj", "drive1-obj",
            "alg-hotknob-obj", "program-hotknob-obj", "activeBypassMomentary-obj",
            "activeBypassLatching-obj", "Boil-obj", "BreakdownTime-obj", "BoilTime-obj",
            "PAN4-obj", "Head4Div-obj", "TAP4-obj", "PAN3-obj", "Head3Div-obj",
            "TAP3-obj", "PAN2-obj"
        )
        
        parameterNames.forEach { name ->
            objects.addAll(createParameterObject(name))
        }
        
        return objects
    }
    
    private fun createParameterObject(name: String): List<Byte> {
        val obj = mutableListOf<Byte>()
        
        // Standard parameter object structure
        obj.addAll(listOf(
            0x40, 0x04, 0x00, 0x00, 0x84, 0x03, 0x00, 0x00,
            0x04, 0x00, 0x00, 0x00, 0x0D, 0x00, 0x00, 0x00
        ).map { it.toByte() })
        
        // Object name
        val nameBytes = name.toByteArray()
        obj.addAll(listOf(nameBytes.size.toByte(), 0x00, 0x00, 0x00))
        obj.addAll(nameBytes.toList())
        obj.add(0x00)
        
        // Padding
        while (obj.size % 4 != 0) obj.add(0x00)
        
        return obj
    }
    
    private fun createCompleteMetadata(preset: H90Preset): List<Byte> {
        val metadata = mutableListOf<Byte>()
        
        // Program name section
        val nameBytes = preset.name.toByteArray()
        metadata.addAll(listOf(nameBytes.size.toByte(), 0x00, 0x00, 0x00))
        metadata.addAll(nameBytes.toList())
        metadata.add(0x00)
        
        // UUID sections
        repeat(3) {
            val uuid = UUID.randomUUID().toString()
            metadata.addAll(listOf(0x24, 0x00, 0x00, 0x00).map { it.toByte() })
            metadata.addAll(uuid.toByteArray().toList())
            metadata.add(0x00)
            while (metadata.size % 4 != 0) metadata.add(0x00)
        }
        
        return metadata
    }
    
    private fun generateAlgorithmJson(algorithm: H90Algorithm, global: H90GlobalParameters): String {
        val jsonMap = mutableMapOf<String, Any>()
        
        // Required fields
        jsonMap["algorithm_name"] = algorithm.algorithmName
        jsonMap["product_id"] = algorithm.productId
        jsonMap["preset_name"] = algorithm.presetName
        jsonMap["version"] = "3"
        
        // Global parameters
        jsonMap["tmpv"] = global.tempo
        jsonMap["tsyn"] = global.tempoSync
        jsonMap["killdry"] = if (global.killDry) 1.0 else 0.0
        jsonMap["preset_mix"] = global.presetMix
        jsonMap["expression_pedal"] = global.expressionPedal
        
        // Algorithm-specific parameters
        jsonMap.putAll(algorithm.parameters)
        
        // Bypass settings
        jsonMap["bypa_normal"] = if (algorithm.bypass) 0.0 else algorithm.mix
        jsonMap["bypt_normal"] = if (algorithm.bypass) 1.0 else 0.0
        
        // Input/output sensitivity (standard values)
        jsonMap["in1_sens"] = 1.0
        jsonMap["in2_sens"] = 1.0
        jsonMap["out1_sens"] = 1.0
        jsonMap["out2_sens"] = 1.0
        
        // Convert to JSON and encode as base64
        val gson = Gson()
        val jsonString = gson.toJson(jsonMap)
        return Base64.getEncoder().encodeToString((jsonString + "\n").toByteArray())
    }
    
    private fun createPgm90Binary(preset: H90Preset, algorithmAJson: String, algorithmBJson: String): ByteArray {
        // Create exact FlatBuffers structure matching working H90 .pgm90 files
        // Based on analysis of "TDrive Delay.pgm90" - a working exported preset
        
        val buffer = mutableListOf<Byte>()
        
        // FlatBuffers root header (exact match with working preset)
        buffer.addAll(listOf(
            0x10, 0x00, 0x00, 0x00,  // Root table offset (16)
            0x0C, 0x00, 0x16, 0x00,  // FlatBuffers version/identifier
            0x04, 0x00, 0x08, 0x00,  // Table vtable offset
            0x0C, 0x00, 0x10, 0x00   // Table structure
        ).map { it.toByte() })
        
        // Main table header (updated offsets to match working preset)
        buffer.addAll(listOf(
            0x0C, 0x00, 0x00, 0x00,  // Table size
            0x28, 0x12, 0x00, 0x00,  // Main offset (4648) - matches working preset
            0xA0, 0x0C, 0x00, 0x00,  // Algorithm section offset (3232)
            0x58, 0x07, 0x00, 0x00   // Metadata section offset (1880)
        ).map { it.toByte() })
        
        // Algorithm data section structure (matches working preset)
        buffer.addAll(listOf(
            0x14, 0x00, 0x00, 0x00,  // Algorithm table header
            0x00, 0x00, 0x0E, 0x00,  // Table metadata
            0x10, 0x00, 0x04, 0x00,  // Vtable structure
            0x08, 0x00, 0x00, 0x00   // Additional metadata
        ).map { it.toByte() })
        
        // Extended hot knob configuration (matches working preset structure)
        buffer.addAll(createWorkingHotKnobStructure())
        
        // Algorithm A data
        buffer.addAll(algorithmAJson.toByteArray().toList())
        
        // Padding to 4-byte boundary
        while (buffer.size % 4 != 0) {
            buffer.add(0x00)
        }
        
        // Algorithm B data  
        buffer.addAll(algorithmBJson.toByteArray().toList())
        
        // Padding to 4-byte boundary
        while (buffer.size % 4 != 0) {
            buffer.add(0x00)
        }
        
        // Program name section (32 bytes, null-terminated)
        val nameBytes = preset.name.toByteArray().take(31).toByteArray()
        val nameSection = ByteArray(32)
        System.arraycopy(nameBytes, 0, nameSection, 0, nameBytes.size)
        buffer.addAll(nameSection.toList())
        
        // Preset metadata and UUID sections (from working structure)
        buffer.addAll(createWorkingMetadataSection(preset))
        
        return buffer.toByteArray()
    }
    
    private fun createWorkingHotKnobStructure(): List<Byte> {
        // Hot knob structure from working "TDrive Delay.pgm90" - has multiple knobs
        val buffer = mutableListOf<Byte>()
        
        // Table header with hot knob count and offsets (from working preset)
        buffer.addAll(listOf(
            0x00, 0x00, 0x0C, 0x00, 0x0E, 0x00, 0x00, 0x00,
            0x40, 0x04, 0x00, 0x00, 0x84, 0x03, 0x00, 0x00,
            0x04, 0x00, 0x00, 0x00, 0x0D, 0x00, 0x00, 0x00,  // 13 hot knobs
            0x3C, 0x03, 0x00, 0x00, 0xFC, 0x02, 0x00, 0x00,
            0xB8, 0x02, 0x00, 0x00, 0x68, 0x02, 0x00, 0x00,
            0x24, 0x02, 0x00, 0x00, 0xE0, 0x01, 0x00, 0x00,
            0x9C, 0x01, 0x00, 0x00, 0x58, 0x01, 0x00, 0x00,
            0x14, 0x01, 0x00, 0x00, 0xD0, 0x00, 0x00, 0x00,
            0x8C, 0x00, 0x00, 0x00, 0x48, 0x00, 0x00, 0x00
        ).map { it.toByte() })
        
        // Hot knob entries (simplified - will create standard knob entries)
        for (i in 2..10) {
            buffer.addAll(createHotKnobEntry("tjknobs-knob$i"))
        }
        
        return buffer
    }
    
    private fun createHotKnobEntry(knobName: String): List<Byte> {
        val entry = mutableListOf<Byte>()
        
        // Standard hot knob entry structure
        entry.addAll(listOf(
            0x04, 0x00, 0x00, 0x00, 0x7E.toByte(), 0xFD.toByte(), 0xFF.toByte(), 0xFF.toByte(),
            0x28, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x03, 0x00, 0x05, 0x00, 0x00, 0x00,
            0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0x00, 0x05, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x80.toByte(), 0x3F,
            0xEC.toByte(), 0x51, 0xF8.toByte(), 0x3E, 0xE4.toByte(), 0x14, 0x5D, 0x3E
        ).map { it.toByte() })
        
        // Knob name length and data
        val nameBytes = knobName.toByteArray()
        entry.addAll(listOf(
            (nameBytes.size and 0xFF).toByte(), 0x00, 0x00, 0x00
        ))
        entry.addAll(nameBytes.toList())
        entry.add(0x00)  // null terminator
        
        // Padding to 4-byte boundary
        while (entry.size % 4 != 0) {
            entry.add(0x00)
        }
        
        return entry
    }
    
    private fun createWorkingMetadataSection(preset: H90Preset): List<Byte> {
        // Metadata section structure from working "TDrive Delay.pgm90"
        val metadata = mutableListOf<Byte>()
        
        // Algorithm data sections (placeholders)
        metadata.addAll(createWorkingAlgorithmMetadata())
        
        // Program name and routing info
        metadata.addAll(createWorkingProgramInfo(preset.name))
        
        // UUID and preset identification
        metadata.addAll(createWorkingUuidSection())
        
        return metadata
    }
    
    private fun createWorkingAlgorithmMetadata(): List<Byte> {
        // Algorithm metadata structure from working preset
        return listOf(
            // Algorithm A section markers
            0x90, 0x01, 0x00, 0x00, 0x94, 0x01, 0x00, 0x00,
            0x98, 0x01, 0x00, 0x00, 0x9C, 0x01, 0x00, 0x00,
            0xA0, 0x01, 0x00, 0x00, 0xA4, 0x01, 0x00, 0x00,
            // Algorithm B section markers
            0xA8, 0x01, 0x00, 0x00, 0xAC, 0x01, 0x00, 0x00,
            0xB0, 0x01, 0x00, 0x00, 0xB4, 0x01, 0x00, 0x00,
            0xB8, 0x01, 0x00, 0x00, 0xBC, 0x01, 0x00, 0x00,
            // Termination markers
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
        ).map { it.toByte() }
    }
    
    private fun createWorkingProgramInfo(programName: String): List<Byte> {
        val info = mutableListOf<Byte>()
        
        // Program section header
        info.addAll(listOf(0x1C, 0x00, 0x00, 0x00).map { it.toByte() })
        
        // Program name with length prefix
        val nameBytes = programName.toByteArray()
        info.addAll(listOf(
            (nameBytes.size and 0xFF).toByte(), 0x00, 0x00, 0x00
        ))
        info.addAll(nameBytes.toList())
        info.add(0x00)  // null terminator
        
        // Padding to 4-byte boundary
        while (info.size % 4 != 0) {
            info.add(0x00)
        }
        
        return info
    }
    
    private fun createWorkingUuidSection(): List<Byte> {
        // UUID section structure from working preset
        val uuids = mutableListOf<Byte>()
        
        // Generate standard UUIDs
        val presetUuid = UUID.randomUUID().toString()
        val algorithmAUuid = UUID.randomUUID().toString()
        val algorithmBUuid = UUID.randomUUID().toString()
        
        // Add UUIDs with proper formatting
        uuids.addAll(createUuidEntry(presetUuid))
        uuids.addAll(createUuidEntry(algorithmAUuid))
        uuids.addAll(createUuidEntry(algorithmBUuid))
        
        return uuids
    }
    
    private fun createUuidEntry(uuid: String): List<Byte> {
        val entry = mutableListOf<Byte>()
        
        // UUID length prefix
        entry.addAll(listOf(0x24, 0x00, 0x00, 0x00).map { it.toByte() })
        
        // UUID string
        entry.addAll(uuid.toByteArray().toList())
        entry.add(0x00)  // null terminator
        
        // Padding to 4-byte boundary
        while (entry.size % 4 != 0) {
            entry.add(0x00)
        }
        
        return entry
    }
    
    private fun createStringSection(str: String): List<Byte> {
        val bytes = str.toByteArray()
        val section = mutableListOf<Byte>()
        
        // String length (little-endian)
        val length = bytes.size
        section.addAll(listOf(
            (length and 0xFF).toByte(),
            ((length shr 8) and 0xFF).toByte(),
            ((length shr 16) and 0xFF).toByte(),
            ((length shr 24) and 0xFF).toByte()
        ))
        
        // String data
        section.addAll(bytes.toList())
        
        // Null terminator and padding to 4-byte boundary
        section.add(0x00)
        while (section.size % 4 != 0) {
            section.add(0x00)
        }
        
        return section
    }
    
    fun getProductIdForAlgorithm(algorithmName: String): String {
        return ALGORITHM_PRODUCT_IDS[algorithmName] ?: "com.eventide.h9.unknown"
    }
}