package com.bol.katalog.cqrs.clustering

import com.bol.katalog.cqrs.Event
import com.bol.katalog.cqrs.PersistentEvent
import com.bol.katalog.security.User
import java.time.Clock

fun <E : Event> E.asPersistentEvent(user: User?, clock: Clock) = PersistentEvent(
    metadata = PersistentEvent.Metadata(
        timestamp = clock.instant(),
        username = user?.username ?: "unknown"
    ), data = this
)
