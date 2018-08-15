package com.bol.blueprint.config

import org.springframework.beans.factory.FactoryBean
import org.springframework.beans.factory.ListableBeanFactory

/**
 * Check if there is already a bean of a specific type. If not, use a fallback bean.
 */
inline fun <reified T : Any> fallback(beanFactory: ListableBeanFactory, fallback: () -> T): FactoryBean<T> {
    val beanMap = beanFactory.getBeansOfType(T::class.java)

    if (beanMap.size > 1) {
        throw BlueprintStartupException("Multiple beans of type ${T::class.java.name} are defined: ${beanMap.keys}. Only a single bean is supposed to be configured.", "Please check your configuration.")
    }

    val beans = beanMap.values

    val bean = if (beans.isEmpty()) {
        fallback()
    } else {
        beans.first()
    }

    return FallbackFactory(bean)
}

class FallbackFactory<T : Any>(private val bean: T) : FactoryBean<T> {
    override fun getObjectType() = bean::class.java
    override fun getObject() = bean
}
