package de.mosimtech.common.core.util

import de.mosimtech.common.core.urn.Urn


/**
 * Converts the current string to a URN (Uniform Resource Name) instance.
 *
 * @return An instance of the Urn class parsed from the current string.
 */
fun String.toUrn(): Urn? = Urn.parse(this)

