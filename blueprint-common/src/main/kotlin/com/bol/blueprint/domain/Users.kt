package com.bol.blueprint.domain

data class Group(val name: String)

interface UserGroupService {
    suspend fun getGroupsByUsername(username: String): List<Group>
}