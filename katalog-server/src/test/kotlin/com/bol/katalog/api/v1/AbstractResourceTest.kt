package com.bol.katalog.api.v1

import com.bol.katalog.api.support.ResetExtension
import com.bol.katalog.cqrs.Aggregate
import com.bol.katalog.features.registry.Registry
import com.bol.katalog.security.Security
import com.bol.katalog.support.ref
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.util.LinkedMultiValueMap

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
        body: Any? = null,
        queryParams: Map<String, String>? = null
    ) = exchange<Void>(method, path, expect, body, queryParams)

    protected inline fun <reified T> exchange(
        method: HttpMethod = HttpMethod.GET,
        path: String = "",
        expect: HttpStatus = HttpStatus.OK,
        body: Any? = null,
        queryParams: Map<String, String>? = null
    ): T? {
        return client.method(method).uri {
            it.path("${getBaseUrl()}/$path")
            if (queryParams != null) {
                val mvm = LinkedMultiValueMap<String, String>()
                queryParams.forEach { k, v ->
                    mvm[k] = listOf(v)
                }
                it.queryParams(mvm)
            }
            it.build()
        }
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