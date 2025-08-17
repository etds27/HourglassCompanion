package com.etds.hourglass.ui.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.etds.hourglass.data.game.BuzzerGameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import kotlin.math.min


interface DevicePersonalizationViewModelProtocol {
    val deviceName: StateFlow<String>
    val editingDeviceName: StateFlow<String>
    val deviceColor: StateFlow<Color>
    val deviceAccentColor: StateFlow<Color>
    val personalizationHasChanged: StateFlow<Boolean>
    var originalDeviceProperties: DevicePersonalizationUiState

    fun setDeviceName(name: String)
    fun setEditingDeviceName(name: String)
    fun setDeviceColor(color: Color)
    fun setDeviceAccentColor(color: Color)
    fun updateDeviceProperties()
    fun resetDeviceProperties()
    fun saveOriginalDeviceProperties()
    fun onNavigate()

}

data class DevicePersonalizationUiState(
    val name: String,
    val color: Color,
    val accentColor: Color
)

@HiltViewModel
class DevicePersonalizationViewModel @Inject constructor(
    private val gameRepository: BuzzerGameRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel(), DevicePersonalizationViewModelProtocol {
    private val deviceId: String = checkNotNull(savedStateHandle["deviceId"])


    private val device = gameRepository.players.value.first { it.device.address == deviceId }.device
    override val deviceName = device.name

    private val mutableEditingDeviceName: MutableStateFlow<String> = MutableStateFlow(deviceName.value)
    override val editingDeviceName: StateFlow<String> = mutableEditingDeviceName

    override val deviceColor = device.color
    override val deviceAccentColor = device.accentColor
    override val personalizationHasChanged: StateFlow<Boolean> = combine(
        deviceName,
        deviceColor,
        deviceAccentColor
    ) { name, color, accentColor ->
        DevicePersonalizationUiState(name, color, accentColor) != originalDeviceProperties
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false)

    override var originalDeviceProperties: DevicePersonalizationUiState = DevicePersonalizationUiState(
        deviceName.value,
        deviceColor.value,
        deviceAccentColor.value
    )

    override fun setDeviceName(name: String) {
        TODO("Not yet implemented")
    }

    override fun setEditingDeviceName(name: String) {
        TODO("Not yet implemented")
    }

    override fun setDeviceColor(color: Color) {
        TODO("Not yet implemented")
    }

    override fun setDeviceAccentColor(color: Color) {
        TODO("Not yet implemented")
    }

    override fun updateDeviceProperties() {
        TODO("Not yet implemented")
    }

    override fun resetDeviceProperties() {
        TODO("Not yet implemented")
    }

    override fun saveOriginalDeviceProperties() {
        TODO("Not yet implemented")
    }

    override fun onNavigate() {
        TODO("Not yet implemented")
    }
}

class MockDevicePersonalizationViewModel() : ViewModel(), DevicePersonalizationViewModelProtocol {
    private val mutableDeviceName: MutableStateFlow<String> = MutableStateFlow("Mock Device")
    override val deviceName: StateFlow<String> = mutableDeviceName

    private val mutableEditingDeviceName: MutableStateFlow<String> = MutableStateFlow(deviceName.value)
    override val editingDeviceName: StateFlow<String> = mutableEditingDeviceName

    private val mutableDeviceColor: MutableStateFlow<Color> = MutableStateFlow(Color.Red)
    override val deviceColor: StateFlow<Color> = mutableDeviceColor

    private val mutableDeviceAccentColor: MutableStateFlow<Color> = MutableStateFlow(Color.Blue)
    override val deviceAccentColor: StateFlow<Color> = mutableDeviceAccentColor

    override val personalizationHasChanged: StateFlow<Boolean> = combine(
        deviceName,
        deviceColor,
        deviceAccentColor
    ) { name, color, accentColor ->
        DevicePersonalizationUiState(name, color, accentColor) != originalDeviceProperties
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false)

    override var originalDeviceProperties: DevicePersonalizationUiState = DevicePersonalizationUiState(
        deviceName.value,
        deviceColor.value,
        deviceAccentColor.value
    )

    override fun setDeviceName(name: String) {
        mutableDeviceName.value = name
    }

    override fun setEditingDeviceName(name: String) {
        mutableEditingDeviceName.value = name.substring(0, min(8, name.count()))
    }

    override fun setDeviceColor(color: Color) {
        mutableDeviceColor.value = color
    }

    override fun setDeviceAccentColor(color: Color) {
        mutableDeviceAccentColor.value = color
    }

    override fun updateDeviceProperties() {
        saveOriginalDeviceProperties()
    }

    override fun resetDeviceProperties() {
        setDeviceName(originalDeviceProperties.name)
        setEditingDeviceName(originalDeviceProperties.name)
        setDeviceColor(originalDeviceProperties.color)
        setDeviceAccentColor(originalDeviceProperties.accentColor)
    }

    override fun saveOriginalDeviceProperties() {
        originalDeviceProperties = DevicePersonalizationUiState(
            deviceName.value,
            deviceColor.value,
            deviceAccentColor.value
        )
        mutableDeviceName.value = originalDeviceProperties.name
    }

    override fun onNavigate() {
        saveOriginalDeviceProperties()
    }
}