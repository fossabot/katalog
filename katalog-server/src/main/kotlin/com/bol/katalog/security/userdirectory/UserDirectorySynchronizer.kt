package com.bol.katalog.security.userdirectory

import com.bol.katalog.security.*
import com.bol.katalog.users.UserDirectory
import com.bol.katalog.users.UserId
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component

@Component
class UserDirectorySynchronizer(
    private val userDirectories: List<UserDirectory> = emptyList(),
    private val security: SecurityAggregate
) {
    private val log = KotlinLogging.logger {}

    fun synchronize() {
        if (userDirectories.isEmpty()) return

        runBlocking {
            val discoveredUserIds = mutableListOf<UserId>()
            val discoveredGroupIds = mutableListOf<GroupId>()
            val discoveredGroupMembers = mutableMapOf<GroupId, MutableList<UserId>>()

            userDirectories.forEach { userDirectory ->
                log.info("Synchronizing users from {}", userDirectory)

                // Add any newly discovered users
                userDirectory.getAvailableUsers().forEach { user ->
                    discoveredUserIds += user.id

                    if (security.read { findUserById(user.id) } == null) {
                        security.send(
                            CreateUserCommand(
                                user.id,
                                user.username,
                                user.encodedPassword,
                                user.roles.map { SimpleGrantedAuthority("ROLE_$it") }.toSet()
                            )
                        )
                    }
                }

                log.info("Synchronizing groups from {}", userDirectory)

                userDirectory.getAvailableGroups().forEach { group ->
                    discoveredGroupIds += group.id

                    if (security.read { findGroupById(group.id) } == null) {
                        security.send(CreateGroupCommand(group.id, group.name))
                    }

                    group.members.forEach { member ->
                        discoveredGroupMembers.getOrPut(group.id) { mutableListOf() }.add(member.userId)

                        if (!security.read { groupHasMember(group.id, member.userId) }) {
                            security.send(AddUserToGroupCommand(member.userId, group.id, member.permissions))
                        }
                    }
                }
            }

            // Cleanup: Remove any users that were removed from groups
            // (before the user is disabled/removed in the subsequent cleanup steps)
            for (member in discoveredGroupMembers) {
                val group = member.key
                val userIds = member.value
                security.read {
                    getGroupMembers(group)
                        .map { it.userId }
                        .minus(userIds)
                }.forEach {
                    security.send(RemoveUserFromGroupCommand(it, group))
                }
            }

            // Cleanup: Disable any users that were not discovered in the user directory
            security.read {
                getUsers()
                    .map { it.id }
                    .minus(discoveredUserIds)
            }.forEach {
                security.send(DisableUserCommand(it))
            }

            // Cleanup: Disable any groups that were not discovered
            security.read {
                getGroups()
                    .map { it.id }
                    .minus(discoveredGroupIds)
            }.forEach {
                security.send(DisableGroupCommand(it))
            }
        }

        log.info("UserDirectory synchronization complete")
    }
}