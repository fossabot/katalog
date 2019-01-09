package com.bol.katalog.security

import com.bol.katalog.cqrs.Processor
import com.bol.katalog.users.GroupPermission
import com.bol.katalog.users.UserId
import org.springframework.security.core.GrantedAuthority
import org.springframework.stereotype.Component

@Component
class SecurityProcessor(
    private val processor: Processor
) {
    suspend fun createGroup(id: GroupId, name: String) {
        processor.apply(CreateGroupCommand(id, name))
    }

    suspend fun createUser(id: UserId, username: String, encodedPassword: String?, authorities: Set<GrantedAuthority>) {
        processor.apply(CreateUserCommand(id, username, encodedPassword, authorities))
    }

    suspend fun addUserToGroup(userId: UserId, groupId: GroupId, permissions: Set<GroupPermission>) {
        processor.apply(AddUserToGroupCommand(userId, groupId, permissions))
    }
}