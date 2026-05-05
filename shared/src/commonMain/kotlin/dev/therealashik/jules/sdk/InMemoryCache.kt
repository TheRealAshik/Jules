package dev.therealashik.jules.sdk

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

class InMemoryCache<K : Any, V : Any>(
    private val defaultTtl: Duration = 60.seconds,
    private val timeSource: TimeSource = TimeSource.Monotonic
) {
    private data class CacheEntry<V>(
        val value: V,
        val expiresAt: kotlin.time.TimeMark
    )

    private val cache = mutableMapOf<K, CacheEntry<V>>()
    private val mutex = Mutex()

    suspend fun get(key: K): V? {
        return mutex.withLock {
            val entry = cache[key] ?: return@withLock null
            if (entry.expiresAt.hasPassedNow()) {
                cache.remove(key)
                null
            } else {
                entry.value
            }
        }
    }

    suspend fun set(key: K, value: V, ttl: Duration = defaultTtl) {
        mutex.withLock {
            cache[key] = CacheEntry(
                value = value,
                expiresAt = timeSource.markNow() + ttl
            )
        }
    }

    suspend fun removeMatching(predicate: (K) -> Boolean) {
        mutex.withLock {
            val keysToRemove = cache.keys.filter(predicate)
            keysToRemove.forEach { cache.remove(it) }
        }
    }

    suspend fun clear() {
        mutex.withLock {
            cache.clear()
        }
    }
}
