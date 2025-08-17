package com.etds.hourglass.ui.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.etds.hourglass.data.game.BuzzerGameRepository
// import com.etds.hourglass.data.game.GameRepository // Assuming BuzzerGameRepository is the concrete type needed
import com.etds.hourglass.model.Device.DevicePersonalizationConfig
import com.etds.hourglass.model.Device.GameDevice
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import kotlin.math.min
import android.util.Log // Added import for Log

// Helper object to fetch initial device properties for the ViewModel constructor
private object DevicePropertiesHelper {
    fun findDevice(repo: BuzzerGameRepository, handle: SavedStateHandle): GameDevice {
        val deviceId: String = checkNotNull(handle["deviceId"]) { "deviceId not found in SavedStateHandle" }
        return repo.players.value.firstOrNull { it.device.address == deviceId }?.device
            ?: throw IllegalStateException("Device with ID $deviceId not found in repository")
    }

    fun getInitialName(repo: BuzzerGameRepository, handle: SavedStateHandle): String {
        return findDevice(repo, handle).name.value
    }

    fun getInitialColor(repo: BuzzerGameRepository, handle: SavedStateHandle): Color {
        // Assuming your GameDevice.color is StateFlow<Color>
        return findDevice(repo, handle).color.value
    }

    fun getInitialAccentColor(repo: BuzzerGameRepository, handle: SavedStateHandle): Color {
        // Assuming your GameDevice.accentColor is StateFlow<Color>
        return findDevice(repo, handle).accentColor.value
    }
}

interface DevicePersonalizationViewModelProtocol {
    val device: GameDevice // The actual device instance, useful for direct operations if needed by UI
    val deviceName: StateFlow<String> // Local UI state for name
    val editingDeviceName: StateFlow<String> // Local UI state for name being edited (e.g. in a TextField)
    val deviceColor: StateFlow<Color> // Local UI state for color
    val deviceAccentColor: StateFlow<Color> // Local UI state for accent color
    val personalizationHasChanged: StateFlow<Boolean>
    var originalDeviceProperties: DevicePersonalizationConfig // Snapshot of properties for reset functionality

    fun setDeviceName(name: String) // Sets the confirmed device name
    fun setEditingDeviceName(name: String) // Updates the name as user types
    fun setDeviceColor(color: Color)
    fun setDeviceAccentColor(color: Color)
    fun updateDeviceProperties() // Saves current local UI changes to the actual device and updates original snapshot
    fun resetDeviceProperties() // Resets local UI changes to the last saved original snapshot
    fun saveOriginalDeviceProperties() // Updates the original snapshot to current local UI state
    fun onNavigate() // Handles navigation events, potentially saving state
}

abstract class BaseDevicePersonalizationViewModel(
    initialDeviceName: String,
    initialDeviceColor: Color,
    initialDeviceAccentColor: Color
) : ViewModel(), DevicePersonalizationViewModelProtocol {

    // `device` is still abstract and will be implemented by concrete ViewModels
    // for their specific needs, like in `updateDeviceProperties` to talk to the repository.

    protected val mutableDeviceName: MutableStateFlow<String> = MutableStateFlow(initialDeviceName)
    override val deviceName: StateFlow<String> = mutableDeviceName

    // Initialize editingDeviceName based on the initial name, applying length constraints
    protected val mutableEditingDeviceName: MutableStateFlow<String> =
        MutableStateFlow(initialDeviceName.substring(0, min(8, initialDeviceName.count())))
    override val editingDeviceName: StateFlow<String> = mutableEditingDeviceName

    protected val mutableDeviceColor: MutableStateFlow<Color> = MutableStateFlow(initialDeviceColor)
    override val deviceColor: StateFlow<Color> = mutableDeviceColor

    protected val mutableDeviceAccentColor: MutableStateFlow<Color> = MutableStateFlow(initialDeviceAccentColor)
    override val deviceAccentColor: StateFlow<Color> = mutableDeviceAccentColor

    // `originalDeviceProperties` now correctly initialized with values from the actual device
    override var originalDeviceProperties: DevicePersonalizationConfig = DevicePersonalizationConfig(
        initialDeviceName,
        initialDeviceColor,
        initialDeviceAccentColor
    )

    override val personalizationHasChanged: StateFlow<Boolean> = combine(
        deviceName, // Compares the confirmed name
        deviceColor,
        deviceAccentColor
    ) { name, color, accentColor ->
        DevicePersonalizationConfig(name, color, accentColor) != originalDeviceProperties
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    override fun resetDeviceProperties() {
        setDeviceName(originalDeviceProperties.name)
        setEditingDeviceName(originalDeviceProperties.name) // Also reset the editing field
        setDeviceColor(originalDeviceProperties.color)
        setDeviceAccentColor(originalDeviceProperties.accentColor)
    }

    override fun onNavigate() {
        // This behavior means navigating away stages current edits as the new "original".
        // Consider if this is desired, or if changes should be explicitly saved/reset.
        saveOriginalDeviceProperties()
    }

    /**
     * This base implementation updates the `originalDeviceProperties` snapshot.
     * Concrete classes should call this and then perform actual device/repository updates.
     */
    override fun updateDeviceProperties() {
        // Before pushing to device, ensure the main deviceName is updated from editingDeviceName
        // Assuming save means the editing name is the new confirmed name
        setDeviceName(editingDeviceName.value)
        saveOriginalDeviceProperties() // Update the baseline to the current (now saved) state
    }

    override fun setDeviceName(name: String) {
        // This updates the "confirmed" name.
        // The editing name might be different until saved.
        mutableDeviceName.value = name
        // Also ensure editing name reflects this if they are meant to be in sync after a save
        // If editingDeviceName is bound to a TextField, this might not be necessary if updateDeviceProperties handles it.
        // mutableEditingDeviceName.value = name.substring(0, min(8, name.count())) // Keep consistent
    }

    override fun setEditingDeviceName(name: String) {
        // This updates the name as the user types, with length constraint.
        mutableEditingDeviceName.value = name.substring(0, min(8, name.count()))
    }

    override fun setDeviceColor(color: Color) {
        mutableDeviceColor.value = color
    }

    override fun setDeviceAccentColor(color: Color) {
        mutableDeviceAccentColor.value = color
    }

    /**
     * Updates the `originalDeviceProperties` snapshot to reflect the current local UI state.
     * This is called when changes are saved or when navigating away (per current onNavigate logic).
     */
    override fun saveOriginalDeviceProperties() {
        originalDeviceProperties = DevicePersonalizationConfig(
            deviceName.value, // Use the confirmed deviceName
            deviceColor.value,
            deviceAccentColor.value
        )
        // No need to set mutableDeviceName here, it's already the source for deviceName.value
    }
}

@HiltViewModel
class DevicePersonalizationViewModel @Inject constructor(
    private val gameRepository: BuzzerGameRepository,
    private val savedStateHandle: SavedStateHandle
) : BaseDevicePersonalizationViewModel(
    initialDeviceName = DevicePropertiesHelper.getInitialName(gameRepository, savedStateHandle),
    initialDeviceColor = DevicePropertiesHelper.getInitialColor(gameRepository, savedStateHandle),
    initialDeviceAccentColor = DevicePropertiesHelper.getInitialAccentColor(gameRepository, savedStateHandle)
), DevicePersonalizationViewModelProtocol {
    // This 'device' instance is specific to DevicePersonalizationViewModel,
    // initialized after the base class, and used for repository interactions.
    override val device: GameDevice = DevicePropertiesHelper.findDevice(gameRepository, savedStateHandle)

    /**
     * Saves the current UI personalization settings to the actual device via the repository.
     * It first calls the base method to update the `originalDeviceProperties` snapshot.
     */
    override fun updateDeviceProperties() {
        // Important: Ensure the main deviceName reflects the final edited name *before* saving original properties
        // The base `updateDeviceProperties` will call `setDeviceName(editingDeviceName.value)` and then `saveOriginalDeviceProperties`.
        super.updateDeviceProperties() // This updates originalDeviceProperties to current UI state

        // Now, push the *current* UI state (which is now also the new original state) to the repository.
        // `editingDeviceName.value` is used as it's the most up-to-date from the UI.
        // `deviceColor.value` and `deviceAccentColor.value` are directly from the UI pickers.
        Log.d("DevicePersonalizationVM", "Updating device properties in repository for ${device.address} with name: ${editingDeviceName.value}")
        gameRepository.updateDevicePersonalizationSettings(
            device,
            DevicePersonalizationConfig(
                editingDeviceName.value, // This is the value from the text field
                deviceColor.value,       // This is the value from the color picker
                deviceAccentColor.value  // This is the value from the accent color picker
            )
        )
    }
}

class MockDevicePersonalizationViewModel(override val device: GameDevice) : BaseDevicePersonalizationViewModel(
    initialDeviceName = device.name.value,
    initialDeviceColor = device.color.value,
    initialDeviceAccentColor = device.accentColor.value
), DevicePersonalizationViewModelProtocol {
    // Mock-specific implementations or overrides if needed.
    // For now, it relies on the base class behavior after proper initialization.
}

