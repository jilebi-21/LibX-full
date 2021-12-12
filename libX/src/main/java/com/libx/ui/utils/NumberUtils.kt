package com.libx.ui.utils

import kotlin.math.pow

fun Float.roundTill(numFractionDigits: Int): Float {
    val factor = 10f.pow(numFractionDigits.toFloat())
    return (this * factor).toInt() / factor
}