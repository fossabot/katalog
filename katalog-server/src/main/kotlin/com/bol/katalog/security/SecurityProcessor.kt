package com.bol.katalog.security

import com.bol.katalog.cqrs.Processor
import org.springframework.stereotype.Component

@Component
class SecurityProcessor(
    private val processor: Processor
) {
    suspend fun createGroup(id: String, name: String) {
        processor.apply(CreateGroupCommand(id, name))
    }

    suspend fun addUserToGroup(userId: UserId, groupId: GroupId, permissions: List<GroupPermission>) {
        processor.apply(AddUserToGroupCommand(userId, groupId, permissions))
    }
}