package com.bol.katalog.security.tokens

import com.bol.katalog.users.UserId

data class Token(val id: TokenId, val subject: UserId)