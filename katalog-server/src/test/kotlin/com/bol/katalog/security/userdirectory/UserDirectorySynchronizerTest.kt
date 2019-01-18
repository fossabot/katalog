package com.bol.katalog.security.userdirectory

import com.bol.katalog.TestData
import com.bol.katalog.config.inmemory.InMemoryEventStore
import com.bol.katalog.cqrs.clustering.inmemory.InMemoryClusteringContext
import com.bol.katalog.readBlocking
import com.bol.katalog.security.*
import com.bol.katalog.users.*
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.security.core.authority.SimpleGrantedAuthority
import strikt.api.expectThat
import strikt.assertions.*

class UserDirectorySynchronizerTest {
    private lateinit var security: SecurityAggregate
    private lateinit var directory: TestUserDirectory
    private lateinit var synchronizer: UserDirectorySynchronizer
    private lateinit var eventStore: InMemoryEventStore

    @Before
    fun before() {
        eventStore = InMemoryEventStore()
        val clustering = InMemoryClusteringContext(eventStore, TestData.clock)

        security = SecurityAggregate()
        security.setClusteringContext(clustering)
        security.start()
        directory = TestUserDirectory()
        synchronizer = UserDirectorySynchronizer(listOf(directory), security)
    }

    @After
    fun after() {
        runBlocking { security.stop() }
    }

    @Test
    fun `Can add users`() {
        directory.addDefaults()
        synchronizer.synchronize()
        expectThat(eventStore.getAll().map { it.data }).contains(
            UserCreatedEvent("id-user1", "user1", "user1password", setOf(SimpleGrantedAuthority("ROLE_USER"))),
            UserCreatedEvent(
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
        expectThat(eventStore.getAll().map { it.data }).contains(
            GroupCreatedEvent("id-group1", "group1"),
            GroupCreatedEvent("id-group-admins-only", "group-admins-only")
        )
    }

    @Test
    fun `Can add users to groups`() {
        directory.addDefaults()
        synchronizer.synchronize()
        expectThat(eventStore.getAll().map { it.data }).contains(
            UserAddedToGroupEvent("id-user1", "id-group1", setOf(GroupPermission.READ)),
            UserAddedToGroupEvent("id-admin", "id-group1", allPermissions()),
            UserAddedToGroupEvent("id-admin", "id-group-admins-only", allPermissions())
        )
    }

    @Test
    fun `Can disable users`() {
        // Perform default sync
        directory.addDefaults()
        synchronizer.synchronize()

        expectThat(security.readBlocking { findUserById("id-user1") }).isNotNull()

        // Now, remove one of the users
        directory.users.removeIf { it.id == "id-user1" }
        eventStore.reset()
        synchronizer.synchronize()

        // The user should have been disabled
        expectThat(eventStore.getAll().map { it.data }).containsExactly(
            UserDisabledEvent("id-user1")
        )

        expectThat(security.readBlocking { findUserById("id-user1") }).isNull()
    }

    @Test
    fun `Can remove users from groups`() {
        // Perform default sync
        directory.addDefaults()
        synchronizer.synchronize()

        expectThat(security.readBlocking { groupHasMember("id-group1", "id-user1") }).isTrue()

        // Now, remove one of the members
        directory.removeMember("id-group1", "id-user1")
        eventStore.reset()
        synchronizer.synchronize()

        // The user should have been removed
        expectThat(security.readBlocking { groupHasMember("id-group1", "id-user1") }).isFalse()
    }

    @Test
    fun `Can disable groups`() {
        // Perform default sync
        directory.addDefaults()
        synchronizer.synchronize()

        expectThat(security.readBlocking { findGroupById("id-group1") }).isNotNull()

        // Now, remove one of the groups
        directory.groups.removeIf { it.id == "id-group1" }
        eventStore.reset()
        synchronizer.synchronize()

        // The group should have been disabled
        expectThat(eventStore.getAll().map { it.data }).containsExactly(
            GroupDisabledEvent("id-group1")
        )

        expectThat(security.readBlocking { findGroupById("id-group1") }).isNull()
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