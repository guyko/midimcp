package com.guyko

import com.guyko.models.MidiCommand
import com.guyko.midi.HardwareMidiExecutor
import javax.sound.midi.MidiSystem

/**
 * Tool to find and test the actual Mercury X pedal connection
 * Tests each MIDI device to see which one responds to Mercury X commands
 */
fun main() {
    println("🔍 === FINDING YOUR REAL MERCURY X PEDAL ===")
    println()
    
    // List all available MIDI devices
    val deviceInfos = MidiSystem.getMidiDeviceInfo()
    
    if (deviceInfos.isEmpty()) {
        println("❌ No MIDI devices found!")
        println("Please check your MIDI interface connections.")
        return
    }
    
    println("📋 Available MIDI devices:")
    deviceInfos.forEachIndexed { index, deviceInfo ->
        try {
            val device = MidiSystem.getMidiDevice(deviceInfo)
            val canReceive = device.maxReceivers != 0
            val isHardware = !deviceInfo.description.contains("Software", ignoreCase = true) &&
                           !deviceInfo.name.contains("Gervill", ignoreCase = true) &&
                           !deviceInfo.name.contains("Sequencer", ignoreCase = true)
            
            val status = when {
                canReceive && isHardware -> "🎸 HARDWARE (can receive MIDI)"
                canReceive -> "💻 SOFTWARE (can receive MIDI)" 
                else -> "📡 INPUT ONLY"
            }
            
            println("[$index] ${deviceInfo.name}")
            println("    Description: ${deviceInfo.description}")
            println("    Vendor: ${deviceInfo.vendor}")
            println("    Status: $status")
            println()
        } catch (e: Exception) {
            println("[$index] ${deviceInfo.name} - ❌ Error: ${e.message}")
            println()
        }
    }
    
    // Test hardware devices for Mercury X response
    println("🎯 Testing hardware devices for Mercury X response...")
    println("(We'll send a Mercury X Mix command and see which device might be your pedal)")
    println()
    
    val hardwareDevices = deviceInfos.filterIndexed { index, deviceInfo ->
        try {
            val device = MidiSystem.getMidiDevice(deviceInfo)
            val canReceive = device.maxReceivers != 0
            val isHardware = !deviceInfo.description.contains("Software", ignoreCase = true) &&
                           !deviceInfo.name.contains("Gervill", ignoreCase = true) &&
                           !deviceInfo.name.contains("Sequencer", ignoreCase = true)
            canReceive && isHardware
        } catch (e: Exception) {
            false
        }
    }
    
    if (hardwareDevices.isEmpty()) {
        println("❌ No hardware MIDI devices found that can receive MIDI!")
        println()
        println("🔧 Troubleshooting:")
        println("1. Check that your MIDI interface is connected via USB")
        println("2. Check that your Mercury X is connected to the MIDI interface")
        println("3. Ensure the Mercury X is powered on")
        println("4. Try a different USB port for your MIDI interface")
        return
    }
    
    println("🎸 Found ${hardwareDevices.size} hardware MIDI device(s) that can receive MIDI:")
    hardwareDevices.forEachIndexed { testIndex, deviceInfo ->
        val realIndex = deviceInfos.indexOf(deviceInfo)
        println("  [Test $testIndex] Device $realIndex: ${deviceInfo.name}")
    }
    println()
    
    // Test each hardware device
    hardwareDevices.forEachIndexed { testIndex, deviceInfo ->
        val realIndex = deviceInfos.indexOf(deviceInfo)
        println("🧪 Testing Device $realIndex: ${deviceInfo.name}")
        
        try {
            // Create a custom MIDI executor for this specific device
            val device = MidiSystem.getMidiDevice(deviceInfo)
            device.open()
            val receiver = device.receiver
            
            // Send Mercury X Mix command (Channel 1, CC 1, Value 127)
            val command = MidiCommand(
                channel = 1,
                ccNumber = 1, // Mix parameter
                value = 127,  // Maximum value
                parameterName = "Mix",
                description = "Mercury X Mix to 100%"
            )
            
            val midiBytes = command.toMidiBytes()
            val midiMessage = javax.sound.midi.ShortMessage()
            midiMessage.setMessage(
                javax.sound.midi.ShortMessage.CONTROL_CHANGE,
                command.channel - 1, // MIDI channels are 0-based
                command.ccNumber,
                command.value
            )
            
            println("   Sending: ${midiBytes.joinToString(" ") { "%02X".format(it) }}")
            receiver.send(midiMessage, -1)
            
            println("   ✅ MIDI sent successfully to: ${deviceInfo.name}")
            println("   👀 CHECK YOUR MERCURY X PEDAL NOW:")
            println("      - Did the Mix knob LED change?")
            println("      - Did any display change?") 
            println("      - Did you hear any audio change?")
            println()
            
            // Clean up
            receiver.close()
            device.close()
            
        } catch (e: Exception) {
            println("   ❌ Failed to send MIDI: ${e.message}")
            println()
        }
        
        if (testIndex < hardwareDevices.size - 1) {
            println("   ⏳ Waiting 2 seconds before testing next device...")
            Thread.sleep(2000)
            println()
        }
    }
    
    println("🏁 === TEST COMPLETE ===")
    println()
    println("📝 RESULTS ANALYSIS:")
    println("- If you saw changes on your Mercury X during one of the tests,")
    println("  that device is your actual Mercury X connection!")
    println("- If no changes occurred on any test, check:")
    println("  1. MIDI cable connections (5-pin DIN cables)")
    println("  2. Mercury X MIDI channel settings (should be Channel 1)")
    println("  3. Mercury X power and MIDI enable settings")
    println("  4. MIDI interface drivers and connections")
    println()
    println("🔧 Next: Tell me which device number (if any) caused changes on your Mercury X!")
}