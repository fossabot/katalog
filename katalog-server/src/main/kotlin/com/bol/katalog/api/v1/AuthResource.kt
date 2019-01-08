package com.bol.katalog.api.v1

import com.bol.katalog.config.security.AuthType
import com.bol.katalog.config.security.SecurityConfigurationProperties
import com.bol.katalog.domain.UserGroup
import com.bol.katalog.security.KatalogUserDetails
import com.bol.katalog.security.groups.GroupService
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.reactor.mono
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthResource(
    private val properties: SecurityConfigurationProperties,
    private val groupService: GroupService
) {
    data class User(
        val username: String,
        val enabled: Boolean,
        val authorities: Collection<String>,
        val groups: Collection<UserGroup>
    )

    data class LoginOptions(
        val type: AuthType,
        val oauth2ProviderName: String?,
        val oauth2LoginUrl: String?
    )

    @GetMapping("user-details")
    @PreAuthorize("hasAnyRole('USER', 'DEPLOYER')")
    fun getUserDetails(@AuthenticationPrincipal userDetails: KatalogUserDetails) = GlobalScope.mono {
        User(
            userDetails.username,
            userDetails.isEnabled,
            userDetails.authorities.map { it.authority },
            groupService.getUserGroups(userDetails)
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