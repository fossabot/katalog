package com.bol.katalog.users

data class UserDirectoryUser(
    val id: String,
    val username: String,
    val encodedPassword: String?,
    val email: String?,
    val roles: Set<UserDirectoryRole>
)

