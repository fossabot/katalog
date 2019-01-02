package com.bol.katalog.config.security

import org.springframework.security.config.web.server.ServerHttpSecurity

interface ServerHttpSecurityCustomizer {
    fun customize(http: ServerHttpSecurity)
}