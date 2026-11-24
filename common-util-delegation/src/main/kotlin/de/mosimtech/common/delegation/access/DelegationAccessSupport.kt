package de.mosimtech.common.delegation.access

import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * Bündelt [filter] und [interceptor], die zusammengehören.
 *
 * Ein Dienst registriert diese eine Klasse als Bean und hängt [filter] hinter die Authentifizierung
 * in seine Security-Filterkette. Den Interceptor trägt die Klasse selbst für alle Pfade ein.
 * Der Filter allein, ohne Interceptor, würde jeden Endpunkt für jede Freigabe öffnen.
 */
class DelegationAccessSupport(grantResolver: DelegationGrantResolver) : WebMvcConfigurer {

    val filter: DelegationAccessFilter = DelegationAccessFilter(grantResolver)

    val interceptor: DelegatedAccessInterceptor = DelegatedAccessInterceptor()

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(interceptor).addPathPatterns("/**")
    }
}
