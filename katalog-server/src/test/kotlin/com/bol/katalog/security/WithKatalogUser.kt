package com.bol.katalog.security

import org.springframework.security.test.context.support.WithSecurityContext

@Retention(AnnotationRetention.RUNTIME)
@WithSecurityContext(factory = WithKatalogUserSecurityContextFactory::class)
annotation class WithKatalogUser(val username: String)