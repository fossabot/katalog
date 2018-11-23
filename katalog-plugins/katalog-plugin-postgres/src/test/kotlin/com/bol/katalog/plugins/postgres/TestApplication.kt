package com.bol.katalog.plugins.postgres

import com.bol.katalog.plugin.postgres.PostgresAutoConfiguration
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
@ImportAutoConfiguration(PostgresAutoConfiguration::class)
class TestApplication
