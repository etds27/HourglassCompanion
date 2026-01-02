package com.etds.hourglass.util

fun <T> List<T>.rotateRight(k: Int): List<T> {
    if (isEmpty()) return this
    val shift = ((k % size) + size) % size
    return drop(size - shift) + take(size - shift)
}