package de.mosimtech.common.vault.messaging

import org.springframework.security.oauth2.jwt.Jwt

/** Die beiden geprüften Tokens einer Nachricht im Dual-Token-Flow ([SignedMessageValidator.validateUser]). */
data class ValidatedMessageTokens(val serviceToken: Jwt, val userToken: Jwt)
