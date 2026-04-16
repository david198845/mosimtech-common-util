package de.mosimtech.common.security.dto

data class TokenExchangeConfig(
    val issuerUri: String,
    val clientId: String,
    val clientSecret: String
)