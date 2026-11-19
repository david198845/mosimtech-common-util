package de.mosimtech.common.security.haapikey

import org.springframework.boot.ssl.SslBundle
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.web.client.RestClient
import java.net.http.HttpClient
import java.time.Duration

/** Erzeugt den [RestClient] für den Prüfaufruf an den Auth-Dienst. */
object HaApiKeyRestClients {

    /** Zeitgrenze für Verbindungsaufbau und Antwort; HA wartet nicht länger auf die Einlieferung. */
    val TIMEOUT: Duration = Duration.ofSeconds(2)

    /**
     * Baut einen Client mit der Basis-URL [baseUrl] (z. B. `https://host:8087/api`).
     * Ist [sslBundle] gesetzt, stammt der SSL-Kontext daraus, sonst gilt der JDK-Standard.
     */
    fun create(baseUrl: String, sslBundle: SslBundle?): RestClient {
        val httpClient = HttpClient.newBuilder()
            .connectTimeout(TIMEOUT)
            .apply { sslBundle?.let { sslContext(it.createSslContext()) } }
            .build()
        val requestFactory = JdkClientHttpRequestFactory(httpClient).apply { setReadTimeout(TIMEOUT) }
        return RestClient.builder().baseUrl(baseUrl).requestFactory(requestFactory).build()
    }
}
