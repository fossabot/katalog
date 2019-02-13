package com.bol.katalog.security.tokens

import com.bol.katalog.cqrs.AbstractAggregate
import com.bol.katalog.cqrs.AggregateContext
import com.bol.katalog.security.CreateUserCommand
import com.bol.katalog.security.SecurityAggregate
import com.bol.katalog.users.UserId
import org.springframework.stereotype.Component

@Component
class TokensAggregate(
    context: AggregateContext,
    private val security: SecurityAggregate
) : AbstractAggregate(context) {
    init {
        setup {
            command<IssueTokenCommand> {
                val issuer = security.findUserById(userId)
                require(CreateUserCommand(command.subjectId, "Token", null, emptySet()))
                event(TokenIssuedEvent(command.id, command.subjectId))
            }

            event<TokenIssuedEvent> {
                addToken(event.id, userId, event.subjectId)
            }
        }
    }

    suspend fun addToken(tokenId: TokenId, userId: UserId, subjectId: UserId) {
        val token = Token(tokenId, subjectId)
        getTokens().write { put(userId, token) }
    }

    suspend fun getTokens() = context.multiMap<UserId, Token>("security/v1/tokens")

    override suspend fun reset() {
        getTokens().reset()
    }
}