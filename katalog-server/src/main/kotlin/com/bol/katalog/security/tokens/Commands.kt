package com.bol.katalog.security.tokens

import com.bol.katalog.cqrs.Command
import com.bol.katalog.security.GroupId
import com.bol.katalog.users.GroupPermission
import com.bol.katalog.users.UserId

data class IssueTokenCommand(
    val id: TokenId,
    val description: String,
    val subjectId: UserId,
    val groupId: GroupId,
    val permissions: Set<GroupPermission>
) : Command

data class RevokeTokenCommand(
    val id: TokenId
) : Command

typealias TokenId = String
