package com.guyko.models

data class MidiCommand(
    val channel: Int,
    val ccNumber: Int,
    val value: Int,
    val parameterName: String? = null,
    val description: String? = null
) {
    init {
        require(channel in 1..16) { "MIDI channel must be between 1 and 16" }
        require(ccNumber in 0..127) { "CC number must be between 0 and 127" }
        require(value in 0..127) { "MIDI value must be between 0 and 127" }
    }
    
    fun toMidiBytes(): ByteArray {
        val status = 0xB0 + (channel - 1) // Control Change message
        return byteArrayOf(status.toByte(), ccNumber.toByte(), value.toByte())
    }
}