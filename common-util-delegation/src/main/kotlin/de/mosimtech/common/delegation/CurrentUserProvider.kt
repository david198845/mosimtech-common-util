package de.mosimtech.common.delegation

import de.mosimtech.common.core.urn.Urn
import de.mosimtech.common.security.SecurityContextAdapter

/**
 * Liefert die "effektive" Nutzer-ID:
 * 1. Wenn [DelegationContextHolder] gesetzt ist (X-Target-User-Id-Header), wird diese ID zurückgegeben.
 * 2. Sonst: eigene ID aus dem JWT (SecurityContextHolder).
 *
 * Diese Klasse ist eine plain class – jeder Service registriert sie als @Bean
 * in seiner eigenen @Configuration-Klasse.
 */
class CurrentUserProvider {

    fun getEffectiveUserId(): Urn =
        DelegationContextHolder.get()
            ?.let { Urn.parse(it) }
            ?: SecurityContextAdapter.getCurrentUserID()
            ?: throw IllegalStateException("No authenticated user found in SecurityContext")
}
