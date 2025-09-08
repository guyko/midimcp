package com.guyko.midi

import com.guyko.models.MidiCommand
import com.guyko.models.MidiProgramChange
import com.guyko.models.MidiSysex
import mu.KotlinLogging

/**
 * Mock MIDI executor for testing that doesn't send real MIDI commands
 * This executor tracks executed commands for test verification
 */
class MockMidiExecutor : MidiExecutor {
    private val logger = KotlinLogging.logger {}
    private val executedCommands = mutableListOf<MidiCommand>()
    private val executedProgramChanges = mutableListOf<MidiProgramChange>()
    private val executedSysex = mutableListOf<MidiSysex>()
    private var shouldFail = false
    
    fun setShouldFail(fail: Boolean) {
        shouldFail = fail
    }
    
    fun getExecutedCommands(): List<MidiCommand> = executedCommands.toList()
    
    fun getExecutedProgramChanges(): List<MidiProgramChange> = executedProgramChanges.toList()
    
    fun getExecutedSysex(): List<MidiSysex> = executedSysex.toList()
    
    fun clearExecutedCommands() {
        executedCommands.clear()
        executedProgramChanges.clear()
        executedSysex.clear()
    }
    
    override fun executeCommand(command: MidiCommand): MidiExecutionResult {
        logger.debug { "Mock executing MIDI command: channel=${command.channel}, cc=${command.ccNumber}, value=${command.value}" }
        return if (shouldFail) {
            logger.warn { "Mock MIDI command execution failed (shouldFail=true): channel=${command.channel}, cc=${command.ccNumber}, value=${command.value}" }
            MidiExecutionResult(
                success = false,
                message = "Mock execution failed",
                executedCommand = command
            )
        } else {
            executedCommands.add(command)
            val hexString = command.toMidiBytes().joinToString(" ") { "%02X".format(it) }
            logger.info { "Mock MIDI CC command executed: $hexString (channel=${command.channel}, cc=${command.ccNumber}, value=${command.value})" }
            MidiExecutionResult(
                success = true,
                message = "Mock MIDI executed: $hexString",
                executedCommand = command
            )
        }
    }
    
    override fun executeCommands(commands: List<MidiCommand>): List<MidiExecutionResult> {
        logger.info { "Mock executing batch of ${commands.size} MIDI commands" }
        val results = commands.map { executeCommand(it) }
        val successCount = results.count { it.success }
        logger.info { "Mock batch execution complete: $successCount/${commands.size} commands successful" }
        return results
    }
    
    override fun executeProgramChange(programChange: MidiProgramChange): MidiExecutionResult {
        logger.debug { "Mock executing MIDI program change: channel=${programChange.channel}, program=${programChange.program}" }
        return if (shouldFail) {
            logger.warn { "Mock MIDI program change execution failed (shouldFail=true): channel=${programChange.channel}, program=${programChange.program}" }
            MidiExecutionResult(
                success = false,
                message = "Mock PC execution failed",
                executedCommand = null
            )
        } else {
            executedProgramChanges.add(programChange)
            val hexString = programChange.toMidiBytes().joinToString(" ") { "%02X".format(it) }
            logger.info { "Mock MIDI program change executed: $hexString (channel=${programChange.channel}, program=${programChange.program})" }
            MidiExecutionResult(
                success = true,
                message = "Mock MIDI PC executed: $hexString",
                executedCommand = null
            )
        }
    }
    
    override fun executeSysex(sysex: MidiSysex): SysexExecutionResult {
        logger.debug { "Mock executing MIDI sysex: ${sysex.data.size} bytes" }
        return if (shouldFail) {
            logger.warn { "Mock MIDI sysex execution failed (shouldFail=true): ${sysex.data.size} bytes" }
            SysexExecutionResult(
                success = false,
                message = "Mock sysex execution failed",
                bytesTransmitted = 0
            )
        } else {
            executedSysex.add(sysex)
            val hexString = sysex.toHexString()
            logger.info { "Mock MIDI sysex executed: $hexString (${sysex.data.size} bytes)" }
            SysexExecutionResult(
                success = true,
                message = "Mock sysex executed: $hexString",
                bytesTransmitted = sysex.data.size
            )
        }
    }
    
    override fun isAvailable(): Boolean = true
    
    override fun getStatus(): String = "Mock MIDI executor (${executedCommands.size} CC commands, ${executedProgramChanges.size} PC commands, ${executedSysex.size} sysex messages executed)"
}