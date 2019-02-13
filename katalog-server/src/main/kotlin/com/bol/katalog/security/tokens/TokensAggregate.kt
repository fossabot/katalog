package com.bol.katalog.security.tokens

import com.bol.katalog.cqrs.AbstractAggregate
import com.bol.katalog.cqrs.AggregateContext
import com.bol.katalog.cqrs.ForbiddenException
import com.bol.katalog.cqrs.NotFoundException
import com.bol.katalog.security.*
import com.bol.katalog.users.UserId
import org.springframework.stereotype.Component

@Component
class TokensAggregate(context: AggregateContext, security: SecurityAggregate) : AbstractAggregate(context) {
    private val tokens = context.multiMap<UserId, Token>("security/v1/tokens")

    init {
        setup {
            command<IssueTokenCommand> {
                if (!security.groupHasMember(command.groupId, userId)) {
                    throw ForbiddenException("User may not issue tokens for groups they are not a member of")
                }

                require(
                    CreateUserCommand(
                        command.subjectId,
                        "token-$userId-${command.subjectId}",
                        null,
                        setOf("ROLE_USER"),
                        userId
                    )
                )
                require(AddUserToGroupCommand(command.subjectId, command.groupId, command.permissions))
                event(TokenIssuedEvent(command.id, command.description, command.subjectId))
            }

            command<RevokeTokenCommand> {
                val token = getById(userId, command.id)
                require(DisableUserCommand(token.subjectId))
                event(TokenRevokedEvent(command.id))
            }

            event<TokenIssuedEvent> {
                tokens.write {
                    put(userId, Token(event.id, event.description, event.subjectId))
                }
            }

            event<TokenRevokedEvent> {
                tokens.write {
                    val token = getById(userId, event.id)
                    remove(userId, token)
                }
            }
        }
    }

    suspend fun getById(userId: UserId, tokenId: TokenId): Token {
        return tokens.read { this[userId].orEmpty() }.singleOrNull { it.id == tokenId }
            ?: throw NotFoundException("Unknown token id: $tokenId, for user id: $userId")
    }

    suspend fun getAllTokens(): Sequence<Token> {
        val userId = CoroutineUserIdContext.get()!!
        return tokens.read { this[userId].orEmpty() }.asSequence()
    }

    override suspend fun reset() {
        tokens.reset()
    }
}