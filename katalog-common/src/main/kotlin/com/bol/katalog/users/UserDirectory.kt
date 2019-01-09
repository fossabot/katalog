package com.bol.katalog.users

interface UserDirectory {
    fun getAvailableGroups(): Collection<UserDirectoryGroup>

    fun getAvailableUsers(): Collection<UserDirectoryUser>
}