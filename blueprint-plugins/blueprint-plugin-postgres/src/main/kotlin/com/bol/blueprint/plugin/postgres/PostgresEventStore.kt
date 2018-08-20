package com.bol.blueprint.plugin.postgres

import com.bol.blueprint.domain.Event
import com.bol.blueprint.store.EventQuery
import com.bol.blueprint.store.EventStore
import com.bol.blueprint.store.Page
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.jdbc.core.JdbcTemplate

class PostgresEventStore(private val jdbcTemplate: JdbcTemplate) : EventStore {
    private val mapper = ObjectMapper()

    init {
        mapper.registerModule(KotlinModule())
        mapper.registerModule(JavaTimeModule())
    }

    override suspend fun get(query: EventQuery): Page<Event> {
        val results = mutableListOf<Event>()

        var sql = "select id, type, contents from events"
        query.cursor?.let { sql += " where id > $it" }
        sql += " order by id limit ${query.pageSize}"

        var nextPageAfterId = 0L
        jdbcTemplate.query(
            sql
        ) { rs, _ ->
            nextPageAfterId = rs.getLong(1)
            val clazz = Class.forName(rs.getString(2))
            val data = rs.getString(3)
            val event = mapper.readValue(data, clazz) as Event
            results += event
        }

        return Page(results, nextPageAfterId.toString())
    }

    override suspend fun store(event: Event) {
        jdbcTemplate.update(
            "insert into events (metadata, type, contents) values (?::jsonb, ?, ?::jsonb)",
            mapper.writeValueAsString(event.metadata), event::class.java.name, mapper.writeValueAsString(event)
        )
    }
}