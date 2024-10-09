package de.modulix.mosimtech.aware

import org.springframework.data.auditing.DateTimeProvider
import java.time.ZonedDateTime
import java.time.temporal.TemporalAccessor
import java.util.*

/**
 * A DateTimeProvider implementation that returns the current date and time using
 * the ZonedDateTime.now() method. This class can be used in Spring Security
 * configurations where the current time is required, for example, in token validation
 * or event logging.
 *
 * Implements the getNow method from the DateTimeProvider interface to provide
 * the current date and time wrapped in an Optional.
 *
 * @see DateTimeProvider
 */
open class SpringSecurityTimeProviderAware : DateTimeProvider {

    /**
     * Returns the current date and time wrapped in an Optional.
     *
     * @return An Optional containing the current date and time as a TemporalAccessor.
     */
    override fun getNow(): Optional<TemporalAccessor> {
        return Optional.of(ZonedDateTime.now())
    }
}
