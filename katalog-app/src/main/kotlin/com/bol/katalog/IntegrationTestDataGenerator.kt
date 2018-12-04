package com.bol.katalog

import com.bol.katalog.domain.DomainProcessor
import com.bol.katalog.domain.Group
import com.bol.katalog.domain.MediaType
import com.bol.katalog.domain.SchemaType
import com.bol.katalog.security.KatalogUserDetails
import com.bol.katalog.security.runBlockingWithUserDetails
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.runBlocking
import org.springframework.context.annotation.Profile
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.stereotype.Component
import java.util.*
import javax.annotation.PostConstruct

@Component
@Profile("it")
class IntegrationTestDataGenerator(
    private val processor: DomainProcessor,
    private val userDetailsService: ReactiveUserDetailsService
) {
    @PostConstruct
    fun init() {
        val admin = runBlocking {
            userDetailsService.findByUsername("admin").awaitSingle() as KatalogUserDetails
        }

        runBlockingWithUserDetails(admin) {
            with(processor) {
                for (group in 1..3) {
                    for (namespace in 1..3) {
                        val namespaceId = UUID.randomUUID()
                        createNamespace(namespaceId, Group("group$group"), "group${group}_ns$namespace")
                        for (schema in 1..3) {
                            val schemaId = UUID.randomUUID()
                            createSchema(namespaceId, schemaId, "schema$schema", SchemaType.default())
                            for (major in 1..3) {
                                for (minor in 1..3) {
                                    for (rev in 0..5) {
                                        val versionId = UUID.randomUUID()
                                        createVersion(schemaId, versionId, "$major.$minor.$rev")

                                        createArtifact(
                                            versionId,
                                            UUID.randomUUID(),
                                            "artifact1.json",
                                            MediaType.JSON,
                                            """{ "hello1": true }""".toByteArray()
                                        )
                                        createArtifact(
                                            versionId,
                                            UUID.randomUUID(),
                                            "artifact2.json",
                                            MediaType.JSON,
                                            """{ "hello2": true }""".toByteArray()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
