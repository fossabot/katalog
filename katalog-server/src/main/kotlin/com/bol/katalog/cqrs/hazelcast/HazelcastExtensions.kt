package com.bol.katalog.cqrs.hazelcast

import com.hazelcast.core.HazelcastInstance
import com.hazelcast.core.Member

fun HazelcastInstance.leader(): Member = cluster.members.first()

fun HazelcastInstance.isLeader() = cluster.localMember == leader()
