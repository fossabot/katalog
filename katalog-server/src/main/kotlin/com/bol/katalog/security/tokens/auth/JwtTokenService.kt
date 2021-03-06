package com.bol.katalog.security.tokens.auth

import com.bol.katalog.security.*
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
import java.time.Instant
import java.util.*

@Component
class JwtTokenService(
    properties: SecurityConfigurationProperties,
    private val security: SecurityAggregate
) : TokenService {
    private val key: Key

    init {
        val keyBytes = Base64.getDecoder().decode(properties.token.hmacShaKey)
        key = Keys.hmacShaKeyFor(keyBytes)
    }

    override suspend fun authenticate(token: String): Authentication? {
        return parseJwt(token)?.let { jwt ->
            withUserId(SystemUser.get().id) {
                security.findUserById(jwt.body.subject)
            }?.let { subjectUser ->
                // Add the 'delegation' information to the user
                val user = subjectUser.copy(delegatedFromUserId = jwt.body.issuer)
                KatalogUserDetailsHolder(user).asAuthentication()
            }
        }
    }

    override suspend fun issueToken(issuer: UserId, subjectId: UserId, namespace: String, createdOn: Instant): String {
        return Jwts.builder()
            .setIssuer(issuer)
            .setSubject(subjectId)
            .setIssuedAt(Date.from(createdOn))
            .setHeaderParam("namespace", namespace)
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