package com.bol.blueprint.api.v1

import com.bol.blueprint.TestData.ARTIFACT1
import com.bol.blueprint.TestData.ARTIFACT2
import com.bol.blueprint.TestData.NS1
import com.bol.blueprint.TestData.SCHEMA1
import com.bol.blueprint.TestData.VERSION1
import com.bol.blueprint.domain.Dispatcher
import com.bol.blueprint.domain.MediaType
import com.bol.blueprint.domain.SchemaType
import com.bol.blueprint.fromJson
import kotlinx.coroutines.experimental.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@RunWith(SpringRunner::class)
@SpringBootTest
@AutoConfigureMockMvc
class ArtifactResourceTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var dispatcher: Dispatcher

    private val baseUrl = "/api/v1/namespaces/ns1/schemas/schema1/versions/1.0.0/artifacts"

    @Before
    fun before() {
        runBlocking {
            dispatcher.createNamespace(NS1)
            dispatcher.createSchema(SCHEMA1, SchemaType.default())
            dispatcher.createVersion(VERSION1)
            dispatcher.createArtifact(ARTIFACT1, MediaType.JSON, byteArrayOf(1, 2, 3))
            dispatcher.createArtifact(ARTIFACT2, MediaType.JSON, byteArrayOf(1, 2, 3))
        }
    }

    @Test
    fun `Can get artifacts`() {
        val result = this.mockMvc.perform(get(baseUrl)).fromJson<ArtifactResource.Responses.Multiple>()
        assertThat(result.artifacts).containsExactly(
                ArtifactResource.Responses.Single(filename = "artifact1.json"),
                ArtifactResource.Responses.Single(filename = "artifact2.json")
        )
    }

    @Test
    fun `Can get single artifact`() {
        val result = this.mockMvc.perform(get("$baseUrl/artifact1.json")).fromJson<ArtifactResource.Responses.Detail>()
        assertThat(result).isEqualTo(
                ArtifactResource.Responses.Detail(filename = "artifact1.json")
        )
    }

    @Test
    fun `Cannot get unknown single artifact`() {
        this.mockMvc.perform(get("$baseUrl/unknown")).andExpect(status().isNotFound)
    }
}