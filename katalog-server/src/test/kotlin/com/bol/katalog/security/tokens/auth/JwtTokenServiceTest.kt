package com.bol.katalog.security.tokens.auth

import com.bol.katalog.security.KatalogUserDetailsHolder
import com.bol.katalog.security.SecurityAggregate
import com.bol.katalog.security.config.SecurityConfigurationProperties
import com.bol.katalog.security.support.created
import com.bol.katalog.security.support.user1
import com.bol.katalog.security.support.userReadOnly
import com.bol.katalog.support.AggregateTester
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.util.*

class JwtTokenServiceTest {
    private val tester = AggregateTester.of { ctx, _ ->
        listOf(SecurityAggregate(ctx))
    }

    @Test
    fun `Can validate correct token`() {
        tester.run {
            given(user1.created(), userReadOnly.created())

            val key = Keys.secretKeyFor(SignatureAlgorithm.HS512)
            val hmacShaKey = Base64.getEncoder().encodeToString(key.encoded)
            val properties = SecurityConfigurationProperties()
            properties.token.hmacShaKey = hmacShaKey
            val tokenService = JwtTokenService(properties, aggregate())
            val token = runBlocking { tokenService.issueToken(user1.id, userReadOnly.id) }

            val authentication = runBlocking { tokenService.authenticate(token) }
            val user = authentication?.principal as KatalogUserDetailsHolder
            expectThat(user.username).isEqualTo(userReadOnly.username)
        }
    }
}