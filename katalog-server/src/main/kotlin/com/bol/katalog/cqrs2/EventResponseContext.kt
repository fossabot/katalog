package com.bol.katalog.cqrs2

import com.bol.katalog.cqrs.Event
import com.bol.katalog.cqrs.State

class EventResponseContext<S : State, T : Event>(val state: S, val event: T)
