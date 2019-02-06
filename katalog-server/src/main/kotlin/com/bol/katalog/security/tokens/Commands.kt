package com.bol.katalog.security.tokens

import com.bol.katalog.cqrs.Command
import com.bol.katalog.users.UserId

data class IssueTokenCommand(val id: TokenId, val subjectId: UserId) : Command

typealias TokenId = String
