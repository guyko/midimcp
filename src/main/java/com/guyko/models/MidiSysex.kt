package com.guyko.models

/**
 * Represents a MIDI System Exclusive (sysex) message for sending bulk data like presets
 */
data class MidiSysex(
    val data: ByteArray,
    val description: String? = null,
    val presetName: String? = null
) {
    init {
        require(data.isNotEmpty()) { "Sysex data cannot be empty" }
        require(data[0] == 0xF0.toByte()) { "Sysex must start with 0xF0" }
        require(data.last() == 0xF7.toByte()) { "Sysex must end with 0xF7" }
    }
    
    /**
     * Returns the sysex data as a hex string for display purposes
     */
    fun toHexString(): String {
        return data.joinToString(" ") { "%02X".format(it) }
    }
    
    /**
     * Returns the raw byte array for MIDI transmission
     */
    fun toMidiBytes(): ByteArray = data
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MidiSysex

        if (!data.contentEquals(other.data)) return false
        if (description != other.description) return false
        if (presetName != other.presetName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = data.contentHashCode()
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + (presetName?.hashCode() ?: 0)
        return result
    }
}