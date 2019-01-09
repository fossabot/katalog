package com.bol.katalog.security

import com.bol.katalog.CoroutineLocal
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.oauth2.core.user.OAuth2User
import reactor.core.publisher.Mono

object CoroutineUserContext {
    private val currentUser = CoroutineLocal<KatalogUserDetails>()

    suspend fun get() = currentUser.get()
    suspend fun set(user: KatalogUserDetails?) {
        this.currentUser.set(user)
    }
}

interface KatalogUserDetails : UserDetails, OAuth2User {
    fun getId(): String
}

class KatalogUserDetailsHolder(
    private val id: String,
    private val username: String,
    private val password: String,
    private val authorities: Collection<GrantedAuthority>
) : KatalogUserDetails {
    override fun getId() = id

    override fun getAuthorities() = authorities

    override fun isEnabled() = true

    override fun getName() = username

    override fun getUsername() = username

    override fun isCredentialsNonExpired() = true

    override fun getPassword() = password

    override fun isAccountNonExpired() = true

    override fun isAccountNonLocked() = true

    override fun getAttributes(): Map<String, Any> = mapOf()
}

class ReactiveKatalogUserDetailsService(private val users: List<KatalogUserDetails>) : ReactiveUserDetailsService {
    override fun findByUsername(username: String?): Mono<UserDetails> =
        Mono.just(users.first { it.username == username })
}