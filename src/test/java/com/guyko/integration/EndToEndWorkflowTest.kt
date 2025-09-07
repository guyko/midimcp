package com.guyko.integration

import com.guyko.models.PedalModel
import com.guyko.models.CCParameter
import com.guyko.models.MidiCommand
import com.guyko.persistence.PedalRepository
import com.guyko.mcp.MCPServer
import com.guyko.midi.MockMidiExecutor
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import org.junit.After
import java.io.File

class EndToEndWorkflowTest {
    
    private lateinit var repository: PedalRepository
    private lateinit var mockMidiExecutor: MockMidiExecutor
    private lateinit var mcpServer: MCPServer
    private lateinit var merisLVX: PedalModel
    private val testDataDir = "test-data/pedals"
    
    @Before
    fun setUp() {
        // Use a test-specific data directory
        repository = PedalRepository(testDataDir)
        mockMidiExecutor = MockMidiExecutor()
        mcpServer = MCPServer(repository, mockMidiExecutor)
        
        // Create the Meris LVX pedal for testing
        merisLVX = createTestMerisLVX()
        repository.save(merisLVX)
    }
    
    @After
    fun tearDown() {
        // Clean up test data
        File(testDataDir).deleteRecursively()
    }
    
    private fun createTestMerisLVX(): PedalModel {
        val parameters = listOf(
            CCParameter("Engine", 0, 0, 5, "Delay engine type", category = "Engine"),
            CCParameter("Mix", 1, 0, 127, "Wet/dry mix", "%", "Global"),
            CCParameter("Time", 3, 0, 127, "Delay time", "ms", "Delay"),
            CCParameter("Feedback", 4, 0, 127, "Delay feedback", "%", "Delay"),
            CCParameter("Filter", 5, 0, 127, "Delay filter", "Hz", "Filter"),
            CCParameter("Low Cut", 6, 0, 127, "High-pass filter", "Hz", "Filter"),
            CCParameter("Mod Rate", 7, 0, 127, "Modulation rate", "Hz", "Modulation"),
            CCParameter("Mod Depth", 8, 0, 127, "Modulation depth", "%", "Modulation"),
            CCParameter("Stereo Width", 9, 0, 127, "Stereo width", "%", "Stereo"),
            CCParameter("Ping Pong", 10, 0, 127, "Ping pong delay", "%", "Stereo"),
            CCParameter("Diffusion", 11, 0, 127, "Signal diffusion", "%", "Advanced"),
            CCParameter("Drive", 13, 0, 127, "Input drive", "%", "Drive")
        )
        
        return PedalModel(
            id = "meris_lvx",
            manufacturer = "Meris",
            modelName = "LVX",
            version = "1.0.2b",
            midiChannel = 1,
            parameters = parameters,
            description = "Meris LVX Test"
        )
    }
    
    @Test
    fun testAIAssistantRequestsBrighterSound() {
        // Simulate AI assistant interpreting "make my delay sound brighter" 
        // and deciding to adjust the filter parameter
        
        // 1. AI assistant determines that "brighter" means increasing filter frequency
        // and provides specific MIDI command to MCP server
        val aiGeneratedCommand = MidiCommand(
            channel = 1,
            ccNumber = 5, // Filter parameter
            value = 100,  // High value for brightness
            parameterName = "Filter",
            description = "AI: Brighten delay sound by increasing filter frequency"
        )
        
        // 2. MCP server executes the command
        val result = mockMidiExecutor.executeCommand(aiGeneratedCommand)
        
        // 3. Verify execution was successful
        assertTrue("MIDI command should execute successfully", result.success)
        assertEquals("Executed command should match", aiGeneratedCommand, result.executedCommand)
        assertTrue("Result message should contain MIDI hex", result.message.contains("B0 05 64"))
        
        // 4. Verify the command was actually sent to hardware (mock)
        val executedCommands = mockMidiExecutor.getExecutedCommands()
        assertEquals("Should have executed 1 command", 1, executedCommands.size)
        
        val executed = executedCommands[0]
        assertEquals("Channel should be 1", 1, executed.channel)
        assertEquals("CC should be 5 (Filter)", 5, executed.ccNumber)
        assertEquals("Value should be 100", 100, executed.value)
        
        // 5. Verify MIDI bytes are correct
        val midiBytes = executed.toMidiBytes()
        assertEquals("MIDI bytes should be B0 05 64", "B0 05 64", 
            midiBytes.joinToString(" ") { "%02X".format(it) })
    }
    
    @Test
    fun testAIAssistantRequestsSlapbackDelay() {
        // Simulate AI assistant interpreting "give me a classic slapback delay"
        // and providing multiple MIDI commands to achieve this sound
        
        // 1. AI assistant analyzes request and generates multiple commands for slapback
        val aiGeneratedCommands = listOf(
            MidiCommand(1, 3, 35, "Time", "AI: Short delay time for slapback"),
            MidiCommand(1, 4, 20, "Feedback", "AI: Low feedback for single repeat"),
            MidiCommand(1, 1, 30, "Mix", "AI: Balanced mix for slapback effect")
        )
        
        // 2. MCP server executes all commands in sequence
        val results = mockMidiExecutor.executeCommands(aiGeneratedCommands)
        
        // 3. Verify all executions were successful
        assertEquals("Should have 3 results", 3, results.size)
        assertTrue("All commands should succeed", results.all { it.success })
        
        // 4. Verify the commands were sent to hardware
        val executedCommands = mockMidiExecutor.getExecutedCommands()
        assertEquals("Should have executed 3 commands", 3, executedCommands.size)
        
        // 5. Verify specific parameter settings
        val timeCommand = executedCommands.find { it.ccNumber == 3 }
        assertNotNull("Time command should be executed", timeCommand)
        assertEquals("Time should be 35", 35, timeCommand?.value)
        
        val feedbackCommand = executedCommands.find { it.ccNumber == 4 }
        assertNotNull("Feedback command should be executed", feedbackCommand)
        assertEquals("Feedback should be 20", 20, feedbackCommand?.value)
        
        val mixCommand = executedCommands.find { it.ccNumber == 1 }
        assertNotNull("Mix command should be executed", mixCommand)
        assertEquals("Mix should be 30", 30, mixCommand?.value)
        
        // 6. Verify MIDI byte sequences
        val expectedMidiBytes = listOf("B0 03 23", "B0 04 14", "B0 01 1E")
        executedCommands.forEachIndexed { index, command ->
            val hexString = command.toMidiBytes().joinToString(" ") { "%02X".format(it) }
            assertEquals("MIDI bytes should match expected", expectedMidiBytes[index], hexString)
        }
    }
    
    @Test
    fun testAIAssistantRequestsAmbientSound() {
        // Simulate AI assistant interpreting "I want a spacey ambient sound"
        // and providing commands to create ambient atmosphere
        
        // 1. AI assistant generates commands for ambient sound
        val aiGeneratedCommands = listOf(
            MidiCommand(1, 1, 80, "Mix", "AI: High wet signal for ambient"),
            MidiCommand(1, 11, 70, "Diffusion", "AI: Increase diffusion for space"),
            MidiCommand(1, 8, 40, "Mod Depth", "AI: Add movement with modulation"),
            MidiCommand(1, 4, 60, "Feedback", "AI: Long tails for ambient")
        )
        
        // 2. Execute commands through MCP server
        val results = mockMidiExecutor.executeCommands(aiGeneratedCommands)
        
        // 3. Verify successful execution
        assertEquals("Should execute 4 commands", 4, results.size)
        assertTrue("All should succeed", results.all { it.success })
        
        // 4. Verify ambient-specific parameter values
        val executedCommands = mockMidiExecutor.getExecutedCommands()
        
        val mixCommand = executedCommands.find { it.ccNumber == 1 }
        assertNotNull("Mix command should be executed", mixCommand)
        assertEquals("Mix should be high (80) for wet ambient sound", 80, mixCommand?.value)
        
        val diffusionCommand = executedCommands.find { it.ccNumber == 11 }
        assertNotNull("Diffusion command should be executed", diffusionCommand)
        assertEquals("Diffusion should be 70 for spaciousness", 70, diffusionCommand?.value)
        
        val modDepthCommand = executedCommands.find { it.ccNumber == 8 }
        assertNotNull("Mod Depth command should be executed", modDepthCommand)
        assertEquals("Mod Depth should be 40 for subtle movement", 40, modDepthCommand?.value)
        
        // 5. Verify all MIDI bytes are correct
        val expectedMidiBytes = listOf("B0 01 50", "B0 0B 46", "B0 08 28", "B0 04 3C")
        executedCommands.forEachIndexed { index, command ->
            val hexString = command.toMidiBytes().joinToString(" ") { "%02X".format(it) }
            assertEquals("MIDI bytes should match", expectedMidiBytes[index], hexString)
        }
    }
    
    
    @Test
    fun testMidiExecutionFailure() {
        // 1. Set up executor to fail
        mockMidiExecutor.setShouldFail(true)
        
        // 2. AI assistant tries to send a command
        val aiCommand = MidiCommand(1, 5, 100, "Filter", "AI: Attempt to brighten")
        
        // 3. Execution should fail gracefully
        val result = mockMidiExecutor.executeCommand(aiCommand)
        
        assertFalse("Execution should fail", result.success)
        assertEquals("Should have command reference", aiCommand, result.executedCommand)
        assertTrue("Message should indicate failure", result.message.contains("failed"))
        
        // 4. No commands should be tracked as executed on failure
        assertEquals("No commands should be executed on failure", 0, mockMidiExecutor.getExecutedCommands().size)
    }
    
    
    @Test
    fun testBatchCommandExecution() {
        // Test executing multiple commands in one MCP call
        
        // 1. AI assistant wants to create a complex sound with multiple parameters
        val batchCommands = listOf(
            MidiCommand(1, 0, 1, "Engine", "AI: Switch to Tape engine"),
            MidiCommand(1, 5, 40, "Filter", "AI: Warm filter setting"),
            MidiCommand(1, 13, 30, "Drive", "AI: Add tape saturation"),
            MidiCommand(1, 3, 60, "Time", "AI: Medium delay time"),
            MidiCommand(1, 4, 45, "Feedback", "AI: Moderate feedback")
        )
        
        // 2. Execute all commands as a batch
        val results = mockMidiExecutor.executeCommands(batchCommands)
        
        // 3. Verify all succeeded
        assertEquals("Should have 5 results", 5, results.size)
        assertTrue("All commands should succeed", results.all { it.success })
        
        // 4. Verify execution order and values
        val executedCommands = mockMidiExecutor.getExecutedCommands()
        assertEquals("Should execute all 5 commands", 5, executedCommands.size)
        
        // Verify specific commands were executed in order
        assertEquals("First: Engine to Tape", 0, executedCommands[0].ccNumber)
        assertEquals("First: Value 1", 1, executedCommands[0].value)
        
        assertEquals("Last: Feedback", 4, executedCommands[4].ccNumber)
        assertEquals("Last: Value 45", 45, executedCommands[4].value)
    }
}