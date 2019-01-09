package com.bol.katalog.api.v1

import com.bol.katalog.security.CoroutineUserContext
import com.bol.katalog.security.GroupPermission
import com.bol.katalog.security.SecurityAggregate
import com.bol.katalog.security.monoWithUserDetails
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/groups")
@PreAuthorize("hasAnyRole('USER', 'DEPLOYER')")
class GroupResource(private val security: SecurityAggregate) {
    data class GroupResponse(
        val id: String,
        val name: String,
        val permissions: Collection<GroupPermission>
    )

    @GetMapping
    fun getGroups() = monoWithUserDetails {
        CoroutineUserContext.get()?.let { user ->
            security.getGroups(user)
                .map {
                    GroupResponse(it.id, it.name, security.getPermissions(user, it.id))
                }
        } ?: emptyList()
    }
}