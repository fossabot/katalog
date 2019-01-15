package com.bol.katalog.security.userdirectory.configurationbased

import com.bol.katalog.security.config.SecurityConfigurationProperties
import com.bol.katalog.users.*
import org.springframework.security.crypto.password.PasswordEncoder

class ConfigurationBasedUserDirectory(
    private val config: SecurityConfigurationProperties,
    private val passwordEncoder: PasswordEncoder
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