package com.bol.katalog.security.tokens

import com.bol.katalog.users.UserId
import java.time.Instant

data class Token(
    val id: TokenId,
    val description: String,
    val subjectId: UserId,
    val createdOn: Instant
)