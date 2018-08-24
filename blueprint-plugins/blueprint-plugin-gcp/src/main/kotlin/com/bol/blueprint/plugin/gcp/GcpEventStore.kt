package com.bol.blueprint.plugin.gcp

import com.bol.blueprint.domain.Event
import com.bol.blueprint.domain.Event.Companion.event
import com.bol.blueprint.store.EventQuery
import com.bol.blueprint.store.EventStore
import com.bol.blueprint.store.Page
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.google.cloud.Timestamp
import com.google.cloud.datastore.*

class GcpEventStore(private val datastore: Datastore) : EventStore {
    private var keyFactory = datastore.newKeyFactory().setKind("Events")
    private val mapper = ObjectMapper()

    init {
        mapper.registerModule(KotlinModule())
        mapper.registerModule(JavaTimeModule())
    }

    override suspend fun get(query: EventQuery): Page<Event<Any>> {
        val results = mutableListOf<Event<Any>>()

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
            val data = mapper.readValue(it.getString("contents"), clazz)
            results += event(Event.Metadata(timestamp = timestamp)) { data }
        }
        return Page(results, entityQueryResults.cursorAfter?.toUrlSafe())
    }

    override suspend fun <T : Any> store(event: Event<T>) {
        val key = keyFactory.newKey()
        val entity = Entity.newBuilder(key)
                .set("timestamp", Timestamp.of(java.sql.Timestamp.from(event.metadata.timestamp)))
                .set("type", event.data::class.java.name)
                .set("contents", mapper.writeValueAsString(event.data))
                .build()

        datastore.add(entity)
    }

    fun deleteAll() {
        val results = datastore.run(Query.newEntityQueryBuilder().build())
        results.forEach { datastore.delete(it.key) }
    }
}