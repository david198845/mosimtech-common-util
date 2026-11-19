package de.mosimtech.common.security.haapikey

/** Zähler eines Schlüssels innerhalb einer Kalenderminute (Minuten seit der Epoche). */
internal data class RateWindow(val minute: Long, val count: Int)
