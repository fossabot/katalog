package com.bol.katalog.api.v1

import com.bol.katalog.cqrs.Aggregate
import com.bol.katalog.security.CoroutineUserContext
import com.bol.katalog.security.SecurityState
import com.bol.katalog.security.monoWithUserDetails
import com.bol.katalog.users.GroupPermission
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/groups")
@PreAuthorize("hasAnyRole('USER', 'DEPLOYER')")
class GroupResource(private val security: Aggregate<SecurityState>) {
    data class GroupResponse(
        val id: String,
        val name: String,
        val permissions: Set<GroupPermission>
    )

    @GetMapping
    fun getGroups() = monoWithUserDetails {
        CoroutineUserContext.get()?.let { user ->
            security.read {
                getGroups(user)
                    .map {
                        GroupResponse(it.id.value, it.name, getPermissions(user, it.id).toSet())
                    }
            }
        } ?: emptyList()
    }
}