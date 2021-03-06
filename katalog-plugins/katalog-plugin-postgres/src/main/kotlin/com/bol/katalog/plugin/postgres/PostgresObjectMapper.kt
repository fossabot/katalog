package com.bol.katalog.plugin.postgres

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule

object PostgresObjectMapper {
    private val mapper = ObjectMapper()

    init {
        mapper.registerModule(KotlinModule())
        mapper.registerModule(JavaTimeModule())
    }

    fun get() = mapper
}