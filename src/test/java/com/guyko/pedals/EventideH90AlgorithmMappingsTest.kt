package com.guyko.pedals

import org.junit.Test
import org.junit.Assert.*

class EventideH90AlgorithmMappingsTest {

    @Test
    fun testH90AlgorithmsLoadFromJson() {
        // Test that algorithms can be loaded from JSON
        val algorithms = EventideH90AlgorithmMappings.getAllAlgorithms()
        
        // Ensure we have a significant number of algorithms (at least 50)
        assertTrue("H90 should have at least 50 algorithms loaded", algorithms.size >= 50)
        
        // Test that we have expected categories
        val categories = EventideH90AlgorithmMappings.getCategories()
        val expectedCategories = setOf("Delay", "Harmonizer", "Modulation", "Reverb", "Synth", "Distortion")
        
        for (category in expectedCategories) {
            assertTrue("Missing expected category: $category", categories.contains(category))
        }
        
        // Test that each algorithm has required fields
        for ((id, algorithm) in algorithms) {
            assertNotNull("Algorithm $id should have a name", algorithm.name)
            assertNotNull("Algorithm $id should have a category", algorithm.category)
            assertNotNull("Algorithm $id should have a description", algorithm.description)
            assertTrue("Algorithm $id should have parameters", algorithm.keyParameters.isNotEmpty())
            
            // Test that each parameter has required fields
            for ((paramName, param) in algorithm.keyParameters) {
                assertNotNull("Parameter $paramName of algorithm $id should have displayName", param.displayName)
                assertNotNull("Parameter $paramName of algorithm $id should have description", param.description)
                assertNotNull("Parameter $paramName of algorithm $id should have range", param.range)
                assertNotNull("Parameter $paramName of algorithm $id should have musicalFunction", param.musicalFunction)
            }
        }
    }
    
    @Test
    fun testSpecificH90Algorithms() {
        val algorithms = EventideH90AlgorithmMappings.getAllAlgorithms()
        
        // Test for key H90 algorithms
        val expectedAlgorithms = mapOf(
            0 to "Delay",
            1 to "Band Delay", 
            2 to "Bouquet Delay",
            23 to "Crystals",
            29 to "MicroPitch",
            43 to "Chorus",
            46 to "Flanger",
            49 to "Blackhole",
            63 to "HotSawz"
        )
        
        for ((id, expectedName) in expectedAlgorithms) {
            val algorithm = algorithms[id]
            assertNotNull("Algorithm $id should exist", algorithm)
            assertEquals("Algorithm $id should be named '$expectedName'", expectedName, algorithm?.name)
        }
    }
    
    @Test
    fun testH90AlgorithmSearch() {
        // Test algorithm search functionality
        val crystalsAlgorithm = EventideH90AlgorithmMappings.findAlgorithmByName("Crystals")
        assertNotNull("Should find Crystals algorithm", crystalsAlgorithm)
        assertEquals("Crystals algorithm should be at position 23", 23, crystalsAlgorithm?.first)
        
        val delayAlgorithms = EventideH90AlgorithmMappings.getAlgorithmsByCategory("Delay")
        assertTrue("Should have multiple delay algorithms", delayAlgorithms.size >= 20)
        
        val harmonizerAlgorithms = EventideH90AlgorithmMappings.getAlgorithmsByCategory("Harmonizer")
        assertTrue("Should have multiple harmonizer algorithms", harmonizerAlgorithms.size >= 15)
    }
    
    @Test
    fun testH90AlgorithmParameterValidation() {
        val algorithms = EventideH90AlgorithmMappings.getAllAlgorithms()
        
        // Test that Mix parameter is common across many algorithms
        var mixParameterCount = 0
        for (algorithm in algorithms.values) {
            if (algorithm.keyParameters.containsKey("Mix")) {
                mixParameterCount++
            }
        }
        
        assertTrue("Mix parameter should be present in most algorithms", mixParameterCount >= 40)
        
        // Test that Delay algorithms have expected parameters
        val delayAlgorithms = EventideH90AlgorithmMappings.getAlgorithmsByCategory("Delay")
        for (algorithm in delayAlgorithms.values) {
            assertTrue("Delay algorithms should have Mix parameter", 
                algorithm.keyParameters.containsKey("Mix") || 
                algorithm.keyParameters.containsKey("mix"))
        }
    }
}