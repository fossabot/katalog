package com.bol.blueprint.api.v1

import com.bol.blueprint.applyBasicTestSet
import com.bol.blueprint.domain.CommandHandler
import com.bol.blueprint.fromJson
import kotlinx.coroutines.experimental.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status


@RunWith(SpringRunner::class)
@SpringBootTest
@AutoConfigureMockMvc
class ArtifactResourceTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var commandHandler: CommandHandler

    private val baseUrl = "/api/v1/namespaces/ns1/schemas/schema1/versions/1.0.0/artifacts"

    @Before
    fun before() {
        runBlocking { commandHandler.applyBasicTestSet() }
    }

    @Test
    fun `Can get artifacts`() {
        val result = this.mockMvc.perform(get(baseUrl).contentType(APPLICATION_BLUEPRINT_V1_VALUE)).fromJson<ArtifactResource.Responses.Multiple>()
        assertThat(result.artifacts).containsExactly(
                ArtifactResource.Responses.Single(filename = "artifact1.json"),
                ArtifactResource.Responses.Single(filename = "artifact2.json")
        )
    }

    @Test
    fun `Can get single artifact`() {
        val result = this.mockMvc.perform(get("$baseUrl/artifact1.json").contentType(APPLICATION_BLUEPRINT_V1_VALUE)).andReturn()
        assertThat(result.response.contentAsByteArray).isEqualTo(byteArrayOf(1, 2, 3))
    }

    @Test
    fun `Can delete single artifact`() {
        this.mockMvc.perform(MockMvcRequestBuilders.delete("$baseUrl/artifact1.json").contentType(APPLICATION_BLUEPRINT_V1_VALUE)).andExpect(status().isNoContent)
        this.mockMvc.perform(get("$baseUrl/artifact1.json").contentType(APPLICATION_BLUEPRINT_V1_VALUE)).andExpect(status().isNotFound)
    }

    @Test
    fun `Cannot get unknown single artifact`() {
        this.mockMvc.perform(get("$baseUrl/unknown").contentType(APPLICATION_BLUEPRINT_V1_VALUE)).andExpect(status().isNotFound)
    }

    @Test
    fun `Can upload artifact`() {
        val file = MockMultipartFile("file", "uploaded.json", "multipart/form-data", byteArrayOf(5, 6, 7))
        mockMvc.perform(multipart(baseUrl).file(file).contentType(APPLICATION_BLUEPRINT_V1_VALUE)).andExpect(status().isOk)

        val result = this.mockMvc.perform(get("$baseUrl/uploaded.json").contentType(APPLICATION_BLUEPRINT_V1_VALUE)).andReturn()
        assertThat(result.response.contentAsByteArray).isEqualTo(byteArrayOf(5, 6, 7))
    }

    @Test
    fun `Cannot upload duplicate artifact`() {
        val file = MockMultipartFile("file", "uploaded.json", "multipart/form-data", byteArrayOf(5, 6, 7))
        mockMvc.perform(multipart(baseUrl).file(file).contentType(APPLICATION_BLUEPRINT_V1_VALUE)).andExpect(status().isOk)
        mockMvc.perform(multipart(baseUrl).file(file).contentType(APPLICATION_BLUEPRINT_V1_VALUE)).andExpect(status().isConflict)
    }
}