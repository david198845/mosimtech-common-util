package de.mosimtech.common.security.i18n

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice

/**
 * Richtet [ContentLanguageResponseAdvice] in jedem Servlet-Dienst ein, der die Bibliothek
 * einbindet. Ein Dienst muss dafür nichts tun.
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(ResponseBodyAdvice::class)
open class RequestLanguageAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    open fun contentLanguageResponseAdvice(): ContentLanguageResponseAdvice = ContentLanguageResponseAdvice()
}
