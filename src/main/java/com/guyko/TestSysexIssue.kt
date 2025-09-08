package com.guyko

import com.guyko.models.MidiCommand
import com.guyko.models.MidiSysex
import com.guyko.midi.HardwareMidiExecutor

/**
 * Debug sysex transmission issues
 * Tests both CC commands (working) vs sysex (not working)
 */
fun main() {
    println("🔍 === DEBUGGING SYSEX vs CC COMMAND TRANSMISSION ===")
    println()
    
    val executor = HardwareMidiExecutor()
    println("MIDI Status: ${executor.getStatus()}")
    println("MIDI Available: ${executor.isAvailable()}")
    println()
    
    if (!executor.isAvailable()) {
        println("❌ No MIDI device available")
        return
    }
    
    // Test 1: CC Command (known to work)
    println("🎛️  TEST 1: CC Command (Mix control - should work)")
    val ccCommand = MidiCommand(
        channel = 1,
        ccNumber = 1,
        value = 100,
        parameterName = "Mix",
        description = "Mercury X Mix to 79%"
    )
    
    println("Sending CC Command...")
    println("MIDI bytes: ${ccCommand.toMidiBytes().joinToString(" ") { "%02X".format(it) }}")
    val ccResult = executor.executeCommand(ccCommand)
    
    if (ccResult.success) {
        println("✅ CC Command sent successfully!")
        println("Status: ${ccResult.message}")
        println("👀 Check your Mercury X - did the mix change?")
    } else {
        println("❌ CC Command failed: ${ccResult.message}")
    }
    println()
    
    // Test 2: Simple Sysex (not working)
    println("📡 TEST 2: Simple Universal Sysex (should work on any device)")
    val simpleSysex = MidiSysex(byteArrayOf(
        0xF0.toByte(),           // Start of sysex
        0x7E.toByte(),           // Universal Non-realtime ID
        0x00.toByte(),           // Device ID (all devices)
        0x06.toByte(),           // General Information
        0x01.toByte(),           // Identity Request
        0xF7.toByte()            // End of sysex
    ))
    
    println("Sending Simple Sysex...")
    println("Sysex bytes: ${simpleSysex.toHexString()}")
    val simpleSysexResult = executor.executeSysex(simpleSysex)
    
    if (simpleSysexResult.success) {
        println("✅ Simple Sysex sent successfully!")
        println("Status: ${simpleSysexResult.message}")
        println("Bytes transmitted: ${simpleSysexResult.bytesTransmitted}")
    } else {
        println("❌ Simple Sysex failed: ${simpleSysexResult.message}")
    }
    println()
    
    // Test 3: Mercury X specific sysex
    println("🌊 TEST 3: Mercury X Specific Sysex")
    
    // Use the actual Mercury X sysex header but minimal data
    val mercuryXSysex = MidiSysex(byteArrayOf(
        0xF0.toByte(),           // Start of sysex
        0x00.toByte(), 0x02.toByte(), 0x10.toByte(),  // Meris manufacturer ID  
        0x00.toByte(),           // Device ID
        0x02.toByte(),           // Mercury X model
        0x01.toByte(),           // Command (preset dump)
        0x26.toByte(),           // Preset number
        // Minimal test data (just a few bytes instead of full 231)
        0x00.toByte(), 0x40.toByte(), 0x40.toByte(), 0x40.toByte(),
        0xF7.toByte()            // End of sysex
    ))
    
    println("Sending Mercury X Sysex...")
    println("Sysex bytes: ${mercuryXSysex.toHexString()}")
    val mercuryXResult = executor.executeSysex(mercuryXSysex)
    
    if (mercuryXResult.success) {
        println("✅ Mercury X Sysex sent successfully!")
        println("Status: ${mercuryXResult.message}")
        println("Bytes transmitted: ${mercuryXResult.bytesTransmitted}")
        println("👀 Check your Mercury X - any response or change?")
    } else {
        println("❌ Mercury X Sysex failed: ${mercuryXResult.message}")
    }
    println()
    
    // Test 4: Raw MIDI System Info
    println("🔧 TEST 4: MIDI System Information")
    try {
        val midiDeviceInfo = javax.sound.midi.MidiSystem.getMidiDeviceInfo()
        val currentDevice = midiDeviceInfo.find { deviceInfo ->
            try {
                val device = javax.sound.midi.MidiSystem.getMidiDevice(deviceInfo)
                device.deviceInfo.name == executor.getStatus().substringAfter("connected to: ")
            } catch (e: Exception) {
                false
            }
        }
        
        if (currentDevice != null) {
            println("Connected device details:")
            println("  Name: ${currentDevice.name}")
            println("  Description: ${currentDevice.description}")
            println("  Vendor: ${currentDevice.vendor}")
            println("  Version: ${currentDevice.version}")
            
            val device = javax.sound.midi.MidiSystem.getMidiDevice(currentDevice)
            println("  Max Receivers: ${device.maxReceivers}")
            println("  Max Transmitters: ${device.maxTransmitters}")
        }
    } catch (e: Exception) {
        println("❌ Error getting device info: ${e.message}")
    }
    
    executor.close()
    
    println()
    println("🔍 === ANALYSIS ===")
    println("If CC commands work but sysex doesn't, possible causes:")
    println("1. 🎛️  Mercury X sysex reception disabled in settings")
    println("2. 📡 MIDI interface doesn't support sysex transmission")  
    println("3. 🔌 USB MIDI vs 5-pin DIN MIDI differences")
    println("4. ⚡ Sysex data rate too fast for Mercury X")
    println("5. 🎯 Wrong Mercury X sysex format or header")
    println()
    println("💡 NEXT STEPS:")
    println("1. Check Mercury X manual for sysex enable/disable settings")
    println("2. Try a different MIDI interface or 5-pin DIN MIDI")
    println("3. Test with other sysex-capable devices")
    println("4. Check Mercury X firmware version compatibility")
}