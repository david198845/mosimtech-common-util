package de.mosimtech.common.delegation.access

/**
 * Markiert eine Controller-Methode als für delegierte Requests erreichbar.
 *
 * Steht an der Methode des Controllers, auch wenn dieser ein generiertes Interface implementiert.
 *
 * @property scope Recht, das der Eigentümer dem Aufrufer dafür eingeräumt haben muss.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class DelegatedAccess(val scope: String)
