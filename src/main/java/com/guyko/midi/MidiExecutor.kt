package com.guyko.midi

import com.guyko.models.MidiCommand
import com.guyko.models.MidiProgramChange
import mu.KotlinLogging

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
 * Uses Java MIDI API to communicate with MIDI devices
 */
class HardwareMidiExecutor : MidiExecutor {
    private val logger = KotlinLogging.logger {}
    private var midiDevice: javax.sound.midi.MidiDevice? = null
    private var receiver: javax.sound.midi.Receiver? = null
    
    init {
        initializeMidiDevice()
    }
    
    private fun initializeMidiDevice() {
        try {
            val midiDeviceInfo = javax.sound.midi.MidiSystem.getMidiDeviceInfo()
            logger.debug { "Available MIDI devices: ${midiDeviceInfo.map { "${it.name} (${it.description})" }}" }
            
            // Prefer hardware MIDI devices over software synthesizers
            val hardwareDevices = midiDeviceInfo.filter { deviceInfo ->
                val device = javax.sound.midi.MidiSystem.getMidiDevice(deviceInfo)
                device.maxReceivers != 0 && // Device can receive MIDI
                !deviceInfo.description.contains("Software", ignoreCase = true) &&
                !deviceInfo.name.contains("Gervill", ignoreCase = true) &&
                !deviceInfo.name.contains("Sequencer", ignoreCase = true)
            }
            
            // Try hardware devices first
            for (deviceInfo in hardwareDevices) {
                val device = javax.sound.midi.MidiSystem.getMidiDevice(deviceInfo)
                logger.info { "Found hardware MIDI device: ${deviceInfo.name} (${deviceInfo.description})" }
                midiDevice = device
                break
            }
            
            // Fall back to any MIDI output device if no hardware found
            if (midiDevice == null) {
                for (deviceInfo in midiDeviceInfo) {
                    val device = javax.sound.midi.MidiSystem.getMidiDevice(deviceInfo)
                    if (device.maxReceivers != 0) { // Device can receive MIDI
                        logger.info { "Found MIDI output device: ${deviceInfo.name} (${deviceInfo.description})" }
                        midiDevice = device
                        break
                    }
                }
            }
            
            midiDevice?.let { device ->
                if (!device.isOpen) {
                    device.open()
                    logger.info { "Opened MIDI device: ${device.deviceInfo.name}" }
                }
                receiver = device.receiver
                logger.info { "MIDI receiver ready for hardware communication" }
            } ?: run {
                logger.warn { "No MIDI output devices found. Commands will be logged only." }
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to initialize MIDI device. Commands will be logged only." }
        }
    }
    
    override fun executeCommand(command: MidiCommand): MidiExecutionResult {
        logger.debug { "Executing MIDI command: channel=${command.channel}, cc=${command.ccNumber}, value=${command.value}" }
        return try {
            val midiBytes = command.toMidiBytes()
            val hexString = midiBytes.joinToString(" ") { "%02X".format(it) }
            
            // Send to actual hardware if available
            receiver?.let { recv ->
                val midiMessage = javax.sound.midi.ShortMessage(
                    javax.sound.midi.ShortMessage.CONTROL_CHANGE,
                    command.channel - 1, // MIDI channels are 0-indexed in Java MIDI API
                    command.ccNumber,
                    command.value
                )
                recv.send(midiMessage, -1) // -1 means send immediately
                logger.info { "MIDI CC command sent to hardware: $hexString (channel=${command.channel}, cc=${command.ccNumber}, value=${command.value})" }
            } ?: run {
                // Log only if no hardware available
                println("MIDI (no device): $hexString")
                logger.info { "MIDI CC command logged (no device): $hexString (channel=${command.channel}, cc=${command.ccNumber}, value=${command.value})" }
            }
            
            MidiExecutionResult(
                success = true,
                message = "MIDI command sent successfully: $hexString",
                executedCommand = command
            )
        } catch (e: Exception) {
            logger.error(e) { "Failed to execute MIDI command: channel=${command.channel}, cc=${command.ccNumber}, value=${command.value}" }
            MidiExecutionResult(
                success = false,
                message = "Failed to execute MIDI command: ${e.message}",
                executedCommand = command
            )
        }
    }
    
    override fun executeCommands(commands: List<MidiCommand>): List<MidiExecutionResult> {
        logger.info { "Executing batch of ${commands.size} MIDI commands" }
        val results = commands.map { executeCommand(it) }
        val successCount = results.count { it.success }
        logger.info { "Batch execution complete: $successCount/${commands.size} commands successful" }
        return results
    }
    
    override fun executeProgramChange(programChange: MidiProgramChange): MidiExecutionResult {
        logger.debug { "Executing MIDI program change: channel=${programChange.channel}, program=${programChange.program}" }
        return try {
            val midiBytes = programChange.toMidiBytes()
            val hexString = midiBytes.joinToString(" ") { "%02X".format(it) }
            
            // Send to actual hardware if available
            receiver?.let { recv ->
                val midiMessage = javax.sound.midi.ShortMessage(
                    javax.sound.midi.ShortMessage.PROGRAM_CHANGE,
                    programChange.channel - 1, // MIDI channels are 0-indexed in Java MIDI API
                    programChange.program,
                    0 // Program change doesn't use the third byte
                )
                recv.send(midiMessage, -1) // -1 means send immediately
                logger.info { "MIDI program change sent to hardware: $hexString (channel=${programChange.channel}, program=${programChange.program})" }
            } ?: run {
                // Log only if no hardware available
                println("MIDI PC (no device): $hexString")
                logger.info { "MIDI program change logged (no device): $hexString (channel=${programChange.channel}, program=${programChange.program})" }
            }
            
            MidiExecutionResult(
                success = true,
                message = "MIDI program change sent successfully: $hexString",
                executedCommand = null
            )
        } catch (e: Exception) {
            logger.error(e) { "Failed to execute MIDI program change: channel=${programChange.channel}, program=${programChange.program}" }
            MidiExecutionResult(
                success = false,
                message = "Failed to execute MIDI program change: ${e.message}",
                executedCommand = null
            )
        }
    }
    
    override fun isAvailable(): Boolean {
        return receiver != null
    }
    
    override fun getStatus(): String {
        return if (receiver != null) {
            "Hardware MIDI executor connected to: ${midiDevice?.deviceInfo?.name}"
        } else {
            "Hardware MIDI executor (no device connected)"
        }
    }
    
    fun close() {
        try {
            receiver?.close()
            midiDevice?.close()
            logger.info { "MIDI device closed" }
        } catch (e: Exception) {
            logger.error(e) { "Error closing MIDI device" }
        }
    }
}

