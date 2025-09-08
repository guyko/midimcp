package com.guyko

import javax.sound.midi.MidiSystem

/**
 * Utility to list all available MIDI devices
 */
fun main() {
    println("=== Available MIDI Devices ===")
    
    val deviceInfos = MidiSystem.getMidiDeviceInfo()
    
    if (deviceInfos.isEmpty()) {
        println("No MIDI devices found!")
        return
    }
    
    deviceInfos.forEachIndexed { index, deviceInfo ->
        try {
            val device = MidiSystem.getMidiDevice(deviceInfo)
            val canReceive = device.maxReceivers != 0
            val canTransmit = device.maxTransmitters != 0
            
            println("\n[$index] ${deviceInfo.name}")
            println("    Description: ${deviceInfo.description}")
            println("    Vendor: ${deviceInfo.vendor}")
            println("    Version: ${deviceInfo.version}")
            println("    Can receive MIDI: $canReceive (maxReceivers: ${device.maxReceivers})")
            println("    Can transmit MIDI: $canTransmit (maxTransmitters: ${device.maxTransmitters})")
            
            if (canReceive) {
                println("    ✅ This device can receive MIDI commands")
            }
        } catch (e: Exception) {
            println("    ❌ Error accessing device: ${e.message}")
        }
    }
    
    println("\nLooking for USB MIDI interfaces...")
    deviceInfos.filter { deviceInfo ->
        deviceInfo.name.contains("USB", ignoreCase = true) ||
        deviceInfo.description.contains("USB", ignoreCase = true) ||
        deviceInfo.description.contains("MIDI", ignoreCase = true)
    }.forEach { deviceInfo ->
        println("  🎹 Potential USB MIDI device: ${deviceInfo.name} (${deviceInfo.description})")
    }
}