package com.bol.katalog.api.v1

import com.bol.katalog.security.SecurityAggregate
import com.bol.katalog.security.monoWithUserId
import com.bol.katalog.users.GroupPermission
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/groups")
@PreAuthorize("hasRole('USER')")
class GroupResource(private val security: SecurityAggregate) {
    data class GroupResponse(
        val id: String,
        val name: String,
        val permissions: Set<GroupPermission>
    )

    @GetMapping
    fun getGroups() = monoWithUserId {
        security.findUserById(userId)?.let { user ->
            security.getGroupsForUser(user)
                .map {
                    GroupResponse(it.id.value, it.name, security.getPermissions(user, it.id).toSet())
                }
        } ?: emptyList()
    }
}