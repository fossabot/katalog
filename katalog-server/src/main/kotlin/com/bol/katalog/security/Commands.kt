package com.bol.katalog.security

import com.bol.katalog.cqrs.Command
import com.bol.katalog.users.GroupPermission
import com.bol.katalog.users.UserId

data class CreateGroupCommand(val id: GroupId, val name: String) : Command

data class DisableGroupCommand(val id: GroupId) : Command

data class CreateUserCommand(
    val id: UserId,
    val username: String,
    val encodedPassword: String?,
    val authorities: Set<String>
) : Command

data class AddUserToGroupCommand(val userId: UserId, val groupId: GroupId, val permissions: Set<GroupPermission>) :
    Command

data class RemoveUserFromGroupCommand(val userId: UserId, val groupId: GroupId) : Command

data class DisableUserCommand(val id: UserId) : Command