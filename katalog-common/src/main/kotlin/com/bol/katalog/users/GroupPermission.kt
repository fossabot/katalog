package com.bol.katalog.users

enum class GroupPermission {
    CREATE,
    READ,
    UPDATE,
    DELETE;

    companion object {
        fun all() = setOf(CREATE, READ, UPDATE, DELETE)
    }
}