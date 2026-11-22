package de.mosimtech.common.security

import de.mosimtech.common.core.i18n.AccountLanguage
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.oauth2.jwt.Jwt

object AccountLanguageClaims {
    fun fromToken(token: Jwt?): AccountLanguage? =
        AccountLanguage.fromTag(token?.claims?.get("locale") as? String)

    fun current(): AccountLanguage? =
        fromToken((SecurityContextHolder.getContext().authentication as? JwtAuthenticationToken)?.token)
}
