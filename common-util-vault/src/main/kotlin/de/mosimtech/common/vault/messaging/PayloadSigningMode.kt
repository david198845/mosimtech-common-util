package de.mosimtech.common.vault.messaging

/**
 * Legt fest, wie der Payload in die Signatur-Eingabe eingeht.
 *
 * Vault begrenzt die Länge einzelner JSON-Strings im Request. Bei großen Payloads (z. B. Dokumenten)
 * würde die vollständige Eingabe diese Grenze überschreiten — dann wird nur der Hash signiert.
 */
enum class PayloadSigningMode {
    /** Payload geht vollständig (Base64) in die Signatur-Eingabe ein. Format bis einschließlich 3.3.8. */
    FULL,

    /**
     * Statt des Payloads geht dessen SHA-256 ein. Der Empfänger berechnet den Hash selbst aus dem
     * empfangenen Payload — die Bindung an den Inhalt ist damit so fest wie bei [FULL].
     */
    DIGEST,
}
