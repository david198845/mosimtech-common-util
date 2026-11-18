package de.mosimtech.common.rabbitmq

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class DocumentCountRoutingKeyTest {

    @Test
    fun buildsRoutingKeyFromNamespace() {
        assertEquals("document.count.momasoft.financial", documentCountRoutingKey("momasoft.financial"))
    }
}
