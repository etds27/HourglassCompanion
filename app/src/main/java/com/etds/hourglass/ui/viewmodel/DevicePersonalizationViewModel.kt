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
import androidx.compose.runtime.currentComposer
import com.etds.hourglass.lib.rate_limiter.DebouncedRateLimiter
import com.etds.hourglass.model.Device.BLEDevice
import com.etds.hourglass.model.Device.DeviceConnectionState
import com.etds.hourglass.model.DeviceState.DeviceState
import com.etds.hourglass.model.config.ColorConfig
import com.etds.hourglass.model.config.approxEquals
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

// Helper object to fetch initial device properties for the ViewModel constructor
private object DevicePropertiesHelper {
    fun findDevice(repo: BuzzerGameRepository, handle: SavedStateHandle): GameDevice {
        val deviceId: String =
            checkNotNull(handle["deviceId"]) { "deviceId not found in SavedStateHandle" }
        return repo.players.value.firstOrNull { it.device.address == deviceId }?.device
            ?: throw IllegalStateException("Device with ID $deviceId not found in repository")
    }

    fun getInitialName(repo: BuzzerGameRepository, handle: SavedStateHandle): String {
        return findDevice(repo, handle).name.value
    }

    fun getInitialColorConfig(repo: BuzzerGameRepository, handle: SavedStateHandle): ColorConfig {
        // Assuming your GameDevice.color is StateFlow<Color>
        return findDevice(repo, handle).colorConfig.value
    }

    fun getInitialConnectionState(
        repo: BuzzerGameRepository,
        handle: SavedStateHandle
    ): DeviceConnectionState {
        return findDevice(repo, handle).connectionState.value
    }

    fun getInitialLEDCount(repo: BuzzerGameRepository, handle: SavedStateHandle): Int {
        return findDevice(repo, handle).ledCount.value
    }

    fun getInitialLEDOffset(repo: BuzzerGameRepository, handle: SavedStateHandle): Int {
        return findDevice(repo, handle).ledOffset.value
    }
}

interface DevicePersonalizationViewModelProtocol {
    val device: GameDevice // The actual device instance, useful for direct operations if needed by UI
    val deviceConnectionState: StateFlow<DeviceConnectionState> // Used to detect disconnects and exit from editor
    val deviceName: StateFlow<String> // Local UI state for name
    val editingDeviceName: StateFlow<String> // Local UI state for name being edited (e.g. in a TextField)
    val deviceColorConfig: StateFlow<ColorConfig> // Local UI state for color
    val deviceConfigState: StateFlow<DeviceState> // Local Device State config
    val ledOffset: StateFlow<Int>
    val ledCount: StateFlow<Int>
    val personalizationHasChanged: StateFlow<Boolean>
    var originalDeviceProperties: DevicePersonalizationConfig // Snapshot of properties for reset functionality

    /// State flow to represent when a color config is being loaded from the device
    val isLoadingConfig: StateFlow<Boolean>


    fun setDeviceName(name: String) // Sets the confirmed device name
    fun setEditingDeviceName(name: String) // Updates the name as user types

    // Suspend so that we can rate limit when many are sent out at once
    fun setDeviceColorConfig(colorConfig: ColorConfig)
    fun setDeviceConfigColor(color: Color, index: Int)
    fun setDeviceConfigState(deviceState: DeviceState)
    fun setDeviceLEDOffset(offset: Int)
    fun setDeviceLEDCount(count: Int)

    fun updateDeviceProperties() // Saves current local UI changes to the actual device and updates original snapshot
    fun resetDeviceProperties() // Resets local UI changes to the last saved original snapshot
    fun saveOriginalDeviceProperties() // Updates the original snapshot to current local UI state
    fun setOriginalDeviceProperties(
        name: String? = null,
        colorConfig: ColorConfig? = null,
        deviceState: DeviceState? = null,
        ledOffset: Int? = null,
        ledCount: Int? = null
    ) // Updates the original snapshot to set values or the local UI state

    fun onNavigate() // Handles navigation events, potentially saving state
    fun onNavigateToLaunchPage() // Handles cleaning up before reverting to launch page
    fun increaseLEDOffset() // Increase the LED offset by one
    fun decreaseLEDOffset() // Decrease the LED offset by one
    fun increaseLEDCount() // Increase the LED count by one
    fun decreaseLEDCount() // Decrease the LED count by one
}

abstract class BaseDevicePersonalizationViewModel(
    initialDeviceName: String,
    initialColorConfig: ColorConfig,
    initialConnectionState: DeviceConnectionState = DeviceConnectionState.Connected,
    initialLEDOffset: Int,
    initialLEDCount: Int
) : ViewModel(), DevicePersonalizationViewModelProtocol {

    // `device` is still abstract and will be implemented by concrete ViewModels
    // for their specific needs, like in `updateDeviceProperties` to talk to the repository.

    protected val mutableDeviceConnectionState: StateFlow<DeviceConnectionState> =
        MutableStateFlow(initialConnectionState)
    override var deviceConnectionState: StateFlow<DeviceConnectionState> =
        mutableDeviceConnectionState

    protected val mutableDeviceName: MutableStateFlow<String> = MutableStateFlow(initialDeviceName)
    override val deviceName: StateFlow<String> = mutableDeviceName

    protected val mutableLEDOffset: MutableStateFlow<Int> = MutableStateFlow(initialLEDOffset)
    override val ledOffset: StateFlow<Int> = mutableLEDOffset

    protected val mutableLEDCount: MutableStateFlow<Int> = MutableStateFlow(initialLEDCount)
    override val ledCount: StateFlow<Int> = mutableLEDCount

    // Initialize editingDeviceName based on the initial name, applying length constraints
    protected val mutableEditingDeviceName: MutableStateFlow<String> =
        MutableStateFlow(initialDeviceName)
    override val editingDeviceName: StateFlow<String> = mutableEditingDeviceName

    protected val mutableDeviceColorConfig: MutableStateFlow<ColorConfig> =
        MutableStateFlow(initialColorConfig)
    override val deviceColorConfig: StateFlow<ColorConfig> = mutableDeviceColorConfig

    protected val mutableDeviceConfigState: MutableStateFlow<DeviceState> =
        MutableStateFlow(DeviceState.DeviceColorMode)
    override val deviceConfigState: StateFlow<DeviceState> = mutableDeviceConfigState

    protected val mutableIsLoadingConfig: MutableStateFlow<Boolean> = MutableStateFlow(false)
    override val isLoadingConfig: StateFlow<Boolean> = mutableIsLoadingConfig

    protected val forceUpdate: MutableStateFlow<Boolean> =
        MutableStateFlow(true) // Flow to force the `hasPersonalizationChanged value to update

    protected val colorConfigRateLimiter: DebouncedRateLimiter = DebouncedRateLimiter(
        minInterval = BLEDevice.defaultBLERequestDelay.toDuration(
            DurationUnit.MILLISECONDS
        ), scope = viewModelScope
    )


    // `originalDeviceProperties` now correctly initialized with values from the actual device
    override var originalDeviceProperties: DevicePersonalizationConfig =
        DevicePersonalizationConfig(
            initialDeviceName,
            initialColorConfig,
            DeviceState.DeviceColorMode,
            initialLEDOffset,
            initialLEDCount
        )

    override val personalizationHasChanged: StateFlow<Boolean> = combine(
        deviceName, // Compares the confirmed name
        deviceColorConfig,
        deviceConfigState,
        ledOffset,
        forceUpdate
    ) { name, colorConfig, deviceStateConfig, offset, update ->
        val nameChanged = name != originalDeviceProperties.name
        val deviceStateChanged = deviceStateConfig != originalDeviceProperties.deviceState
        val colorsChanged = colorConfig.colors.zip(originalDeviceProperties.colorConfig.colors)
            .any { !it.first.approxEquals(it.second) }
        val offsetChanged = offset != originalDeviceProperties.ledOffset
        nameChanged || deviceStateChanged || colorsChanged || offsetChanged
    }.combine(
        ledCount
    ) { hasChanged, ledCount ->
        val countChanged = ledCount != originalDeviceProperties.ledCount
        hasChanged || countChanged
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    override fun resetDeviceProperties() {
        setDeviceName(originalDeviceProperties.name)
        setEditingDeviceName(originalDeviceProperties.name) // Also reset the editing field
        setDeviceColorConfig(originalDeviceProperties.colorConfig)
        setDeviceConfigState(originalDeviceProperties.deviceState)
        setLEDCount(originalDeviceProperties.ledCount)
        setLEDOffset(originalDeviceProperties.ledOffset)
    }

    override fun onNavigate() {
        // This behavior means navigating away stages current edits as the new "original".
        // Consider if this is desired, or if changes should be explicitly saved/reset.
        saveOriginalDeviceProperties()
        setDeviceConfigState(DeviceState.DeviceColorMode)
        mutableLEDCount.value = device.ledCount.value
        mutableLEDOffset.value = device.ledOffset.value
        device.setDeviceState(DeviceState.ConfigurationMode)
        deviceConnectionState = device.connectionState


    }

    override fun onNavigateToLaunchPage() {
        device.setDeviceState(DeviceState.AwaitingGameStart)
    }

    /**
     * This base implementation updates the `originalDeviceProperties` snapshot.
     * Concrete classes should call this and then perform actual device/repository updates.
     */
    override fun updateDeviceProperties() {
        // Before pushing to device, ensure the main deviceName is updated from editingDeviceName
        // Assuming save means the editing name is the new confirmed name
        setDeviceName(editingDeviceName.value)
        // saveOriginalDeviceProperties() // Update the baseline to the current (now saved) state
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
        mutableEditingDeviceName.value = name.substring(0, min(16, name.count()))
    }

    override fun setDeviceColorConfig(colorConfig: ColorConfig) {
        mutableDeviceColorConfig.value = colorConfig
    }

    override fun setDeviceLEDOffset(offset: Int) {
        mutableLEDOffset.value = offset
    }

    override fun setDeviceLEDCount(count: Int) {
        mutableLEDCount.value = count
    }

    override fun setDeviceConfigColor(
        color: Color,
        index: Int
    ) {
        mutableDeviceColorConfig.update { current ->
            val newColors = current.colors.toMutableList()
            newColors[index] = color
            current.copy(colors = newColors)
        }
    }

    override fun decreaseLEDOffset() {
        changeLEDOffset(-1)
    }

    override fun increaseLEDOffset() {
        changeLEDOffset(1)
    }

    protected open fun changeLEDOffset(value: Int) {
        setLEDOffset(mutableLEDOffset.value + value)
    }

    protected open fun setLEDOffset(value: Int) {
        mutableLEDOffset.value = (value + ledCount.value) % ledCount.value
    }

    override fun decreaseLEDCount() {
        changeLEDCount(-1)
    }

    override fun increaseLEDCount() {
        changeLEDCount(1)
    }

    protected open fun changeLEDCount(value: Int) {
        setLEDCount(mutableLEDCount.value + value)
    }

    protected open fun setLEDCount(value: Int) {
        if (value > 0) {
            mutableLEDCount.value = value
        }
    }

    override fun setDeviceConfigState(deviceState: DeviceState) {
        // This indicates a change in the device config state.
        // When connected to an actual device, what we need to do is write the change in state
        // Then wait for the device to send over the updated states config
        // Then we can load and populate the view model

        mutableDeviceConfigState.value = deviceState

        whileLoading {
            val colorConfig = device.performColorConfigRetrieval(deviceState)

            setOriginalDeviceProperties(colorConfig = colorConfig, deviceState = deviceState)
            // setDeviceColorConfig(colorConfig)
            mutableDeviceColorConfig.value = colorConfig
            mutableDeviceConfigState.value = deviceState
            forceUpdate.value = !forceUpdate.value
        }
    }

    /**
     * Updates the `originalDeviceProperties` snapshot to reflect the current local UI state.
     * This is called when changes are saved or when navigating away (per current onNavigate logic).
     */
    override fun saveOriginalDeviceProperties() {
        Log.d(TAG, "Saving original device properties")
        originalDeviceProperties = DevicePersonalizationConfig(
            deviceName.value, // Use the confirmed deviceName
            deviceColorConfig.value,
            deviceConfigState.value,
            ledOffset.value,
            ledCount.value
        )

        forceUpdate.value = !forceUpdate.value
        // No need to set mutableDeviceName here, it's already the source for deviceName.value
    }

    override fun setOriginalDeviceProperties(
        name: String?,
        colorConfig: ColorConfig?,
        deviceState: DeviceState?,
        ledOffset: Int?,
        ledCount: Int?
    ) {
        originalDeviceProperties = DevicePersonalizationConfig(
            name ?: originalDeviceProperties.name,
            colorConfig ?: originalDeviceProperties.colorConfig,
            deviceState ?: originalDeviceProperties.deviceState,
            ledOffset ?: originalDeviceProperties.ledOffset,
            ledCount ?: originalDeviceProperties.ledCount
        )
    }

    fun whileLoading(func: suspend () -> Unit) {
        mutableIsLoadingConfig.value = true
        viewModelScope.launch {
            func()
            mutableIsLoadingConfig.value = false
        }
    }

    companion object {
        const val TAG = "DevicePersonalizationViewModel"
    }
}

@HiltViewModel
class DevicePersonalizationViewModel @Inject constructor(
    private val gameRepository: BuzzerGameRepository,
    private val savedStateHandle: SavedStateHandle
) : BaseDevicePersonalizationViewModel(
    initialDeviceName = DevicePropertiesHelper.getInitialName(gameRepository, savedStateHandle),
    initialColorConfig = DevicePropertiesHelper.getInitialColorConfig(
        gameRepository,
        savedStateHandle
    ),
    initialLEDCount = DevicePropertiesHelper.getInitialLEDCount(gameRepository, savedStateHandle),
    initialLEDOffset = DevicePropertiesHelper.getInitialLEDOffset(gameRepository, savedStateHandle)
), DevicePersonalizationViewModelProtocol {
    // This 'device' instance is specific to DevicePersonalizationViewModel,
    // initialized after the base class, and used for repository interactions.
    override val device: GameDevice =
        DevicePropertiesHelper.findDevice(gameRepository, savedStateHandle)

    override fun setDeviceConfigColor(color: Color, index: Int) {
        super.setDeviceConfigColor(color, index)
        viewModelScope.launch {
            colorConfigRateLimiter.run {
                // Send the color config to the BLE device but don't write to it
                gameRepository.updateDeviceColorConfig(device, mutableDeviceColorConfig.value)
            }
        }
    }

    override fun setDeviceColorConfig(colorConfig: ColorConfig) {
        super.setDeviceColorConfig(colorConfig)
        viewModelScope.launch {
            colorConfigRateLimiter.run {
                // Send the color config to the BLE device but don't write to it
                gameRepository.updateDeviceColorConfig(device, mutableDeviceColorConfig.value)
            }
        }
    }

    override fun setLEDOffset(value: Int) {
        super.setLEDOffset(value)
        Log.d(TAG, "Setting LED offset to $value")
        viewModelScope.launch {
            gameRepository.updateDeviceLEDOffset(device = device, offset = ledOffset.value)
        }
    }

    override fun setLEDCount(value: Int) {
        super.setLEDCount(value)
        Log.d(TAG, "Setting LED count to $value")
        viewModelScope.launch {
            gameRepository.updateDeviceLEDCount(device = device, count = ledCount.value)
        }
    }

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
        Log.d(
            "DevicePersonalizationVM",
            "Updating device properties in repository for ${device.address} with name: ${editingDeviceName.value}"
        )
        gameRepository.updateDevicePersonalizationSettings(
            device = device,
            settings = DevicePersonalizationConfig(
                mutableDeviceName.value, // This is the value from the text field
                deviceColorConfig.value,       // This is the value from the color picker
                deviceConfigState.value,  // This is the value from the accent color picker
                ledOffset.value,
                ledCount.value
            ),
            originalSettings = originalDeviceProperties
        )

        saveOriginalDeviceProperties()
        forceUpdate.value = !forceUpdate.value
    }
}

class MockDevicePersonalizationViewModel(override val device: GameDevice) :
    BaseDevicePersonalizationViewModel(
        initialDeviceName = device.name.value,
        initialColorConfig = device.colorConfig.value,
        initialLEDCount = device.ledCount.value,
        initialLEDOffset = device.ledOffset.value
    ), DevicePersonalizationViewModelProtocol {
    // Mock-specific implementations or overrides if needed.
    // For now, it relies on the base class behavior after proper initialization.

}

