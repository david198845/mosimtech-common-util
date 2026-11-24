package de.mosimtech.common.delegation.access

import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.servlet.handler.MappedInterceptor
import org.springframework.web.util.ServletRequestPathUtils
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

class DelegationAccessSupportTest {

    private class ExposedRegistry : InterceptorRegistry() {
        fun registered(): List<Any> = interceptors
    }

    private val support = DelegationAccessSupport { _, _ -> null }

    @Test
    fun `addInterceptors registers exactly its own interceptor`() {
        val registry = ExposedRegistry()

        support.addInterceptors(registry)

        assertEquals(1, registry.registered().size)
        val mapped = registry.registered().single() as MappedInterceptor
        assertSame(support.interceptor, mapped.interceptor)
        listOf("/", "/a", "/a/b/c").forEach { uri ->
            val request = MockHttpServletRequest("GET", uri)
            ServletRequestPathUtils.parseAndCache(request)
            assertTrue(mapped.matches(request), uri)
        }
    }

    @Test
    fun `filter and interceptor are the same instances on repeated access`() {
        assertSame(support.filter, support.filter)
        assertSame(support.interceptor, support.interceptor)
    }
}
