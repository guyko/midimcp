package com.guyko.midi

import com.guyko.models.MidiCommand
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before

class MidiExecutorTest {
    
    private lateinit var mockExecutor: MockMidiExecutor
    
    @Before
    fun setUp() {
        mockExecutor = MockMidiExecutor()
    }
    
    @Test
    fun testExecuteSingleCommand() {
        val command = MidiCommand(
            channel = 1,
            ccNumber = 5,
            value = 100,
            parameterName = "Filter",
            description = "Brighten the delay"
        )
        
        val result = mockExecutor.executeCommand(command)
        
        assertTrue("Execution should succeed", result.success)
        assertEquals("Should have executed command", command, result.executedCommand)
        assertTrue("Message should contain MIDI hex", result.message.contains("B0 05 64"))
        
        val executedCommands = mockExecutor.getExecutedCommands()
        assertEquals("Should have 1 executed command", 1, executedCommands.size)
        assertEquals("Should be the same command", command, executedCommands[0])
    }
    
    @Test
    fun testExecuteMultipleCommands() {
        val commands = listOf(
            MidiCommand(1, 3, 35, "Time", "Slapback timing"),
            MidiCommand(1, 4, 20, "Feedback", "Low feedback"),
            MidiCommand(1, 1, 30, "Mix", "Slapback mix")
        )
        
        val results = mockExecutor.executeCommands(commands)
        
        assertEquals("Should have 3 results", 3, results.size)
        assertTrue("All should succeed", results.all { it.success })
        
        val executedCommands = mockExecutor.getExecutedCommands()
        assertEquals("Should have executed 3 commands", 3, executedCommands.size)
        assertEquals("Should match input commands", commands, executedCommands)
    }
    
    @Test
    fun testExecutorFailure() {
        mockExecutor.setShouldFail(true)
        
        val command = MidiCommand(1, 5, 100, "Filter")
        val result = mockExecutor.executeCommand(command)
        
        assertFalse("Execution should fail", result.success)
        assertEquals("Should still have executed command reference", command, result.executedCommand)
        assertTrue("Message should indicate failure", result.message.contains("failed"))
        
        // Even failed commands are not tracked in mock
        val executedCommands = mockExecutor.getExecutedCommands()
        assertEquals("Should have no executed commands on failure", 0, executedCommands.size)
    }
    
    @Test
    fun testExecutorStatus() {
        assertTrue("Should be available", mockExecutor.isAvailable())
        
        val status = mockExecutor.getStatus()
        assertTrue("Status should mention mock", status.contains("Mock"))
        assertTrue("Status should show count", status.contains("0 CC commands"))
        
        // Execute a command and check status again
        mockExecutor.executeCommand(MidiCommand(1, 5, 100))
        val newStatus = mockExecutor.getStatus()
        assertTrue("Status should show updated count", newStatus.contains("1 CC commands"))
    }
    
    @Test
    fun testClearExecutedCommands() {
        mockExecutor.executeCommand(MidiCommand(1, 5, 100))
        mockExecutor.executeCommand(MidiCommand(1, 6, 50))
        
        assertEquals("Should have 2 commands", 2, mockExecutor.getExecutedCommands().size)
        
        mockExecutor.clearExecutedCommands()
        assertEquals("Should have no commands after clear", 0, mockExecutor.getExecutedCommands().size)
    }
    
    @Test
    fun testHardwareExecutorBasics() {
        val hardwareExecutor = HardwareMidiExecutor()
        
        assertTrue("Hardware executor should be available", hardwareExecutor.isAvailable())
        
        val status = hardwareExecutor.getStatus()
        assertTrue("Status should mention hardware", status.contains("Hardware"))
        
        val command = MidiCommand(1, 5, 100, "Filter")
        val result = hardwareExecutor.executeCommand(command)
        
        // In mock mode, hardware executor should still succeed
        assertTrue("Hardware execution should succeed", result.success)
        assertEquals("Should have executed command reference", command, result.executedCommand)
    }
}