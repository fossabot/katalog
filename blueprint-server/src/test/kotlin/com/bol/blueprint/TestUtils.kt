package com.bol.blueprint

import com.bol.blueprint.TestUtils.objectMapper
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.test.web.servlet.ResultActions

object TestUtils {
    val objectMapper = ObjectMapper()

    init {
        objectMapper.registerModule(KotlinModule())
    }
}

inline fun <reified T> ResultActions.fromJson() = objectMapper.readValue(this.andReturn().response.contentAsString, T::class.java)
