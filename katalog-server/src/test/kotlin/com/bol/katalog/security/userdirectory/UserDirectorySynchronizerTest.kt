package com.bol.katalog.security.userdirectory

import com.bol.katalog.AggregateTester
import com.bol.katalog.security.*
import com.bol.katalog.users.*
import org.junit.jupiter.api.Test

class UserDirectorySynchronizerTest {
    private val tester = AggregateTester.of { ctx ->
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

    @Test
    fun `Can add users`() {
        tester.run {
            synchronize {
                users += directoryUser1
                users += directoryAdmin
            }
            expect {
                event(
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
                event(
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

    private fun AggregateTester.TestBuilder<SecurityAggregate, SecurityState>.synchronize(directoryCustomizer: (TestUserDirectory.() -> Unit)? = null) {
        val directory = TestUserDirectory()
        if (directoryCustomizer != null) {
            directoryCustomizer(directory)
        }
        val synchronizer = UserDirectorySynchronizer(listOf(directory), aggregate)
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