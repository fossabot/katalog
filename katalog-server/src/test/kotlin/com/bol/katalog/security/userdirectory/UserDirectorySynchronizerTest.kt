package com.bol.katalog.security.userdirectory

import com.bol.katalog.security.*
import com.bol.katalog.security.support.*
import com.bol.katalog.support.AggregateTester
import com.bol.katalog.users.*
import org.junit.jupiter.api.Test

class UserDirectorySynchronizerTest {
    private val tester = AggregateTester.of { ctx, _ ->
        SecurityAggregate(ctx)
    }

    private val directoryUser1 = UserDirectoryUser(
        "id-user1",
        "user1",
        "password",
        "user1@foo.com",
        setOf(UserDirectoryRole.USER)
    )
    private val directoryAdmin = UserDirectoryUser(
        "id-admin",
        "admin",
        "password",
        "admin@foo.com",
        setOf(UserDirectoryRole.USER, UserDirectoryRole.ADMIN)
    )
    private val directoryGroup1 = UserDirectoryGroup("id-group1", "group1", emptyList())
    private val directoryGroup2 = UserDirectoryGroup("id-group2", "group2", emptyList())

    @Test
    fun `Can add users`() {
        tester.run {
            synchronize {
                users += directoryUser1
                users += directoryAdmin
            }
            expect {
                events(
                    user1.created(),
                    admin.created()
                )
            }
        }
    }

    @Test
    fun `Can add groups`() {
        tester.run {
            synchronize {
                groups += directoryGroup1
                groups += directoryGroup2
            }
            expect {
                events(
                    GroupCreatedEvent(GroupId("id-group1"), "group1"),
                    GroupCreatedEvent(GroupId("id-group2"), "group2")
                )
            }
        }
    }

    @Test
    fun `Can add customized groups`() {
        tester.run {
            val customizer = object : UserDirectoryGroupCustomizer {
                override fun customize(group: UserDirectoryGroup): UserDirectoryGroup? {
                    return group.copy(name = group.name.reversed())
                }
            }
            synchronize(customizer) {
                groups += directoryGroup1
            }
            expect {
                event(GroupCreatedEvent(GroupId("id-group1"), "1puorg"))
            }
        }
    }

    @Test
    fun `Can filter groups`() {
        tester.run {
            val customizer = object : UserDirectoryGroupCustomizer {
                override fun customize(group: UserDirectoryGroup): UserDirectoryGroup? {
                    return if (group.name == "group2") null else group
                }
            }
            synchronize(customizer) {
                groups += directoryGroup1
                groups += directoryGroup2
            }
            expect {
                event(GroupCreatedEvent(GroupId("id-group1"), "group1"))
            }
        }
    }

    @Test
    fun `Can add users to groups`() {
        tester.run {
            given(
                group1.created(),
                user1.created(),
                admin.created()
            )
            synchronize {
                groups += directoryGroup1.copy(
                    members = listOf(
                        UserDirectoryGroupMember("id-user1", setOf(GroupPermission.READ)),
                        UserDirectoryGroupMember("id-admin", allPermissions())
                    )
                )
                users += directoryUser1
                users += directoryAdmin
            }
            expect {
                events(
                    user1.addedToGroup(group1, setOf(GroupPermission.READ)),
                    admin.addedToGroup(group1, allPermissions())
                )
            }
        }
    }

    @Test
    fun `Can remove users from groups`() {
        tester.run {
            given(
                group1.created(),
                user1.created(),
                admin.created(),
                user1.addedToGroup(group1),
                admin.addedToGroup(group1)
            )
            synchronize {
                groups += directoryGroup1.copy(
                    members = listOf(
                        // Don't add user1 as a member here
                        UserDirectoryGroupMember("id-admin", allPermissions())
                    )
                )
                users += directoryUser1
                users += directoryAdmin
            }
            expect {
                event(
                    user1.removedFromGroup(group1)
                )
            }
        }
    }

    @Test
    fun `Can disable users`() {
        tester.run {
            given(
                user1.created(),
                group1.created(),
                user1.addedToGroup(group1)
            )
            synchronize {
                groups += directoryGroup1
                // Don't add directoryUser1 here, so it should become disabled
            }
            expect {
                event(user1.disabled())
            }
        }
    }

    @Test
    fun `Can disable groups`() {
        tester.run {
            given(
                group1.created()
            )
            synchronize {
                // Don't add directoryGroup1 here, so it should become disabled
            }
            expect {
                event(group1.disabled())
            }
        }
    }

    private fun AggregateTester<SecurityAggregate, Security>.TestBuilder<SecurityAggregate, Security>.synchronize(
        groupCustomizer: UserDirectoryGroupCustomizer? = null,
        directoryCustomizer: (TestUserDirectory.() -> Unit)? = null
    ) {
        val directory = TestUserDirectory()
        if (directoryCustomizer != null) {
            directoryCustomizer(directory)
        }
        val synchronizer = UserDirectorySynchronizer(listOf(directory), listOfNotNull(groupCustomizer), aggregate)
        synchronizer.synchronize()
    }

    class TestUserDirectory(
        val groups: MutableList<UserDirectoryGroup> = mutableListOf(),
        val users: MutableList<UserDirectoryUser> = mutableListOf()
    ) : UserDirectory {
        override fun getAvailableGroups() = groups.toList()
        override fun getAvailableUsers() = users.toList()
    }
}