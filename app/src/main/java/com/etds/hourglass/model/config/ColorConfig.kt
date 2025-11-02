package com.etds.hourglass.model.config

import androidx.compose.ui.graphics.Color
import kotlin.math.absoluteValue

fun Color.approxEquals(other: Color, epsilon: Float = 0.001f): Boolean {
    return (red - other.red).absoluteValue < epsilon &&
            (green - other.green).absoluteValue < epsilon &&
            (blue - other.blue).absoluteValue < epsilon &&
            (alpha - other.alpha).absoluteValue < epsilon
}

data class ColorConfig(
    val colors: MutableList<Color> = mutableListOf(
        Color.Red,
        Color.Green,
        Color.Blue,
        Color.Yellow
    )
) {

    companion object {
        fun fromByteArray(byteArray: ByteArray): ColorConfig {
            val colors = mutableListOf<Color>()
            var i = 0
            while (i + 3 < byteArray.size) {
                val r = byteArray[i].toInt() and 0xFF
                val g = byteArray[i + 1].toInt() and 0xFF
                val b = byteArray[i + 2].toInt() and 0xFF
                val a = byteArray[i + 3].toInt() and 0xFF

                colors.add(Color(red = r, green = g, blue = b, alpha = a))
                i += 4
            }
            return ColorConfig(colors.toMutableList())
        }
    }

    fun toByteArray(): ByteArray {
        val byteList = mutableListOf<Byte>()
        for (color in colors) {

            byteList.add(color.red.toInt().toByte())
            byteList.add(color.green.toInt().toByte())
            byteList.add(color.blue.toInt().toByte())
            byteList.add(color.alpha.toInt().toByte())
        }
        return byteList.toByteArray()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ColorConfig

        return colors == other.colors
    }

    override fun hashCode(): Int {
        return colors.hashCode()
    }
}
