package com.guyko

import com.guyko.models.MidiCommand
import com.guyko.models.MidiProgramChange
import com.guyko.midi.HardwareMidiExecutor

/**
 * Real MIDI testing utility for sending commands to actual hardware pedals
 * This is NOT a unit test - it's a utility for testing real MIDI communication
 */
fun main(args: Array<String>) {
    if (args.isEmpty()) {
        showUsage()
        return
    }
    
    val executor = HardwareMidiExecutor()
    println("=== MIDI Hardware Test Utility ===")
    println("MIDI Status: ${executor.getStatus()}")
    println("Available: ${executor.isAvailable()}")
    println()
    
    when (args[0].toLowerCase()) {
        "mercury-mix" -> testMercuryXMix(executor, args.getOrNull(1)?.toIntOrNull() ?: 64)
        "mercury-reverb" -> testMercuryXReverb(executor, args.getOrNull(1)?.toIntOrNull() ?: 3)
        "lvx-mix" -> testLVXMix(executor, args.getOrNull(1)?.toIntOrNull() ?: 64)
        "lvx-time" -> testLVXTime(executor, args.getOrNull(1)?.toIntOrNull() ?: 50)
        "enzo-mix" -> testEnzoXMix(executor, args.getOrNull(1)?.toIntOrNull() ?: 64)
        "program-change" -> testProgramChange(executor, 
            args.getOrNull(1)?.toIntOrNull() ?: 1, 
            args.getOrNull(2)?.toIntOrNull() ?: 0)
        "batch-test" -> testBatchCommands(executor)
        else -> {
            println("❌ Unknown command: ${args[0]}")
            showUsage()
        }
    }
    
    // Close MIDI device when done
    executor.close()
}

fun showUsage() {
    println("""
    MIDI Hardware Test Utility
    
    Usage: mvn exec:java -Dexec.mainClass="com.guyko.SendMidiTestKt" -Dexec.args="<command> [value]"
    
    Commands:
      mercury-mix [0-127]     - Set Mercury X Mix (default: 64 = 50%)
      mercury-reverb [0-7]    - Set Mercury X Reverb Structure (default: 3 = Room)
      lvx-mix [0-127]         - Set LVX Mix (default: 64 = 50%)
      lvx-time [0-127]        - Set LVX Delay Time (default: 50)
      enzo-mix [0-127]        - Set Enzo X Mix (default: 64 = 50%)
      program-change [ch] [prog] - Send Program Change (default: channel 1, program 0)
      batch-test              - Send multiple commands in sequence
    
    Examples:
      mvn exec:java -Dexec.mainClass="com.guyko.SendMidiTestKt" -Dexec.args="mercury-mix 127"
      mvn exec:java -Dexec.mainClass="com.guyko.SendMidiTestKt" -Dexec.args="lvx-time 80"
      mvn exec:java -Dexec.mainClass="com.guyko.SendMidiTestKt" -Dexec.args="program-change 1 5"
    """.trimIndent())
}

fun testMercuryXMix(executor: HardwareMidiExecutor, value: Int) {
    println("🎛️  Testing Mercury X Mix Control")
    val command = MidiCommand(
        channel = 1, // Mercury X on channel 1
        ccNumber = 1, // Mix parameter
        value = value,
        parameterName = "Mix",
        description = "Set Mercury X Mix to ${value * 100 / 127}%"
    )
    executeAndReport(executor, command)
}

fun testMercuryXReverb(executor: HardwareMidiExecutor, value: Int) {
    println("🌊 Testing Mercury X Reverb Structure")
    val reverbNames = arrayOf("Ultraplate", "Cathedra", "Spring", "Room", "Plate", "Hall", "Prism", "Gravity")
    val reverbName = if (value in 0..7) reverbNames[value] else "Unknown"
    
    val command = MidiCommand(
        channel = 1, // Mercury X on channel 1
        ccNumber = 5, // Reverb Structure parameter
        value = value * 16, // Scale 0-7 to 0-112 (approximately)
        parameterName = "Reverb Structure",
        description = "Set Mercury X Reverb to $reverbName"
    )
    executeAndReport(executor, command)
}

fun testLVXMix(executor: HardwareMidiExecutor, value: Int) {
    println("🎛️  Testing LVX Mix Control")
    val command = MidiCommand(
        channel = 2, // LVX on channel 2
        ccNumber = 1, // Mix parameter
        value = value,
        parameterName = "Mix",
        description = "Set LVX Mix to ${value * 100 / 127}%"
    )
    executeAndReport(executor, command)
}

fun testLVXTime(executor: HardwareMidiExecutor, value: Int) {
    println("⏱️  Testing LVX Delay Time")
    val command = MidiCommand(
        channel = 2, // LVX on channel 2
        ccNumber = 15, // Time parameter
        value = value,
        parameterName = "Time",
        description = "Set LVX Delay Time to $value"
    )
    executeAndReport(executor, command)
}

fun testEnzoXMix(executor: HardwareMidiExecutor, value: Int) {
    println("🎛️  Testing Enzo X Mix Control")
    val command = MidiCommand(
        channel = 3, // Enzo X on channel 3
        ccNumber = 1, // Mix parameter
        value = value,
        parameterName = "Mix",
        description = "Set Enzo X Mix to ${value * 100 / 127}%"
    )
    executeAndReport(executor, command)
}

fun testProgramChange(executor: HardwareMidiExecutor, channel: Int, program: Int) {
    println("🔄 Testing Program Change")
    val programChange = MidiProgramChange(
        channel = channel,
        program = program,
        description = "Switch to program $program on channel $channel"
    )
    
    val result = executor.executeProgramChange(programChange)
    println("Channel: $channel, Program: $program")
    println("MIDI bytes: ${programChange.toMidiBytes().joinToString(" ") { "%02X".format(it) }}")
    reportResult(result)
}

fun testBatchCommands(executor: HardwareMidiExecutor) {
    println("📦 Testing Batch Commands")
    val commands = listOf(
        MidiCommand(1, 1, 32, "Mix", "Mercury X Mix to 25%"),
        MidiCommand(2, 1, 96, "Mix", "LVX Mix to 75%"),
        MidiCommand(3, 1, 64, "Mix", "Enzo X Mix to 50%")
    )
    
    val results = executor.executeCommands(commands)
    
    println("Executed ${commands.size} commands:")
    results.forEachIndexed { index, result ->
        println("  ${index + 1}. ${commands[index].description}: ${if (result.success) "✅" else "❌"}")
    }
    
    val successCount = results.count { it.success }
    println("Success rate: $successCount/${commands.size}")
}

fun executeAndReport(executor: HardwareMidiExecutor, command: MidiCommand) {
    println("Channel: ${command.channel}, CC: ${command.ccNumber}, Value: ${command.value}")
    println("MIDI bytes: ${command.toMidiBytes().joinToString(" ") { "%02X".format(it) }}")
    
    val result = executor.executeCommand(command)
    reportResult(result)
}

fun reportResult(result: com.guyko.midi.MidiExecutionResult) {
    if (result.success) {
        println("✅ Success: ${result.message}")
    } else {
        println("❌ Failed: ${result.message}")
    }
    println("Timestamp: ${result.timestamp}")
    println()
}