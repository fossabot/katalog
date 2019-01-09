package com.bol.katalog.plugin.azure

import com.bol.katalog.users.*
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import strikt.api.expectThat
import strikt.assertions.containsExactly

@RunWith(SpringRunner::class)
@SpringBootTest
class AzureGraphUserDirectoryIT {
    @Autowired
    private lateinit var userDirectory: AzureGraphUserDirectory

    object Identifiers {
        const val GROUP1 = "43f776d0-19c9-4c07-82de-243fc4601e8e"
        const val GROUP2 = "6355d5c9-a201-4c41-a3f2-4fbe360b3d0c"

        const val USER1 = "d696d37e-e5e4-46f1-be80-91273f2238b7"
        const val USER2 = "685b92c2-e67e-49ae-b243-623342bc9661"
    }

    @Test
    fun `Can get groups from example project`() {
        val groups = userDirectory.getAvailableGroups()
        expectThat(groups).containsExactly(
            UserDirectoryGroup(
                Identifiers.GROUP1,
                "group1",
                listOf(UserDirectoryGroupMember(Identifiers.USER1, GroupPermission.all()))
            ),
            UserDirectoryGroup(
                Identifiers.GROUP2,
                "group2",
                listOf(UserDirectoryGroupMember(Identifiers.USER2, GroupPermission.all()))
            )
        )
    }

    @Test
    fun `Can get users from example project`() {
        val users = userDirectory.getAvailableUsers()
            .filter { it.username.startsWith("User") } // filter admin account from example project
        expectThat(users).containsExactly(
            UserDirectoryUser(Identifiers.USER1, "User One", null, "user1@foo.com", setOf(UserDirectoryRole.USER)),
            UserDirectoryUser(Identifiers.USER2, "User Two", null, "user2@foo.com", setOf(UserDirectoryRole.USER))
        )
    }
}