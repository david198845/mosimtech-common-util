package de.mosimtech.common.core.util

import de.mosimtech.common.core.urn.Urn


/**
 * Converts a Urn object to a URI-compatible string by replacing special characters
 * with their percent-encoded equivalents and formatting accordingly.
 *
 * @receiver The Urn instance to be converted to a URI-compatible string.
 * @return A string representation of the Urn formatted as a URI.
 */
fun Urn.toUri() = toString()
    .replace(":", "_")
    .replace("/", "%2F")
    .replace(" ", "%20")
    .replace("?", "%3F")
    .replace("#", "%23")
    .replace("[", "%5B")
    .replace("]", "%5D")
    .replace("@", "%40")
    .replace("!", "%21")
    .replace("$", "%24")
    .replace("&", "%26")
    .replace("'", "%27")
    .replace("(", "%28")
    .replace(")", "%29")
    .replace("*", "%2A")
    .replace("+", "%2B")
    .replace(",", "%2C")
    .replace(";", "%3B")
    .replace("=", "%3D")
    .replace("%", "%25")
