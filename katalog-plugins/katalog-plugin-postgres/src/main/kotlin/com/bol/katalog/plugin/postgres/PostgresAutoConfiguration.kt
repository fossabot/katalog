package com.bol.katalog.plugin.postgres

import com.bol.katalog.store.BlobStore
import com.bol.katalog.store.EventStore
import com.bol.katalog.store.TaskStore
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

    @Bean
    fun postgresTaskStore(jdbcTemplate: JdbcTemplate): TaskStore {
        return PostgresTaskStore(jdbcTemplate.dataSource!!)
    }
}