package com.bol.blueprint

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

    override fun get(query: EventQuery): Page<EventBase> {
        val results = mutableListOf<EventBase>()

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
            val event = mapper.readValue(data, clazz) as EventBase
            results.add(event)
        }

        return Page(results, nextPageAfterId)
    }

    override fun store(event: EventBase) {
        jdbcTemplate.update(
            "insert into events (type, contents) values (?, ?::jsonb)",
            event::class.java.name, mapper.writeValueAsString(event)
        )
    }
}