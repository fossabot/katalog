package com.bol.katalog.cqrs

class EventResponseContext<S : State, T : Event>(val state: S, val event: T, val metadata: PersistentEvent.Metadata)
