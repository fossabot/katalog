package com.bol.katalog.api.v1

import com.bol.katalog.cqrs.Aggregate
import com.bol.katalog.security.CoroutineUserIdContext
import com.bol.katalog.security.SecurityState
import com.bol.katalog.security.config.AuthType
import com.bol.katalog.security.config.SecurityConfigurationProperties
import com.bol.katalog.security.monoWithUserId
import com.bol.katalog.users.UserId
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.reactor.mono
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthResource(
    private val properties: SecurityConfigurationProperties,
    private val security: Aggregate<SecurityState>
) {
    data class User(
        val id: UserId,
        val username: String,
        val authorities: Set<String>
    )

    data class LoginOptions(
        val type: AuthType,
        val oauth2ProviderName: String?,
        val oauth2LoginUrl: String?
    )

    @GetMapping("user-details")
    @PreAuthorize("hasAnyRole('USER', 'DEPLOYER')")
    fun getUserDetails() = monoWithUserId {
        val userId = CoroutineUserIdContext.get()!!
        val user = security.read { findUserById(userId) }!!
        User(
            user.id,
            user.username,
            user.authorities.map { it.authority }.toSet()
        )
    }

    @GetMapping("login-options")
    fun getLoginOptions() = GlobalScope.mono {
        when (properties.auth.type) {
            AuthType.FORM -> LoginOptions(AuthType.FORM, null, null)
            AuthType.OAUTH2 ->
                LoginOptions(
                    properties.auth.type,
                    properties.auth.oauth2.registrationId,
                    "oauth2/authorization/${properties.auth.oauth2.registrationId}"
                )
        }
    }
}