package com.bol.katalog.security

import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.oauth2.core.user.OAuth2User

interface KatalogUserDetails : UserDetails, OAuth2User {
    fun getUser(): User
}

class KatalogUserDetailsHolder(
    private val user: User
) : KatalogUserDetails {
    override fun getUser() = user

    override fun getAuthorities() = user.authorities

    override fun isEnabled() = true

    override fun getName() = user.username

    override fun getUsername() = user.username

    override fun isCredentialsNonExpired() = true

    override fun getPassword() = user.encodedPassword

    override fun isAccountNonExpired() = true

    override fun isAccountNonLocked() = true

    override fun getAttributes(): Map<String, Any> = mapOf()
}