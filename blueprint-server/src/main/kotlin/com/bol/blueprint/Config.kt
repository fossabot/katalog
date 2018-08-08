package com.bol.blueprint

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate

@Configuration
class Config {
    @Bean
    fun eventStore(jdbcTemplate: JdbcTemplate) = PostgresEventStore(jdbcTemplate)
}