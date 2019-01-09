package com.bol.katalog

import com.bol.katalog.domain.DomainProcessor
import com.bol.katalog.domain.MediaType
import com.bol.katalog.domain.SchemaType
import com.bol.katalog.security.SecurityAggregate
import com.bol.katalog.security.withUserDetails
import kotlinx.coroutines.runBlocking
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.util.*
import javax.annotation.PostConstruct

@Component
@ConditionalOnProperty("katalog.testdata.enabled", matchIfMissing = false)
class IntegrationTestDataGenerator(
    private val security: SecurityAggregate,
    private val processor: DomainProcessor
) {
    @PostConstruct
    fun init() {
        runBlocking {
            val admin = security.findUserByUsername("admin")
            withUserDetails(admin) {
                with(processor) {
                    for (group in 1..3) {
                        for (namespace in 1..3) {
                            val namespaceId = UUID.randomUUID()
                            createNamespace(namespaceId, "id-group$group", "group${group}_ns$namespace")
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
}
