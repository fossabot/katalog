package com.bol.katalog.plugin.postgres

import com.bol.katalog.messaging.MessageBus
import com.bol.katalog.store.BlobStore
import com.bol.katalog.store.EventStore
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate

@Configuration
class PostgresAutoConfiguration {
    @Bean
    @ConditionalOnProperty("katalog.event-store.type", havingValue = "postgres")
    fun postgresEventStore(jdbcTemplate: JdbcTemplate): EventStore {
        return PostgresEventStore(jdbcTemplate)
    }

    @Bean
    @ConditionalOnProperty("katalog.blob-store.type", havingValue = "postgres")
    fun postgresBlobStore(jdbcTemplate: JdbcTemplate): BlobStore {
        return PostgresBlobStore(jdbcTemplate)
    }

    @Bean
    @ConditionalOnProperty("katalog.message-bus.type", havingValue = "postgres")
    fun postgresMessageBus(jdbcTemplate: JdbcTemplate): MessageBus {
        return PostgresMessageBus(jdbcTemplate.dataSource!!)
    }
}