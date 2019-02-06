package com.bol.katalog.support

import com.bol.katalog.cqrs.*
import com.bol.katalog.cqrs.support.TestAggregateContext
import com.bol.katalog.security.*
import com.bol.katalog.security.support.TestPermissionManager
import com.bol.katalog.users.GroupPermission
import com.bol.katalog.utils.runBlockingAs
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.junit.jupiter.api.fail
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isA
import strikt.assertions.isEqualTo

class AggregateTester<T : CqrsAggregate<S>, S : State>(val factory: (AggregateContext, PermissionManager) -> T) {
    private val log = KotlinLogging.logger {}

    companion object {
        fun <T : CqrsAggregate<S>, S : State> of(factory: (AggregateContext, PermissionManager) -> T): AggregateTester<T, S> {
            return AggregateTester(factory)
        }
    }

    lateinit var permissionManager: TestPermissionManager

    fun run(block: TestBuilder<T, S>.() -> Unit) {
        permissionManager = TestPermissionManager()

        val context = TestAggregateContext()
        val aggregate = factory(context, permissionManager)

        val builder = TestBuilder(aggregate)
        context.onEvent = { builder.received(it) }
        block(builder)

        if (builder.caughtException != null) {
            fail("Unexpected exception: " + builder.caughtException, builder.caughtException)
        }
    }

    inner class TestBuilder<T : CqrsAggregate<S>, S : State>(val aggregate: T) {
        val receivedEvents = mutableListOf<Event>()
        var caughtException: Throwable? = null

        fun <E : Event> given(vararg events: E) = givenAs(SystemUser.get(), *events)

        fun <E : Event> givenAs(user: User, vararg events: E) {
            runBlockingAs(user.id) {
                events.forEach {
                    aggregate.handlePersistentEvent(
                        it.asPersistentEvent(
                            user.id,
                            TestData.clock
                        )
                    )
                }
            }
        }

        fun <C : Command> send(command: C) = sendAs(SystemUser.get(), command)

        fun <C : Command> sendAs(user: User, command: C) {
            runBlockingAs(user.id) {
                try {
                    aggregate.send(command)
                } catch (e: Throwable) {
                    log.warn("Caught exception: $e")
                    if (caughtException != null) {
                        fail("Already an exception caught: $caughtException")
                    }
                    caughtException = e
                }
            }
        }

        fun expect(block: ExpectationBuilder<T, S>.() -> Unit) {
            val builder = ExpectationBuilder(this)
            block(builder)
        }

        fun received(event: Event) {
            receivedEvents += event
        }

        fun permissions(block: PermissionBuilder.() -> Unit) {
            val builder = PermissionBuilder()
            block(builder)
        }

        inner class ExpectationBuilder<T : CqrsAggregate<S>, S : State>(val testBuilder: TestBuilder<T, S>) {
            fun events(vararg events: Event) {
                expectThat(testBuilder.receivedEvents).containsExactly(*events)
            }

            fun event(event: Event) {
                expectThat(testBuilder.receivedEvents).containsExactly(listOf(event))
            }

            fun state(block: suspend (S) -> Unit) = stateAs(SystemUser.get(), block)

            fun stateAs(user: User, block: suspend (S) -> Unit) {
                runBlocking {
                    CoroutineUserIdContext.set(user.id)
                    testBuilder.aggregate.read {
                        block(this)
                    }
                }
            }

            inline fun <reified E : Throwable> throws(message: String? = null) {
                if (message != null) {
                    expectThat(testBuilder.caughtException!!.message).isEqualTo(message)
                }
                expectThat(testBuilder.caughtException).isA<E>()

                // Reset caught exception
                testBuilder.caughtException = null
            }
        }

        inner class PermissionBuilder {
            fun group(groupId: GroupId, block: PermissionGroupBuilder.() -> Unit) = groups(
                listOf(groupId), block
            )

            fun groups(vararg groupIds: GroupId, block: PermissionGroupBuilder.() -> Unit) =
                groups(groupIds.asList(), block)

            fun groups(groupIds: List<GroupId>, block: PermissionGroupBuilder.() -> Unit) {
                val builder = PermissionGroupBuilder(groupIds)
                block(builder)
            }

            inner class PermissionGroupBuilder(private val groupIds: List<GroupId>) {
                fun allow(user: User, permissions: Set<GroupPermission>) =
                    groupIds.forEach { entity ->
                        permissions.forEach { permission ->
                            permissionManager.addPermission(user.id, entity, permission)
                        }
                    }

                fun allowCreate(user: User) = allow(user, setOf(GroupPermission.CREATE))
                fun allowRead(user: User) = allow(user, setOf(GroupPermission.READ))
                fun allowUpdate(user: User) = allow(user, setOf(GroupPermission.UPDATE))
                fun allowDelete(user: User) = allow(user, setOf(GroupPermission.DELETE))

                fun allowAll(user: User) = allow(user, allPermissions())
            }
        }
    }
}