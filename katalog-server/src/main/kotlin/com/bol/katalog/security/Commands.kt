package com.bol.katalog.security

import com.bol.katalog.domain.Command
import com.bol.katalog.users.GroupPermission
import com.bol.katalog.users.UserId
import org.springframework.security.core.GrantedAuthority

data class CreateGroupCommand(val id: GroupId, val name: String) : Command()

data class CreateUserCommand(
    val id: UserId,
    val username: String,
    val encodedPassword: String?,
    val authorities: Set<GrantedAuthority>
) : Command()

data class AddUserToGroupCommand(val userId: UserId, val groupId: GroupId, val permissions: Set<GroupPermission>) :
    Command()