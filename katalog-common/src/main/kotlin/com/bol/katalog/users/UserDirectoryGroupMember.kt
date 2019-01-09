package com.bol.katalog.users

data class UserDirectoryGroupMember(
    var userId: UserId,
    var permissions: Set<GroupPermission>
)