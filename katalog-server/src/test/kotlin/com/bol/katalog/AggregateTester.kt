package com.bol.katalog

import com.bol.katalog.cqrs.*
import com.bol.katalog.security.CoroutineUserContext
import com.bol.katalog.security.User
import junit.framework.TestCase.fail
import kotlinx.coroutines.runBlocking
import org.springframework.security.core.authority.SimpleGrantedAuthority
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isA

class AggregateTester<T : Aggregate<S>, S : State>(val factory: (AggregateContext) -> T) {
    companion object {
        fun <T : Aggregate<S>, S : State> of(factory: (AggregateContext) -> T): AggregateTester<T, S> {
            return AggregateTester(factory)
        }
    }

    fun run(block: TestBuilder<T, S>.() -> Unit) {
        val context = TestAggregateContext()
        val aggregate = factory(context)

        val builder = TestBuilder(aggregate)
        context.onEvent = { builder.received(it) }
        block(builder)
    }

    class TestBuilder<T : Aggregate<S>, S : State>(private val aggregate: T) {
        val receivedEvents = mutableListOf<Event>()
        var caughtException: Throwable? = null

        fun <E : Event> given(event: E) {
            runBlocking {
                aggregate.handlePersistentEvent(event.asPersistentEvent("admin", TestData.clock))
            }
        }

        fun <C : Command> send(command: C) {
            runBlocking {
                try {
                    aggregate.send(command)
                } catch (e: Throwable) {
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

        class ExpectationBuilder<T : Aggregate<S>, S : State>(val testBuilder: TestBuilder<T, S>) {
            fun event(vararg events: Event) {
                expectThat(testBuilder.receivedEvents).containsExactly(*events)
            }

            fun state(block: suspend (S) -> Unit) {
                runBlocking {
                    CoroutineUserContext.set(
                        User(
                            "id-admin",
                            "admin",
                            null,
                            setOf(SimpleGrantedAuthority("ROLE_ADMIN"))
                        )
                    )
                    testBuilder.aggregate.read {
                        block(this)
                    }
                }
            }

            inline fun <reified E : Throwable> throws() {
                expectThat(testBuilder.caughtException).isA<E>()
            }
        }
    }
}