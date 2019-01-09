package com.bol.katalog.security.userdirectory.simple

import com.bol.katalog.config.security.SecurityConfigurationProperties
import com.bol.katalog.users.*
import org.springframework.security.crypto.password.PasswordEncoder

class SimpleUserDirectory(
    val config: SecurityConfigurationProperties.SimpleUserDirectoryProperties,
    val passwordEncoder: PasswordEncoder
) : UserDirectory {
    override fun getAvailableGroups(): Collection<UserDirectoryGroup> {
        return config.groups.map { group ->
            val members = mutableListOf<UserDirectoryGroupMember>()

            config.users.forEach { user ->
                val groupPermissions: List<GroupPermission>? = user.value.groups[group.key]
                if (groupPermissions != null) {
                    members.add(UserDirectoryGroupMember(user.key, groupPermissions.toSet()))
                }
            }

            UserDirectoryGroup(group.key, group.value, members)
        }
    }

    override fun getAvailableUsers(): Collection<UserDirectoryUser> {
        return config.users.map { user ->
            UserDirectoryUser(
                user.key,
                user.value.username,
                passwordEncoder.encode(user.value.password),
                user.value.email,
                user.value.roles
            )
        }
    }
}