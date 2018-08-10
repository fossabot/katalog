package com.bol.blueprint.plugin.postgres

import com.bol.blueprint.store.BlobStore
import com.bol.blueprint.store.EventStore
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate

@Configuration
class PostgresAutoConfiguration {
    @Bean
    fun postgresEventStore(jdbcTemplate: JdbcTemplate): EventStore {
        return PostgresEventStore(jdbcTemplate)
    }

    @Bean
    fun postgresBlobStore(jdbcTemplate: JdbcTemplate): BlobStore {
        return PostgresBlobStore(jdbcTemplate)
    }
}