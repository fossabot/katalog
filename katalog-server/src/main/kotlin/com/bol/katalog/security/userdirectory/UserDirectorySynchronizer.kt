package com.bol.katalog.security.userdirectory

import com.bol.katalog.config.StartupRunner
import com.bol.katalog.cqrs.AggregateContext
import com.bol.katalog.cqrs.send
import com.bol.katalog.security.*
import com.bol.katalog.users.UserDirectory
import com.bol.katalog.users.UserDirectoryGroup
import com.bol.katalog.users.UserDirectoryGroupCustomizer
import com.bol.katalog.users.UserId
import com.bol.katalog.utils.runBlockingAsSystem
import mu.KotlinLogging
import org.springframework.stereotype.Component

@Component
class UserDirectorySynchronizer(
    private val userDirectories: List<UserDirectory> = emptyList(),
    private val userDirectoryGroupCustomizers: List<UserDirectoryGroupCustomizer> = emptyList(),
    private val context: AggregateContext,
    private val security: SecurityAggregate
) : StartupRunner {
    private val log = KotlinLogging.logger {}

    override fun runAfterStartup() {
        synchronize()
    }

    fun synchronize() {
        if (userDirectories.isEmpty()) return

        runBlockingAsSystem {
            val discoveredUserIds = mutableListOf<UserId>()
            val discoveredGroupIds = mutableListOf<GroupId>()
            val discoveredGroupMembers = mutableMapOf<GroupId, MutableList<UserId>>()

            userDirectories.forEach { userDirectory ->
                log.info("Synchronizing users from {}", userDirectory)

                // Add any newly discovered users
                userDirectory.getAvailableUsers().forEach { user ->
                    discoveredUserIds += user.id

                    if (security.findUserById(user.id) == null) {
                        context.send(
                            CreateUserCommand(
                                user.id,
                                user.username,
                                user.encodedPassword,
                                user.roles.map { "ROLE_$it" }.toSet()
                            )
                        )
                    }
                }

                log.info("Synchronizing groups from {}", userDirectory)

                userDirectory.getAvailableGroups().forEach { originalGroup ->
                    val group =
                        userDirectoryGroupCustomizers.fold(originalGroup as UserDirectoryGroup?) { g, customizer ->
                            if (g != null) {
                                customizer.customize(g)
                            } else {
                                null
                            }
                        }

                    if (group != null) {
                        val groupId = GroupId(group.id)
                        discoveredGroupIds += groupId

                        if (security.findGroupById(groupId) == null) {
                            context.send(CreateGroupCommand(groupId, group.name))
                        }

                        group.members.forEach { member ->
                            discoveredGroupMembers.getOrPut(groupId) { mutableListOf() }.add(member.userId)

                            if (!security.groupHasMember(groupId, member.userId)) {
                                context.send(AddUserToGroupCommand(member.userId, groupId, member.permissions))
                            }
                        }
                    }
                }
            }

            // Cleanup: Remove any users that were removed from groups
            // (before the user is disabled/removed in the subsequent cleanup steps)
            for (member in discoveredGroupMembers) {
                val group = member.key
                val userIds = member.value
                security.getGroupMembers(group)
                    .map { it.userId }
                    .minus(userIds)
                    .forEach {
                        context.send(RemoveUserFromGroupCommand(it, group))
                    }
            }

            // Cleanup: Disable any users that were not discovered in the user directory
            security.getAllUsers()
                .map { it.id }
                .minus(discoveredUserIds)
                .forEach {
                    context.send(DisableUserCommand(it))
                }

            // Cleanup: Disable any groups that were not discovered
            security.getAllGroups()
                .map { it.id }
                .minus(discoveredGroupIds)
                .forEach {
                    context.send(DisableGroupCommand(it))
                }
        }

        log.info("UserDirectory synchronization complete")
    }
}