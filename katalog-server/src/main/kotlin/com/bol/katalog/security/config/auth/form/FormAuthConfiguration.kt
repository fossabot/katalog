package com.bol.katalog.security.config.auth.form

import com.bol.katalog.security.config.ServerHttpSecurityCustomizer
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository
import reactor.core.publisher.Mono

@Configuration
@ConditionalOnProperty("katalog.security.auth.type", havingValue = "FORM", matchIfMissing = false)
class FormAuthConfiguration {
    @Bean
    @ConditionalOnMissingBean
    fun formLoginSecurityWebFilterChain() = object : ServerHttpSecurityCustomizer {
        override fun customize(http: ServerHttpSecurity) {
            http
                .formLogin().securityContextRepository(WebSessionServerSecurityContextRepository())
                .loginPage("/api/v1/auth/login")
                .authenticationEntryPoint(HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED))
                .authenticationSuccessHandler { _, _ -> Mono.empty<Void>() }
                .authenticationFailureHandler { filterExchange, _ ->
                    Mono.fromRunnable {
                        filterExchange.exchange.response.apply {
                            statusCode = HttpStatus.UNAUTHORIZED
                        }
                    }
                }
        }
    }
}