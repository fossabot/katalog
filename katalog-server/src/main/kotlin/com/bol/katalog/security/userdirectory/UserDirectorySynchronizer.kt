package com.bol.katalog.security.userdirectory

import com.bol.katalog.security.SecurityProcessor
import com.bol.katalog.users.UserDirectory
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class UserDirectorySynchronizer {
    private val log = KotlinLogging.logger {}

    @Autowired(required = false)
    private var userDirectories: List<UserDirectory> = emptyList()

    @Autowired
    private lateinit var security: SecurityProcessor

    @PostConstruct
    fun synchronize() {
        if (userDirectories.isEmpty()) return

        runBlocking {
            // TODO: This should happen periodically, instead of just at startup
            userDirectories.forEach { userDirectory ->
                log.info("Synchronizing users from {}", userDirectory)

                userDirectory.getAvailableUsers().forEach { user ->
                    security.createUser(
                        user.id,
                        user.username,
                        user.encodedPassword,
                        user.roles.map { SimpleGrantedAuthority("ROLE_$it") }.toSet()
                    )
                }

                log.info("Synchronizing groups from {}", userDirectory)

                userDirectory.getAvailableGroups().forEach { group ->
                    security.createGroup(group.id, group.name)
                    group.members.forEach { member ->
                        security.addUserToGroup(member.userId, group.id, member.permissions)
                    }
                }
            }
        }

        log.info("UserDirectory synchronization complete")
    }
}