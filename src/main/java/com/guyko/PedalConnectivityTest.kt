package com.guyko

import com.guyko.models.MidiCommand
import com.guyko.models.MidiSysex
import com.guyko.midi.HardwareMidiExecutor
import com.guyko.pedals.MercuryXPresetGenerator
import com.guyko.pedals.LVXPresetGenerator
import java.util.Scanner

/**
 * Comprehensive pedal connectivity test program
 * Tests MIDI CC commands, sysex transmission, and preset uploads
 * Includes user interaction to verify changes on actual pedals
 */
fun main(args: Array<String>) {
    val scanner = Scanner(System.`in`)
    val executor = HardwareMidiExecutor()
    
    println("=== 🎸 Guitar Pedal Connectivity Test ===")
    println()
    println("MIDI Status: ${executor.getStatus()}")
    println("MIDI Available: ${executor.isAvailable()}")
    println()
    
    if (!executor.isAvailable()) {
        println("❌ No MIDI device detected!")
        println("Please connect your MIDI interface and guitar pedals, then restart.")
        return
    }
    
    loop@ while (true) {
        showMenu()
        print("Choose a test (1-8, or 'q' to quit): ")
        val choice = scanner.nextLine().trim().lowercase()
        
        when (choice) {
            "q", "quit", "exit" -> {
                println("Closing MIDI connection...")
                executor.close()
                println("Test complete. Goodbye! 🎸")
                break@loop
            }
            "1" -> testMercuryXVisibleChange(executor, scanner)
            "2" -> testLVXVisibleChange(executor, scanner) 
            "3" -> testEnzoXVisibleChange(executor, scanner)
            "4" -> testMercuryXPresetUpload(executor, scanner)
            "5" -> testLVXPresetUpload(executor, scanner)
            "6" -> testSysexTransmission(executor, scanner)
            "7" -> testAllPedalsSequence(executor, scanner)
            "8" -> showConnectedDevices()
            else -> println("❌ Invalid choice. Please try again.")
        }
        println()
    }
}

fun showMenu() {
    println("""
    🎛️  PEDAL CONNECTIVITY TESTS
    
    1. Mercury X Mix Test      - Test Mercury X reverb mix control (visible change)
    2. LVX Time Test          - Test LVX delay time control (audible change) 
    3. Enzo X Mode Test       - Test Enzo X synth mode switching (audible change)
    4. Mercury X Preset Upload - Generate and upload a Mercury X preset
    5. LVX Preset Upload      - Generate and upload an LVX preset
    6. Raw Sysex Test         - Send custom sysex data
    7. All Pedals Sequence    - Test all connected pedals in sequence
    8. Show MIDI Devices      - List available MIDI devices
    """.trimIndent())
}

fun testMercuryXVisibleChange(executor: HardwareMidiExecutor, scanner: Scanner) {
    println("🌊 === MERCURY X REVERB MIX TEST ===")
    println()
    println("This will change the Mix parameter on your Mercury X pedal.")
    println("You should see the Mix knob LED or display change on your pedal.")
    println()
    
    // First set to minimum
    println("Setting Mercury X Mix to MINIMUM (0%)...")
    val minCommand = MidiCommand(
        channel = 1,
        ccNumber = 1, 
        value = 0,
        parameterName = "Mix",
        description = "Mercury X Mix to 0%"
    )
    
    executeCommandAndVerify(executor, minCommand, scanner,
        "Do you see the Mix knob/display on your Mercury X show minimum/0%? (y/n)")
    
    // Then set to maximum
    println("Setting Mercury X Mix to MAXIMUM (100%)...")
    val maxCommand = MidiCommand(
        channel = 1,
        ccNumber = 1,
        value = 127,
        parameterName = "Mix", 
        description = "Mercury X Mix to 100%"
    )
    
    executeCommandAndVerify(executor, maxCommand, scanner,
        "Do you see the Mix knob/display on your Mercury X show maximum/100%? (y/n)")
    
    // Reset to middle
    println("Resetting Mercury X Mix to 50%...")
    val midCommand = MidiCommand(
        channel = 1,
        ccNumber = 1,
        value = 64,
        parameterName = "Mix",
        description = "Mercury X Mix to 50%"
    )
    executeCommandAndVerify(executor, midCommand, scanner, null)
}

fun testLVXVisibleChange(executor: HardwareMidiExecutor, scanner: Scanner) {
    println("⏱️  === LVX DELAY TIME TEST ===")
    println()
    println("This will change the delay time on your LVX pedal.")
    println("You should hear the delay time change dramatically.")
    println()
    
    println("Setting LVX to SHORT delay time...")
    val shortCommand = MidiCommand(
        channel = 2,
        ccNumber = 15,
        value = 10,
        parameterName = "Time",
        description = "LVX short delay time"
    )
    
    executeCommandAndVerify(executor, shortCommand, scanner,
        "Do you hear a very short/quick delay from your LVX? (y/n)")
    
    println("Setting LVX to LONG delay time...")
    val longCommand = MidiCommand(
        channel = 2,
        ccNumber = 15,
        value = 120,
        parameterName = "Time",
        description = "LVX long delay time"
    )
    
    executeCommandAndVerify(executor, longCommand, scanner,
        "Do you hear a much longer/slower delay from your LVX? (y/n)")
    
    // Reset to medium
    println("Resetting LVX to medium delay time...")
    val midCommand = MidiCommand(
        channel = 2,
        ccNumber = 15,
        value = 64,
        parameterName = "Time",
        description = "LVX medium delay time"
    )
    executeCommandAndVerify(executor, midCommand, scanner, null)
}

fun testEnzoXVisibleChange(executor: HardwareMidiExecutor, scanner: Scanner) {
    println("🎹 === ENZO X SYNTH MODE TEST ===")
    println()
    println("This will switch the synth mode on your Enzo X pedal.")
    println("You should hear the synthesizer character change dramatically.")
    println()
    
    println("Setting Enzo X to MONO SYNTH mode...")
    val monoCommand = MidiCommand(
        channel = 3,
        ccNumber = 22,
        value = 12, // Mono Synth mode (0-25 range)
        parameterName = "Synth Mode",
        description = "Enzo X Mono Synth mode"
    )
    
    executeCommandAndVerify(executor, monoCommand, scanner,
        "Do you hear a monophonic synthesizer sound from your Enzo X? (y/n)")
    
    println("Setting Enzo X to POLY SYNTH mode...")
    val polyCommand = MidiCommand(
        channel = 3,
        ccNumber = 22,
        value = 38, // Poly Synth mode (26-51 range)
        parameterName = "Synth Mode",
        description = "Enzo X Poly Synth mode"
    )
    
    executeCommandAndVerify(executor, polyCommand, scanner,
        "Do you hear a polyphonic synthesizer sound from your Enzo X? (y/n)")
}

fun testMercuryXPresetUpload(executor: HardwareMidiExecutor, scanner: Scanner) {
    println("🌊 === MERCURY X PRESET UPLOAD TEST ===")
    println()
    println("This will generate and upload a test preset to your Mercury X pedal.")
    println("The preset will have specific settings that should be audible/visible.")
    println()
    
    // Create a distinctive preset
    val parameters = mapOf(
        1 to 100,   // Mix at 80% (very wet)
        5 to 25,    // Cathedra reverb structure  
        11 to 110,  // Very long decay
        12 to 60    // Medium predelay
    )
    
    val presetName = "CONNECTIVITY_TEST"
    
    println("Generating Mercury X preset: '$presetName'")
    println("Settings: Wet mix (80%), Cathedra reverb, Long decay, Medium predelay")
    
    try {
        val sysex = MercuryXPresetGenerator.generatePreset(parameters, presetName)
        println("✅ Preset generated: ${sysex.data.size} bytes")
        println("Sysex: ${sysex.toHexString()}")
        println()
        
        println("Uploading preset to Mercury X pedal...")
        val result = executor.executeSysex(sysex)
        
        if (result.success) {
            println("✅ Preset uploaded successfully!")
            println("Status: ${result.message}")
            println("Bytes transmitted: ${result.bytesTransmitted}")
            println()
            
            print("Can you see '$presetName' in your Mercury X preset list? (y/n): ")
            val response = scanner.nextLine().trim().lowercase()
            
            if (response == "y" || response == "yes") {
                println("🎉 SUCCESS: Preset upload confirmed!")
            } else {
                println("⚠️  Preset may not have uploaded correctly.")
                println("Check your MIDI connections and pedal settings.")
            }
        } else {
            println("❌ Preset upload failed!")
            println("Error: ${result.message}")
        }
    } catch (e: Exception) {
        println("❌ Error generating/uploading preset: ${e.message}")
    }
}

fun testLVXPresetUpload(executor: HardwareMidiExecutor, scanner: Scanner) {
    println("⏱️  === LVX PRESET UPLOAD TEST ===")
    println()
    println("This will generate and upload a test preset to your LVX pedal.")
    println()
    
    // Create a distinctive preset - vintage slapback
    val parameters = mapOf(
        1 to 85,    // Mix - high wet signal
        15 to 25,   // Time - short slapback timing  
        16 to 86,   // Type - tape delay engine
        19 to 15,   // Feedback - low repeats
        21 to 20    // Modulation - subtle tape warble
    )
    
    val presetName = "SLAPBACK_TEST"
    
    println("Generating LVX preset: '$presetName'")
    println("Settings: Vintage slapback delay with tape engine")
    
    try {
        val sysex = LVXPresetGenerator.generatePreset(parameters, presetName)
        println("✅ Preset generated: ${sysex.data.size} bytes")
        println()
        
        println("Uploading preset to LVX pedal...")
        val result = executor.executeSysex(sysex)
        
        if (result.success) {
            println("✅ Preset uploaded successfully!")
            println("Status: ${result.message}")
            println("Bytes transmitted: ${result.bytesTransmitted}")
            println()
            
            print("Can you see '$presetName' in your LVX preset list? (y/n): ")
            val response = scanner.nextLine().trim().lowercase()
            
            if (response == "y" || response == "yes") {
                println("🎉 SUCCESS: Preset upload confirmed!")
            } else {
                println("⚠️  Preset may not have uploaded correctly.")
            }
        } else {
            println("❌ Preset upload failed!")
            println("Error: ${result.message}")
        }
    } catch (e: Exception) {
        println("❌ Error generating/uploading preset: ${e.message}")
    }
}

fun testSysexTransmission(executor: HardwareMidiExecutor, scanner: Scanner) {
    println("📡 === RAW SYSEX TRANSMISSION TEST ===")
    println()
    print("Enter sysex data in hex format (e.g., 'F0 7F 00 01 F7'): ")
    val hexInput = scanner.nextLine().trim()
    
    if (hexInput.isBlank()) {
        println("❌ No sysex data provided.")
        return
    }
    
    try {
        val bytes = hexInput.split(" ", "-").map { it.trim().toInt(16).toByte() }.toByteArray()
        val sysex = MidiSysex(bytes)
        
        println("Sending sysex: ${sysex.toHexString()}")
        val result = executor.executeSysex(sysex)
        
        if (result.success) {
            println("✅ Sysex sent successfully!")
            println("Bytes transmitted: ${result.bytesTransmitted}")
        } else {
            println("❌ Sysex transmission failed!")
            println("Error: ${result.message}")
        }
    } catch (e: Exception) {
        println("❌ Invalid sysex format: ${e.message}")
    }
}

fun testAllPedalsSequence(executor: HardwareMidiExecutor, scanner: Scanner) {
    println("🎸 === ALL PEDALS SEQUENCE TEST ===")
    println()
    println("This will send commands to all pedals in sequence.")
    println("You should see/hear changes on each pedal.")
    println()
    
    val commands = listOf(
        MidiCommand(1, 1, 127, "Mix", "Mercury X Mix to 100%"),
        MidiCommand(2, 1, 127, "Mix", "LVX Mix to 100%"),
        MidiCommand(3, 1, 127, "Mix", "Enzo X Mix to 100%")
    )
    
    commands.forEach { command ->
        println("Sending: ${command.description}")
        val result = executor.executeCommand(command)
        
        if (result.success) {
            println("✅ ${command.description}")
        } else {
            println("❌ Failed: ${result.message}")
        }
        
        Thread.sleep(1000) // Wait 1 second between commands
    }
    
    println()
    print("Did you see/hear changes on all your connected pedals? (y/n): ")
    val response = scanner.nextLine().trim().lowercase()
    
    if (response == "y" || response == "yes") {
        println("🎉 SUCCESS: All pedals responded!")
    } else {
        println("⚠️  Some pedals may not be connected or configured correctly.")
        println("Check MIDI channel settings and connections.")
    }
}

fun showConnectedDevices() {
    println("🔌 === MIDI DEVICES ===")
    println()
    
    try {
        val deviceInfos = javax.sound.midi.MidiSystem.getMidiDeviceInfo()
        
        if (deviceInfos.isEmpty()) {
            println("❌ No MIDI devices found!")
            return
        }
        
        println("Available MIDI devices:")
        deviceInfos.forEachIndexed { index, deviceInfo ->
            try {
                val device = javax.sound.midi.MidiSystem.getMidiDevice(deviceInfo)
                val canReceive = device.maxReceivers != 0
                
                println("[$index] ${deviceInfo.name}")
                println("    Description: ${deviceInfo.description}")
                println("    Can receive: $canReceive")
                
                if (canReceive) {
                    println("    ✅ This device can receive MIDI commands")
                }
                println()
            } catch (e: Exception) {
                println("    ❌ Error accessing device: ${e.message}")
            }
        }
    } catch (e: Exception) {
        println("❌ Error listing MIDI devices: ${e.message}")
    }
}

fun executeCommandAndVerify(
    executor: HardwareMidiExecutor, 
    command: MidiCommand, 
    scanner: Scanner,
    verificationQuestion: String?
) {
    println("Sending: ${command.description}")
    println("MIDI: Channel ${command.channel}, CC ${command.ccNumber}, Value ${command.value}")
    println("Bytes: ${command.toMidiBytes().joinToString(" ") { "%02X".format(it) }}")
    
    val result = executor.executeCommand(command)
    
    if (result.success) {
        println("✅ MIDI command sent successfully!")
        
        if (verificationQuestion != null) {
            print("$verificationQuestion ")
            val response = scanner.nextLine().trim().lowercase()
            
            if (response == "y" || response == "yes") {
                println("🎉 SUCCESS: Change confirmed on pedal!")
            } else {
                println("⚠️  No change detected. Check:")
                println("   - MIDI connections")
                println("   - Pedal power and MIDI channel settings")
                println("   - Correct pedal for this test")
            }
        }
    } else {
        println("❌ MIDI command failed: ${result.message}")
    }
    println()
}