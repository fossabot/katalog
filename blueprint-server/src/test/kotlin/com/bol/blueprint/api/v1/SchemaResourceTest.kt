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
class SchemaResourceTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var commandHandler: CommandHandler

    private val baseUrl = "/api/v1/namespaces/ns1/schemas"

    @Before
    fun before() {
        runBlocking { commandHandler.applyBasicTestSet() }
    }

    @Test
    fun `Can get schemas`() {
        val result = this.mockMvc.perform(get(baseUrl)).fromJson<SchemaResource.Responses.Multiple>()
        assertThat(result.schemas).containsExactly(
                SchemaResource.Responses.Single(name = "schema1"),
                SchemaResource.Responses.Single(name = "schema2")
        )
    }

    @Test
    fun `Can get single schema`() {
        val result = this.mockMvc.perform(get("$baseUrl/schema1")).fromJson<SchemaResource.Responses.Detail>()
        assertThat(result).isEqualTo(
                SchemaResource.Responses.Detail(name = "schema1")
        )
    }

    @Test
    fun `Cannot get unknown single schema`() {
        this.mockMvc.perform(get("$baseUrl/unknown")).andExpect(status().isNotFound)
    }

    @Test
    fun `Can create schema`() {
        val content = SchemaResource.Requests.NewSchema(name = "foo")
        this.mockMvc.perform(MockMvcRequestBuilders.post(baseUrl).json(content)).andExpect(status().isCreated)

        val result = this.mockMvc.perform(get("$baseUrl/foo")).fromJson<SchemaResource.Responses.Detail>()
        assertThat(result).isEqualTo(SchemaResource.Responses.Detail(name = "foo"))
    }
}