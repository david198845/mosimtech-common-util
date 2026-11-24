package de.mosimtech.common.delegation.access

import de.mosimtech.common.core.urn.Urn
import org.springframework.aop.framework.ProxyFactory
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.web.method.HandlerMethod
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

interface SampleApi {
    fun read()
    fun write()
    fun plain()
}

open class SampleController : SampleApi {
    @DelegatedAccess("plan:read")
    override fun read() = Unit

    @DelegatedAccess("plan:write")
    override fun write() = Unit

    override fun plain() = Unit
}

class DelegatedAccessInterceptorTest {

    private val interceptor = DelegatedAccessInterceptor()
    private val controller = SampleController()
    private val request = MockHttpServletRequest()
    private val response = MockHttpServletResponse()

    @AfterTest
    fun cleanup() {
        DelegationAccessHolder.clear()
    }

    private fun delegate(vararg scopes: String) {
        DelegationAccessHolder.set(
            DelegationContext(
                ownerId = Urn.parse("urn:user:momasoft:owner")!!,
                actorId = Urn.parse("urn:user:momasoft:actor")!!,
                scopes = scopes.toSet(),
                delegationId = "urn:delegation:d-1",
            )
        )
    }

    private fun handler(method: String) = HandlerMethod(controller, method)

    @Test
    fun `allows any handler when no delegation context is set`() {
        assertTrue(interceptor.preHandle(request, response, handler("plain")))
        assertEquals(200, response.status)
    }

    @Test
    fun `rejects delegated request to a method without annotation`() {
        delegate("plan:read")

        assertFalse(interceptor.preHandle(request, response, handler("plain")))
        assertEquals(403, response.status)
    }

    @Test
    fun `rejects delegated request when the scope is missing`() {
        delegate("plan:read")

        assertFalse(interceptor.preHandle(request, response, handler("write")))
        assertEquals(403, response.status)
    }

    @Test
    fun `allows delegated request when the scope is granted`() {
        delegate("plan:read")

        assertTrue(interceptor.preHandle(request, response, handler("read")))
    }

    @Test
    fun `finds annotation when the handler method belongs to a class based proxy`() {
        delegate("plan:read")
        val factory = ProxyFactory(SampleController())
        factory.isProxyTargetClass = true
        val proxy = factory.proxy
        val proxyMethod = proxy.javaClass.getMethod("read")
        assertNull(proxyMethod.getAnnotation(DelegatedAccess::class.java))

        assertTrue(interceptor.preHandle(request, response, HandlerMethod(proxy, proxyMethod)))
    }

    @Test
    fun `rejects when the handler method comes from an interface based proxy`() {
        delegate("plan:read")
        val factory = ProxyFactory(SampleController())
        factory.setInterfaces(SampleApi::class.java)
        factory.isProxyTargetClass = false
        val proxy = factory.proxy

        val method = HandlerMethod(proxy, SampleApi::class.java.getMethod("read"))

        assertFalse(interceptor.preHandle(request, response, method))
        assertEquals(403, response.status)
    }

    @Test
    fun `rejects delegated request when context has no scopes at an annotated method`() {
        delegate()

        assertFalse(interceptor.preHandle(request, response, handler("read")))
        assertEquals(403, response.status)
    }

    @Test
    fun `rejects delegated request when handler is not a controller method`() {
        delegate("plan:read")

        assertFalse(interceptor.preHandle(request, response, Any()))
        assertEquals(403, response.status)
    }
}
