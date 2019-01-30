package com.bol.katalog.support

import org.springframework.core.ParameterizedTypeReference
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

object TestData {
    val clock: Clock = Clock.fixed(Instant.EPOCH, ZoneId.systemDefault())
}

inline fun <reified T> ref() = object : ParameterizedTypeReference<T>() {}
