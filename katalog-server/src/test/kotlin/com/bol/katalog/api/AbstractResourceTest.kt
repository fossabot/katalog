package com.bol.katalog.api

import com.bol.katalog.api.support.ResetExtension
import com.bol.katalog.cqrs.AggregateContext
import com.bol.katalog.cqrs.sendLocal
import com.bol.katalog.features.registry.RegistryAggregate
import com.bol.katalog.security.SecurityAggregate
import com.bol.katalog.security.allPermissions
import com.bol.katalog.security.support.*
import com.bol.katalog.testing.ref
import com.bol.katalog.users.GroupPermission
import com.bol.katalog.utils.runBlockingAsSystem
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.util.LinkedMultiValueMap

@ExtendWith(SpringExtension::class, ResetExtension::class)
@DirtiesContext
abstract class AbstractResourceTest {
    abstract fun getBaseUrl(): String

    @Autowired
    protected lateinit var client: WebTestClient

    @Autowired
    protected lateinit var context: AggregateContext

    @Autowired
    protected lateinit var security: SecurityAggregate

    @Autowired
    protected lateinit var registry: RegistryAggregate

    @BeforeEach
    fun beforeEach() {
        runBlockingAsSystem {
            context.sendLocal(
                group1.create(),
                user1.create(),
                userReadOnly.create(),
                admin.create(),
                userNoGroups.create(),

                user1.addToGroup(group1, allPermissions()),
                userReadOnly.addToGroup(group1, setOf(GroupPermission.READ))
            )
        }
    }

    protected fun exchange(
        method: HttpMethod = HttpMethod.GET,
        path: String = "",
        expect: HttpStatus = HttpStatus.OK,
        body: Any? = null,
        queryParams: Map<String, String>? = null,
        headers: Map<String, String>? = null
    ) = exchange<Void>(method, path, expect, body, queryParams, headers)

    protected inline fun <reified T> exchange(
        method: HttpMethod = HttpMethod.GET,
        path: String = "",
        expect: HttpStatus = HttpStatus.OK,
        body: Any? = null,
        queryParams: Map<String, String>? = null,
        headers: Map<String, String>? = null
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
                headers?.forEach { k, v ->
                    it.header(k, v)
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