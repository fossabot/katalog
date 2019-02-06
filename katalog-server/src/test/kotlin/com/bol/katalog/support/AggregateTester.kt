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
import java.lang.reflect.ParameterizedType

class AggregateTester(val factory: (AggregateContext, PermissionManager) -> List<Aggregate<*>>) {
    private val log = KotlinLogging.logger {}

    companion object {
        fun of(factory: (AggregateContext, PermissionManager) -> List<Aggregate<*>>): AggregateTester {
            return AggregateTester(factory)
        }
    }

    lateinit var permissionManager: TestPermissionManager

    fun run(block: TestBuilder.() -> Unit) {
        permissionManager = TestPermissionManager()

        val context = TestAggregateContext()
        val aggregates = factory(context, permissionManager)

        val builder = TestBuilder(context, aggregates)
        block(builder)

        if (builder.caughtException != null) {
            fail("Unexpected exception: " + builder.caughtException, builder.caughtException)
        }
    }

    inner class TestBuilder(
        val context: TestAggregateContext,
        val aggregates: List<Aggregate<*>>
    ) {
        private val receivedEvents = mutableListOf<Event>()
        var caughtException: Throwable? = null

        init {
            // Capture any events that may be published
            context.onEvent = { received(it) }
        }

        @Suppress("UNCHECKED_CAST")
        inline fun <reified S : State> aggregate(): Aggregate<S> {
            return aggregates.single {
                val stateClass = (it.javaClass
                    .genericSuperclass as ParameterizedType)
                    .actualTypeArguments[0]
                stateClass == S::class.java
            } as Aggregate<S>
        }

        fun <E : Event> given(vararg events: E) = givenAs(SystemUser.get(), *events)

        fun <E : Event> givenAs(user: User, vararg events: E) {
            runBlockingAs(user.id) {
                // Don't capture these events, since they are 'given'
                context.onEvent = null

                // Publish the events
                events.forEach {
                    context.publish(it, user.id)
                }

                // Start capturing events again
                context.onEvent = { received(it) }
            }
        }

        fun <C : Command> send(command: C) = sendAs(SystemUser.get(), command)

        fun <C : Command> sendAs(user: User, command: C) {
            runBlockingAs(user.id) {
                val result = context.require(command, Command.Metadata(user.id))
                when (result) {
                    is Command.Result.Failure -> {
                        val e = result.asThrowable()
                        log.warn("Caught exception: $e")
                        if (caughtException != null) {
                            fail("Already an exception caught: $caughtException")
                        }
                        caughtException = e
                    }
                    else -> {
                    }
                }
            }
        }

        fun expect(block: ExpectationBuilder.() -> Unit) {
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

        inner class ExpectationBuilder(val testBuilder: TestBuilder) {
            fun event(event: Event) {
                expectThat(testBuilder.receivedEvents.first()).isEqualTo(event)
                testBuilder.receivedEvents.remove(event)
            }

            inline fun <reified S : State> state(noinline block: suspend (S) -> Unit) = stateAs(SystemUser.get(), block)

            inline fun <reified S : State> stateAs(user: User, noinline block: suspend (S) -> Unit) {
                runBlocking {
                    CoroutineUserIdContext.set(user.id)
                    testBuilder.aggregate<S>().read {
                        block(this)
                    }
                }
            }

            inline fun <reified E : Throwable> throws(message: String? = null) {
                if (message != null) {
                    expectThat(testBuilder.caughtException?.message).isEqualTo(message)
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