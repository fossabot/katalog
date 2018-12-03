package com.bol.katalog.config

import com.bol.katalog.domain.*
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.util.*
import javax.annotation.PostConstruct
import kotlin.random.nextInt

@Configuration
@Profile("it")
class IntegrationTestConfiguration {
    @Autowired
    lateinit var commandHandler: Processor

    @PostConstruct
    fun init() {
        val ns1: NamespaceId = UUID.randomUUID()
        val ns2: NamespaceId = UUID.randomUUID()
        val group1: GroupId = UUID.randomUUID()
        val ns1_schema1: SchemaId = UUID.randomUUID()
        val ns1_schema2: SchemaId = UUID.randomUUID()
        val ns2_schema3: SchemaId = UUID.randomUUID()
        val ns1_schema1_v100: VersionId = UUID.randomUUID()
        val ns1_schema1_v101: VersionId = UUID.randomUUID()
        val ns1_schema1_v110snapshot: VersionId = UUID.randomUUID()
        val ns1_schema1_v200snapshot: VersionId = UUID.randomUUID()
        val ns2_schema3_v100: VersionId = UUID.randomUUID()
        val artifact1: ArtifactId = UUID.randomUUID()
        val artifact2: ArtifactId = UUID.randomUUID()

        runBlocking {
            with(commandHandler) {
                createNamespace(ns1, group1, "ns1")
                createNamespace(ns2, group1, "ns2")

                createSchema(ns1, ns1_schema1, "schema1", SchemaType.default())
                createSchema(ns1, ns1_schema2, "schema2", SchemaType.default())
                createSchema(ns2, ns2_schema3, "schema3", SchemaType.default())

                createVersion(ns1_schema1, ns1_schema1_v100, "1.0.0")
                createVersion(ns1_schema1, ns1_schema1_v101, "1.0.1")
                createVersion(ns1_schema1, ns1_schema1_v110snapshot, "1.1.0-SNAPSHOT")
                createVersion(ns1_schema1, ns1_schema1_v200snapshot, "2.0.0-SNAPSHOT")

                // Add a huge amount of versions for ns1_schema1
                for (major in 3..20) {
                    val minorCount = kotlin.random.Random.nextInt(10..30)
                    for (minor in 0..minorCount) {
                        val revCount = kotlin.random.Random.nextInt(10..30)
                        for (rev in 0..revCount) {
                            createVersion(ns1_schema1, UUID.randomUUID(), "$major.$minor.$rev")
                        }
                    }
                }

                createVersion(ns2_schema3, ns2_schema3_v100, "1.0.0")

                createArtifact(ns1_schema1_v100, artifact1, "artifact1.json", MediaType.JSON, byteArrayOf(1, 2, 3))
                createArtifact(ns1_schema1_v101, artifact2, "artifact2.json", MediaType.JSON, byteArrayOf(4, 5, 6))
            }
        }
    }
}
