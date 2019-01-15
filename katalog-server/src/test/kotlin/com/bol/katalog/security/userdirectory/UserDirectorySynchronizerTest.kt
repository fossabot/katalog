package com.bol.katalog.security.userdirectory

import com.bol.katalog.TestApplication
import com.bol.katalog.TestApplication.processor
import com.bol.katalog.TestApplication.security
import com.bol.katalog.security.*
import com.bol.katalog.users.*
import org.junit.Before
import org.junit.Test
import org.springframework.security.core.authority.SimpleGrantedAuthority
import strikt.api.expectThat
import strikt.assertions.*

class UserDirectorySynchronizerTest {
    private lateinit var directory: TestUserDirectory
    private lateinit var synchronizer: UserDirectorySynchronizer

    @Before
    fun before() {
        TestApplication.reset(false)
        directory = TestUserDirectory()
        synchronizer = UserDirectorySynchronizer(listOf(directory), processor, security)
    }

    @Test
    fun `Can add users`() {
        directory.addDefaults()
        synchronizer.synchronize()
        expectThat(processor.received).contains(
            CreateUserCommand("id-user1", "user1", "user1password", setOf(SimpleGrantedAuthority("ROLE_USER"))),
            CreateUserCommand(
                "id-admin", "admin", "adminpassword", setOf(
                    SimpleGrantedAuthority("ROLE_USER"),
                    SimpleGrantedAuthority("ROLE_ADMIN")
                )
            )
        )
    }

    @Test
    fun `Can add groups`() {
        directory.addDefaults()
        synchronizer.synchronize()
        expectThat(processor.received).contains(
            CreateGroupCommand("id-group1", "group1"),
            CreateGroupCommand("id-group-admins-only", "group-admins-only")
        )
    }

    @Test
    fun `Can add users to groups`() {
        directory.addDefaults()
        synchronizer.synchronize()
        expectThat(processor.received).contains(
            AddUserToGroupCommand("id-user1", "id-group1", setOf(GroupPermission.READ)),
            AddUserToGroupCommand("id-admin", "id-group1", allPermissions()),
            AddUserToGroupCommand(
                "id-admin", "id-group-admins-only", allPermissions()
            )
        )
    }

    @Test
    fun `Can disable users`() {
        // Perform default sync
        directory.addDefaults()
        synchronizer.synchronize()

        expectThat(security.findUserById("id-user1")).isNotNull()

        // Now, remove one of the users
        directory.users.removeIf { it.id == "id-user1" }
        processor.clearReceivedEvents()
        synchronizer.synchronize()

        // The user should have been disabled
        expectThat(processor.received).containsExactly(
            DisableUserCommand("id-user1")
        )

        expectThat(security.findUserById("id-user1")).isNull()
    }

    @Test
    fun `Can remove users from groups`() {
        // Perform default sync
        directory.addDefaults()
        synchronizer.synchronize()

        expectThat(security.groupHasMember("id-group1", "id-user1")).isTrue()

        // Now, remove one of the members
        directory.removeMember("id-group1", "id-user1")
        processor.clearReceivedEvents()
        synchronizer.synchronize()

        // The user should have been removed
        expectThat(security.groupHasMember("id-group1", "id-user1")).isFalse()
    }

    @Test
    fun `Can disable groups`() {
        // Perform default sync
        directory.addDefaults()
        synchronizer.synchronize()

        expectThat(security.findGroupById("id-group1")).isNotNull()

        // Now, remove one of the groups
        directory.groups.removeIf { it.id == "id-group1" }
        processor.clearReceivedEvents()
        synchronizer.synchronize()

        // The group should have been disabled
        expectThat(processor.received).containsExactly(
            DisableGroupCommand("id-group1")
        )

        expectThat(security.findGroupById("id-group1")).isNull()
    }

    class TestUserDirectory(
        val groups: MutableList<UserDirectoryGroup> = mutableListOf(),
        val users: MutableList<UserDirectoryUser> = mutableListOf()
    ) : UserDirectory {
        fun addDefaults() {
            users += UserDirectoryUser(
                "id-user1",
                "user1",
                "user1password",
                "user1@foo.com",
                setOf(UserDirectoryRole.USER)
            )
            users += UserDirectoryUser(
                "id-admin",
                "admin",
                "adminpassword",
                "admin@foo.com",
                setOf(UserDirectoryRole.USER, UserDirectoryRole.ADMIN)
            )

            val memberUser1 = UserDirectoryGroupMember("id-user1", setOf(GroupPermission.READ))
            val memberAdmin = UserDirectoryGroupMember("id-admin", allPermissions())

            groups += UserDirectoryGroup(
                "id-group1", "group1", listOf(
                    memberUser1,
                    memberAdmin
                )
            )
            groups += UserDirectoryGroup(
                "id-group-admins-only", "group-admins-only", listOf(
                    memberAdmin
                )
            )
        }

        override fun getAvailableGroups() = groups
        override fun getAvailableUsers() = users

        fun removeMember(groupId: GroupId, userId: UserId) {
            val group = groups.single { it.id == groupId }
            groups -= group
            groups += group.copy(members = group.members.filterNot { member -> member.userId == userId })
        }
    }
}