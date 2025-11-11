package com.etds.hourglass.lib.rate_limiter
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.TimeSource

class RateLimiter(
    private val minInterval: Duration
) {
    private val mutex = Mutex()
    private var lastCallTime = TimeSource.Monotonic.markNow() - minInterval

    suspend fun run(block: suspend () -> Unit) {
        mutex.withLock {
            val elapsed = lastCallTime.elapsedNow()
            if (elapsed < minInterval) {
                delay(minInterval - elapsed)
            }
            lastCallTime = TimeSource.Monotonic.markNow()
        }
        return block()
    }
}