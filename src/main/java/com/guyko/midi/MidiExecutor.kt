package com.guyko.midi

import com.guyko.models.MidiCommand
import com.guyko.models.MidiProgramChange
import com.guyko.models.MidiSysex
import io.github.oshai.kotlinlogging.KotlinLogging

data class MidiExecutionResult(
    val success: Boolean,
    val message: String,
    val executedCommand: MidiCommand? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class SysexExecutionResult(
    val success: Boolean,
    val message: String,
    val bytesTransmitted: Int = 0,
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
     * Execute a MIDI sysex transmission
     */
    fun executeSysex(sysex: MidiSysex): SysexExecutionResult
    
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
    private var lastConnectionAttempt: Long = 0
    private val connectionCooldownMs = 5000 // Don't retry connections more often than every 5 seconds
    
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
                    try {
                        device.open()
                        logger.info { "Opened MIDI device: ${device.deviceInfo.name}" }
                    } catch (e: Exception) {
                        logger.error(e) { "Failed to open MIDI device ${device.deviceInfo.name}. Device may be in use by another application (like SysEx Librarian). ${e.message}" }
                        throw e
                    }
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
            
            // If no receiver available, try to re-initialize MIDI devices (hot-plug support)
            if (receiver == null || midiDevice?.isOpen != true) {
                tryFastConnection()
            }
            
            // Send to actual hardware if available
            receiver?.let { recv ->
                // Check if device is still open before sending
                if (midiDevice?.isOpen == true) {
                    val midiMessage = javax.sound.midi.ShortMessage(
                        javax.sound.midi.ShortMessage.CONTROL_CHANGE,
                        command.channel - 1, // MIDI channels are 0-indexed in Java MIDI API
                        command.ccNumber,
                        command.value
                    )
                    recv.send(midiMessage, -1) // -1 means send immediately
                    logger.info { "MIDI CC command sent to hardware: $hexString (channel=${command.channel}, cc=${command.ccNumber}, value=${command.value})" }
                } else {
                    logger.warn { "MIDI device is not open, cannot send command: $hexString. Device may have been closed by another application." }
                    throw Exception("MIDI device is not open")
                }
            } ?: run {
                // Log only if no hardware available
                println("MIDI (no device): $hexString")
                logger.warn { "MIDI CC command logged (no device): $hexString (channel=${command.channel}, cc=${command.ccNumber}, value=${command.value}). Check MIDI device connection and ensure devices are powered on." }
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
    
    override fun executeSysex(sysex: MidiSysex): SysexExecutionResult {
        logger.debug { "Executing MIDI sysex: ${sysex.data.size} bytes" }
        
        return try {
            if (receiver != null) {
                // Create MIDI sysex message
                val sysexMessage = javax.sound.midi.SysexMessage()
                sysexMessage.setMessage(sysex.data, sysex.data.size)
                
                // Send the sysex message
                receiver!!.send(sysexMessage, -1)
                
                val hexString = sysex.toHexString()
                logger.info { "MIDI sysex sent to hardware: $hexString (${sysex.data.size} bytes)" }
                
                SysexExecutionResult(
                    success = true,
                    message = "Sysex transmitted successfully to ${midiDevice?.deviceInfo?.name}",
                    bytesTransmitted = sysex.data.size
                )
            } else {
                val hexString = sysex.toHexString()
                logger.info { "MIDI sysex logged (no device): $hexString (${sysex.data.size} bytes)" }
                
                SysexExecutionResult(
                    success = false,
                    message = "No MIDI device available - sysex data logged only",
                    bytesTransmitted = 0
                )
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to execute MIDI sysex: ${sysex.data.size} bytes" }
            SysexExecutionResult(
                success = false,
                message = "Failed to transmit sysex: ${e.message}",
                bytesTransmitted = 0
            )
        }
    }
    
    override fun isAvailable(): Boolean {
        return receiver != null
    }
    
    override fun getStatus(): String {
        return if (receiver != null && midiDevice != null) {
            val deviceInfo = midiDevice!!.deviceInfo
            val isOpen = midiDevice!!.isOpen
            "Hardware MIDI executor connected to: ${deviceInfo.name} (${deviceInfo.description}) - Open: $isOpen"
        } else if (midiDevice != null) {
            val deviceInfo = midiDevice!!.deviceInfo
            val isOpen = midiDevice!!.isOpen
            "Hardware MIDI executor found device: ${deviceInfo.name} but no receiver available - Open: $isOpen"
        } else {
            "Hardware MIDI executor (no device found or device conflict)"
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
    
    fun reconnect() {
        logger.info { "Attempting to reconnect MIDI device..." }
        try {
            close()
            receiver = null
            midiDevice = null
            initializeMidiDevice()
        } catch (e: Exception) {
            logger.error(e) { "Failed to reconnect MIDI device" }
        }
    }
    
    /**
     * Attempts a fast MIDI connection with timeout to avoid blocking user commands
     */
    private fun tryFastConnection() {
        val currentTime = System.currentTimeMillis()
        
        // Rate limit connection attempts to avoid performance issues
        if (currentTime - lastConnectionAttempt < connectionCooldownMs) {
            logger.debug { "Skipping MIDI connection attempt (cooldown period)" }
            return
        }
        
        lastConnectionAttempt = currentTime
        
        try {
            // Quick timeout-based connection attempt
            val connectionThread = Thread {
                try {
                    logger.debug { "Starting fast MIDI device detection..." }
                    initializeMidiDevice()
                } catch (e: Exception) {
                    logger.debug(e) { "Fast MIDI connection failed: ${e.message}" }
                }
            }
            
            connectionThread.start()
            // Give it a short time to complete, but don't block user commands
            connectionThread.join(1000) // 1 second timeout
            
            if (connectionThread.isAlive) {
                logger.debug { "MIDI connection taking too long, continuing without device (will retry later)" }
                // Let the thread continue in background for future commands
            } else if (receiver != null) {
                logger.info { "Fast MIDI connection successful" }
            } else {
                logger.debug { "Fast MIDI connection completed but no device found" }
            }
            
        } catch (e: Exception) {
            logger.debug(e) { "Fast MIDI connection attempt failed: ${e.message}" }
        }
    }
}

