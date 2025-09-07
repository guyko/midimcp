package com.guyko.models

data class MidiProgramChange(
    val channel: Int,
    val program: Int,
    val description: String? = null
) {
    init {
        require(channel in 1..16) { "MIDI channel must be between 1 and 16" }
        require(program in 0..127) { "Program number must be between 0 and 127" }
    }
    
    fun toMidiBytes(): ByteArray {
        val status = 0xC0 + (channel - 1) // Program Change message
        return byteArrayOf(status.toByte(), program.toByte())
    }
}