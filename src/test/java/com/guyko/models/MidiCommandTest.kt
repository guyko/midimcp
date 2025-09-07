package com.guyko.models

import org.junit.Test
import org.junit.Assert.*

class MidiCommandTest {
    
    @Test
    fun testMidiByteGeneration() {
        val command = MidiCommand(
            channel = 1,
            ccNumber = 7,
            value = 127
        )
        
        val bytes = command.toMidiBytes()
        
        // Channel 1 CC message: 0xB0, CC 7, Value 127
        assertEquals(3, bytes.size)
        assertEquals(0xB0.toByte(), bytes[0]) // Control Change on channel 1
        assertEquals(7.toByte(), bytes[1])    // CC number 7
        assertEquals(127.toByte(), bytes[2])  // Value 127
    }
    
    @Test
    fun testMidiByteGenerationChannel16() {
        val command = MidiCommand(
            channel = 16,
            ccNumber = 1,
            value = 0
        )
        
        val bytes = command.toMidiBytes()
        
        // Channel 16 CC message: 0xBF, CC 1, Value 0
        assertEquals(3, bytes.size)
        assertEquals(0xBF.toByte(), bytes[0]) // Control Change on channel 16
        assertEquals(1.toByte(), bytes[1])    // CC number 1
        assertEquals(0.toByte(), bytes[2])    // Value 0
    }
    
    @Test(expected = IllegalArgumentException::class)
    fun testInvalidChannel() {
        MidiCommand(
            channel = 17, // Invalid: must be 1-16
            ccNumber = 7,
            value = 64
        )
    }
    
    @Test(expected = IllegalArgumentException::class)
    fun testInvalidCCNumber() {
        MidiCommand(
            channel = 1,
            ccNumber = 128, // Invalid: must be 0-127
            value = 64
        )
    }
    
    @Test(expected = IllegalArgumentException::class)
    fun testInvalidValue() {
        MidiCommand(
            channel = 1,
            ccNumber = 7,
            value = 128 // Invalid: must be 0-127
        )
    }
    
    @Test
    fun testMidiBytesFormatting() {
        val command = MidiCommand(
            channel = 5,
            ccNumber = 74,
            value = 42
        )
        
        val bytes = command.toMidiBytes()
        val hexString = bytes.joinToString(" ") { "%02X".format(it) }
        
        assertEquals("B4 4A 2A", hexString)
    }
}