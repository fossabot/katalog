package com.bol.katalog.api.support

import com.bol.katalog.support.Resettable
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.springframework.beans.factory.getBeansOfType
import org.springframework.test.context.junit.jupiter.SpringExtension

/**
 * Extension to reset all 'Resettable' beans after every test
 */
class ResetExtension : AfterEachCallback {
    override fun afterEach(context: ExtensionContext) {
        val resettable = SpringExtension.getApplicationContext(context).getBeansOfType<Resettable>()
        resettable.values.forEach { it.reset() }
    }
}