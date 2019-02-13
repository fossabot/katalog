package com.bol.katalog.security.tokens.auth

import com.bol.katalog.security.KatalogUserDetailsHolder
import com.bol.katalog.security.SecurityAggregate
import com.bol.katalog.security.config.SecurityConfigurationProperties
import com.bol.katalog.security.support.created
import com.bol.katalog.security.support.user1
import com.bol.katalog.security.support.user2
import com.bol.katalog.support.AggregateTester
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.util.*

class JwtTokenServiceTest {
    private val tester = AggregateTester.of { ctx, _ ->
        listOf(SecurityAggregate(ctx))
    }

    private val key = Keys.secretKeyFor(SignatureAlgorithm.HS512)
    private val hmacShaKey = Base64.getEncoder().encodeToString(key.encoded)
    private lateinit var properties: SecurityConfigurationProperties

    @BeforeEach
    fun before() {
        properties = SecurityConfigurationProperties()
        properties.token.hmacShaKey = hmacShaKey
    }

    @Test
    fun `Can validate correct token`() {
        tester.run {
            given(
                user1.created(),
                user2.created()
            )

            val tokenService = JwtTokenService(properties, context.get())
            val token = runBlocking { tokenService.issueToken(user1.id, user2.id) }

            val authentication = runBlocking { tokenService.authenticate(token) }
            val user = authentication?.principal as KatalogUserDetailsHolder
            expectThat(user.username).isEqualTo(user2.username)
        }
    }
}