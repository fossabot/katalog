package com.bol.katalog.plugin.postgres

import com.bol.katalog.domain.Event
import com.bol.katalog.domain.PersistentEvent
import com.bol.katalog.store.EventQuery
import com.bol.katalog.store.EventStore
import com.bol.katalog.store.Page
import org.springframework.jdbc.core.JdbcTemplate
import java.time.ZoneOffset

class PostgresEventStore(private val jdbcTemplate: JdbcTemplate) : EventStore {
    override suspend fun get(query: EventQuery): Page<PersistentEvent<Event>> {
        val results = mutableListOf<PersistentEvent<Event>>()

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
            val event = PersistentEvent(
                metadata = PersistentEvent.Metadata(timestamp = timestamp.toInstant(), username = username),
                data = PostgresObjectMapper.get().readValue(data, clazz) as Event
            )
            results += event
        }

        return Page(results, nextPageAfterId.toString())
    }

    override suspend fun <T : Event> store(event: PersistentEvent<T>) {
        jdbcTemplate.update(
            "insert into events (timestamp, username, type, contents) values (?, ?, ?, ?::jsonb)",
            event.metadata.timestamp.atOffset(ZoneOffset.UTC),
            event.metadata.username,
            event.data::class.java.name,
            PostgresObjectMapper.get().writeValueAsString(event.data)
        )
    }
}