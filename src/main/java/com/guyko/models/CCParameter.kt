package com.guyko.models

data class CCParameter(
    val name: String,
    val ccNumber: Int,
    val minValue: Int = 0,
    val maxValue: Int = 127,
    val description: String? = null,
    val unit: String? = null,
    val category: String? = null
)