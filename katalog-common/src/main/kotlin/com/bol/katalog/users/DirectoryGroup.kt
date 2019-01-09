package com.bol.katalog.users

data class UserDirectoryGroup(val id: String, val name: String, val userIds: Collection<String>)