package com.bol.blueprint.plugins.postgres

import com.bol.blueprint.plugin.postgres.PostgresAutoConfiguration
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
@ImportAutoConfiguration(PostgresAutoConfiguration::class)
class TestApplication
