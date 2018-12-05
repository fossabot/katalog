package com.bol.katalog.security

import com.bol.katalog.CoroutineLocal
import com.bol.katalog.domain.Group
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.UserDetails
import reactor.core.publisher.Mono

object CoroutineUserContext {
    private val currentUser = CoroutineLocal<KatalogUserDetails>()

    suspend fun get() = currentUser.get()
    suspend fun set(user: KatalogUserDetails?) {
        this.currentUser.set(user)
    }
}

interface KatalogUserDetails : UserDetails {
    fun getGroups(): Collection<Group>

    fun isAdmin(): Boolean {
        return this.authorities.any { it.authority == "ROLE_ADMIN" }
    }

    fun isInGroup(group: Group): Boolean = when {
        getGroups().contains(group) -> true
        isAdmin() -> true
        else -> false
    }
}

class KatalogUserDetailsHolder(
    private val username: String,
    private val password: String,
    private val authorities: Collection<GrantedAuthority>,
    private val groups: Collection<Group>
) : KatalogUserDetails {
    override fun getAuthorities() = authorities

    override fun isEnabled() = true

    override fun getUsername() = username

    override fun isCredentialsNonExpired() = true

    override fun getPassword() = password

    override fun isAccountNonExpired() = true

    override fun isAccountNonLocked() = true

    override fun getGroups() = groups
}

class ReactiveKatalogUserDetailsService(private val users: List<KatalogUserDetails>) : ReactiveUserDetailsService {
    override fun findByUsername(username: String?): Mono<UserDetails> =
        Mono.just(users.first { it.username == username })
}