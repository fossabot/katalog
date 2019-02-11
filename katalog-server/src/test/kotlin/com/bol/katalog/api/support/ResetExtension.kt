package com.bol.katalog.api.support

import com.bol.katalog.Resettable
import com.bol.katalog.cqrs.hazelcast.transaction
import com.bol.katalog.support.TestHazelcastAggregateContext
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.springframework.beans.factory.getBean
import org.springframework.beans.factory.getBeansOfType
import org.springframework.test.context.junit.jupiter.SpringExtension

/**
 * Extension to reset all 'Resettable' beans after every test
 */
class ResetExtension : AfterEachCallback {
    override fun afterEach(context: ExtensionContext) {
        val applicationContext = SpringExtension.getApplicationContext(context)
        val hazelcastAggregateContext = applicationContext.getBean<TestHazelcastAggregateContext>()
        val resettable = applicationContext.getBeansOfType<Resettable>()
        runBlocking {
            transaction(hazelcastAggregateContext) {
                resettable.values.forEach { it.reset() }
            }
        }
    }
}