package com.bol.katalog.api.v1

import com.bol.katalog.api.ResetExtension
import com.bol.katalog.cqrs.Aggregate
import com.bol.katalog.features.registry.Registry
import com.bol.katalog.ref
import com.bol.katalog.security.Security
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient

@ExtendWith(SpringExtension::class, ResetExtension::class)
abstract class AbstractResourceTest {
    abstract fun getBaseUrl(): String

    @Autowired
    protected lateinit var client: WebTestClient

    @Autowired
    protected lateinit var security: Aggregate<Security>

    @Autowired
    protected lateinit var registry: Aggregate<Registry>

    protected fun exchange(
        method: HttpMethod = HttpMethod.GET,
        path: String = "",
        expect: HttpStatus = HttpStatus.OK,
        body: Any? = null
    ) = exchange<Void>(method, path, expect, body)

    protected inline fun <reified T> exchange(
        method: HttpMethod = HttpMethod.GET,
        path: String = "",
        expect: HttpStatus = HttpStatus.OK,
        body: Any? = null
    ): T? {
        return client.method(method).uri("${getBaseUrl()}/$path")
            .let {
                if (body != null) {
                    it.syncBody(body)
                }
                it
            }
            .exchange()
            .expectStatus().isEqualTo(expect)
            .expectBody(ref<T>())
            .returnResult()
            .responseBody
    }
}