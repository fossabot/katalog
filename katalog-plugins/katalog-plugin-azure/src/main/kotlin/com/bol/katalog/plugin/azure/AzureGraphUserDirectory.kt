package com.bol.katalog.plugin.azure

import com.bol.katalog.users.*
import org.springframework.stereotype.Component

@Component
class AzureGraphUserDirectory(private val graphClient: GraphClient) : UserDirectory {
    override fun getAvailableGroups(): Collection<UserDirectoryGroup> {
        return graphClient.getGroups()
            .map {
                val members = graphClient.getGroupMembers(it.id).map { member ->
                    UserDirectoryGroupMember(
                        member.id,
                        GroupPermission.values().toSet() // All permissions, for now
                    )
                }
                UserDirectoryGroup(it.id, it.displayName, members)
            }
    }

    override fun getAvailableUsers(): Collection<UserDirectoryUser> {
        return graphClient.getUsers()
            .map {
                UserDirectoryUser(
                    it.id,
                    it.displayName,
                    null,
                    it.otherMails.firstOrNull(),
                    setOf(UserDirectoryRole.USER)
                )
            }
    }
}