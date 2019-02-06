package com.bol.katalog.security.tokens.auth

import com.bol.katalog.cqrs.Aggregate
import com.bol.katalog.security.KatalogUserDetailsHolder
import com.bol.katalog.security.Security
import com.bol.katalog.security.asAuthentication
import com.bol.katalog.security.config.SecurityConfigurationProperties
import com.bol.katalog.users.UserId
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import java.security.Key
import java.util.*

@Component
class JwtTokenService(
    properties: SecurityConfigurationProperties,
    private val security: Aggregate<Security>
) : TokenService {
    private val key: Key

    init {
        val keyBytes = Base64.getDecoder().decode(properties.token.hmacShaKey)
        key = Keys.hmacShaKeyFor(keyBytes)
    }

    override suspend fun authenticate(token: String): Authentication? {
        return parseJwt(token)?.let { jwt ->
            security.readAs(jwt.body.issuer) { findUserById(jwt.body.subject) }?.let { user ->
                KatalogUserDetailsHolder(user).asAuthentication()
            }
        }
    }

    override suspend fun issueToken(issuer: UserId, userId: UserId): String {
        return Jwts.builder()
            .setIssuer(issuer)
            .setSubject(userId)
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
}