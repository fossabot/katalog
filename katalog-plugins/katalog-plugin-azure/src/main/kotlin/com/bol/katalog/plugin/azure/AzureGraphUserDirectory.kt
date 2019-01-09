package com.bol.katalog.plugin.azure

import com.bol.katalog.users.UserDirectory
import com.bol.katalog.users.UserDirectoryGroup
import com.bol.katalog.users.UserDirectoryUser
import org.springframework.stereotype.Component

@Component
class AzureGraphUserDirectory(private val graphClient: GraphClient) : UserDirectory {
    override fun getAvailableGroups(): Collection<UserDirectoryGroup> {
        return graphClient.getGroups()
            .map {
                val userIds = graphClient.getGroupMembers(it.id).map { member -> member.id }
                UserDirectoryGroup(it.id, it.displayName, userIds)
            }
    }

    override fun getAvailableUsers(): Collection<UserDirectoryUser> {
        return graphClient.getUsers()
            .map {
                UserDirectoryUser(it.id, it.displayName, it.otherMails.firstOrNull())
            }
    }
}