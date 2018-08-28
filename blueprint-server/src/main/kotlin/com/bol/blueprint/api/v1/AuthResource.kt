package com.bol.blueprint.api.v1

import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthResource {
    data class User(val name: String)

    @GetMapping("user")
    fun getUser(@AuthenticationPrincipal principal: UserDetails) = principal
}