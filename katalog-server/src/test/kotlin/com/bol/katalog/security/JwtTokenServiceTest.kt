package com.bol.katalog.security

import com.bol.katalog.domain.Group
import com.bol.katalog.domain.UserGroup
import com.bol.katalog.domain.allPermissions
import com.bol.katalog.security.tokens.JwtTokenService
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.test.context.junit4.SpringRunner
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEqualTo
import java.util.*

@RunWith(SpringRunner::class)
@SpringBootTest
class JwtTokenServiceTest {
    @Autowired
    private lateinit var userDetailsService: ReactiveUserDetailsService

    private lateinit var tokenService: JwtTokenService

    @Before
    fun before() {
        val key = Keys.secretKeyFor(SignatureAlgorithm.HS512)
        val hmacShaKey = Base64.getEncoder().encodeToString(key.encoded)
        tokenService = JwtTokenService(hmacShaKey, userDetailsService)
    }

    @Test
    fun `Can validate correct token`() {
        val authorities = listOf(SimpleGrantedAuthority("ROLE_A"), SimpleGrantedAuthority("ROLE_B"))
        val groups = listOf(
            UserGroup(Group("foo"), allPermissions()),
            UserGroup(Group("bar"), allPermissions())
        )

        val token = runBlocking {
            val user = userDetailsService.findByUsername("user").awaitSingle() as KatalogUserDetails
            tokenService.issueToken(user, authorities, groups.map { it.group })
        }

        val userDetails = runBlocking {
            val authentication = tokenService.authenticate(token)!!
            authentication.principal as JwtTokenService.BearerUserDetails
        }

        expectThat(userDetails.username).isEqualTo("user")
        expectThat(userDetails.authorities).containsExactly(authorities)

        // TODO: Make sure
    }
}