package com.guyko

import com.guyko.midi.HardwareMidiExecutor
import com.guyko.pedals.MercuryXPresetGenerator

/**
 * Direct test of Mercury X preset upload functionality
 */
fun main() {
    println("=== 🌊 MERCURY X PRESET UPLOAD TEST ===")
    println()
    
    val executor = HardwareMidiExecutor()
    println("MIDI Status: ${executor.getStatus()}")
    println("MIDI Available: ${executor.isAvailable()}")
    println()
    
    if (!executor.isAvailable()) {
        println("❌ No MIDI device detected!")
        println("Please connect your MIDI interface and guitar pedals.")
        return
    }
    
    // Create a distinctive Mercury X preset
    val parameters = mapOf(
        1 to 100,   // Mix at 80% (very wet)
        5 to 25,    // Cathedra reverb structure  
        11 to 110,  // Very long decay
        12 to 60    // Medium predelay
    )
    
    val presetName = "CONNECTIVITY_TEST"
    
    println("🎛️  Generating Mercury X preset: '$presetName'")
    println("Settings:")
    println("  - Mix: 80% (very wet)")
    println("  - Reverb: Cathedra structure")
    println("  - Decay: Very long")
    println("  - Predelay: Medium")
    println()
    
    try {
        // Generate the preset
        val sysex = MercuryXPresetGenerator.generatePreset(parameters, presetName)
        println("✅ Preset generated successfully!")
        println("   Size: ${sysex.data.size} bytes")
        println("   Sysex: ${sysex.toHexString()}")
        println()
        
        // Upload to pedal
        println("📡 Uploading preset to Mercury X pedal...")
        val result = executor.executeSysex(sysex)
        
        if (result.success) {
            println("✅ PRESET UPLOAD SUCCESSFUL!")
            println("   Status: ${result.message}")
            println("   Bytes transmitted: ${result.bytesTransmitted}")
            println()
            println("🔍 CHECK YOUR MERCURY X PEDAL:")
            println("   1. Look for '$presetName' in your preset list")
            println("   2. Load the preset and test the reverb")
            println("   3. You should hear a very wet, long Cathedra reverb")
            println()
            println("✨ If you can see and use the preset, the upload worked!")
        } else {
            println("❌ PRESET UPLOAD FAILED!")
            println("   Error: ${result.message}")
            println()
            println("🔧 TROUBLESHOOTING:")
            println("   1. Check MIDI cable connections")
            println("   2. Verify Mercury X is on MIDI channel 1")
            println("   3. Ensure Mercury X is powered on")
            println("   4. Check MIDI interface drivers")
        }
        
    } catch (e: Exception) {
        println("❌ ERROR: ${e.message}")
        e.printStackTrace()
    } finally {
        executor.close()
    }
    
    println()
    println("=== TEST COMPLETE ===")
}