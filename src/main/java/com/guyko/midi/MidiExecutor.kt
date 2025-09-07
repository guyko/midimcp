package com.guyko.midi

import com.guyko.models.MidiCommand
import com.guyko.models.MidiProgramChange

data class MidiExecutionResult(
    val success: Boolean,
    val message: String,
    val executedCommand: MidiCommand? = null,
    val timestamp: Long = System.currentTimeMillis()
)

interface MidiExecutor {
    /**
     * Execute a single MIDI command
     */
    fun executeCommand(command: MidiCommand): MidiExecutionResult
    
    /**
     * Execute multiple MIDI commands in sequence
     */
    fun executeCommands(commands: List<MidiCommand>): List<MidiExecutionResult>
    
    /**
     * Execute a MIDI program change
     */
    fun executeProgramChange(programChange: MidiProgramChange): MidiExecutionResult
    
    /**
     * Check if MIDI output is available
     */
    fun isAvailable(): Boolean
    
    /**
     * Get status information about the MIDI connection
     */
    fun getStatus(): String
}

/**
 * Real MIDI executor that sends commands to actual hardware
 * This would integrate with Java MIDI API in a real implementation
 */
class HardwareMidiExecutor : MidiExecutor {
    
    override fun executeCommand(command: MidiCommand): MidiExecutionResult {
        return try {
            // In a real implementation, this would use javax.sound.midi
            // to send the command to the actual MIDI device
            val midiBytes = command.toMidiBytes()
            val hexString = midiBytes.joinToString(" ") { "%02X".format(it) }
            
            // Simulate sending to hardware
            println("Sending MIDI: $hexString")
            
            MidiExecutionResult(
                success = true,
                message = "MIDI command sent successfully: $hexString",
                executedCommand = command
            )
        } catch (e: Exception) {
            MidiExecutionResult(
                success = false,
                message = "Failed to execute MIDI command: ${e.message}",
                executedCommand = command
            )
        }
    }
    
    override fun executeCommands(commands: List<MidiCommand>): List<MidiExecutionResult> {
        return commands.map { executeCommand(it) }
    }
    
    override fun executeProgramChange(programChange: MidiProgramChange): MidiExecutionResult {
        return try {
            val midiBytes = programChange.toMidiBytes()
            val hexString = midiBytes.joinToString(" ") { "%02X".format(it) }
            
            // Simulate sending to hardware
            println("Sending MIDI PC: $hexString")
            
            MidiExecutionResult(
                success = true,
                message = "MIDI program change sent successfully: $hexString",
                executedCommand = null
            )
        } catch (e: Exception) {
            MidiExecutionResult(
                success = false,
                message = "Failed to execute MIDI program change: ${e.message}",
                executedCommand = null
            )
        }
    }
    
    override fun isAvailable(): Boolean {
        // In real implementation, check if MIDI devices are available
        return true
    }
    
    override fun getStatus(): String {
        return "Hardware MIDI executor ready"
    }
}

/**
 * Mock MIDI executor for testing that doesn't send real MIDI commands
 */
class MockMidiExecutor : MidiExecutor {
    private val executedCommands = mutableListOf<MidiCommand>()
    private val executedProgramChanges = mutableListOf<MidiProgramChange>()
    private var shouldFail = false
    
    fun setShouldFail(fail: Boolean) {
        shouldFail = fail
    }
    
    fun getExecutedCommands(): List<MidiCommand> = executedCommands.toList()
    
    fun getExecutedProgramChanges(): List<MidiProgramChange> = executedProgramChanges.toList()
    
    fun clearExecutedCommands() {
        executedCommands.clear()
        executedProgramChanges.clear()
    }
    
    override fun executeCommand(command: MidiCommand): MidiExecutionResult {
        return if (shouldFail) {
            MidiExecutionResult(
                success = false,
                message = "Mock execution failed",
                executedCommand = command
            )
        } else {
            executedCommands.add(command)
            val hexString = command.toMidiBytes().joinToString(" ") { "%02X".format(it) }
            MidiExecutionResult(
                success = true,
                message = "Mock MIDI executed: $hexString",
                executedCommand = command
            )
        }
    }
    
    override fun executeCommands(commands: List<MidiCommand>): List<MidiExecutionResult> {
        return commands.map { executeCommand(it) }
    }
    
    override fun executeProgramChange(programChange: MidiProgramChange): MidiExecutionResult {
        return if (shouldFail) {
            MidiExecutionResult(
                success = false,
                message = "Mock PC execution failed",
                executedCommand = null
            )
        } else {
            executedProgramChanges.add(programChange)
            val hexString = programChange.toMidiBytes().joinToString(" ") { "%02X".format(it) }
            MidiExecutionResult(
                success = true,
                message = "Mock MIDI PC executed: $hexString",
                executedCommand = null
            )
        }
    }
    
    override fun isAvailable(): Boolean = true
    
    override fun getStatus(): String = "Mock MIDI executor (${executedCommands.size} CC commands, ${executedProgramChanges.size} PC commands executed)"
}