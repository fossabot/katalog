package com.bol.blueprint.domain

import java.security.Principal

interface PrincipalEnforcingCommandHandler {
    suspend fun withPrincipal(principal: Principal, block: suspend CommandHandler.() -> Unit)
}