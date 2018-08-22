package com.bol.blueprint.api.v1

import com.bol.blueprint.applyBasicTestSet
import com.bol.blueprint.domain.CommandHandler
import com.bol.blueprint.fromJson
import com.bol.blueprint.json
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@RunWith(SpringRunner::class)
@SpringBootTest
@AutoConfigureMockMvc
class VersionResourceTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var commandHandler: CommandHandler

    private val baseUrl = "/api/v1/namespaces/ns1/schemas/schema1/versions"

    @Before
    fun before() {
        runBlocking { commandHandler.applyBasicTestSet() }
    }

    @Test
    fun `Can get versions`() {
        val result = this.mockMvc.perform(get(baseUrl)).fromJson<VersionResource.Responses.Multiple>()
        assertThat(result.versions).containsExactly(
                VersionResource.Responses.Single(version = "1.0.0"),
                VersionResource.Responses.Single(version = "1.0.1")
        )
    }

    @Test
    fun `Can get single version`() {
        val result = this.mockMvc.perform(get("$baseUrl/1.0.0")).fromJson<VersionResource.Responses.Detail>()
        assertThat(result).isEqualTo(
                VersionResource.Responses.Detail(version = "1.0.0")
        )
    }

    @Test
    fun `Cannot get unknown single version`() {
        this.mockMvc.perform(get("$baseUrl/unknown")).andExpect(status().isNotFound)
    }

    @Test
    fun `Can create version`() {
        val content = VersionResource.Requests.NewVersion(version = "0.1.2")
        this.mockMvc.perform(MockMvcRequestBuilders.post(baseUrl).json(content)).andExpect(status().isCreated)

        val result = this.mockMvc.perform(get("$baseUrl/0.1.2")).fromJson<VersionResource.Responses.Detail>()
        assertThat(result).isEqualTo(VersionResource.Responses.Detail(version = "0.1.2"))
    }
}