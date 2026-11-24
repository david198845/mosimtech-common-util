package de.mosimtech.common.core.dto.notification.email

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * Mail-Auftrag an den Notification-Service, gesendet über RabbitMQ.
 *
 * Der Dienst rendert die Vorlage [templateId] in der Sprache [locale] und versendet das Ergebnis an [to].
 * Eine unbekannte Sprache fällt auf Deutsch zurück.
 *
 * [variables] sind rohe Werte; das Maskieren für HTML und Betreff übernimmt der Empfänger.
 *
 * @property to genau eine Empfängeradresse
 * @property templateId Kennung der Vorlage, z. B. `delegation-invite`
 * @property locale Sprache der Mail, z. B. `de` oder `en-US`; nur der Sprachteil zählt
 * @property variables Werte für die Platzhalter der Vorlage
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class EmailRequestDTO(
    val to: String,
    val templateId: String,
    val locale: String = "de",
    val variables: Map<String, String> = emptyMap(),
)
