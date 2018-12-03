package com.bol.katalog.security.tokens

import com.bol.katalog.domain.Group
import com.bol.katalog.security.KatalogUserDetails
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority

interface TokenService {
    suspend fun authenticate(token: String): Authentication?

    suspend fun issueToken(
        user: KatalogUserDetails,
        authorities: Collection<GrantedAuthority>,
        groups: Collection<Group>
    ): String
}