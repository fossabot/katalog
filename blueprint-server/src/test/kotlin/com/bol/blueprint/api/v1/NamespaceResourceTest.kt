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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@RunWith(SpringRunner::class)
@SpringBootTest
@AutoConfigureMockMvc
class NamespaceResourceTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var commandHandler: CommandHandler

    private val baseUrl = "/api/v1/namespaces"

    @Before
    fun before() {
        runBlocking { commandHandler.applyBasicTestSet() }
    }

    @Test
    fun `Can get namespaces`() {
        val result = this.mockMvc.perform(get(baseUrl)).fromJson<NamespaceResource.Responses.Multiple>()
        assertThat(result.namespaces).containsExactly(
                NamespaceResource.Responses.Single(name = "ns1"),
                NamespaceResource.Responses.Single(name = "ns2")
        )
    }

    @Test
    fun `Can get single namespace`() {
        val result = this.mockMvc.perform(get("$baseUrl/ns1")).fromJson<NamespaceResource.Responses.Detail>()
        assertThat(result).isEqualTo(NamespaceResource.Responses.Detail(name = "ns1"))
    }

    @Test
    fun `Cannot get unknown single namespace`() {
        this.mockMvc.perform(get("$baseUrl/unknown")).andExpect(status().isNotFound)
    }

    @Test
    fun `Can create namespace`() {
        val content = NamespaceResource.Requests.NewNamespace(name = "foo")
        this.mockMvc.perform(post(baseUrl).json(content)).andExpect(status().isCreated)

        val result = this.mockMvc.perform(get("$baseUrl/foo")).fromJson<NamespaceResource.Responses.Detail>()
        assertThat(result).isEqualTo(NamespaceResource.Responses.Detail(name = "foo"))
    }
}