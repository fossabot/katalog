package com.bol.katalog.security

import com.bol.katalog.AggregateTester
import com.bol.katalog.users.GroupPermission
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.security.core.authority.SimpleGrantedAuthority
import strikt.api.expectThat
import strikt.assertions.*

class SecurityAggregateTest {
    private val tester = AggregateTester.of { ctx ->
        SecurityAggregate(ctx)
    }

    private val group1 = Group("id-group1", "group1", emptyList())
    private val user1 = User("id-user1", "user1", "password", setOf(SimpleGrantedAuthority("ROLE_USER")))
    private val admin = User("id-admin", "admin", "password", setOf(SimpleGrantedAuthority("ROLE_ADMIN")))

    @Nested
    inner class Groups {
        @Test
        fun `Can create`() {
            tester.run {
                send(group1.create())
                expect {
                    event(group1.created())
                    state {
                        expectThat(it.getGroups()).containsExactly(group1)
                        expectThat(it.findGroupById(group1.id)).isEqualTo(group1)
                    }
                }
            }
        }

        @Test
        fun `Can disable`() {
            tester.run {
                given(group1.created())
                send(group1.disable())
                expect {
                    event(group1.disabled())
                    state {
                        expectThat(it.getGroups()).isEmpty()
                    }
                }
            }
        }
    }

    @Nested
    inner class Users {
        @Test
        fun `Can create`() {
            tester.run {
                send(user1.create())
                expect {
                    event(user1.created())
                    state {
                        expectThat(it.getUsers()).containsExactly(user1)
                        expectThat(it.findUserById(user1.id)).isEqualTo(user1)
                        expectThat(it.findUserByUsername(user1.username)).isEqualTo(user1)
                    }
                }
            }
        }

        @Test
        fun `Can add to group`() {
            tester.run {
                given(
                    user1.created(),
                    group1.created()
                )
                send(user1.addToGroup(group1, setOf(GroupPermission.READ)))
                expect {
                    event(user1.addedToGroup(group1, setOf(GroupPermission.READ)))
                    state {
                        expectThat(it.getGroups(user1).map { g -> g.id }).containsExactly(group1.id)
                        expectThat(it.getGroupMembers(group1.id)).containsExactly(
                            GroupMember(user1.id, setOf(GroupPermission.READ))
                        )
                        expectThat(it.groupHasMember(group1.id, user1.id)).isTrue()
                        expectThat(it.hasPermission(user1, group1.id, GroupPermission.READ)).isTrue()
                        expectThat(it.hasPermission(user1, group1.id, GroupPermission.CREATE)).isFalse()
                    }
                }
            }
        }

        @Test
        fun `Admin has all permissions`() {
            tester.run {
                given(
                    admin.created(),
                    group1.created(),
                    admin.addedToGroup(group1)
                )
                expect {
                    state {
                        expectThat(it.getGroupMembers(group1.id)).containsExactly(
                            GroupMember(admin.id, emptySet())
                        )
                        allPermissions().forEach { permission ->
                            expectThat(it.hasPermission(admin, group1.id, permission)).isTrue()
                        }
                    }
                }
            }
        }

        @Test
        fun `Can remove from group`() {
            tester.run {
                given(
                    user1.created(),
                    group1.created(),
                    user1.addedToGroup(group1, setOf(GroupPermission.READ))
                )
                send(user1.removeFromGroup(group1))
                expect {
                    event(user1.removedFromGroup(group1))
                    state {
                        expectThat(it.getGroups(user1)).isEmpty()
                        expectThat(it.getGroupMembers(group1.id)).isEmpty()
                        expectThat(it.groupHasMember(group1.id, user1.id)).isFalse()
                    }
                }
            }
        }

        @Test
        fun `Can disable`() {
            tester.run {
                given(user1.created())
                send(user1.disable())
                expect {
                    event(user1.disabled())
                    state {
                        expectThat(it.getUsers()).isEmpty()
                    }
                }
            }
        }
    }
}

