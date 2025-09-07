package com.guyko.models

import org.junit.Test
import org.junit.Assert.*
import org.junit.Before

class PedalModelTest {
    
    private lateinit var testPedal: PedalModel
    
    @Before
    fun setUp() {
        val parameters = listOf(
            CCParameter("Volume", 7, 0, 127, "Master volume", "%", "Global"),
            CCParameter("Delay Time", 12, 0, 127, "Delay time", "ms", "Delay"),
            CCParameter("Feedback", 13, 0, 127, "Delay feedback", "%", "Delay"),
            CCParameter("Filter", 14, 0, 127, "Delay filter", "Hz", "Filter")
        )
        
        testPedal = PedalModel(
            id = "test_pedal",
            manufacturer = "Test",
            modelName = "Delay Pedal",
            version = "1.0",
            midiChannel = 1,
            parameters = parameters,
            description = "Test delay pedal"
        )
    }
    
    @Test
    fun testGetParameterByCC() {
        val parameter = testPedal.getParameterByCC(7)
        assertNotNull(parameter)
        assertEquals("Volume", parameter?.name)
        assertEquals(7, parameter?.ccNumber)
    }
    
    @Test
    fun testGetParameterByCCNotFound() {
        val parameter = testPedal.getParameterByCC(99)
        assertNull(parameter)
    }
    
    @Test
    fun testGetParameterByName() {
        val parameter = testPedal.getParameterByName("Delay Time")
        assertNotNull(parameter)
        assertEquals(12, parameter?.ccNumber)
        assertEquals("ms", parameter?.unit)
    }
    
    @Test
    fun testGetParameterByNameCaseInsensitive() {
        val parameter = testPedal.getParameterByName("delay time")
        assertNotNull(parameter)
        assertEquals("Delay Time", parameter?.name)
    }
    
    @Test
    fun testGetParameterByNameNotFound() {
        val parameter = testPedal.getParameterByName("Nonexistent")
        assertNull(parameter)
    }
    
    @Test
    fun testGetParametersByCategory() {
        val delayParams = testPedal.getParametersByCategory("Delay")
        assertEquals(2, delayParams.size)
        assertTrue(delayParams.any { it.name == "Delay Time" })
        assertTrue(delayParams.any { it.name == "Feedback" })
    }
    
    @Test
    fun testGetParametersByCategoryCaseInsensitive() {
        val delayParams = testPedal.getParametersByCategory("delay")
        assertEquals(2, delayParams.size)
    }
    
    @Test
    fun testGetParametersByCategoryEmpty() {
        val params = testPedal.getParametersByCategory("Nonexistent")
        assertEquals(0, params.size)
    }
}