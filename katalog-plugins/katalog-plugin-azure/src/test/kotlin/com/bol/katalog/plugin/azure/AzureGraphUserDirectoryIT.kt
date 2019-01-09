package com.bol.katalog.plugin.azure

import com.bol.katalog.users.UserDirectoryGroup
import com.bol.katalog.users.UserDirectoryUser
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

    private val GROUP1_ID = "43f776d0-19c9-4c07-82de-243fc4601e8e"
    private val GROUP2_ID = "6355d5c9-a201-4c41-a3f2-4fbe360b3d0c"

    private val USER1_ID = "d696d37e-e5e4-46f1-be80-91273f2238b7"
    private val USER2_ID = "685b92c2-e67e-49ae-b243-623342bc9661"

    @Test
    fun `Can get groups from example project`() {
        val groups = userDirectory.getAvailableGroups()
        expectThat(groups).containsExactly(
            UserDirectoryGroup(GROUP1_ID, "group1", listOf(USER1_ID)),
            UserDirectoryGroup(GROUP2_ID, "group2", listOf(USER2_ID))
        )
    }

    @Test
    fun `Can get users from example project`() {
        val users = userDirectory.getAvailableUsers()
            .filterNot { it.name == "Roy Jacobs" } // filter admin account from example project
        expectThat(users).containsExactly(
            UserDirectoryUser(USER1_ID, "User One", "user1@foo.com"),
            UserDirectoryUser(USER2_ID, "User Two", "user2@foo.com")
        )
    }
}