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
     * Generates a pgm90 preset file content
     */
    fun generatePreset(preset: H90Preset): ByteArray {
        val algorithmAJson = generateAlgorithmJson(preset.algorithmA, preset.globalParameters)
        val algorithmBJson = generateAlgorithmJson(preset.algorithmB, preset.globalParameters)
        
        // Create the binary structure with embedded base64 JSON
        return createPgm90Binary(preset, algorithmAJson, algorithmBJson)
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
        // This is a simplified binary format - real pgm90 files have complex binary structures
        // For this implementation, we'll create a minimal binary wrapper with the JSON data
        
        val header = byteArrayOf(
            0x80.toByte(), 0x00, 0x00, 0x00,  // Magic header
            0x03, 0x00, 0x00, 0x00            // Version
        )
        
        val programName = preset.name.toByteArray().take(32).toByteArray()
        val programNamePadded = ByteArray(32)
        programName.copyInto(programNamePadded)
        
        // Algorithm A section
        val algAHeader = byteArrayOf(0x41, 0x00, 0x00, 0x00) // 'A' marker
        val algAData = algorithmAJson.toByteArray()
        val algALength = ByteArray(4)
        algALength[0] = (algAData.size and 0xFF).toByte()
        algALength[1] = ((algAData.size shr 8) and 0xFF).toByte()
        algALength[2] = ((algAData.size shr 16) and 0xFF).toByte()
        algALength[3] = ((algAData.size shr 24) and 0xFF).toByte()
        
        // Algorithm B section  
        val algBHeader = byteArrayOf(0x42, 0x00, 0x00, 0x00) // 'B' marker
        val algBData = algorithmBJson.toByteArray()
        val algBLength = ByteArray(4)
        algBLength[0] = (algBData.size and 0xFF).toByte()
        algBLength[1] = ((algBData.size shr 8) and 0xFF).toByte()
        algBLength[2] = ((algBData.size shr 16) and 0xFF).toByte()
        algBLength[3] = ((algBData.size shr 24) and 0xFF).toByte()
        
        // UUID for the preset
        val uuid = UUID.randomUUID().toString().toByteArray()
        
        // Combine all sections
        return header + programNamePadded + 
               algAHeader + algALength + algAData +
               algBHeader + algBLength + algBData +
               uuid
    }
    
    fun getProductIdForAlgorithm(algorithmName: String): String {
        return ALGORITHM_PRODUCT_IDS[algorithmName] ?: "com.eventide.h9.unknown"
    }
}