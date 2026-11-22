package de.mosimtech.common.security.haapikey

import java.time.Clock
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * Begrenzt die Einlieferungen je Schlüssel-Hash mit einem festen Minutenfenster.
 *
 * Gezählt wird vor der Schlüsselprüfung, also auch für unbekannte Schlüssel – sonst verriete die
 * 429 einem Angreifer, dass ein Schlüssel gültig ist. Abgelaufene Fenster werden beim ersten Aufruf
 * einer neuen Minute entfernt, damit zufällige Schlüssel den Speicher nicht füllen.
 * Der Zähler lebt je Instanz; bei mehreren Instanzen gilt die Grenze je Instanz.
 */
class HaApiKeyRateLimiter(private val requestsPerMinute: Int, private val clock: Clock) {

    private val windows = ConcurrentHashMap<String, RateWindow>()
    private val lastPurgedMinute = AtomicLong(Long.MIN_VALUE)

    fun tryAcquire(keyHash: String): Boolean {
        val minute = clock.instant().epochSecond / SECONDS_PER_MINUTE
        purgeBefore(minute)
        val window = windows.compute(keyHash) { _, current ->
            when {
                current == null || current.minute != minute -> RateWindow(minute, 1)
                current.count > requestsPerMinute -> current
                else -> current.copy(count = current.count + 1)
            }
        }!!
        return window.count <= requestsPerMinute
    }

    private fun purgeBefore(minute: Long) {
        val previous = lastPurgedMinute.get()
        if (previous < minute && lastPurgedMinute.compareAndSet(previous, minute)) {
            windows.values.removeIf { it.minute < minute }
        }
    }

    private companion object {
        const val SECONDS_PER_MINUTE = 60L
    }
}
