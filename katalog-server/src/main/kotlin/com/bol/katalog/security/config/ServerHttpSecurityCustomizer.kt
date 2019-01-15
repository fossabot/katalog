package com.bol.katalog.security.config

import org.springframework.security.config.web.server.ServerHttpSecurity

interface ServerHttpSecurityCustomizer {
    fun customize(http: ServerHttpSecurity)
}