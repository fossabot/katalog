package com.bol.katalog.features.registry

import com.bol.katalog.TestApplication
import org.junit.After
import org.junit.Before

abstract class AbstractAggregateTest {
    @Before
    fun before() {
        TestApplication.before()
    }

    @After
    fun after() {
        TestApplication.after()
    }
}