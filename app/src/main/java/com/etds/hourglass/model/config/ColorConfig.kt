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
                val b = byteArray[i].toInt() and 0xFF
                val g = byteArray[i + 1].toInt() and 0xFF
                val r = byteArray[i + 2].toInt() and 0xFF
                val a = 0xFF

                colors.add(Color(blue = b, green = g, red = r, alpha = a))
                i += 4
            }
            return ColorConfig(colors.toMutableList())
        }
    }

    fun toByteArray(): ByteArray {
        val byteList = mutableListOf<Byte>()
        for (color in colors) {

            // Color gets sent over as BGR
            byteList.add((color.blue * 255).toInt().toByte())
            byteList.add((color.green * 255).toInt().toByte())
            byteList.add((color.red * 255).toInt().toByte())
            byteList.add(0xFF.toByte()) // Pad the alpha byte
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

    private fun colorString(color: Color): String {
        val r = (color.red * 255).toInt()
        val g = (color.green * 255).toInt()
        val b = (color.blue * 255).toInt()
        // val a = (color.alpha * 255).toInt()
        return "#%02X%02X%02X".format(r, g, b)
    }

    override fun toString(): String {
        return "ColorConfig([${colorString(colors[0])}, ${colorString(colors[1])}, ${colorString(colors[2])}, ${colorString(colors[3])}])"
    }
}
