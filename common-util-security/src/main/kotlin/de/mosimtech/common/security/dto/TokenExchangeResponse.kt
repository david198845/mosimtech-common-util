package de.mosimtech.common.security.dto

import com.fasterxml.jackson.annotation.JsonProperty


data class TokenExchangeResponse(
    @param:JsonProperty("access_token")
    val accessToken: String,

    @param:JsonProperty("expires_in")
    val expiresIn: Int,

    @param:JsonProperty("token_type")
    val tokenType: String
)