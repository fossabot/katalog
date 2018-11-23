package com.bol.katalog

import com.bol.katalog.domain.Group
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.UserDetails
import reactor.core.publisher.Mono

interface CurrentUserSupplier {
    suspend fun getCurrentUser(): KatalogUserDetails?
}

class ReactiveSecurityContextCurrentUserSupplier : CurrentUserSupplier {
    override suspend fun getCurrentUser(): KatalogUserDetails? =
        ReactiveSecurityContextHolder.getContext().awaitFirstOrNull()?.authentication?.principal as KatalogUserDetails?
}

class KatalogUserDetails(
    private val username: String,
    private val password: String,
    private val authorities: Collection<GrantedAuthority>,
    private val groups: List<Group>
) : UserDetails {
    override fun getAuthorities() = authorities

    override fun isEnabled() = true

    override fun getUsername() = username

    override fun isCredentialsNonExpired() = true

    override fun getPassword() = password

    override fun isAccountNonExpired() = true

    override fun isAccountNonLocked() = true

    fun getGroups() = groups
}

class ReactiveKatalogUserDetailsService(private val users: List<KatalogUserDetails>) : ReactiveUserDetailsService {
    override fun findByUsername(username: String?): Mono<UserDetails> =
        Mono.just(users.first { it.username == username })
}