package com.bol.katalog.testing

import java.time.Clock
import java.time.Instant
import java.time.ZoneId

object TestData {
    val clock: Clock = Clock.fixed(Instant.EPOCH, ZoneId.systemDefault())
}

