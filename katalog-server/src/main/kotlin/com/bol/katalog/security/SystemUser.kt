package com.bol.katalog.security

import org.springframework.security.core.authority.SimpleGrantedAuthority

object SystemUser {
    private val systemUser = User(
        "system", "system", null, setOf(
            SimpleGrantedAuthority("ROLE_USER"),
            SimpleGrantedAuthority("ROLE_ADMIN")
        )
    )

    fun get() = systemUser
}