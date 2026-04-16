package de.mosimtech.common.security.dto

data class TokenExchangeConfig(
    val issuerUri: String? = null,
    val tokenUri: String? = null,
    val clientId: String,
    val clientSecret: String
)
