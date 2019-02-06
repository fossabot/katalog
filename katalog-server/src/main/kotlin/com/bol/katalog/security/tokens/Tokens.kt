package com.bol.katalog.security.tokens

import com.bol.katalog.cqrs.AggregateContext
import com.bol.katalog.cqrs.State
import com.bol.katalog.users.UserId

data class Tokens(private val context: AggregateContext) : State {
    internal val tokens: MutableMap<UserId, MutableList<Token>> = context.getMap("security/v1/tokens")

    fun addToken(tokenId: TokenId, userId: UserId, subjectId: UserId) {
        val token = Token(tokenId, subjectId)
        tokens.getOrPut(userId) { mutableListOf() }.add(token)
    }
}