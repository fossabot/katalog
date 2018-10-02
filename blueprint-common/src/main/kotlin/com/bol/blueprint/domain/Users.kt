package com.bol.blueprint.domain

import reactor.core.publisher.Flux

data class Group(val id: String, val name: String)

interface UserGroupService {
    fun getGroupsByUsername(username: String): Flux<Group>
}