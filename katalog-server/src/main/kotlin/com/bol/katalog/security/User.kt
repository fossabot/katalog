package com.bol.katalog.security

import com.bol.katalog.users.UserId
import org.springframework.security.core.GrantedAuthority

data class User(
    val id: UserId,
    val username: String,
    val encodedPassword: String?,
    val authorities: Set<GrantedAuthority>
) {
    fun isAdmin() = authorities.any { it.authority == "ROLE_ADMIN" }
}
