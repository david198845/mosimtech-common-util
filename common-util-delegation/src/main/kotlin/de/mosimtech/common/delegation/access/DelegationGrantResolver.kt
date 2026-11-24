package de.mosimtech.common.delegation.access

import de.mosimtech.common.core.urn.Urn

/**
 * Wird vom Fachmodul implementiert und beantwortet, was ein Aufrufer im Bestand eines Eigentümers darf.
 *
 * Die Implementierung prüft selbst Aktivität und Ablaufdatum der Freigabe und liefert bei einer
 * gültigen Freigabe nie leere `scopes` (eine leere Menge wird vom Filter wie „keine Freigabe“ behandelt).
 *
 * Der Vergleich von `actorId` und `ownerId` mit der gespeicherten Freigabe muss exakt sein (über `toUrnString()`,
 * ohne Angleichung der Groß-/Kleinschreibung): Der Filter übernimmt die URN aus dem Header als Eigentümer;
 * ein unscharfer Treffer würde auf eine ID schreiben, die niemandem gehört.
 */
fun interface DelegationGrantResolver {

    /** Freigabe des Aufrufers im Bestand des Eigentümers; `null`, wenn keine gültige Freigabe besteht. */
    fun resolve(actorId: Urn, ownerId: Urn): DelegationGrant?
}
