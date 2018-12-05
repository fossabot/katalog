package com.bol.katalog.api.v1

import com.bol.katalog.domain.UserGroup
import com.bol.katalog.security.KatalogUserDetails
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.reactor.mono
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
@PreAuthorize("hasAnyRole('USER', 'DEPLOYER')")
class AuthResource {
    data class User(
        val username: String,
        val enabled: Boolean,
        val authorities: Collection<String>,
        val groups: Collection<UserGroup>
    )

    @GetMapping("user-details")
    fun getUserDetails(@AuthenticationPrincipal userDetails: KatalogUserDetails) = GlobalScope.mono {
        User(
            userDetails.username,
            userDetails.isEnabled,
            userDetails.authorities.map { it.authority },
            userDetails.getGroups()
        )
    }
}