package com.bol.blueprint.eventstore

import com.bol.blueprint.Event
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate

class PostgresEventStore(
    @Autowired val jdbcTemplate: JdbcTemplate
) : EventStore {
    private val mapper = ObjectMapper()

    init {
        mapper.registerModule(KotlinModule())
    }

    override fun get(query: EventQuery): Page<Event> {
        val results = mutableListOf<Event>()

        var sql = "select id, type, contents from events"
        query.afterId?.let { sql += " where id > $it" }
        sql += " order by id limit ${query.pageSize}"

        var nextPageAfterId = 0L
        jdbcTemplate.query(
            sql
        ) { rs, _ ->
            nextPageAfterId = rs.getLong(1)
            val clazz = Class.forName(rs.getString(2))
            val data = rs.getString(3)
            val event = mapper.readValue(data, clazz) as Event
            results.add(event)
        }

        return Page(results, nextPageAfterId)
    }

    override fun store(event: Event) {
        jdbcTemplate.update(
            "insert into events (type, contents) values (?, ?::jsonb)",
            event::class.java.name, mapper.writeValueAsString(event)
        )
    }
}