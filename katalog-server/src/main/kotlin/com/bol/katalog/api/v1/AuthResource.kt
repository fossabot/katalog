package com.bol.katalog.api.v1

import com.bol.katalog.security.*
import com.bol.katalog.security.config.AuthType
import com.bol.katalog.security.config.SecurityConfigurationProperties
import com.bol.katalog.security.tokens.TokensAggregate
import com.bol.katalog.users.GroupPermission
import com.bol.katalog.users.UserId
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.reactor.mono
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/auth")
class AuthResource(
    private val properties: SecurityConfigurationProperties,
    private val security: SecurityAggregate,
    private val tokens: TokensAggregate
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

    object Requests {
        data class NewToken(val description: String, val permissions: Set<GroupPermission>)
    }

    @GetMapping("user-details")
    @PreAuthorize("hasAnyRole('USER', 'DEPLOYER')")
    fun getUserDetails() = monoWithUserId {
        val userId = CoroutineUserIdContext.get()!!
        val user = withUserId(SystemUser.get().id) { security.findUserById(userId) }!!
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

    @PostMapping("tokens")
    @ResponseStatus(HttpStatus.CREATED)
    fun issueToken(@RequestBody data: Requests.NewToken) = monoWithUserId {
        /*val userId = CoroutineUserIdContext.get()!!
        val user = security.readAs(SystemUser.get().id) { findUserById(userId) }!!

        // Create a new 'token' user
        val tokenUserId = userId + "-token-" + UUID.randomUUID()
        security.send(CreateUserCommand(tokenUserId, data.description, null, user.authorities.map { it.authority }.toSet()))

        // Assign the desired permissions to this user (filtered on what the user's permissions are)

        tokenService.issueToken(userId, tokenUserId)*/
    }
}