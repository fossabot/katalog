package com.bol.katalog.plugin.postgres

import com.bol.katalog.cqrs.Event
import com.bol.katalog.cqrs.PersistentEvent
import com.bol.katalog.store.EventQuery
import com.bol.katalog.store.EventStore
import com.bol.katalog.store.Page
import org.springframework.jdbc.core.JdbcTemplate
import java.time.ZoneOffset

class PostgresEventStore(private val jdbcTemplate: JdbcTemplate) : EventStore {
    override suspend fun get(query: EventQuery): Page<PersistentEvent<Event>> {
        val pageSizePlusOne = query.pageSize + 1
        val resultsPlusOne = mutableListOf<PersistentEvent<Event>>()

        var sql = "select id, timestamp, userId, type, contents from events"
        query.cursor?.let { sql += " where id > $it" }
        sql += " order by id limit $pageSizePlusOne"

        var lastRowId = 0L
        jdbcTemplate.query(
            sql
        ) { rs, _ ->
            lastRowId = rs.getLong(1)
            val timestamp = rs.getTimestamp(2)
            val userId = rs.getString(3)
            val clazz = Class.forName(rs.getString(4))
            val data = rs.getString(5)
            val event = PersistentEvent(
                metadata = PersistentEvent.Metadata(
                    timestamp = timestamp.toInstant(),
                    userId = userId
                ),
                data = PostgresObjectMapper.get().readValue(data, clazz) as Event
            )
            resultsPlusOne += event
        }

        val cursor = if (resultsPlusOne.size == pageSizePlusOne) {
            // We got more than the page size, so we would be able to cursor to the next page
            lastRowId.toString()
        } else {
            null
        }

        return Page(resultsPlusOne.take(query.pageSize), cursor)
    }

    override suspend fun <T : Event> store(event: PersistentEvent<T>): PersistentEvent<T> {
        jdbcTemplate.update(
            "insert into events (timestamp, userId, type, contents) values (?, ?, ?, ?::jsonb)",
            event.metadata.timestamp.atOffset(ZoneOffset.UTC),
            event.metadata.userId,
            event.data::class.java.name,
            PostgresObjectMapper.get().writeValueAsString(event.data)
        )
        return event
    }
}