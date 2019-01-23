package com.bol.katalog.plugin.atomix

import io.atomix.core.Atomix

fun Atomix.getLeaderId(): String? {
    return this.membershipService.reachableMembers.firstOrNull()?.id()?.id()
}

fun Atomix.getFollowerIds(): List<String> {
    return this.membershipService.reachableMembers.drop(1).map { it.id().id() }
}