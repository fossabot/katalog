package com.bol.katalog.security

data class Group(val id: GroupId, val name: String, val members: List<GroupMember>)

typealias GroupId = String

data class GroupMember(val userId: UserId, val permissions: Collection<GroupPermission>)

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