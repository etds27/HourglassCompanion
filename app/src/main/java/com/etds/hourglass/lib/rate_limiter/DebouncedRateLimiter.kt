package com.etds.hourglass.lib.rate_limiter

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration
import kotlin.time.TimeSource

class DebouncedRateLimiter(
    private val minInterval: Duration,
    private val scope: CoroutineScope
) {
    companion object {
        const val TAG = "DebouncedRateLimiter"
    }

    private var currentJob: Job? = null
    private var pendingBlock: (suspend () -> Unit)? = null
    private var lastRun = TimeSource.Monotonic.markNow() - minInterval

    fun run(block: suspend () -> Unit) {
        pendingBlock = block // replace any pending one

        // if nothing running, start processing
        if (currentJob == null) {
            currentJob = scope.launch { processQueue() }
        }
    }

    private suspend fun processQueue() {
        while (true) {
            val block = pendingBlock ?: break // no queued work → exit
            pendingBlock = null

            // wait for min interval since last execution
            val elapsed = lastRun.elapsedNow()
            if (elapsed < minInterval) {
                delay(minInterval - elapsed)
            }

            // run the job
            try {
                block()
            } finally {
                lastRun = TimeSource.Monotonic.markNow()
            }
        }

        // no more work left
        currentJob = null
    }
}