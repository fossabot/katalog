package com.bol.katalog.api.v1

import com.bol.katalog.domain.UserGroup
import com.bol.katalog.domain.allPermissions
import com.bol.katalog.security.CoroutineUserContext
import com.bol.katalog.security.groups.GroupService
import com.bol.katalog.security.monoWithUserDetails
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/groups")
@PreAuthorize("hasAnyRole('USER', 'DEPLOYER')")
class GroupResource(private val groupService: GroupService) {
    @GetMapping
    fun getGroups() = monoWithUserDetails {
        CoroutineUserContext.get()?.let { user ->
            when (user.isAdmin()) {
                true -> groupService.getAvailableGroups().map { UserGroup(it, allPermissions()) }
                false -> groupService.getUserGroups(user)
            }
        } ?: emptyList()
    }
}