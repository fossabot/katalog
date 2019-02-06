package com.bol.katalog.security.tokens

import com.bol.katalog.security.SecurityAggregate
import com.bol.katalog.security.UserCreatedEvent
import com.bol.katalog.security.support.created
import com.bol.katalog.security.support.user1
import com.bol.katalog.support.AggregateTester
import org.junit.jupiter.api.Test

class TokensAggregateTest {
    private val tester = AggregateTester.of { ctx, _ ->
        val security = SecurityAggregate(ctx)
        val tokens = TokensAggregate(ctx, security)
        listOf(security, tokens)
    }

    @Test
    fun `Can issue token`() {
        tester.run {
            given(
                user1.created()
            )

            val tokenId = "id-token"
            val subjectId = "id-token-user"
            sendAs(user1, IssueTokenCommand(tokenId, subjectId))

            expect {
                event(UserCreatedEvent(subjectId, "Token", null, emptySet()))
                event(TokenIssuedEvent(tokenId, subjectId))
            }
        }
    }
}