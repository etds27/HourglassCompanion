package com.etds.hourglass.model.Device

data class BLEDevice(
    override val name: String = "",
    override val address: String = "",
    val deviceMac: String = ""
): GameDevice {}
