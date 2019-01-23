package com.bol.katalog.plugin.atomix

import java.time.Clock
import java.time.Instant
import java.time.ZoneId

object TestData {
    val clock: Clock = Clock.fixed(Instant.EPOCH, ZoneId.systemDefault())
}
