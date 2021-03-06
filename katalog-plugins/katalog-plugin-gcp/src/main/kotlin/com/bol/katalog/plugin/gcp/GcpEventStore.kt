package com.bol.katalog.plugin.gcp

import com.bol.katalog.cqrs.Event
import com.bol.katalog.cqrs.PersistentEvent
import com.bol.katalog.store.EventQuery
import com.bol.katalog.store.EventStore
import com.bol.katalog.store.Page
import com.google.cloud.Timestamp
import com.google.cloud.datastore.*

class GcpEventStore(private val datastore: Datastore) : EventStore {
    private var keyFactory = datastore.newKeyFactory().setKind("Events")

    override suspend fun get(query: EventQuery): Page<PersistentEvent<Event>> {
        val results = mutableListOf<PersistentEvent<Event>>()

        val startCursor = if (query.cursor != null) {
            Cursor.fromUrlSafe(query.cursor)
        } else {
            null
        }

        val entityQuery = Query.newEntityQueryBuilder()
            .setKind("Events")
            .setLimit(query.pageSize)
            .setStartCursor(startCursor)
            .setOrderBy(StructuredQuery.OrderBy.asc("timestamp"))
            .build()
        val entityQueryResults = datastore.run(entityQuery)
        entityQueryResults.forEach {
            val clazz = Class.forName(it.getString("type"))
            val timestamp = it.getTimestamp("timestamp").toSqlTimestamp().toInstant()
            val userId = it.getString("userId")
            val data = GcpObjectMapper.get().readValue(it.getString("contents"), clazz) as Event
            results += PersistentEvent(
                PersistentEvent.Metadata(
                    timestamp = timestamp,
                    userId = userId
                ), data
            )
        }
        return Page(results, entityQueryResults.cursorAfter?.toUrlSafe())
    }

    override suspend fun <T : Event> store(event: PersistentEvent<T>): PersistentEvent<T> {
        val key = keyFactory.newKey()
        val entity = Entity.newBuilder(key)
            .set("timestamp", Timestamp.of(java.sql.Timestamp.from(event.metadata.timestamp)))
            .set("userId", event.metadata.userId)
            .set("type", event.data::class.java.name)
            .set("contents", GcpObjectMapper.get().writeValueAsString(event.data))
            .build()

        datastore.add(entity)
        return event
    }

    fun deleteAll() {
        val results = datastore.run(Query.newEntityQueryBuilder().build())
        results.forEach { datastore.delete(it.key) }
    }
}