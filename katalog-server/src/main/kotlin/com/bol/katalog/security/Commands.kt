package com.bol.katalog.security

import com.bol.katalog.domain.Command

data class CreateGroupCommand(val id: GroupId, val name: String) : Command()

data class AddUserToGroupCommand(val userId: UserId, val groupId: GroupId, val permissions: List<GroupPermission>) :
    Command()