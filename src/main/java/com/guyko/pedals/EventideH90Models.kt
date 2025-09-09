package com.guyko.pedals

import java.util.*
import java.util.Base64

/**
 * Represents an Eventide H90 preset with dual algorithms
 */
data class H90Preset(
    val name: String,
    val algorithmA: H90Algorithm,
    val algorithmB: H90Algorithm,
    val globalParameters: H90GlobalParameters = H90GlobalParameters(),
    val routing: H90Routing = H90Routing()
) {
    companion object {
        /**
         * Create a preset with algorithm numbers and basic parameters
         */
        fun create(
            name: String,
            algorithmANumber: Int,
            algorithmBNumber: Int,
            algorithmAParams: Map<String, Any> = emptyMap(),
            algorithmBParams: Map<String, Any> = emptyMap(),
            routing: H90Routing = H90Routing(),
            globalParams: H90GlobalParameters = H90GlobalParameters()
        ): H90Preset {
            val algorithmA = H90Algorithm.fromAlgorithmNumber(
                algorithmNumber = algorithmANumber,
                presetName = "$name A",
                parameters = algorithmAParams
            )
            
            val algorithmB = H90Algorithm.fromAlgorithmNumber(
                algorithmNumber = algorithmBNumber,
                presetName = "$name B", 
                parameters = algorithmBParams
            )
            
            return H90Preset(
                name = name,
                algorithmA = algorithmA,
                algorithmB = algorithmB,
                globalParameters = globalParams,
                routing = routing
            )
        }
        
        /**
         * Create a preset with algorithm names
         */
        fun createWithNames(
            name: String,
            algorithmAName: String,
            algorithmBName: String,
            algorithmAParams: Map<String, Any> = emptyMap(),
            algorithmBParams: Map<String, Any> = emptyMap(),
            routing: H90Routing = H90Routing(),
            globalParams: H90GlobalParameters = H90GlobalParameters()
        ): H90Preset? {
            val algorithmA = H90Algorithm.fromAlgorithmName(
                algorithmName = algorithmAName,
                presetName = "$name A",
                parameters = algorithmAParams
            ) ?: return null
            
            val algorithmB = H90Algorithm.fromAlgorithmName(
                algorithmName = algorithmBName,
                presetName = "$name B",
                parameters = algorithmBParams
            ) ?: return null
            
            return H90Preset(
                name = name,
                algorithmA = algorithmA,
                algorithmB = algorithmB,
                globalParameters = globalParams,
                routing = routing
            )
        }
    }
    
    /**
     * Get all parameters for algorithm A with their mapping info
     */
    fun getAlgorithmAParameterMappings(): Map<String, EventideH90AlgorithmMappings.ParameterInfo>? {
        return EventideH90AlgorithmMappings.getAlgorithmInfo(algorithmA.algorithmNumber)?.keyParameters
    }
    
    /**
     * Get all parameters for algorithm B with their mapping info
     */
    fun getAlgorithmBParameterMappings(): Map<String, EventideH90AlgorithmMappings.ParameterInfo>? {
        return EventideH90AlgorithmMappings.getAlgorithmInfo(algorithmB.algorithmNumber)?.keyParameters
    }
}

/**
 * Represents a single algorithm configuration in the H90
 */
data class H90Algorithm(
    val algorithmNumber: Int,
    val algorithmName: String,
    val productId: String,
    val presetName: String,
    val parameters: Map<String, Any>,
    val bypass: Boolean = false,
    val mix: Double = 1.0
) {
    companion object {
        /**
         * Create an H90Algorithm from algorithm number with mapping support
         */
        fun fromAlgorithmNumber(
            algorithmNumber: Int,
            presetName: String = "Custom",
            parameters: Map<String, Any> = emptyMap(),
            bypass: Boolean = false,
            mix: Double = 1.0
        ): H90Algorithm {
            val algorithmInfo = EventideH90AlgorithmMappings.getAlgorithmInfo(algorithmNumber)
            val algorithmName = algorithmInfo?.name ?: "Unknown"
            val productId = EventideH90PresetGenerator.getProductIdForAlgorithm(algorithmName)
            
            return H90Algorithm(
                algorithmNumber = algorithmNumber,
                algorithmName = algorithmName,
                productId = productId,
                presetName = presetName,
                parameters = parameters,
                bypass = bypass,
                mix = mix
            )
        }
        
        /**
         * Create an H90Algorithm from algorithm name with mapping support
         */
        fun fromAlgorithmName(
            algorithmName: String,
            presetName: String = "Custom",
            parameters: Map<String, Any> = emptyMap(),
            bypass: Boolean = false,
            mix: Double = 1.0
        ): H90Algorithm? {
            val algorithmEntry = EventideH90AlgorithmMappings.findAlgorithmByName(algorithmName)
            return algorithmEntry?.let { (number, info) ->
                val productId = EventideH90PresetGenerator.getProductIdForAlgorithm(algorithmName)
                
                H90Algorithm(
                    algorithmNumber = number,
                    algorithmName = info.name,
                    productId = productId,
                    presetName = presetName,
                    parameters = parameters,
                    bypass = bypass,
                    mix = mix
                )
            }
        }
    }
}

/**
 * Global parameters for the H90 program
 */
data class H90GlobalParameters(
    val tempo: Double = 120.0,
    val tempoSync: Boolean = false,
    val killDry: Boolean = false,
    val presetMix: Double = 1.0,
    val expressionPedal: Double = 0.0
)

/**
 * Routing configuration for the H90
 */
data class H90Routing(
    val mode: RoutingMode = RoutingMode.PARALLEL,
    val spilloverTime: Double = 2.0
)

enum class RoutingMode {
    SERIES_A_TO_B,
    SERIES_B_TO_A, 
    PARALLEL,
    SERIES_WITH_CROSSFADE
}