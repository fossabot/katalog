package com.bol.blueprint

import com.bol.blueprint.TestUtils.objectMapper
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder

object TestUtils {
    val objectMapper = ObjectMapper()

    init {
        objectMapper.registerModule(KotlinModule())
    }
}

inline fun <reified T> ResultActions.fromJson() = objectMapper.readValue(this.andReturn().response.contentAsString, T::class.java)

inline fun <reified T> MockHttpServletRequestBuilder.json(content: T): MockHttpServletRequestBuilder {
    this.contentType(MediaType.APPLICATION_JSON)
    this.content(objectMapper.writeValueAsString(content))
    return this
}
