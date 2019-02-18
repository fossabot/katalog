package com.bol.katalog.security.tokens

import com.bol.katalog.cqrs.Event
import com.bol.katalog.users.UserId

data class TokenIssuedEvent(val id: TokenId, val description: String, val subjectId: UserId, val token: String) : Event
data class TokenRevokedEvent(val id: TokenId) : Event