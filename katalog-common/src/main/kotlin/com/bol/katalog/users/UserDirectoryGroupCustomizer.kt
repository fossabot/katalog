package com.bol.katalog.users

/**
 * The customizer can be used to filter groups and customize the details (such as the name) of the groups.
 */
interface UserDirectoryGroupCustomizer {
    fun customize(group: UserDirectoryGroup): UserDirectoryGroup?
}