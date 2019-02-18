package com.bol.katalog.api.v1

import com.bol.katalog.security.*
import com.bol.katalog.security.config.AuthType
import com.bol.katalog.security.config.SecurityConfigurationProperties
import com.bol.katalog.users.UserId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
    private val security: SecurityAggregate
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
    @PreAuthorize("hasRole('USER')")
    fun getUserDetails() = monoWithUserId {
        val userId = CoroutineUserIdContext.get()!!
        val user = withUserId(SystemUser.get().id) { security.findUserById(userId) }!!
        User(
            user.id,
            user.username,
            user.authorities.map { it.authority }.toSet()
        )
    }

    @UseExperimental(ExperimentalCoroutinesApi::class)
    @GetMapping("login-options")
    fun getLoginOptions() = GlobalScope.mono(Dispatchers.Unconfined) {
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