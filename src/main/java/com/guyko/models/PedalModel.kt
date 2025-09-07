package com.guyko.models

data class PedalModel(
    val id: String,
    val manufacturer: String,
    val modelName: String,
    val version: String? = null,
    val midiChannel: Int = 1,
    val parameters: List<CCParameter> = emptyList(),
    val description: String? = null
) {
    fun getParameterByCC(ccNumber: Int): CCParameter? {
        return parameters.find { it.ccNumber == ccNumber }
    }
    
    fun getParameterByName(name: String): CCParameter? {
        return parameters.find { it.name.equals(name, ignoreCase = true) }
    }
    
    fun getParametersByCategory(category: String): List<CCParameter> {
        return parameters.filter { it.category?.equals(category, ignoreCase = true) == true }
    }
}