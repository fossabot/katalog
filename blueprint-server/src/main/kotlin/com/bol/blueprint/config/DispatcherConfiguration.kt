package com.bol.blueprint.config

import com.bol.blueprint.domain.Dispatcher
import com.bol.blueprint.queries.Query
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(Dispatcher::class)
class DispatcherConfiguration {
    @Bean
    fun query() = Query()
}