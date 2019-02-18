package com.bol.katalog.security.tokens

import com.bol.katalog.cqrs.ForbiddenException
import com.bol.katalog.features.registry.Namespace
import com.bol.katalog.features.registry.RegistryAggregate
import com.bol.katalog.features.registry.support.created
import com.bol.katalog.security.SecurityAggregate
import com.bol.katalog.security.UserAddedToGroupEvent
import com.bol.katalog.security.UserCreatedEvent
import com.bol.katalog.security.support.*
import com.bol.katalog.store.inmemory.InMemoryBlobStore
import com.bol.katalog.support.AggregateTester
import com.bol.katalog.testing.TestData
import com.bol.katalog.users.GroupPermission
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isFalse
import strikt.assertions.isTrue

class TokensAggregateTest {
    private val tester = AggregateTester.of { ctx, perm ->
        val security = SecurityAggregate(ctx)
        val registry = RegistryAggregate(ctx, perm, InMemoryBlobStore())
        val tokens = TokensAggregate(ctx, security, registry, TestTokenService())
        listOf(security, tokens)
    }

    private val ns1 = Namespace("id-ns1", "ns1", group1.id, TestData.clock.instant())
    private val ns2 = Namespace("id-ns2", "ns2", group2.id, TestData.clock.instant())

    @Test
    fun `Can issue token`() {
        tester.run {
            given(
                user1.created(),
                group1.created(),
                ns1.created(),
                user1.addedToGroup(group1, setOf(GroupPermission.READ))
            )

            val tokenId = "id-token"
            val subjectId = "id-token-user"
            val permissions = setOf(GroupPermission.READ, GroupPermission.CREATE)
            sendAs(user1, IssueTokenCommand(tokenId, "token", subjectId, ns1.id, permissions))

            expect {
                event(UserCreatedEvent(subjectId, "token-id-user1-id-token-user", null, setOf("ROLE_USER"), user1.id))
                event(UserAddedToGroupEvent(subjectId, group1.id, permissions))
                event(TokenIssuedEvent(tokenId, "token", subjectId, "jwt-id-user1-id-token-user"))
            }
        }
    }

    @Test
    fun `Cannot issue token for namespaces the user does not belong to`() {
        tester.run {
            given(
                user1.created(),
                group1.created(),
                ns2.created(),
                user1.addedToGroup(group1, setOf(GroupPermission.READ))
            )

            val tokenId = "id-token"
            val subjectId = "id-token-user"
            val permissions = setOf(GroupPermission.READ, GroupPermission.CREATE)
            sendAs(user1, IssueTokenCommand(tokenId, "token", subjectId, ns2.id, permissions))

            expect {
                throws<ForbiddenException>()
            }
        }
    }

    @Test
    fun `Token permissions can never exceed issuer's permissions`() {
        tester.run {
            given(
                user1.created(),
                user2.created(),
                group1.created(),
                ns1.created(),
                user1.addedToGroup(group1, setOf(GroupPermission.READ)),
                user2.addedToGroup(group1, setOf(GroupPermission.READ, GroupPermission.CREATE))
            )

            val subjectId1 = "id-token-user1"
            val subjectId2 = "id-token-user2"
            val permissions = setOf(GroupPermission.READ, GroupPermission.CREATE)
            sendAs(user1, IssueTokenCommand("id-token1", "token1", subjectId1, ns1.id, permissions))
            sendAs(user2, IssueTokenCommand("id-token2", "token2", subjectId2, ns1.id, permissions))

            val security = context.get<SecurityAggregate>()
            runBlocking {
                // Issuers 'user1' and 'user2' have READ permission, so this should be fine
                expectThat(
                    security.hasPermission(
                        subjectId1,
                        group1.id,
                        GroupPermission.READ
                    )
                ).isTrue()
                expectThat(
                    security.hasPermission(
                        subjectId2,
                        group1.id,
                        GroupPermission.READ
                    )
                ).isTrue()

                // Issuer 'user1' does not have CREATE permission, so this should be disallowed for the token as well
                // (even though the token itself wants to have this permission)
                expectThat(
                    security.hasPermission(
                        subjectId1,
                        group1.id,
                        GroupPermission.CREATE
                    )
                ).isFalse()

                expectThat(
                    security.hasPermission(
                        subjectId2,
                        group1.id,
                        GroupPermission.CREATE
                    )
                ).isTrue()
            }
        }
    }
}