package com.bol.katalog.security.tokens

import com.bol.katalog.cqrs.Event
import com.bol.katalog.users.UserId

data class TokenIssuedEvent(val id: TokenId, val subjectId: UserId) : Event