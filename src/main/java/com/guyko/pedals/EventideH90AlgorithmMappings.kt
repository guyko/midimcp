package com.guyko.pedals

/**
 * Complete H90 algorithm mappings with parameter details for AI translation
 */
object EventideH90AlgorithmMappings {
    
    data class AlgorithmInfo(
        val name: String,
        val category: String,
        val description: String,
        val keyParameters: Map<String, ParameterInfo>
    )
    
    data class ParameterInfo(
        val displayName: String,
        val description: String,
        val range: String,
        val musicalFunction: String
    )
    
    val ALGORITHMS = mapOf(
        // DELAY ALGORITHMS
        0 to AlgorithmInfo(
            name = "Delay",
            category = "Delay",
            description = "Classic digital delay with up to 4 seconds - clean, pristine digital echoes",
            keyParameters = mapOf(
                "mix" to ParameterInfo("Mix", "Wet/dry balance", "0-100%", "Overall effect intensity"),
                "delay_time" to ParameterInfo("Delay Time", "Echo timing", "0-4000ms", "Delay timing"),
                "feedback" to ParameterInfo("Feedback", "Regeneration", "0-100%", "Echo repeats"),
                "filter" to ParameterInfo("Filter", "High frequency rolloff", "20Hz-20kHz", "Echo tone shaping")
            )
        ),
        
        1 to AlgorithmInfo(
            name = "Band Delay",
            category = "Delay",
            description = "Delays with user selectable modulated filters - perfect for rhythmic textures and filtered echoes",
            keyParameters = mapOf(
                "mix" to ParameterInfo("Mix", "Wet/dry balance", "0-100%", "Overall effect intensity"),
                "dlya" to ParameterInfo("Delay A", "Left delay time", "0-3000ms", "Primary delay timing"),
                "dlyb" to ParameterInfo("Delay B", "Right delay time", "0-3000ms", "Secondary delay timing"),
                "fbka" to ParameterInfo("Feedback A", "Left feedback amount", "0-100%", "Delay repeat intensity"),
                "dmix" to ParameterInfo("Delay Mix", "Balance between delays", "A-50%-B", "Stereo delay balance"),
                "filter_freq" to ParameterInfo("Filter Freq", "Filter cutoff", "20Hz-20kHz", "Tonal shaping of echoes")
            )
        ),
        
        2 to AlgorithmInfo(
            name = "Bouquet Delay",
            category = "Delay", 
            description = "BBD analog delay emulation with two flavors - warm vintage bucket brigade character",
            keyParameters = mapOf(
                "mix" to ParameterInfo("Mix", "Wet/dry balance", "0-100%", "Overall effect intensity"),
                "delay_time" to ParameterInfo("Delay Time", "Echo timing", "0-600ms", "Delay timing"),
                "feedback" to ParameterInfo("Feedback", "Regeneration", "0-100%", "Echo repeats"),
                "tone" to ParameterInfo("Tone", "Frequency response", "Dark-Bright", "Analog character"),
                "modulation" to ParameterInfo("Modulation", "BBD modulation", "0-100%", "Vintage wobble")
            )
        ),
        
        3 to AlgorithmInfo(
            name = "Digital Delay",
            category = "Delay", 
            description = "Twin 3 second delays with independent time and feedback controls - pristine digital echoes",
            keyParameters = mapOf(
                "mix" to ParameterInfo("Mix", "Wet/dry balance", "0-100%", "Overall effect intensity"),
                "dlya" to ParameterInfo("Delay A", "Left delay time", "0-3000ms", "Primary delay timing"),
                "dlyb" to ParameterInfo("Delay B", "Right delay time", "0-3000ms", "Secondary delay timing"),
                "fbka" to ParameterInfo("Feedback A", "Left feedback", "0-100%", "Left delay repeats"),
                "fbkb" to ParameterInfo("Feedback B", "Right feedback", "0-100%", "Right delay repeats"),
                "dmix" to ParameterInfo("Delay Mix", "A/B balance", "A-50%-B", "Stereo delay balance"),
                "fltr" to ParameterInfo("Filter", "High cut filter", "20Hz-20kHz", "Delay tone shaping")
            )
        ),
        
        4 to AlgorithmInfo(
            name = "Ducked Delay",
            category = "Delay",
            description = "Delay levels dynamically lower while playing, restore when you stop - perfect for clean lead lines with ambient trails",
            keyParameters = mapOf(
                "mix" to ParameterInfo("Mix", "Wet/dry balance", "0-100%", "Overall effect intensity"),
                "dlya" to ParameterInfo("Delay A", "Left delay time", "0-3000ms", "Primary delay timing"),
                "dlyb" to ParameterInfo("Delay B", "Right delay time", "0-3000ms", "Secondary delay timing"),
                "threshold" to ParameterInfo("Threshold", "Ducking sensitivity", "0-100%", "Input level for ducking"),
                "release" to ParameterInfo("Release", "Delay return time", "0-5000ms", "How fast delays return")
            )
        ),
        
        // HARMONIZER ALGORITHMS
        23 to AlgorithmInfo(
            name = "MicroPitch",
            category = "Harmonizer",
            description = "Fine-resolution pitch shifter for doubling and chorusing",
            keyParameters = mapOf(
                "mix" to ParameterInfo("Mix", "Wet/dry balance", "0-100%", "Overall effect intensity"),
                "pcha" to ParameterInfo("Pitch A", "Voice A detune", "0 to +50 cents", "Upward pitch shift"),
                "pchb" to ParameterInfo("Pitch B", "Voice B detune", "0 to -50 cents", "Downward pitch shift"),
                "dlya" to ParameterInfo("Delay A", "Voice A delay", "0-500ms", "Voice A timing offset"),
                "dlyb" to ParameterInfo("Delay B", "Voice B delay", "0-500ms", "Voice B timing offset"),
                "mod_depth" to ParameterInfo("Mod Depth", "Pitch modulation depth", "0-100%", "Chorus-like movement"),
                "mod_rate" to ParameterInfo("Mod Rate", "Modulation speed", "0.1-10Hz", "Modulation frequency")
            )
        ),
        
        // MODULATION ALGORITHMS  
        45 to AlgorithmInfo(
            name = "TremoloPan",
            category = "Modulation",
            description = "Stereo tremolo with autopan capabilities",
            keyParameters = mapOf(
                "intensity" to ParameterInfo("Intensity", "Tremolo depth", "0-100%", "Volume modulation amount"),
                "speed" to ParameterInfo("Speed", "Tremolo rate", "0.1-20Hz", "Modulation frequency"),
                "pan_width" to ParameterInfo("Pan Width", "Stereo width", "0-100%", "Spatial movement"),
                "shape" to ParameterInfo("Shape", "Waveform", "Sine/Triangle/Square/Random", "Modulation character"),
                "phase" to ParameterInfo("Phase", "L/R phase offset", "0-180°", "Stereo relationship")
            )
        )
    )
    
    /**
     * Get algorithm info by algorithm number
     */
    fun getAlgorithmInfo(algorithmNumber: Int): AlgorithmInfo? {
        return ALGORITHMS[algorithmNumber]
    }
    
    /**
     * Get algorithms by category
     */
    fun getAlgorithmsByCategory(category: String): Map<Int, AlgorithmInfo> {
        return ALGORITHMS.filter { it.value.category == category }
    }
    
    /**
     * Search algorithms by name
     */
    fun findAlgorithmByName(name: String): Pair<Int, AlgorithmInfo>? {
        return ALGORITHMS.entries.find { it.value.name.equals(name, ignoreCase = true) }?.toPair()
    }
    
    /**
     * Get all categories
     */
    fun getCategories(): Set<String> {
        return ALGORITHMS.values.map { it.category }.toSet()
    }
    
    /**
     * Suggest algorithm based on musical request
     */
    fun suggestAlgorithm(request: String): List<Pair<Int, AlgorithmInfo>> {
        val requestLower = request.lowercase()
        val suggestions = mutableListOf<Pair<Int, AlgorithmInfo>>()
        
        // Direct keyword matching
        when {
            "delay" in requestLower || "echo" in requestLower -> {
                suggestions.addAll(getAlgorithmsByCategory("Delay").toList())
            }
            "reverb" in requestLower || "space" in requestLower || "ambient" in requestLower -> {
                suggestions.addAll(getAlgorithmsByCategory("Reverb").toList())
            }
            "harmony" in requestLower || "pitch" in requestLower || "octave" in requestLower -> {
                suggestions.addAll(getAlgorithmsByCategory("Harmonizer").toList())
            }
            "chorus" in requestLower || "flange" in requestLower || "phase" in requestLower || "modulation" in requestLower -> {
                suggestions.addAll(getAlgorithmsByCategory("Modulation").toList())
            }
            "distortion" in requestLower || "overdrive" in requestLower || "fuzz" in requestLower -> {
                suggestions.addAll(getAlgorithmsByCategory("Distortion").toList())
            }
            "synth" in requestLower || "synthesizer" in requestLower -> {
                suggestions.addAll(getAlgorithmsByCategory("Synth").toList())
            }
        }
        
        return suggestions.distinctBy { it.first }
    }
}