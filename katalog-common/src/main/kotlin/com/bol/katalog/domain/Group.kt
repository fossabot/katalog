package com.bol.katalog.domain

data class Group(val name: String)

enum class GroupPermission {
    CREATE,
    READ,
    UPDATE,
    DELETE
}

fun allPermissions() = listOf(
    GroupPermission.CREATE,
    GroupPermission.READ,
    GroupPermission.UPDATE,
    GroupPermission.DELETE
)

data class UserGroup(val group: Group, val permissions: Collection<GroupPermission>)
