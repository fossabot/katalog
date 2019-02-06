package com.bol.katalog.security.tokens

import com.bol.katalog.cqrs.Aggregate
import com.bol.katalog.cqrs.AggregateContext
import com.bol.katalog.cqrs.CqrsAggregate
import com.bol.katalog.security.CreateUserCommand
import com.bol.katalog.security.Security
import org.springframework.stereotype.Component

@Component
internal class TokensAggregate(
    context: AggregateContext,
    private val security: Aggregate<Security>
) : CqrsAggregate<Tokens>(context, Tokens(context)) {
    override fun getCommandHandler() = commandHandler {
        handle<IssueTokenCommand> {
            //val issuer = security.read { findUserById(metadata.userId) }
            require(CreateUserCommand(command.subjectId, "Token", null, emptySet()))
            event(TokenIssuedEvent(command.id, command.subjectId))
        }
    }

    override fun getEventHandler() = eventHandler {
        handle<TokenIssuedEvent> {
            state.addToken(event.id, metadata.userId, event.subjectId)
        }
    }
}
