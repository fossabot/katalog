package com.bol.katalog.security.tokens

import com.bol.katalog.cqrs.Command
import com.bol.katalog.security.GroupId
import com.bol.katalog.users.GroupPermission
import com.bol.katalog.users.UserId

data class IssueTokenCommand(
    val id: TokenId,
    val username: String,
    val subjectId: UserId, val groupId: GroupId, val permissions: Set<GroupPermission>
) : Command

typealias TokenId = String
