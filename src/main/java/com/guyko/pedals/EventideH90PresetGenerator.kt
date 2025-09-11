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
        return try {
            // Translate generic parameter names to H9 Control format before generation
            val translatedPreset = translateParameters(preset)
            
            // Use working template but fix algorithm IDs properly
            modifyWorkingPreset(translatedPreset)
        } catch (e: Exception) {
            println("Error during preset generation: ${e.message}")
            println("Falling back to minimal preset copy with name change only")
            
            // Fallback: just copy a working preset and change the name
            createMinimalWorkingPreset(preset.name)
        }
    }
    
    /**
     * Fallback method: create a working preset by just copying a template and changing the name
     */
    private fun createMinimalWorkingPreset(presetName: String): ByteArray {
        val presetsDir = java.io.File(System.getProperty("user.home") + "/Downloads/h90/presets")
        val workingPreset = presetsDir.listFiles()?.firstOrNull { it.name.endsWith(".pgm90") }
        
        if (workingPreset == null) {
            throw RuntimeException("No working H90 preset found for fallback. Please place a .pgm90 file in ~/Downloads/h90/presets/")
        }
        
        println("Creating minimal preset from: ${workingPreset.name}")
        val templateBytes = workingPreset.readBytes()
        
        // Only change the preset name, keep everything else intact
        return replacePresetName(templateBytes, presetName)
    }
    
    /**
     * Translate generic parameter names to actual H9 Control parameter names
     */
    private fun translateParameters(preset: H90Preset): H90Preset {
        return preset.copy(
            algorithmA = translateAlgorithmParameters(preset.algorithmA),
            algorithmB = translateAlgorithmParameters(preset.algorithmB)
        )
    }
    
    private fun translateAlgorithmParameters(algorithm: H90Algorithm): H90Algorithm {
        val algorithmInfo = EventideH90AlgorithmMappings.getAlgorithmInfo(algorithm.algorithmNumber)
        if (algorithmInfo == null) {
            println("Warning: No algorithm info found for algorithm ${algorithm.algorithmNumber}")
            return algorithm
        }
        
        val translatedParams = mutableMapOf<String, Any>()
        
        // Get the expected H9 parameter names for this algorithm
        val expectedParams = algorithmInfo.keyParameters.keys
        
        // Translate common generic parameter names to H9 Control names
        algorithm.parameters.forEach { (genericName, value) ->
            val h9ParamName = translateParameterName(genericName, expectedParams, algorithm.algorithmName)
            if (h9ParamName != null) {
                translatedParams[h9ParamName] = value
                println("Translated parameter: $genericName -> $h9ParamName = $value")
            } else {
                println("Warning: Could not translate parameter '$genericName' for algorithm ${algorithm.algorithmName}")
                // Keep the original name as fallback
                translatedParams[genericName] = value
            }
        }
        
        return algorithm.copy(parameters = translatedParams)
    }
    
    /**
     * Translate generic parameter names to specific H9 Control parameter names
     */
    private fun translateParameterName(genericName: String, expectedParams: Set<String>, algorithmName: String): String? {
        // Direct match first (case-insensitive)
        val directMatch = expectedParams.find { it.equals(genericName, ignoreCase = true) }
        if (directMatch != null) return directMatch
        
        // Common parameter name translations
        val commonTranslations = mapOf(
            "mix" to listOf("Mix"),
            "feedback" to listOf("Feedback", "Feedback A", "Fdbk", "Fdbk A"),
            "delay" to listOf("Delay A", "Time", "Delay", "Delay Time"),
            "time" to listOf("Time", "Delay A", "Delay", "Delay Time"),
            "tone" to listOf("Tone", "Filter", "High Cut", "Low Cut"),
            "depth" to listOf("Depth", "Intensity", "Mod Depth", "Modulation Depth"),
            "rate" to listOf("Rate", "Speed", "LFO Rate", "Modulation Rate"),
            "speed" to listOf("Speed", "Rate", "LFO Rate"),
            "pitch" to listOf("Pitch", "Shift", "Pitch Shift", "Coarse", "Fine"),
            "steps" to listOf("Steps", "Taps", "Divisions"),
            "swing" to listOf("Swing", "Rhythm", "Pattern"),
            "pattern" to listOf("Pattern", "Sequence", "Steps")
        )
        
        // Algorithm-specific translations
        val algorithmSpecificTranslations = when (algorithmName) {
            "Vintage Delay" -> mapOf(
                "delay" to "Delay A",
                "feedback" to "Feedback A", 
                "tone" to "Filter A",
                "mix" to "Mix"
            )
            "TremoloPan" -> mapOf(
                "rate" to "Speed",
                "depth" to "Intensity",
                "speed" to "Speed",
                "mix" to "Mix"
            )
            else -> emptyMap()
        }
        
        // Try algorithm-specific translation first
        algorithmSpecificTranslations[genericName.lowercase()]?.let { specificParam ->
            if (expectedParams.contains(specificParam)) {
                return specificParam
            }
        }
        
        // Try common translations
        commonTranslations[genericName.lowercase()]?.forEach { possibleMatch ->
            if (expectedParams.contains(possibleMatch)) {
                return possibleMatch
            }
        }
        
        // Try fuzzy matching (contains)
        expectedParams.forEach { expectedParam ->
            if (expectedParam.contains(genericName, ignoreCase = true) ||
                genericName.contains(expectedParam, ignoreCase = true)) {
                return expectedParam
            }
        }
        
        return null
    }
    
    private fun modifyWorkingPreset(preset: H90Preset): ByteArray {
        // Use a simple, single-algorithm preset as template for more reliable modification
        val presetsDir = java.io.File(System.getProperty("user.home") + "/Downloads/h90/presets")
        
        // Prefer single-algorithm presets for simpler modification
        val preferredTemplates = listOf(
            "TDrive Delay.pgm90",      // Single Digital Delay algorithm
            "Autowah.pgm90",           // Single Q-Wah algorithm  
            "Clean Ambient.pgm90",     // Single Digital Delay algorithm
            "Teacher Preacher.pgm90"   // Single MicroPitch algorithm
        )
        
        var workingPreset: java.io.File? = null
        
        // Try preferred templates first
        for (templateName in preferredTemplates) {
            val candidate = java.io.File(presetsDir, templateName)
            if (candidate.exists()) {
                workingPreset = candidate
                break
            }
        }
        
        // Fallback to any .pgm90 file
        if (workingPreset == null) {
            workingPreset = presetsDir.listFiles()?.firstOrNull { it.name.endsWith(".pgm90") }
        }
        
        if (workingPreset == null) {
            throw RuntimeException("No working H90 preset found to use as template. Please place a .pgm90 file in ~/Downloads/h90/presets/")
        }
        
        println("Using template: ${workingPreset.name}")
        val templateBytes = workingPreset.readBytes()
        println("Template size: ${templateBytes.size} bytes")
        
        return modifyPresetFile(templateBytes, preset)
    }
    
    private fun modifyPresetFile(templateBytes: ByteArray, preset: H90Preset): ByteArray {
        // Generate new algorithm JSON
        val algorithmAJson = generateAlgorithmJson(preset.algorithmA, preset.globalParameters)
        val algorithmBJson = generateAlgorithmJson(preset.algorithmB, preset.globalParameters)
        
        // Replace the base64 algorithm data at known exact offsets to avoid corruption
        return replaceBase64AtOffsets(templateBytes, algorithmAJson, algorithmBJson, preset.name)
    }
    
    private fun replaceBase64AtOffsets(templateBytes: ByteArray, algAJson: String, algBJson: String, presetName: String): ByteArray {
        // Try conservative approach - only modify JSON contents, not binary structure
        return replaceJsonContentsOnly(templateBytes, algAJson, algBJson, presetName)
    }
    
    /**
     * Conservative approach: decode existing base64, modify JSON, re-encode
     */
    private fun replaceJsonContentsOnly(bytes: ByteArray, algAJson: String, algBJson: String, presetName: String): ByteArray {
        val result = bytes.copyOf()
        
        println("Using conservative JSON replacement approach")
        
        // If we can't modify safely, return the template with just name change
        try {
            // Find base64 blocks and decode them to modify only the JSON content
            val base64Pattern = "eyJ".toByteArray()
            var searchStart = 0
            var algorithmsModified = 0
            var anySuccessfulReplacement = false
            
            while (searchStart < result.size - 3 && algorithmsModified < 2) {
                val matchIndex = findBase64JsonPattern(result, base64Pattern, searchStart)
                if (matchIndex == -1) break
                
                val blockEnd = findBase64BlockEnd(result, matchIndex)
                if (blockEnd == -1) {
                    searchStart = matchIndex + 3
                    continue
                }
                
                try {
                    // Extract and decode the existing base64
                    val existingBase64 = String(result.sliceArray(matchIndex until blockEnd))
                    val existingJson = String(Base64.getDecoder().decode(existingBase64))
                    
                    println("Found existing algorithm JSON: ${existingJson.take(100)}...")
                    
                    // Parse existing JSON and modify only specific fields
                    val gson = Gson()
                    val existingData = gson.fromJson(existingJson, Map::class.java) as MutableMap<String, Any>
                    
                    // Choose which algorithm data to apply
                    val newAlgorithmJson = if (algorithmsModified == 0) algAJson else algBJson
                    val newAlgorithmData = gson.fromJson(String(Base64.getDecoder().decode(newAlgorithmJson)), Map::class.java) as Map<String, Any>
                    
                    // Update only key fields, preserve the structure
                    newAlgorithmData["algorithm_name"]?.let { existingData["algorithm_name"] = it }
                    newAlgorithmData["product_id"]?.let { existingData["product_id"] = it }
                    newAlgorithmData["preset_name"]?.let { existingData["preset_name"] = it }
                    
                    // Update parameters while preserving structure - be more conservative
                    newAlgorithmData.forEach { (key, value) ->
                        if (key !in listOf("algorithm_name", "product_id", "preset_name", "version") && 
                            existingData.containsKey(key)) {
                            // Only update parameters that already exist in the template
                            existingData[key] = value
                        }
                    }
                    
                    // Re-encode to base64
                    val modifiedJson = gson.toJson(existingData) + "\n"
                    val modifiedBase64 = Base64.getEncoder().encodeToString(modifiedJson.toByteArray())
                    
                    println("Modified algorithm ${algorithmsModified + 1}: ${existingData["algorithm_name"]}")
                    
                    // Replace only if the size matches (to avoid corrupting binary structure)
                    if (modifiedBase64.length == existingBase64.length) {
                        System.arraycopy(modifiedBase64.toByteArray(), 0, result, matchIndex, modifiedBase64.length)
                        println("Successfully replaced algorithm JSON (same size)")
                        anySuccessfulReplacement = true
                    } else {
                        println("Size mismatch (${modifiedBase64.length} vs ${existingBase64.length}), skipping this algorithm")
                    }
                    
                    algorithmsModified++
                    searchStart = blockEnd
                    
                } catch (e: Exception) {
                    println("Error modifying algorithm JSON: ${e.message}")
                    searchStart = matchIndex + 3
                }
            }
            
            if (!anySuccessfulReplacement) {
                println("Warning: No algorithms were successfully modified, returning template with name change only")
            }
            
        } catch (e: Exception) {
            println("Error during JSON replacement: ${e.message}")
            println("Falling back to template with name change only")
        }
        
        // Always try to replace preset name, even if algorithm modification failed
        return replacePresetName(result, presetName)
    }
    
    private fun replaceBase64AtOffset(bytes: ByteArray, offset: Int, newBase64: String) {
        val newBytes = newBase64.toByteArray()
        
        // Find the end of the existing base64 block starting at offset
        var endOffset = offset
        while (endOffset < bytes.size) {
            val char = bytes[endOffset].toInt().toChar()
            if (char !in 'A'..'Z' && char !in 'a'..'z' && char !in '0'..'9' && char != '+' && char != '/' && char != '=') {
                break
            }
            endOffset++
        }
        
        val oldLength = endOffset - offset
        val newLength = newBytes.size
        
        // If lengths match, simple replacement
        if (oldLength == newLength) {
            System.arraycopy(newBytes, 0, bytes, offset, newLength)
        } else {
            // Different lengths would require shifting entire file - not safe for now
            // Fall back to just keeping original algorithm but with updated name
            throw RuntimeException("Base64 length mismatch - keeping original algorithm")
        }
    }
    
    
    private fun replaceAlgorithmDataBinary(bytes: ByteArray, algAJson: String, algBJson: String, presetName: String): ByteArray {
        // Find and replace base64 algorithm data using improved pattern matching
        var result = bytes.copyOf()
        
        val algABytes = algAJson.toByteArray()
        val algBBytes = algBJson.toByteArray()
        
        println("Replacing algorithm data in binary:")
        println("  Algorithm A JSON size: ${algABytes.size}")
        println("  Algorithm B JSON size: ${algBBytes.size}")
        println("  Template binary size: ${bytes.size}")
        
        // Look for base64 patterns that start JSON objects (typically "eyJ")
        val base64JsonPattern = "eyJ".toByteArray()
        var replacements = 0
        
        var searchStart = 0
        while (searchStart < result.size - 3 && replacements < 2) {
            val matchIndex = findBase64JsonPattern(result, base64JsonPattern, searchStart)
            if (matchIndex == -1) {
                println("  No more base64 patterns found after position $searchStart")
                break
            }
            
            // Find the complete base64 block
            val blockEnd = findBase64BlockEnd(result, matchIndex)
            if (blockEnd == -1) {
                println("  Invalid base64 block at position $matchIndex")
                searchStart = matchIndex + 3
                continue
            }
            
            val originalBlockSize = blockEnd - matchIndex
            println("  Found base64 block at position $matchIndex, size: $originalBlockSize")
            
            // Choose which algorithm to use for replacement
            val replacementBytes = if (replacements == 0) algABytes else algBBytes
            val algorithmLabel = if (replacements == 0) "A" else "B"
            
            println("  Replacing with Algorithm $algorithmLabel (${replacementBytes.size} bytes)")
            
            // Perform the replacement
            result = replaceAtOffset(result, matchIndex, blockEnd, replacementBytes)
            
            // Update search position for next algorithm
            searchStart = matchIndex + replacementBytes.size
            replacements++
            
            println("  Successfully replaced algorithm $algorithmLabel, new binary size: ${result.size}")
        }
        
        println("Total algorithm replacements: $replacements")
        
        // Replace preset name in the final result
        return replacePresetName(result, presetName)
    }
    
    private fun findBase64JsonPattern(bytes: ByteArray, pattern: ByteArray, startIndex: Int): Int {
        for (i in startIndex until bytes.size - pattern.size + 1) {
            var match = true
            for (j in pattern.indices) {
                if (bytes[i + j] != pattern[j]) {
                    match = false
                    break
                }
            }
            if (match) return i
        }
        return -1
    }
    
    private fun findBase64BlockEnd(bytes: ByteArray, startIndex: Int): Int {
        var i = startIndex
        while (i < bytes.size) {
            val char = bytes[i].toInt().toChar()
            // Valid base64 characters plus padding
            if (char !in 'A'..'Z' && char !in 'a'..'z' && char !in '0'..'9' && 
                char != '+' && char != '/' && char != '=') {
                return i
            }
            i++
        }
        return bytes.size
    }
    
    private fun replaceAtOffset(bytes: ByteArray, start: Int, end: Int, replacement: ByteArray): ByteArray {
        val newSize = bytes.size - (end - start) + replacement.size
        val result = ByteArray(newSize)
        
        // Copy data before replacement
        System.arraycopy(bytes, 0, result, 0, start)
        
        // Copy replacement data
        System.arraycopy(replacement, 0, result, start, replacement.size)
        
        // Copy data after replacement
        System.arraycopy(bytes, end, result, start + replacement.size, bytes.size - end)
        
        return result
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
        var result = bytes.copyOf()
        
        // Find and replace all instances of the old preset name with the new one
        // This handles the main preset name that appears in the binary structure
        
        // Look for string length patterns followed by names
        val searchPatterns = listOf(
            "dft",           // Default template name
            "Autowah",       // Example names from sample presets
            "Awaken",
            "Entanglements",
            "Test Preset",   // Our test preset name
            "Crystals",      // Common algorithm preset names
            "TremoloVerb",
            "VINTAGE WAH",
            "FauxVerb",
            "SPLITTER VERB",
            "ULTRASWELL"
        )
        
        // Replace any occurrence of these patterns with our preset name (truncated if needed)
        for (pattern in searchPatterns) {
            result = replaceStringInBinary(result, pattern, presetName)
        }
        
        return result
    }
    
    private fun replaceStringInBinary(bytes: ByteArray, oldString: String, newString: String): ByteArray {
        val oldBytes = oldString.toByteArray()
        val newBytes = newString.toByteArray()
        var result = bytes
        
        // Find all occurrences of the old string and replace with new string
        var searchStart = 0
        while (searchStart <= result.size - oldBytes.size) {
            val matchIndex = findStringInBytes(result, oldBytes, searchStart)
            if (matchIndex == -1) break
            
            // Check if this looks like a string field (preceded by length or in a string context)
            if (isStringContext(result, matchIndex)) {
                // Replace with new string, truncated to fit the same space
                val replacementBytes = if (newBytes.size <= oldBytes.size) {
                    newBytes + ByteArray(oldBytes.size - newBytes.size) { 0 }
                } else {
                    newBytes.sliceArray(0 until oldBytes.size)
                }
                
                System.arraycopy(replacementBytes, 0, result, matchIndex, oldBytes.size)
            }
            
            searchStart = matchIndex + oldBytes.size
        }
        
        return result
    }
    
    private fun findStringInBytes(bytes: ByteArray, pattern: ByteArray, startIndex: Int): Int {
        for (i in startIndex until bytes.size - pattern.size + 1) {
            var match = true
            for (j in pattern.indices) {
                if (bytes[i + j] != pattern[j]) {
                    match = false
                    break
                }
            }
            if (match) return i
        }
        return -1
    }
    
    private fun isStringContext(bytes: ByteArray, stringIndex: Int): Boolean {
        // Check if this looks like a string in the H90 format:
        // - Preceded by a reasonable length value (4 bytes before)
        // - Or surrounded by reasonable ASCII/null bytes
        
        if (stringIndex < 4) return true
        
        // Check for length prefix pattern (little-endian length)
        val possibleLength = (bytes[stringIndex - 4].toInt() and 0xFF) or
                            ((bytes[stringIndex - 3].toInt() and 0xFF) shl 8) or
                            ((bytes[stringIndex - 2].toInt() and 0xFF) shl 16) or
                            ((bytes[stringIndex - 1].toInt() and 0xFF) shl 24)
                            
        // If the length makes sense for this string, it's probably a string field
        if (possibleLength >= 3 && possibleLength <= 100) {
            return true
        }
        
        // Also check for null-terminated string pattern
        if (stringIndex > 0 && bytes[stringIndex - 1] == 0.toByte()) {
            return true
        }
        
        return false
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
        
        // Core algorithm identification - MUST match H9 Control exactly
        jsonMap["algorithm_name"] = algorithm.algorithmName
        jsonMap["product_id"] = algorithm.productId
        jsonMap["preset_name"] = algorithm.presetName
        jsonMap["version"] = "3"
        
        // Debug output to verify algorithm mapping
        println("Generating algorithm JSON:")
        println("  Algorithm Name: ${algorithm.algorithmName}")
        println("  Product ID: ${algorithm.productId}")
        println("  Preset Name: ${algorithm.presetName}")
        println("  Algorithm Number: ${algorithm.algorithmNumber}")
        
        // Global parameters using exact H9 parameter names
        jsonMap["tmpv"] = global.tempo
        jsonMap["tsyn"] = global.tempoSync
        jsonMap["killdry"] = if (global.killDry) 1.0 else 0.0
        jsonMap["preset_mix"] = global.presetMix
        jsonMap["expression_pedal"] = global.expressionPedal
        
        // Standard I/O sensitivity (always present in real presets)
        jsonMap["in1_sens"] = 1.0
        jsonMap["in2_sens"] = 1.0
        jsonMap["out1_sens"] = 1.0  
        jsonMap["out2_sens"] = 1.0
        
        // Bypass configuration using H9 Control naming
        jsonMap["bypa_normal"] = if (algorithm.bypass) 0.0 else algorithm.mix
        jsonMap["bypt_normal"] = if (algorithm.bypass) 1.0 else 0.0
        
        // Slow mode flag (present in all algorithms)
        jsonMap["slow_mode"] = false
        
        // Additional common parameters found in real presets
        jsonMap["pedal"] = 0.0  // Physical pedal input
        
        // Algorithm-specific parameters - preserve exact types and precision
        algorithm.parameters.forEach { (key, value) ->
            jsonMap[key] = when (value) {
                is Float -> value.toDouble()  // Ensure double precision
                is Int -> value.toDouble()    // Convert ints to doubles for consistency
                is Boolean -> value
                is String -> value
                else -> value.toString()
            }
        }
        
        // Convert to JSON with exact formatting (compact, no extra whitespace)
        val gson = Gson()
        val jsonString = gson.toJson(jsonMap)
        
        // Encode as base64 with trailing newline (matches H9 Control format)
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
    
    /**
     * Test function to verify H90 preset generation with real H90 Control parameter sets
     */
    fun createTestPreset(): ByteArray {
        // Create a test preset using real H9 parameter names from analyzed presets
        val qWahParams = mapOf(
            "base" to 50.0,
            "dpth" to 80.0,
            "itsy" to 60.0,
            "sped" to 40.0,
            "shpe" to 7.0,
            "type" to 0.0,
            "mrat" to 0.5,
            "msrc" to 1.0,
            "brake" to 0.0,
            "dmod" to 0.0,
            "smod" to 0.0
        )
        
        val tremoloParams = mapOf(
            "mmix" to 50.0,
            "dcay" to 5.0,
            "size" to 60.0,
            "dpth" to 200.0,
            "sped" to 10.0,
            "shap" to 0.9,
            "pdly" to 240.0,
            "hifq" to 6000.0,
            "hilv" to 100.0,
            "lolv" to 10.0
        )
        
        val algorithmA = H90Algorithm.fromAlgorithmName(
            algorithmName = "Q-Wah",
            presetName = "Test Wah",
            parameters = qWahParams,
            mix = 0.7,
            bypass = false
        ) ?: throw IllegalArgumentException("Q-Wah algorithm not found")
        
        val algorithmB = H90Algorithm.fromAlgorithmName(
            algorithmName = "TremoloVerb", 
            presetName = "Test Tremolo",
            parameters = tremoloParams,
            mix = 0.8,
            bypass = false
        ) ?: throw IllegalArgumentException("TremoloVerb algorithm not found")
        
        val global = H90GlobalParameters(
            tempo = 120.0,
            tempoSync = false,
            killDry = false,
            presetMix = 0.75,
            expressionPedal = 0.0
        )
        
        val preset = H90Preset(
            name = "Test Preset",
            algorithmA = algorithmA,
            algorithmB = algorithmB,
            globalParameters = global
        )
        
        return generatePreset(preset)
    }
}