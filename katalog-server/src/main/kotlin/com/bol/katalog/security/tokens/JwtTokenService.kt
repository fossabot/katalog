package com.bol.katalog.security.tokens

import com.bol.katalog.domain.Group
import com.bol.katalog.domain.UserGroup
import com.bol.katalog.domain.allPermissions
import com.bol.katalog.security.KatalogUserDetails
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import java.security.Key
import java.util.*

class JwtTokenService(
    hmacShaKey: String,
    private val userDetailsService: ReactiveUserDetailsService
) : TokenService {
    private val key: Key

    init {
        val keyBytes = Base64.getDecoder().decode(hmacShaKey)
        key = Keys.hmacShaKeyFor(keyBytes)
    }

    override suspend fun authenticate(token: String): Authentication? {
        val jwt = parseJwt(token)
        if (jwt != null) {
            val user = (userDetailsService.findByUsername(jwt.body.subject).awaitFirstOrNull()) as KatalogUserDetails?
            if (user != null) {
                val roleCsv = jwt.body["roles"] as String?
                val groupCsv = jwt.body["groups"] as String?

                if (roleCsv != null && groupCsv != null) {
                    val authorities = roleCsv.split(',').map {
                        SimpleGrantedAuthority(it)
                    }

                    val groups = groupCsv.split(',').map {
                        UserGroup(Group(it), allPermissions())
                    }

                    val bearerUserDetails =
                        BearerUserDetails(user, authorities, groups)
                    return AnonymousAuthenticationToken("bearer", bearerUserDetails, authorities)
                }
            }
        }

        return null
    }

    override suspend fun issueToken(
        user: KatalogUserDetails,
        authorities: Collection<GrantedAuthority>,
        groups: Collection<Group>
    ): String {
        val roleCsv = authorities.mapNotNull { it.authority }.joinToString(",")
        val groupCsv = groups.joinToString(",") { it.name }

        return Jwts.builder()
            .setIssuer(user.username)
            .setSubject(user.username)
            .claim("roles", roleCsv)
            .claim("groups", groupCsv)
            .signWith(key)
            .compact()
    }

    private fun parseJwt(it: String): Jws<Claims>? {
        return try {
            Jwts.parser().setSigningKey(key).parseClaimsJws(it)
        } catch (e: JwtException) {
            null
        }
    }

    class BearerUserDetails(
        userDetails: KatalogUserDetails,
        private val authorities: Collection<GrantedAuthority>,
        private val groups: Collection<UserGroup>
    ) : KatalogUserDetails by userDetails {
        override fun getAuthorities() = authorities

        override fun getGroups() = groups
    }
}