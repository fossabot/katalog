package com.bol.blueprint.api.v1

import com.bol.blueprint.domain.UserGroupService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthResource(
        val userGroupService: UserGroupService
) {
    data class User(val name: String)

    @GetMapping("user-details")
    fun getUserDetails(@AuthenticationPrincipal principal: UserDetails) = principal

    @GetMapping("user-groups")
    fun getUserGroups(@AuthenticationPrincipal principal: UserDetails) = userGroupService.getGroupsByUsername(principal.username)
}