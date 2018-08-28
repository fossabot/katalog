package com.bol.blueprint.plugin.postgres

import com.bol.blueprint.domain.Event
import com.bol.blueprint.store.EventQuery
import com.bol.blueprint.store.EventStore
import com.bol.blueprint.store.Page
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.jdbc.core.JdbcTemplate
import java.time.ZoneOffset

class PostgresEventStore(private val jdbcTemplate: JdbcTemplate) : EventStore {
    private val mapper = ObjectMapper()

    init {
        mapper.registerModule(KotlinModule())
        mapper.registerModule(JavaTimeModule())
    }

    override suspend fun get(query: EventQuery): Page<Event<Any>> {
        val results = mutableListOf<Event<Any>>()

        var sql = "select id, timestamp, username, type, contents from events"
        query.cursor?.let { sql += " where id > $it" }
        sql += " order by id limit ${query.pageSize}"

        var nextPageAfterId = 0L
        jdbcTemplate.query(
            sql
        ) { rs, _ ->
            nextPageAfterId = rs.getLong(1)
            val timestamp = rs.getTimestamp(2)
            val username = rs.getString(3)
            val clazz = Class.forName(rs.getString(4))
            val data = rs.getString(5)
            val event = Event(metadata = Event.Metadata(timestamp = timestamp.toInstant(), username = username), data = mapper.readValue(data, clazz))
            results += event
        }

        return Page(results, nextPageAfterId.toString())
    }

    override suspend fun <T : Any> store(event: Event<T>) {
        jdbcTemplate.update(
            "insert into events (timestamp, username, type, contents) values (?, ?, ?, ?::jsonb)",
            event.metadata.timestamp.atOffset(ZoneOffset.UTC),
            event.metadata.username,
            event.data::class.java.name,
            mapper.writeValueAsString(event.data)
        )
    }
}