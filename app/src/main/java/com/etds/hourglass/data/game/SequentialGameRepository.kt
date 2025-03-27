package com.etds.hourglass.data.game

import android.util.Log
import com.etds.hourglass.data.BLEData.remote.BLERemoteDatasource
import com.etds.hourglass.data.game.local.LocalGameDatasource
import com.etds.hourglass.model.DeviceState.DeviceState
import com.etds.hourglass.model.Player.Player
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import kotlin.math.min

class SequentialGameRepository @Inject constructor(
    private val localGameDatasource: LocalGameDatasource,
    private val bluetoothDatasource: BLERemoteDatasource,
    private val scope: CoroutineScope
) : GameRepository(
    localGameDatasource = localGameDatasource,
    bluetoothDatasource = bluetoothDatasource,
    scope = scope
) {

    private val _defaultActivePlayerIndex: Int = 0
    private val _defaultEnforceTotalTimer: Boolean = false


    private val _activePlayerIndex = MutableStateFlow(_defaultActivePlayerIndex)
    private val activePlayerIndex: StateFlow<Int> = _activePlayerIndex

    private val _activePlayer = MutableStateFlow<Player?>(null)
    val activePlayer: StateFlow<Player?> = _activePlayer

    private val _enforceTotalTimer = MutableStateFlow(_defaultEnforceTotalTimer)
    val enforceTotalTimer: StateFlow<Boolean> = _enforceTotalTimer


    fun setTotalTurnTimerEnforced() {
        _enforceTotalTimer.value = true
    }

    fun setTotalTurnTimerNotEnforced() {
        _enforceTotalTimer.value = false
    }

    override fun setDeviceCallbacks(player: Player) {
        player.setDeviceOnActiveTurnCallback { playerValue: Player, newValue: Boolean ->
            onPlayerActiveTurnChange(playerValue, newValue)
        }
        super.setDeviceCallbacks(player)
    }


    override fun pauseGame() {
        _activePlayer.value = null
        super.pauseGame()
    }

    override fun resumeGame() {
        Log.d(TAG, "resumeGame: Resuming game with player: ${activePlayerIndex.value}")
        // updateActivePlayer()
        // The active player is manually set here so that no BT updates are sent out when unpausing

        _activePlayer.value = mutablePlayers.value[_activePlayerIndex.value]
        super.resumeGame()
    }

    override fun setSkippedPlayer(player: Player) {
        super.setSkippedPlayer(player)

        Log.d(TAG, "Skipped Player: ${player.name}, Active Player: ${activePlayer.value!!.name}")
        if (player == activePlayer.value) {
            Log.d(TAG, "Advancing to next player")
            nextPlayer()
        }
        checkAllSkipped()
    }

    // MARK: Turn Sequencing / Active Player Handling
    private fun getActivePlayer(): Player {
        return mutablePlayers.value[activePlayerIndex.value]
    }

    private fun setActivePlayerIndex(index: Int) {
        // Reset previous player
        _activePlayer.value?.let {
            _activePlayer.value!!.device.writeElapsedTime(0L)
        }

        val previousPlayer = _activePlayer.value
        // Update to next player
        _activePlayerIndex.value = index
        _activePlayer.value = players.value[index]

        // First update the previous active player. This will provide the most responsive feedback
        // for the most recent action
        previousPlayer?.let {
            updatePlayerDevice(previousPlayer)
        }

        // Then update the new active player to reduce dead time between turns
        _activePlayer.value?.let {
            updatePlayerDevice(it)
        }

        // Then update the turn sequences for all players
        players.value.forEach {
            it.device.writeCurrentPlayer(activePlayerIndex.value)
        }
    }

    private fun updateActivePlayer() {
        setActivePlayerIndex(activePlayerIndex.value)
    }

    fun nextPlayer() {
        if (checkAllSkipped()) {
            return
        }
        // Prevent the turn from being skipped if there is only one person left
        if (skippedPlayers.value.size + 1 == players.value.size) {
            if (!skippedPlayers.value.contains(activePlayer.value)) {
                return
            }
        }

        // Find the next non-skipped index
        for (i in 1..<players.value.size + 1) {
            val index = (activePlayerIndex.value + i) % players.value.size
            if (!skippedPlayers.value.contains(players.value[index])) {
                setActivePlayerIndex(index)
                break
            }
        }

        activePlayer.value?.let {
            updatePlayerState(player = activePlayer.value!!)
        }
        startTurn()
    }

    fun previousPlayer() {
        if (checkAllSkipped()) {
            return
        }
        val index = (activePlayerIndex.value - 1 + players.value.size) % players.value.size
        setActivePlayerIndex(index)
        if (skippedPlayers.value.contains(activePlayer.value)) {
            previousPlayer()
        }
        startTurn()
    }

    // MARK: Reordering Players
    fun reorderPlayers(from: Int, to: Int) {
        if (0 <= from && from < players.value.size && 0 <= to && to < players.value.size) {
            localGameDatasource.movePlayer(from, to)
            updatePlayersList()
        }
        endRound()
    }

    fun shiftPlayerOrderForward() {
        localGameDatasource.shiftPlayerOrderForward()
        updatePlayersList()
        endRound()
    }

    fun shiftPlayerOrderBackward() {
        localGameDatasource.shiftPlayerOrderBackward()
        updatePlayersList()
        endRound()
    }

    private suspend fun startTurnTimer() {
        val startingPlayer = activePlayer.value ?: return
        var timerElapsedTime = 0L
        withContext(Dispatchers.Default) {
            timerElapsedTime = runTimer(
                startingPlayer = startingPlayer,
                startingTime = 0,
                elapsedTimeStateFlow = mutableElapsedTurnTime,
                timerMaxLength = timerDuration.value,
                updateTimerCallback = {
                    activePlayer.value?.let {
                        updateDeviceElapsedTime()
                    }
                },
                updateTimerInterval = 250,
                enforceTimer = enforceTimer.value
            )

            // Skip to the next player if the turn timer was reached and then enforce timer was set
            if (activePlayer.value == startingPlayer && timerElapsedTime >= timerDuration.value && enforceTimer.value) {
                Log.d(TAG, "Timer duration reached, advancing to next player")
                nextPlayer()
            }
        }
    }

    private suspend fun startTotalTurnTimer() {
        val startingPlayer = activePlayer.value ?: return
        withContext(Dispatchers.Default) {
            val timerElapsedTime = runTimer(
                startingPlayer = startingPlayer,
                startingTime = startingPlayer.totalTurnTime,
                elapsedTimeStateFlow = mutableTotalElapsedTurnTime,
                timerMaxLength = totalTimerDuration.value,
                updateTimerInterval = 250,
                enforceTimer = enforceTotalTimer.value
            )

            // Skip to the next player if the turn timer was reached and then enforce timer was set
            if (activePlayer.value == startingPlayer && timerElapsedTime >= totalTimerDuration.value && enforceTimer.value) {
                Log.d(TAG, "Total timer duration reached, advancing to next player")
                nextPlayer()
            }

            // Add the elapsed turn time to the starting players total time
            Log.d(
                TAG,
                "Adding elapsed time to starting player: ${startingPlayer.name} ${startingPlayer.totalTurnTime} -> $timerElapsedTime"
            )
            startingPlayer.totalTurnTime = timerElapsedTime
        }
    }

    private suspend fun runTimer(
        startingTime: Long = 0L,
        startingPlayer: Player,
        elapsedTimeStateFlow: MutableStateFlow<Long>,
        enforceTimer: Boolean,
        updateTimerCallback: (() -> Unit)? = null,
        updateTimerInterval: Int? = 100,
        timerMaxLength: Long
    ): Long {
        var lastUpdate = Instant.now()
        Log.d(
            TAG, "Starting Timer: Elapsed: ${elapsedTimeStateFlow.value}, duration: $timerMaxLength"
        )
        var timerElapsedTime = startingTime
        var lastDeviceUpdate = lastUpdate
        elapsedTimeStateFlow.value =
            if (enforceTimer) timerMaxLength - timerElapsedTime else timerElapsedTime
        while (true) {
            delay(25L)
            if (needsRestart) {
                break
            }
            if (isPaused.value) {
                lastUpdate = Instant.now()
                continue
            }
            if (timerElapsedTime >= timerMaxLength && enforceTimer) {
                Log.d(TAG, "Time limit reached")
                break
            }

            if (startingPlayer != activePlayer.value) {
                Log.d(
                    TAG,
                    "Active player changed from ${startingPlayer.name} to ${activePlayer.value!!.name}"
                )
                break
            }


            elapsedTimeStateFlow.value =
                if (enforceTimer) timerMaxLength - timerElapsedTime else timerElapsedTime


            val now = Instant.now()

            updateTimerCallback?.let {
                updateTimerInterval?.let {
                    if (Duration.between(lastDeviceUpdate, now).toMillis() > updateTimerInterval) {
                        updateTimerCallback()
                        lastDeviceUpdate = now
                    }
                }
            }

            timerElapsedTime += Duration.between(lastUpdate, now).toMillis()
            lastUpdate = Instant.now()
        }
        if (enforceTimer && startingPlayer == activePlayer.value) {
            timerElapsedTime = min(timerElapsedTime, timerMaxLength)
            // elapsedTimeStateFlow.value = timerElapsedTime
        }

        return timerElapsedTime
    }


    // MARK: Player State

    /// Determine the expected Player Device state based on the current game information
    ///
    /// The following decision tree is followed to determine the expected state of the device:
    /// 1. Check if the game hasn't started yet: AwaitingGameStart
    /// 2. Check if the game is paused: Paused
    /// 3. Check if the current player is the active player
    /// 3.1 Determine if turn timer is enforced: ActiveTurnEnforced
    /// 3.2 Determine if turn timer is not enforced: ActiveTurnNotEnforced
    /// 4. Check if the player is skipped: Skipped
    /// 5. Otherwise: AwaitingTurn
    override fun resolvePlayerDeviceState(player: Player): DeviceState {
        if (!gameActive.value) {
            return DeviceState.AwaitingGameStart
        }

        if (isPaused.value) {
            return DeviceState.Paused
        }

        if (player == activePlayer.value) {
            return if (enforceTimer.value) {
                DeviceState.ActiveTurnEnforced
            } else {
                DeviceState.ActiveTurnNotEnforced
            }
        }

        if (skippedPlayers.value.contains(player)) {
            return DeviceState.Skipped
        }

        return DeviceState.AwaitingTurn
    }

    /// Update the device with the current resolved device state
    override fun updatePlayerDevice(player: Player) {
        val deviceState = resolvePlayerDeviceState(player)

        // Ensure data is updated when updating to a new state that requires supplemental data
        when (deviceState) {
            DeviceState.AwaitingTurn -> {
                updatePlayerTurnSequence(player)
            }

            DeviceState.ActiveTurnEnforced -> {
                updatePlayerTimeData(player)
            }

            DeviceState.AwaitingGameStart -> {
                updatePlayerDeviceCount(player)
            }

            else -> {}
        }

        // Only update the device state if it differs from the current device state
        if (deviceState != player.device.getDeviceState()) {
            player.device.setDeviceState(deviceState)
        }
    }

    /// Update the device with all information necessary to display the AwaitingTurn display
    private fun updatePlayerTurnSequence(player: Player) {
        player.device.writeNumberOfPlayers(numberOfPlayers)
        player.device.writeCurrentPlayer(activePlayerIndex.value)
        player.device.writePlayerIndex(players.value.indexOf(player))
        player.device.writeSkippedPlayers(encodedSkippedPlayers)
    }



    override fun startTurn() {
        super.startTurn()
        scope.launch {
            val startingPlayer = activePlayer.value
            startingPlayer ?: return@launch

            currentRound.value.incrementPlayerTurnCounter(startingPlayer)
            startingPlayer.incrementTurnCounter()
            startingPlayer.lastTurnStart = Instant.now()
            launch { startTotalTurnTimer() }
            launch { startTurnTimer() }
        }
    }

    private fun onPlayerActiveTurnChange(player: Player, turnValue: Boolean) {
        // BLE Notifications are fired from the peripheral device by performing a write of 1 followed
        // by a write of 0. Only the write of 1 will be used to initiate state change
        if (turnValue) {
            if (player != activePlayer.value) {
                Log.d(TAG, "Player $player is not the active player")
                return
            }

            nextPlayer()
        }
    }

    private fun updateDeviceElapsedTime() {
        activePlayer.value?.device?.writeElapsedTime(timerDuration.value - elapsedTurnTime.value)
    }

    override fun updatePlayerState(player: Player) {
        player.device.writeCurrentPlayer(activePlayerIndex.value)
        super.updatePlayerState(player)
    }

    override fun startRound() {
        setActivePlayerIndex(0)
        super.startRound()
    }

}